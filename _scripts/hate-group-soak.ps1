[CmdletBinding()]
param(
    [int]$RconPort = 25576,
    [int]$Groups = 10,
    [int]$Seconds = 240,
    [int]$SampleSeconds = 20,
    [string]$OutFile = '',
    [string]$Rcon = 'C:\Users\Sails\Documents\Workspace\01-Active\Domain-Projects\Minecraft\BetterPeaceMode\_scripts\rcon.ps1'
)

# Soak test for the reinforcement rules: many hate groups at once, each with its own invulnerable
# enemy, and the arena left alone for minutes.
#
# Every group should stop at "caller + reinforcementCount" helpers and stay there, the mob count
# should stay flat once the groups are full, and the server tick rate should stay near 20. Run it
# with areaLimitEnabled=false so the per-area budget does not hide the group logic (the budget is
# verified separately by mode-regression and the alpha-4 tests).
#
# Half of the callers are cows on purpose: a provoked animal exercises universal retaliation and the
# chase path, not just the zombie's own melee goal.

$ErrorActionPreference = 'Stop'
$inv = [System.Globalization.CultureInfo]::InvariantCulture
$rows = New-Object System.Collections.Generic.List[string]

function Invoke-Rcon {
    param([string[]]$Commands)
    return (& $Rcon -Command $Commands -Port $RconPort) -join "`n"
}

function Get-GameTime {
    $out = Invoke-Rcon -Commands @('time query gametime')
    $match = [regex]::Match($out, 'is\s+(\d+)')
    if (-not $match.Success) { return -1L }
    return [long]$match.Groups[1].Value
}

function Get-MobCount {
    Invoke-Rcon -Commands @(
        'execute store result score bpm_mobs bpmcount run execute if entity @e[type=!player,type=!item,type=!armor_stand]'
    ) | Out-Null
    $out = Invoke-Rcon -Commands @('scoreboard players get bpm_mobs bpmcount')
    $match = [regex]::Match($out, 'has\s+(-?\d+)')
    if (-not $match.Success) { return -1 }
    return [int]$match.Groups[1].Value
}

function Get-ServerMemoryMb {
    $server = Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
        Where-Object { $_.CommandLine -like '*fabric-server-launch.jar*' } |
        Select-Object -First 1
    if (-not $server) { return -1 }
    return [math]::Round($server.WorkingSetSize / 1MB, 0)
}

# --- scene: a walled arena and one enemy per group ------------------------------------------
$setup = New-Object System.Collections.Generic.List[string]
$setup.Add('kill @e[type=!player]')
$setup.Add('scoreboard objectives add bpmcount dummy')
$setup.Add('time set midnight')
$setup.Add('fill -40 110 -40 40 110 40 minecraft:stone')
$setup.Add('fill -40 116 -40 40 116 40 minecraft:stone')
$setup.Add('fill -41 111 -41 41 114 -41 minecraft:stone')
$setup.Add('fill -41 111 41 41 114 41 minecraft:stone')
$setup.Add('fill -41 111 -41 -41 114 41 minecraft:stone')
$setup.Add('fill 41 111 -41 41 114 41 minecraft:stone')
for ($i = 0; $i -lt $Groups; $i++) {
    $x = -30 + ($i * 6)
    $setup.Add("summon minecraft:armor_stand $x 111 0 {Tags:[""bpm_enemy$i""],Invulnerable:1b,NoGravity:1b}")
    $kind = if ($i % 2 -eq 0) { 'zombie' } else { 'cow' }
    $setup.Add("summon minecraft:$kind $x 111 4 {Tags:[""bpm_caller$i""],PersistenceRequired:1b,attributes:[{id:""minecraft:max_health"",base:100.0d}]}")
}
for ($i = 0; $i -lt $Groups; $i++) {
    $setup.Add("damage @e[tag=bpm_caller$i,limit=1] 1 minecraft:mob_attack by @e[tag=bpm_enemy$i,limit=1]")
}
Invoke-Rcon -Commands $setup.ToArray() | Out-Null

Start-Sleep -Seconds 8
$header = '{0,5} | {1,7} | {2,6} | {3,9}' -f 't', 'ticks/s', 'mobs', 'mem(MB)'
$rows.Add(('{0} groups, target {1} mobs each' -f $Groups, 8))
$rows.Add($header)
Write-Host $rows[0]
Write-Host $header

$start = Get-Date
$previousTime = Get-GameTime
$previousStamp = Get-Date
$minTicks = 999.0
$maxMobs = 0

while (((Get-Date) - $start).TotalSeconds -lt $Seconds) {
    Start-Sleep -Seconds $SampleSeconds
    $now = Get-Date
    $gameTime = Get-GameTime
    $wall = ($now - $previousStamp).TotalSeconds
    $ticks = if ($wall -gt 0 -and $gameTime -ge 0) { ($gameTime - $previousTime) / $wall } else { -1 }
    $mobs = Get-MobCount
    $memory = Get-ServerMemoryMb
    $previousTime = $gameTime
    $previousStamp = $now
    if ($ticks -ge 0 -and $ticks -lt $minTicks) { $minTicks = $ticks }
    if ($mobs -gt $maxMobs) { $maxMobs = $mobs }
    $line = '{0,4}s | {1,7:0.00} | {2,6} | {3,9}' -f `
        [math]::Round(($now - $start).TotalSeconds, 0), $ticks, $mobs, $memory
    $rows.Add($line)
    Write-Host $line
}

$summary = 'summary: min ticks/s={0:0.00} (target 20), peak mobs={1}, expected ceiling={2}' -f `
    $minTicks, $maxMobs, ($Groups * 8)
$rows.Add($summary)
Write-Host $summary

# Clear the arena again so a repeat run starts from zero.
Invoke-Rcon -Commands @('kill @e[type=!player]') | Out-Null

if ($OutFile -ne '') {
    $rows | Out-File -FilePath $OutFile -Encoding utf8
    Write-Host ("written: {0}" -f $OutFile)
}
