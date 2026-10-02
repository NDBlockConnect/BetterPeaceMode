<p style="font-size:28px;text-align:center;"><b>BetterPeaceMode CHANGELOG</b></p>

> Newest first. Records change sets against the version they belong to.

> [!NOTE]
> BetterPeaceMode starts at `v26.0-Alpha.1`; the first alpha contains the complete feature set
> requested for the two modes.

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
