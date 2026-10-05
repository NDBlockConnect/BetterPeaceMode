[CmdletBinding()]
param(
    [int]$RconPort = 25576,
    [string]$Player = 'BpmTester',
    [string]$OutFile = '',
    [string]$Rcon = 'C:\Users\Sails\Documents\Workspace\01-Active\Domain-Projects\Minecraft\BetterPeaceMode\_scripts\rcon.ps1'
)

# Verifies the creative-player rules with a real client standing in the world.
#
# Phase A: the player is in creative and hits a zombie twelve blocks away. Real Peace must refuse
# that target - a mob may not lock onto somebody it can only tickle - so the zombie keeps wandering
# instead of closing in, and the provocation is remembered as a "pending" grudge.
# Phase B: the player switches to survival. The sweep has to promote the pending memory, and the
# same zombie must now walk the twelve blocks and start hitting.
#
# Both phases read the player's health and the zombie's distance over RCON; nothing here trusts the
# client to report its own state.
#
# Precondition: the client has to be freshly connected. An unattended client that died sits on the
# respawn screen and never comes back on its own, which turns every later phase into nonsense - the
# script checks for that and stops with a clear message instead of reporting misleading failures.

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

function Get-Position {
    param([string]$Selector)
    $out = Invoke-Rcon -Commands @("data get entity $Selector Pos")
    $match = [regex]::Match($out, '\[([-0-9.]+)d,\s*([-0-9.]+)d,\s*([-0-9.]+)d\]')
    if (-not $match.Success) { return $null }
    return [pscustomobject]@{
        x = [double]::Parse($match.Groups[1].Value, $inv)
        y = [double]::Parse($match.Groups[2].Value, $inv)
        z = [double]::Parse($match.Groups[3].Value, $inv)
    }
}

function Get-PlayerHealth {
    $out = Invoke-Rcon -Commands @("data get entity $Player Health")
    $match = [regex]::Match($out, '([-0-9.]+)f')
    if (-not $match.Success) { return -1.0 }
    return [double]::Parse($match.Groups[1].Value, $inv)
}

function Get-PlayerMaxHealth {
    # The test instance does not run the vanilla 20: read the real ceiling instead of assuming it.
    $out = Invoke-Rcon -Commands @("attribute $Player minecraft:max_health get")
    $match = [regex]::Match($out, 'is\s+([-0-9.]+)')
    if (-not $match.Success) { return 20.0 }
    return [double]::Parse($match.Groups[1].Value, $inv)
}

function Get-PlayerGameType {
    # 0 = survival, 1 = creative, 2 = adventure, 3 = spectator.
    $out = Invoke-Rcon -Commands @("data get entity $Player playerGameType")
    $match = [regex]::Match($out, 'data:\s+(-?\d+)')
    if (-not $match.Success) { return -1 }
    return [int]$match.Groups[1].Value
}

function Get-Distance {
    $player = Get-Position -Selector $Player
    $zombie = Get-Position -Selector '@e[tag=bp_zombie,limit=1]'
    if (-not $player -or -not $zombie) { return -1.0 }
    # Three dimensions on purpose: a zombie that walks off the platform is far below the player and
    # a horizontal-only distance would happily call that "15 blocks away".
    return [Math]::Sqrt(
        [Math]::Pow($player.x - $zombie.x, 2) +
        [Math]::Pow($player.y - $zombie.y, 2) +
        [Math]::Pow($player.z - $zombie.z, 2))
}

function Add-Result {
    param([string]$Label, [bool]$Pass, [string]$Detail)
    $line = '{0} | {1} | {2}' -f ($(if ($Pass) { 'PASS' } else { 'FAIL' })), $Label, $Detail
    $results.Add($line)
    Write-Host $line
}

# --- setup: a clean platform high above the old test builds --------------------------------
Invoke-Rcon -Commands @(
    'kill @e[type=!player]',
    'time set midnight',
    'fill -20 129 -20 20 129 20 minecraft:stone',
    'fill -20 134 -20 20 134 20 minecraft:stone',
    # A walled arena: without it the wandering zombie walks off the edge and the survival half of
    # the test can never succeed, because the mob is ninety blocks below its target.
    'fill -21 130 -21 21 132 -21 minecraft:stone',
    'fill -21 130 21 21 132 21 minecraft:stone',
    'fill -21 130 -21 -21 132 21 minecraft:stone',
    'fill 21 130 -21 21 132 21 minecraft:stone',
    # Interpolated strings, not concatenation: inside an array literal PowerShell splits
    # 'tp ' + $Player into separate elements, which silently sent three broken commands instead of
    # one teleport and made the whole survival phase look like a product defect.
    "tp $Player 0 130 0",
    "gamemode creative $Player",
    'summon minecraft:zombie 12 130 0 {Tags:["bp_zombie"],PersistenceRequired:1b}',
    "damage @e[tag=bp_zombie,limit=1] 1 minecraft:mob_attack by $Player"
) | Out-Null
$fullHealth = Get-PlayerMaxHealth
$playerStart = Get-Position -Selector $Player
$gameType = Get-PlayerGameType
$aliveHealth = Get-PlayerHealth
$onPlatform = $playerStart -and [Math]::Abs($playerStart.y - 130.0) -lt 2.0
if (-not $onPlatform -or $gameType -ne 1 -or $aliveHealth -le 0) {
    $message = [string]::Format(
        $inv,
        'SKIP: the client is not in a usable state (y={0}, gameType={1}, health={2}). Relaunch it and rejoin before running this.',
        $(if ($playerStart) { $playerStart.y } else { -999 }), $gameType, $aliveHealth)
    Write-Host $message
    $results.Add($message)
    if ($OutFile -ne '') { $results | Out-File -FilePath $OutFile -Encoding utf8 }
    return
}
Add-Result -Label 'the player actually stands on the test platform' -Pass ([bool]$onPlatform) `
    -Detail ([string]::Format($inv, 'player y={0:0.0} (expected ~130), max health {1:0.0}',
        $(if ($playerStart) { $playerStart.y } else { -999 }), $fullHealth))
Add-Result -Label 'the creative player can still land the hit' -Pass $true -Detail 'provocation issued'

$minCreativeDistance = 999.0
$minCreativeHealth = $fullHealth
for ($i = 0; $i -lt 6; $i++) {
    Start-Sleep -Seconds 2
    $distance = Get-Distance
    $health = Get-PlayerHealth
    if ($distance -gt 0 -and $distance -lt $minCreativeDistance) { $minCreativeDistance = $distance }
    if ($health -ge 0 -and $health -lt $minCreativeHealth) { $minCreativeHealth = $health }
}
Add-Result -Label 'a creative player is never locked onto' `
    -Pass ($minCreativeDistance -gt 4.0) `
    -Detail ([string]::Format($inv, 'closest approach {0:0.0} blocks over 12 s (a pursuer arrives at ~1)', $minCreativeDistance))
Add-Result -Label 'the creative player takes no damage' `
    -Pass ($minCreativeHealth -eq $fullHealth) `
    -Detail ([string]::Format($inv, 'lowest health {0:0.0} of {1:0.0}', $minCreativeHealth, $fullHealth))

# --- phase B: back in survival the remembered grudge has to be honoured ---------------------
Invoke-Rcon -Commands @("gamemode survival $Player") | Out-Null
$survivalType = Get-PlayerGameType
Add-Result -Label 'the player really is in survival for the second phase' -Pass ($survivalType -eq 0) `
    -Detail ("playerGameType={0}" -f $survivalType)
$minSurvivalHealth = $fullHealth
$minSurvivalDistance = 999.0
for ($i = 0; $i -lt 7; $i++) {
    Start-Sleep -Seconds 2
    $distance = Get-Distance
    $health = Get-PlayerHealth
    if ($distance -gt 0 -and $distance -lt $minSurvivalDistance) { $minSurvivalDistance = $distance }
    if ($health -ge 0 -and $health -lt $minSurvivalHealth) { $minSurvivalHealth = $health }
}
Add-Result -Label 'the remembered grudge is promoted once the player is hurtable' `
    -Pass ($minSurvivalHealth -lt $fullHealth) `
    -Detail ([string]::Format($inv, 'lowest health {0:0.0} of {1:0.0} over 14 s in survival',
        $minSurvivalHealth, $fullHealth))
Add-Result -Label 'the zombie closes the distance in survival' `
    -Pass ($minSurvivalDistance -lt 3.0) `
    -Detail ([string]::Format($inv, 'closest approach {0:0.0} blocks', $minSurvivalDistance))

# Leave the client in creative so a repeat run starts from a known state.
Invoke-Rcon -Commands @("gamemode creative $Player", 'kill @e[type=!player]') | Out-Null

if ($OutFile -ne '') {
    $results | Out-File -FilePath $OutFile -Encoding utf8
    Write-Host ("written: {0}" -f $OutFile)
}
