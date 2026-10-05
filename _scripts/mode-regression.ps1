[CmdletBinding()]
param(
    [int]$RconPort = 25576,
    [string]$OutFile = '',
    [string]$Rcon = 'C:\Users\Sails\Documents\Workspace\01-Active\Domain-Projects\Minecraft\BetterPeaceMode\_scripts\rcon.ps1'
)

# Regression matrix for the three modes on the isolated test server.
#
# Every case is a mob-versus-mob hit whose only variable is the active mode: vanilla has to let it
# through, Better Peace has to refuse it between friendly mobs, Real Peace has to refuse it while
# nothing has been provoked. A second hit with no attacker at all is the control - it must always
# land, so a refusal can never be confused with a dead sheep.

$ErrorActionPreference = 'Stop'
$inv = [System.Globalization.CultureInfo]::InvariantCulture
$results = New-Object System.Collections.Generic.List[string]

function Invoke-Rcon {
    param([string[]]$Commands)
    return (& $Rcon -Command $Commands -Port $RconPort) -join "`n"
}

function Get-Health {
    param([string]$Tag)
    $out = Invoke-Rcon -Commands @("data get entity @e[tag=$Tag,limit=1] Health")
    $match = [regex]::Match($out, '([-0-9.]+)f')
    if (-not $match.Success) { return -1.0 }
    return [double]::Parse($match.Groups[1].Value, $inv)
}

function Get-Difficulty {
    $out = Invoke-Rcon -Commands @('difficulty')
    $match = [regex]::Match($out, 'is (Peaceful|Easy|Normal|Hard)')
    if (-not $match.Success) { return '?' }
    return $match.Groups[1].Value
}

function Add-Result {
    param([string]$Label, [bool]$Pass, [string]$Detail)
    $line = '{0} | {1} | {2}' -f ($(if ($Pass) { 'PASS' } else { 'FAIL' })), $Label, $Detail
    $results.Add($line)
    Write-Host $line
}

<#
    Lays out one sheep and one wolf, applies one mob-attack hit and (optionally) one attacker-less
    fall hit, and reports both health deltas.
#>
function Test-FriendlyHit {
    param([string]$Label, [double]$ExpectedMobHit)
    Invoke-Rcon -Commands @(
        'kill @e[type=!player]',
        'time set midnight',
        'fill -12 110 -12 12 110 12 minecraft:stone',
        'fill -12 114 -12 12 114 12 minecraft:stone',
        'summon minecraft:sheep 0 111 0 {Tags:["bp_sheep"],PersistenceRequired:1b,NoAI:1b,attributes:[{id:"minecraft:max_health",base:100.0d}]}',
        'summon minecraft:wolf 2 111 0 {Tags:["bp_wolf"],PersistenceRequired:1b,NoAI:1b}'
    ) | Out-Null
    $before = Get-Health -Tag 'bp_sheep'
    Invoke-Rcon -Commands @('damage @e[tag=bp_sheep,limit=1] 3 minecraft:mob_attack by @e[tag=bp_wolf,limit=1]') | Out-Null
    $afterMob = Get-Health -Tag 'bp_sheep'
    # Wait out the 20-tick invulnerability window, otherwise the control hit is swallowed by it.
    Start-Sleep -Milliseconds 1400
    Invoke-Rcon -Commands @('damage @e[tag=bp_sheep,limit=1] 3 minecraft:fall') | Out-Null
    $afterFall = Get-Health -Tag 'bp_sheep'
    $mobDrop = $before - $afterMob
    $fallDrop = $afterMob - $afterFall
    $pass = [Math]::Abs($mobDrop - $ExpectedMobHit) -lt 0.05 -and $fallDrop -gt 0.0
    Add-Result -Label $Label -Pass $pass -Detail ([string]::Format(
        $inv, 'mob hit removed {0:0.00} (expected {1:0.00}), attacker-less hit removed {2:0.00}',
        $mobDrop, $ExpectedMobHit, $fallDrop))
}

# --- vanilla: nothing is gated ---------------------------------------------------------------
Invoke-Rcon -Commands @('betterpeace mode vanilla') | Out-Null
Test-FriendlyHit -Label 'vanilla allows a wolf to hurt a sheep' -ExpectedMobHit 3.0

# --- Better Peace: friendly versus friendly is refused, environment still lands ---------------
Invoke-Rcon -Commands @('betterpeace mode better_peace') | Out-Null
Start-Sleep -Seconds 3
$difficulty = Get-Difficulty
Add-Result -Label 'Better Peace pins the world to Peaceful' -Pass ($difficulty -eq 'Peaceful') -Detail ("difficulty={0}" -f $difficulty)
Test-FriendlyHit -Label 'Better Peace refuses wolf-on-sheep' -ExpectedMobHit 0.0

# --- Real Peace: unprovoked is refused, environment still lands -------------------------------
Invoke-Rcon -Commands @('betterpeace mode real_peace') | Out-Null
Start-Sleep -Seconds 3
$difficulty = Get-Difficulty
Add-Result -Label 'Real Peace pins the world to Hard' -Pass ($difficulty -eq 'Hard') -Detail ("difficulty={0}" -f $difficulty)
Test-FriendlyHit -Label 'Real Peace refuses an unprovoked wolf-on-sheep' -ExpectedMobHit 0.0

# --- Real Peace: an unprovoked warden leaves a villager alone ---------------------------------
Invoke-Rcon -Commands @(
    'kill @e[type=!player]',
    # High above the old test builds: a villager standing inside leftover stone suffocates at 2 HP/s
    # with no attacker recorded, which looks exactly like a mob beating on it.
    'fill -8 124 -8 8 124 8 minecraft:stone',
    'fill -8 128 -8 8 128 8 minecraft:stone',
    'summon minecraft:warden 0 125 0 {Tags:["bp_warden"],PersistenceRequired:1b}',
    'summon minecraft:villager 4 125 0 {Tags:["bp_villager"],PersistenceRequired:1b,attributes:[{id:"minecraft:max_health",base:100.0d}]}'
) | Out-Null
$villagerBefore = Get-Health -Tag 'bp_villager'
Start-Sleep -Seconds 15
$villagerAfter = Get-Health -Tag 'bp_villager'
Add-Result -Label 'Real Peace keeps an unprovoked warden off a villager' `
    -Pass ($villagerAfter -eq $villagerBefore) `
    -Detail ([string]::Format($inv, 'villager {0:0.0} -> {1:0.0} over 15 s', $villagerBefore, $villagerAfter))

# --- Real Peace: the same gate must still let a provoked warden retaliate ---------------------
# The baby guard writes a mutual grudge between the parent and the intruder, which is the one way
# to give a warden a live grudge without a player: the adult cow is therefore a legitimate target.
Invoke-Rcon -Commands @(
    'kill @e[type=!player]',
    'summon minecraft:cow 0 125 0 {Tags:["bp_cow"],PersistenceRequired:1b,attributes:[{id:"minecraft:max_health",base:100.0d}]}',
    'summon minecraft:cow 4 125 0 {Tags:["bp_calf"],PersistenceRequired:1b,NoAI:1b,Age:-24000}',
    'summon minecraft:warden 4 125 2 {Tags:["bp_warden2"],PersistenceRequired:1b}'
) | Out-Null
$cowBefore = Get-Health -Tag 'bp_cow'
Start-Sleep -Seconds 15
$cowAfter = Get-Health -Tag 'bp_cow'
Add-Result -Label 'Real Peace lets a provoked warden retaliate' `
    -Pass ($cowAfter -lt $cowBefore) `
    -Detail ([string]::Format($inv, 'cow {0:0.0} -> {1:0.0} over 15 s', $cowBefore, $cowAfter))

if ($OutFile -ne '') {
    $results | Out-File -FilePath $OutFile -Encoding utf8
    Write-Host ("written: {0}" -f $OutFile)
}
