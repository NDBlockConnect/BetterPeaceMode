<p style="font-size:28px;text-align:center;"><b>BetterPeaceMode CHANGELOG</b></p>

> Newest first. Records change sets against the version they belong to.

> [!NOTE]
> BetterPeaceMode starts at `v26.0-Alpha.1`; the first alpha contains the complete feature set
> requested for the two modes.

---

## {ChangeTime: 2026.10.04-05:10:00} SharedConfigPagesAndCreativeGrudges

GitCommitHash: feat/config-page-api

ChangedFiles:
```
.\gradle.properties +2 -2
.\src\main\java\dev\blockconnect\betterpeacemode\client\api\ConfigPage.java +32 -0
.\src\main\java\dev\blockconnect\betterpeacemode\client\api\ConfigPageContext.java +64 -0
.\src\main\java\dev\blockconnect\betterpeacemode\client\api\ConfigPageRegistry.java +37 -0
.\src\main\java\dev\blockconnect\betterpeacemode\client\BetterPeaceModeConfigScreen.java +260 -180
.\src\main\java\dev\blockconnect\betterpeacemode\core\PeacePolicy.java +30 -6
.\src\main\java\dev\blockconnect\betterpeacemode\core\ProvocationLedger.java +92 -2
.\src\main\java\dev\blockconnect\betterpeacemode\core\ReinforcementManager.java +12 -0
.\src\main\java\dev\blockconnect\betterpeacemode\core\PeaceSweep.java +6 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\LivingEntityMixin.java +10 -0
.\src\main\java\dev\blockconnect\betterpeacemode\config\BetterPeaceModeConfig.java +12 -0
.\src\main\java\dev\blockconnect\betterpeacemode\command\BetterPeaceCommand.java +4 -2
.\README.md +18 -1
.\README.zh.md +18 -1
```

ChangeLog:
Num|File Name|Change Description|Change Time|Changer
----|----|----|----|----
1|client/api/*.java|New: the settings screen is now extensible - companion mods register pages through ConfigPageRegistry and build them against ConfigPageContext|2026.10.04-05:10:00|StarsailsClover
2|BetterPeaceModeConfigScreen.java|Change: built-in pages re-expressed on the same context, page selector shows the position, and the two previously missing rule toggles (friendly peace / real peace) were added back to the Rules page|2026.10.04-05:10:00|StarsailsClover
3|PeacePolicy.java|Fix: a mob never targets a creative or spectator player, and in Real Peace only a live grudge may acquire a target (wolves stop hunting, golems stop picking fights)|2026.10.04-05:10:00|StarsailsClover
4|ProvocationLedger.java|New: creativeGrudgeCarryOver - a hit from an untouchable player is remembered and promoted to a live grudge once that player is hurtable again|2026.10.04-05:10:00|StarsailsClover
5|ReinforcementManager.java|New: entities tagged bpm.no_reinforcements are excluded from recruitment, so staged fights keep their configured size|2026.10.04-05:10:00|StarsailsClover
6|BetterPeaceModeConfigScreen.java|Fix: full-width controls started a new row instead of overlapping the control beside them; cycle buttons no longer render their label twice|2026.10.04-05:10:00|StarsailsClover

version: v26.0-Alpha.5

---

## {ChangeTime: 2026.10.03-06:50:00} StabilityAndReinforcementContainment

GitCommitHash: feat/alpha4-stability-and-reinforcement-rules

ChangedFiles:
```
.\gradle.properties +2 -2
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\LivingEntityMixin.java +13 -8
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\SpearUseGoalMixin.java +49 -0
.\src\main\java\dev\blockconnect\betterpeacemode\core\ProvocationLedger.java +68 -12
.\src\main\java\dev\blockconnect\betterpeacemode\core\ReinforcementManager.java +87 -64
.\src\main\java\dev\blockconnect\betterpeacemode\core\HateGroup.java +27 -19
.\src\main\java\dev\blockconnect\betterpeacemode\core\BabyGuardManager.java +86 -0
.\src\main\java\dev\blockconnect\betterpeacemode\core\PeaceSweep.java +6 -1
.\src\main\java\dev\blockconnect\betterpeacemode\config\BetterPeaceModeConfig.java +38 -0
.\src\main\java\dev\blockconnect\betterpeacemode\client\BetterPeaceModeConfigScreen.java +123 -96
.\src\main\java\dev\blockconnect\betterpeacemode\command\BetterPeaceCommand.java +18 -0
.\src\main\resources\betterpeacemode.mixins.json +1 -1
.\README.md +28 -4
.\README.zh.md +28 -4
```

ChangeLog:
Num|File Name|Change Description|Change Time|Changer
----|----|----|----|----
1|SpearUseGoalMixin.java|Fix: cancel `SpearUseGoal#tick` when its target is null - the crash the owner hit when a zombie fighting a creative player picked up a spear|2026.10.03-06:40:00|StarsailsClover
2|LivingEntityMixin.java|Fix: cancel refused hits instead of zeroing their damage, so contact damage no longer leaves anger, knockback or an invulnerability window behind|2026.10.03-06:10:00|StarsailsClover
3|ProvocationLedger.java|Fix: keep grudges in the mod instead of vanilla's `lastHurtByMob`, which vanilla clears after 100 ticks and which silently capped `aggroDurationTicks` at five seconds|2026.10.03-05:50:00|StarsailsClover
4|ReinforcementManager.java Feature: one shared hate group per enemy; any race joins an existing group instead of opening a new one|2026.10.03-05:50:00|StarsailsClover
5|ReinforcementManager.java Feature: chunk-aligned area entity budget (`areaLimit*`) that refuses helper spawns once the area is saturated|2026.10.03-05:50:00|StarsailsClover
6|ReinforcementManager.java Feature: optional super reinforcements with doubled health and indefinite defensive effects|2026.10.03-05:50:00|StarsailsClover
7|BabyGuardManager.java Feature: same-species adults hate whatever enters the four-block ring around a baby|2026.10.03-05:20:00|StarsailsClover
8|HateGroup.java Change: groups carry the dimension and an anchor position so joining is decided by enemy and distance|2026.10.03-05:00:00|StarsailsClover
9|PeaceSweep.java Change: prune expired grudges once per sweep and drive the baby guard on a faster 5-tick cadence|2026.10.03-05:00:00|StarsailsClover
10|BetterPeaceModeConfig.java BetterPeaceModeConfigScreen.java BetterPeaceCommand.java|Change: expose the six new settings, split the configuration screen into Rules and Reinforcements pages, and print the new values in `/betterpeace status`|2026.10.03-05:00:00|StarsailsClover
11|README.md README.zh.md|Docs: the new containment rules, the corrected contact-damage description and the new configuration fields|2026.10.03-06:45:00|StarsailsClover

version: v26.0-Alpha.4

---

## {ChangeTime: 2026.10.02-19:50:00} LoaderAndFabricApiCompatibility

GitCommitHash: chore/loader-0.19.3-compat

ChangedFiles:
```
.\gradle.properties +2 -2
.\src\main\resources\fabric.mod.json +2 -2
.\README.md +2 -2
.\README.zh.md +2 -2
```

ChangeLog:
Num|File Name|Change Description|Change Time|Changer
----|----|----|----|----
1|fabric.mod.json|Lower the Fabric Loader requirement from >=0.19.5 to >=0.19.3 and pin Fabric API to >=0.141.6|2026.10.02-19:50:00|StarsailsClover
2|gradle.properties|Build against loader 0.19.3 so the compile classpath matches the oldest supported loader, and bump to v26.0-Alpha.3|2026.10.02-19:50:00|StarsailsClover
3|README.md README.zh.md|Document the lowered loader floor|2026.10.02-19:50:00|StarsailsClover

version: v26.0-Alpha.3

---

## {ChangeTime: 2026.10.02-17:50:00} UniversalRetaliationAndReinforcements

GitCommitHash: feat/universal-retaliation-and-reinforcements

ChangedFiles:
```
.\src\main\java\dev\blockconnect\betterpeacemode\core\PeacePolicy.java +90 -12
.\src\main\java\dev\blockconnect\betterpeacemode\core\RetaliationManager.java +79 -0
.\src\main\java\dev\blockconnect\betterpeacemode\core\ReinforcementManager.java +206 -0
.\src\main\java\dev\blockconnect\betterpeacemode\core\HateGroup.java +61 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\LivingEntityMixin.java +62 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\MobMixin.java +16 -0
.\src\main\java\dev\blockconnect\betterpeacemode\config\BetterPeaceModeConfig.java +60 -0
.\src\main\java\dev\blockconnect\betterpeacemode\client\BetterPeaceModeClient.java +44 -0
.\src\main\java\dev\blockconnect\betterpeacemode\client\BetterPeaceModeConfigScreen.java +202 -0
.\src\main\resources\assets\betterpeacemode\lang\en_us.json +5 -0
.\src\main\resources\assets\betterpeacemode\lang\zh_cn.json +5 -0
.\README.md +30 -2
.\README.zh.md +30 -2
```

ChangeLog:
Num|File Name|Change Description|Change Time|Changer
----|----|----|----|----
1|LivingEntityMixin.java|Fix: route every damage path through one refusal test so contact damage (slime, pufferfish) can no longer start a fight unprovoked|2026.10.02-17:50:00|StarsailsClover
2|PeacePolicy.java|New: shouldRefuseDamage, retaliationDamage (attribute value or size-based fallback) and the universal-retaliation switch|2026.10.02-17:50:00|StarsailsClover
3|RetaliationManager.java|New: tick-driven counter-attack so any provoked mob, including animals and villagers, actually fights back|2026.10.02-17:50:00|StarsailsClover
4|ReinforcementManager.java|New: every entity type can call helpers, capped at count + 1, topped up on a randomised 10 s..max schedule with Speed II-V and Strength I-III|2026.10.02-17:50:00|StarsailsClover
5|HateGroup.java|New: caller, helpers and the shared enemy as one bounded group|2026.10.02-17:50:00|StarsailsClover
6|BetterPeaceModeConfig.java|New: recruitment, radius, count and auto-reinforce settings with clamping|2026.10.02-17:50:00|StarsailsClover
7|BetterPeaceModeClient.java BetterPeaceModeConfigScreen.java|New: in-game configuration screen bound to a key (default B)|2026.10.02-17:50:00|StarsailsClover
8|MobMixin.java|New: mob tick hook driving retaliation|2026.10.02-17:50:00|StarsailsClover
9|FACT.md|Correction: wolves DO hunt foxes via NonTameRandomTargetGoal(Animal); the previous note was wrong|2026.10.02-17:50:00|StarsailsClover

version: v26.0-Alpha.2

---

## {ChangeTime: 2026.10.02-13:45:00} TwoPeaceModes

GitCommitHash: initial

ChangedFiles:
```
.\src\main\java\dev\blockconnect\betterpeacemode\BetterPeaceMode.java +45 -0
.\src\main\java\dev\blockconnect\betterpeacemode\GameMode.java +69 -0
.\src\main\java\dev\blockconnect\betterpeacemode\command\BetterPeaceCommand.java +95 -0
.\src\main\java\dev\blockconnect\betterpeacemode\config\BetterPeaceModeConfig.java +36 -0
.\src\main\java\dev\blockconnect\betterpeacemode\config\ConfigManager.java +64 -0
.\src\main\java\dev\blockconnect\betterpeacemode\core\PeacePolicy.java +176 -0
.\src\main\java\dev\blockconnect\betterpeacemode\core\PeaceSweep.java +100 -0
.\src\main\java\dev\blockconnect\betterpeacemode\core\ProvocationLedger.java +53 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\CreeperMixin.java +27 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\EnderManMixin.java +27 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\FoxMixin.java +27 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\LivingEntityMixin.java +65 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\MobMixin.java +39 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\ServerLevelMixin.java +26 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\ServerPlayerMixin.java +45 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\ZombieMixin.java +38 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\ZombifiedPiglinMixin.java +27 -0
.\src\main\java\dev\blockconnect\betterpeacemode\platform\PlatformPaths.java +22 -0
.\src\main\resources\betterpeacemode.mixins.json +18 -0
.\src\main\resources\fabric.mod.json +29 -0
.\build.gradle +68 -0
.\gradle.properties +24 -0
.\settings.gradle +15 -0
.\README.md +139 -0
.\README.zh.md +139 -0
```

ChangeLog:
Num|File Name|Change Description|Change Time|Changer
----|----|----|----|----
1|GameMode.java|New: VANILLA / BETTER_PEACE / REAL_PEACE mode model with tiered rule activation|2026.10.02-13:45:00|StarsailsClover
2|PeacePolicy.java|New: mode predicates, difficulty mapping, protection classification and target-refusal decision|2026.10.02-13:45:00|StarsailsClover
3|ProvocationLedger.java|New: bounded one-to-one grudge tracking and mutual grudge recording|2026.10.02-13:45:00|StarsailsClover
4|PeaceSweep.java|New: once-per-second enforcement of targets, difficulty, regeneration mode and Nether mutation|2026.10.02-13:45:00|StarsailsClover
5|MobMixin.java|New: target gate for every mob that inherits the vanilla setTarget|2026.10.02-13:45:00|StarsailsClover
6|FoxMixin.java|New: target gate for fox, which overrides setTarget and is the Better Peace example case|2026.10.02-13:45:00|StarsailsClover
7|CreeperMixin.java|New: target gate for creeper, which overrides setTarget|2026.10.02-13:45:00|StarsailsClover
8|EnderManMixin.java|New: target gate for enderman, which overrides setTarget|2026.10.02-13:45:00|StarsailsClover
9|ZombifiedPiglinMixin.java|New: target gate for zombified piglin, which overrides setTarget|2026.10.02-13:45:00|StarsailsClover
10|LivingEntityMixin.java|New: damage gate plus grudge recording|2026.10.02-13:45:00|StarsailsClover
11|ZombieMixin.java|New: pins hard-difficulty reinforcements to zero so fights stay one-to-one|2026.10.02-13:45:00|StarsailsClover
12|ServerPlayerMixin.java|New: restores peace-mode regeneration under the forced Hard difficulty|2026.10.02-13:45:00|StarsailsClover
13|ServerLevelMixin.java|New: drives the periodic peace sweep from the server level tick|2026.10.02-13:45:00|StarsailsClover
14|BetterPeaceCommand.java|New: /betterpeace status, mode, reload and save|2026.10.02-13:45:00|StarsailsClover
15|ConfigManager.java|New: JSON config load/save with defaults on failure|2026.10.02-13:45:00|StarsailsClover
16|BetterPeaceModeConfig.java|New: config value model and normalisation|2026.10.02-13:45:00|StarsailsClover
17|BetterPeaceMode.java|New: mod entry point, config load and command registration|2026.10.02-13:45:00|StarsailsClover
18|PlatformPaths.java|New: loader-specific config directory resolution|2026.10.02-13:45:00|StarsailsClover
19|fabric.mod.json|New: mod metadata for 1.21.11 with the fabric-api dependency|2026.10.02-13:45:00|StarsailsClover
20|betterpeacemode.mixins.json|New: client-agnostic mixin list with a pinned refmap name|2026.10.02-13:45:00|StarsailsClover
21|build.gradle|New: Loom remap build for the obfuscated 1.21.11 line, Moijang mappings, refmap pinning|2026.10.02-13:45:00|StarsailsClover
22|README.md README.zh.md|New: English and Chinese documentation|2026.10.02-13:45:00|StarsailsClover

version: v26.0-Alpha.1

---

License: Apache-2.0

GitHub@NDBlockConnect
