[CmdletBinding()]
param(
    [int]$RconPort = 25576,
    [int]$Seconds = 26,
    [string]$Label = 'build',
    [string]$OutFile = '',
    [switch]$NoPanicHits,
    [string]$Rcon = 'C:\Users\Sails\Documents\Workspace\01-Active\Domain-Projects\Minecraft\BetterPeaceMode\_scripts\rcon.ps1'
)

# Deterministic A/B scenario for the "provoked mobs fight instead of fleeing" rule.
#
# BetterPeaceMode's baby guard gives the cow a live grudge against the dummy zombie standing next
# to its calf, so the cow is provoked and starts attacking. On top of that the harness feeds the
# cow an attacker-less fall hit every two seconds: vanilla treats that as a panic cause, so
# PanicGoal takes over and the cow runs instead of fighting. Alpha.6 lets that happen; Alpha.7
# suppresses the panic while the grudge is live, so the cow keeps swinging.
#
# Every number printed here is read back from the live server over RCON.

$ErrorActionPreference = 'Stop'
$inv = [System.Globalization.CultureInfo]::InvariantCulture

function Invoke-Rcon {
    param([string[]]$Commands)
    return (& $Rcon -Command $Commands -Port $RconPort) -join "`n"
}

function Get-Position {
    param([string]$Tag)
    $out = Invoke-Rcon -Commands @("data get entity @e[tag=$Tag,limit=1] Pos")
    $match = [regex]::Match($out, '\[([-0-9.]+)d,\s*([-0-9.]+)d,\s*([-0-9.]+)d\]')
    if (-not $match.Success) { return $null }
    return [pscustomobject]@{
        x = [double]::Parse($match.Groups[1].Value, $inv)
        y = [double]::Parse($match.Groups[2].Value, $inv)
        z = [double]::Parse($match.Groups[3].Value, $inv)
    }
}

function Get-Health {
    param([string]$Tag)
    $out = Invoke-Rcon -Commands @("data get entity @e[tag=$Tag,limit=1] Health")
    $match = [regex]::Match($out, '([-0-9.]+)f')
    if (-not $match.Success) { return -1.0 }
    return [double]::Parse($match.Groups[1].Value, $inv)
}

$setup = @(
    'forceload add -64 -64 64 64',
    'time set midnight',
    'kill @e[type=!player]',
    'fill -24 110 -24 24 110 24 minecraft:stone',
    'fill -24 114 -24 24 114 24 minecraft:stone',
    'summon minecraft:cow 0 111 0 {Tags:["bpm_parent"],PersistenceRequired:1b,attributes:[{id:"minecraft:max_health",base:100.0d}]}',
    'summon minecraft:cow 4 111 0 {Tags:["bpm_baby"],PersistenceRequired:1b,NoAI:1b,Age:-24000,attributes:[{id:"minecraft:max_health",base:100.0d}]}',
    'summon minecraft:zombie 4 111 2 {Tags:["bpm_intruder"],PersistenceRequired:1b,NoAI:1b}'
)
Invoke-Rcon -Commands $setup | Out-Null

$rows = New-Object System.Collections.Generic.List[string]
$rows.Add(("# {0}: provoked cow versus AI zombie" -f $Label))
$rows.Add('t | zombieHP | cowHP | distance')
$rows.Add('--|----------|-------|---------')
Write-Host $rows[0]

$start = Get-Date
for ($elapsed = 0; $elapsed -le $Seconds; $elapsed += 2) {
    if (-not $NoPanicHits) {
        # Attacker-less panic hit: the cow is frightened, but its grudge and target stay untouched.
        Invoke-Rcon -Commands @('damage @e[tag=bpm_parent,limit=1] 1 minecraft:fall') | Out-Null
    }
    $zombie = Get-Position -Tag 'bpm_intruder'
    $cow = Get-Position -Tag 'bpm_parent'
    $zombieHp = Get-Health -Tag 'bpm_intruder'
    $cowHp = Get-Health -Tag 'bpm_parent'
    $distance = if ($zombie -and $cow) {
        [Math]::Sqrt([Math]::Pow($zombie.x - $cow.x, 2) + [Math]::Pow($zombie.z - $cow.z, 2))
    } else {
        -1
    }
    $line = [string]::Format(
        $inv, '{0,2}s | {1,8:0.00} | {2,5:0.0} | {3,7:0.00}', $elapsed, $zombieHp, $cowHp, $distance)
    $rows.Add($line)
    Write-Host $line
    Start-Sleep -Seconds 2
}

if ($OutFile -ne '') {
    $rows | Out-File -FilePath $OutFile -Encoding utf8
    Write-Host ("written: {0}" -f $OutFile)
}
