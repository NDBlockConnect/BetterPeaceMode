<p style="font-size:28px;text-align:center;"><b>BetterPeaceMode FACT</b></p>

> Development facts for BetterPeaceMode, newest first. Each entry records what was checked, how it
> was checked, and what the evidence was.

---

## {FACTTime: 2026.10.04-05:35:00} WardenAngerGateVerified {FACTNum 13}

GitCommitHashRange: fix/warden-anger-gate (unmerged)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\WardenMixin.java +37 -0
.\src\main\resources\betterpeacemode.mixins.json +1 -1
```

### What's Happened?
The owner clarified the mob-versus-mob report: what remains is the **attempt** - a warden hearing an
iron golem, deciding to attack it, and playing the animation, with the damage already refused. That
meant a decision path had been missed.

### Any evidence?
The warden's decision path was read out of the shipped 1.21.11 classes:

| Site | Call |
|---|---|
| `Warden$VibrationUser#canReceiveVibration` | `!this.canTargetEntity(livingEntity)` decides whether the warden even reacts to a noise |
| `Warden#increaseAngerAt` | `if (!isNoAi() && this.canTargetEntity(entity))` before anger is added |
| `Warden#angerManagement.tick(serverLevel, this::canTargetEntity)` | the predicate ages the anger table |

So one predicate covers reception, accumulation and ageing. The behaviour was then measured by
injecting an identical anger entry through the warden's own save codec
(`anger:{suspects:[{uuid:<golem>,anger:150}]}`):

| Build | Same injection, read 2 s later | Meaning |
|---|---|---|
| v26.0-Alpha.5 | `{suspects: [{uuid: [I; ...], anger: 148}]}` | warden stays angry at the golem - it roars, charges and swings |
| v26.0-Alpha.6 | `{suspects: []}` | the entry is refused by the peace policy; no attempt, no animation |

Both mobs stayed at full health in both runs.

### FACTs
"Damage is blocked" and "the fight never starts" are different claims. A mod that only gates damage
has to walk every decision path of every mob that has one - and the warden has one of its own.

version: v26.0-Alpha.6

---

## {FACTTime: 2026.10.04-05:20:00} SharedConfigPages {FACTNum 12}

GitCommitHashRange: feat/config-page-api (unmerged)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\client\api\ConfigPage.java +32 -0
.\src\main\java\dev\blockconnect\betterpeacemode\client\api\ConfigPageContext.java +64 -0
.\src\main\java\dev\blockconnect\betterpeacemode\client\api\ConfigPageRegistry.java +37 -0
.\src\main\java\dev\blockconnect\betterpeacemode\client\BetterPeaceModeConfigScreen.java +260 -180
```

### What's Happened?
The settings window became the shared window for the BlockConnect Minecraft line so companion mods
do not each need their own key binding and screen.

### Any evidence?
Verified with a real 1.21.11 client driven through MDL/Despotes (`mdl game key` / `click` /
`screenshot`), with ArenaMode registering two pages:

| Check | Evidence |
|---|---|
| Companion registration | client log: `[ArenaMode] registered 2 settings pages with BetterPeaceMode` |
| Page count and titles | selector reads `Page: Rules (1/4)`, `Reinforcements (2/4)`, `Arena (3/4)`, `Waves (4/4)` |
| Rules page | screenshots show mode + friendly peace + real peace + boss + ignore-each-other + nether calm + retaliation + grudge + damage + creative carry-over + baby guard |
| Waves page | entity id field renders `minecraft:zombie`, with count/seconds sliders and add/remove/move buttons |

### Any Founds?
Three defects the screenshots exposed and that reasoning alone had missed:

1. A full-width control placed while the layout cursor sat in the right column was drawn on top of
   the control beside it (Boundary particles and the Wave selector were both hidden).
2. The cycle button renders `<name>: <message>`, so a message that repeated the label produced
   `Page: Page: Rules (1/4)` and `Mode: Mode: vanilla`.
3. An `EditBox` built with a placeholder zero width kept `displayPos` at the end of the value and
   rendered nothing; and the first colour constant used to "fix" it (`0xFFFFFF`) has alpha 0, which
   renders nothing either. The value must be set after the box has its final width, and the colour
   needs an alpha channel (`0xFFFFFFFF`).

### FACTs
Render the screen and read the pixels; a GUI can pass every unit-level expectation and still be
unusable.

version: v26.0-Alpha.5

---

## {FACTTime: 2026.10.04-04:55:00} CreativeGrudgesAndIdleMobs {FACTNum 11}

GitCommitHashRange: feat/config-page-api (unmerged)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\core\PeacePolicy.java +30 -6
.\src\main\java\dev\blockconnect\betterpeacemode\core\ProvocationLedger.java +92 -2
.\src\main\java\dev\blockconnect\betterpeacemode\core\ReinforcementManager.java +12 -0
```

### What's Happened?
Three owner reports: mobs still fought each other, mobs chased creative players, and creative-mode
provocations should optionally carry over into survival.

### Any evidence?
All three measured on the dedicated server in Real Peace with `hostilesIgnoreEachOther = true`:

| Check | Setup | Result |
|---|---|---|
| Creative player is not hunted | creative player hits a zombie from 8 blocks | zombie never approaches; player stays at 20.0 |
| Grudge carries over | same zombie, player switches to survival | within 8 s player health 20.0 -> 9.0 and the zombie is adjacent |
| No mob-versus-mob fights | wolf + sheep, zombie + villager, zombie + iron golem, 15 s | every victim at full health, and no participant has `last_hurt_by_mob` or a non-zero `HurtByTimestamp` |
| Staged fight keeps its size | arena zombie carrying `bpm.no_reinforcements` is hit | group stays at 3 instead of calling 7 helpers |

The earlier counter-example - a zombie that lost 13 health next to an iron golem - turned out to be
sunburn: the mob had wandered out from under the test roof and the sun had risen during the run.
The surviving zombie and the golem both had `HurtByTimestamp: 0`, which is what disproved the
combat theory.

### Solutions
`shouldRefuseTarget` now refuses untouchable players before anything else, refuses every
non-grudge target in Real Peace (unless the operator opted into hostile brawls), and
`ProvocationLedger` keeps a pending store that is promoted once the player is hurtable again.

### FACTs
Before blaming combat, check the damage source: a mob that dies outdoors at dawn looks exactly like
a mob that lost a fight if you only read the health number.

version: v26.0-Alpha.5

---

## {FACTTime: 2026.10.03-06:40:00} SpearUseGoalCrashReproduced {FACTNum 9}

GitCommitHashRange: feat/alpha4-stability-and-reinforcement-rules (unmerged)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\SpearUseGoalMixin.java +49 -0
.\src\main\resources\betterpeacemode.mixins.json +1 -1
```

### What's Happened?
The owner reported "Reason: `minecraft:zombie` let the game crashed" after hitting a baby zombie.
The crash report was found at
`...\NeedsOfNature@StarsailsCloverCrk-Enhance, 1.21.11-fa\crash-reports\crash-2026-10-03_03.43.51-server.txt`.

### Any evidence?
Decoding the intermediary frames with `mappings.tiny` gave the exact call path:

| Frame | Named |
|---|---|
| `class_12112.method_6268` | `SpearUseGoal.tick` |
| `class_4135.method_6268` | `WrappedGoal.tick` |
| `class_1355.method_38849` | `GoalSelector.tickRunningGoals` |
| `class_1355.method_6275` | `GoalSelector.tick` |
| `class_1308.method_6023` | `Mob.serverAiStep` |
| `class_1642.method_5773` | `Zombie.tick` |

`SpearUseGoal.tick` reads `LivingEntity livingEntity = this.mob.getTarget()` and then dereferences it
without a null check; `Zombie.addBehaviourGoals` registers that goal at priority 2 and
`ZombieAttackGoal` at priority 3. `MeleeAttackGoal.stop` clears the target **when the mob's target is
a creative or spectator player**, and a `GoalSelector` pass that stops the melee goal and starts the
spear goal in the same tick then ticks the spear goal with a null target.

Reproduced against the published v26.0-Alpha.3 jar with a real client (`BpmTester`, creative) on the
dedicated test server: provoke a plain zombie, let it start chasing, then hand it a copper spear with
`/item replace entity ... weapon.mainhand`.

| Build | Trigger | Result |
|---|---|---|
| v26.0-Alpha.3 | zombie chasing a creative player is given a copper spear mid-chase | server crash, `NullPointerException ... class_12112.method_6268(class_12112.java:85)`, identical frame-for-frame to the owner's report |
| v26.0-Alpha.4 | the same sequence, repeated with 6 spear give/remove cycles | no exception in the log, server stays up, zombie keeps fighting |

### Any Founds?
The crash is a vanilla 1.21.11 defect, but Real Peace reaches it far more often than vanilla does,
because a provoked mob is handed a target directly instead of acquiring one through
`HurtByTargetGoal`, which makes the two goals churn against each other.

### Solutions
`SpearUseGoalMixin` cancels `tick` at HEAD when the mob has no target. The goal's own cleanup pass
stops it on the following tick, because `canContinueToUse` also requires a live target; the mob
takes no damage and keeps fighting.

### FACTs
A crash whose stack contains no mod frames can still be caused by the mod's state; read the vanilla
goal ordering before concluding otherwise.

version: v26.0-Alpha.4

---

## {FACTTime: 2026.10.03-06:10:00} ContactDamageStillLanded {FACTNum 8}

GitCommitHashRange: feat/alpha4-stability-and-reinforcement-rules (unmerged)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\LivingEntityMixin.java +13 -8
```

### What's Happened?
The owner reported that slime contact damage was still happening, and that a slime touching an iron
golem escalated into an endless helper spiral. Both traced back to the same mistake in the Alpha.2
damage gate.

### Any evidence?
`LivingEntity.hurtServer` has no `amount <= 0` early exit; the Alpha.2 hook only rewrote the amount to
`0.0F`, so the rest of the pipeline still ran:

```java
this.invulnerableTime = 20;                 // the victim lost its i-frame window
... this.knockback(0.4F, d, e);             // and was still pushed around
this.resolveMobResponsibleForDamage(damageSource);  // -> setLastHurtByMob(attacker)
```

That last line is what broke Real Peace: a refused slime touch still set the victim's
`lastHurtByMob`, which the ledger then read back as a live grudge, so the *next* touch landed real
damage and both sides started recruiting.

### Solutions
The gate now injects at HEAD with `cancellable = true` and returns `false` from `hurtServer`, so a
refused hit leaves no trace at all.

Verified on the dedicated server (Real Peace): a size-3 slime was left pushing an iron golem for
12 s.

| Check | Alpha.2 behaviour (predicted) | Alpha.4 result |
|---|---|---|
| Iron golem health | would drop, then escalate | stays 100.0 |
| Iron golem count | would grow to 8 | stays 1 |
| Slime count | would grow as golems killed slimes | stays 1 |

### FACTs
In 1.21.11 an "unprovoked" hit must be cancelled, not zeroed; zero damage is still a hit.

version: v26.0-Alpha.4

---

## {FACTTime: 2026.10.03-05:50:00} GrudgeWindowAndContainment {FACTNum 7}

GitCommitHashRange: feat/alpha4-stability-and-reinforcement-rules (unmerged)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\core\ProvocationLedger.java
.\src\main\java\dev\blockconnect\betterpeacemode\core\ReinforcementManager.java
.\src\main\java\dev\blockconnect\betterpeacemode\core\BabyGuardManager.java
.\src\main\java\dev\blockconnect\betterpeacemode\core\HateGroup.java
```

### What's Happened?
Three containment rules the owner asked for, plus a latent bug in the grudge window.

### Any evidence?
1. `LivingEntity.tick` clears `lastHurtByMob` after 100 ticks, so the configured
   `aggroDurationTicks = 600` never took effect; the ledger now keeps its own expiry per
   (mob, enemy) pair and only uses vanilla memory as an additional source.
2. Groups are now found by enemy instead of by caller.
3. A chunk-aligned area census bounds helper spawning, and super helpers get doubled health.

Measured on the dedicated server, Real Peace, Hard:

| Test | Setup | Result |
|---|---|---|
| Reinforcement size | one zombie provoked by an invulnerable armour stand | 8 zombies = caller + 7 |
| Area cap | `areaLimitMaxEntities = 6`, one zombie provoked | 6 zombies, spawns stop at the cap |
| Group merge | 4 zombies provoked by the same enemy, cap off | 11 zombies (4 + 7), stable after 35 s - not 4x8 |
| Super reinforcements | `superReinforcements = true` | helper cow `Health = 20.0f` (base 10), indefinite `regeneration`, `resistance`, `fire_resistance` |
| Grudge window | provoked sheep/cow pair observed for 12 s | damage continues past tick 100 with no re-provocation |

Zombie helpers only received `resistance` and `fire_resistance`: vanilla's `canBeAffected` refuses
Regeneration on undead mobs, which is correct and documented rather than worked around.

### Any Founds?
An earlier test run showed damage apparently stopping after one hit. The cause was vanilla: an
animal that has been hurt runs `PanicGoal` and flees, so a low-HP attacker only lands a hit when the
fight happens to stay in reach. Not a defect in the retaliation path - recorded here so the next
person does not chase it again.

### FACTs
Vanilla's own "how long does this mob remember being hit" field is measured in 100-tick units;
anything configurable beyond that must be stored by the mod.

version: v26.0-Alpha.4

---

## {FACTTime: 2026.10.03-05:20:00} BabyGuardVerified {FACTNum 6}

GitCommitHashRange: feat/alpha4-stability-and-reinforcement-rules (unmerged)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\core\BabyGuardManager.java +86 -0
.\src\main\java\dev\blockconnect\betterpeacemode\core\PeaceSweep.java +6 -1
```

### What's Happened?
Parents now hate anything that walks into the four-block ring around their baby.

### Any evidence?
Dedicated server, Real Peace, on a roofed stone platform (a first attempt at world height -60 gave a
baby 6 points of damage that turned out to be suffocation from the summon position, not combat; the
clean platform removed it).

| Check | Setup | Result |
|---|---|---|
| Intruder is attacked | adult cow + baby cow + no-AI zombie 2 blocks from the baby | zombie 20.0 -> 17.06 -> 11.18 -> 8.24 -> 2.36 -> dead over 12 s |
| Baby is safe | same run | baby stays at 100.0 the whole time |
| Family is not a threat | adult cow + baby cow + second adult cow inside the ring | nothing attacks anything; all stay at full health |

The 12-second run also demonstrates the grudge store outliving vanilla's 100-tick memory.

### FACTs
Four blocks is small enough that a per-second check misses fast mobs; the guard runs every 5 ticks.

version: v26.0-Alpha.4

---

## {FACTTime: 2026.10.02-19:50:00} Loader19_3Verified {FACTNum 5}

GitCommitHashRange: chore/loader-0.19.3-compat (unmerged)

Files:
```
.\gradle.properties +2 -2
.\src\main\resources\fabric.mod.json +2 -2
```

### What's Happened?
The mod previously demanded Fabric Loader 0.19.5 even though nothing in it uses a 0.19.4+ API, which
excluded every instance pinned to 0.19.3.

### Any evidence?
Built against `loader_version=0.19.3` and launched on both sides:

| Target | Result |
|---|---|
| Client instance created with `--loader-version 0.19.3` | `fabricloader 0.19.3`, `betterpeacemode 26.0.0-Alpha.3`, `[BetterPeaceMode] loaded`, reached the main menu, no mixin errors |
| Dedicated server with the 0.19.3 Fabric server launcher | `fabricloader 0.19.3`, `[BetterPeaceMode] loaded`, `Done (1.089s)!`, no mixin errors |

Fabric API stayed at `0.141.6+1.21.11` throughout.

### Solutions
The metadata floor is now `>=0.19.3` and the API dependency is pinned to `>=0.141.6`, and the build
compiles against 0.19.3 so any accidental use of a newer loader API fails at build time instead of at
runtime.

### FACTs
Declare the oldest loader you actually support and compile against it.

version: v26.0-Alpha.3

***

## {FACTTime: 2026.10.02-17:45:00} UniversalRetaliationVerified {FACTNum 4}

GitCommitHashRange: feat/universal-retaliation-and-reinforcements (unmerged)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\core\RetaliationManager.java +79 -0
.\src\main\java\dev\blockconnect\betterpeacemode\core\ReinforcementManager.java +206 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\LivingEntityMixin.java +62 -0
```

### What's Happened?
Three defects/gaps reported by the owner were confirmed against the shipped 1.21.11 bytecode and
then fixed and verified in a live game.

### Any evidence?
Bytecode findings:

1. `Wolf#registerGoals` ends with `NonTameRandomTargetGoal(Animal)`, so wolves hunt **every** animal
   including foxes - the earlier claim in this document that wolves no longer hunt sheep was wrong
   because it only inspected `NearestAttackableTargetGoal`.
2. `Slime#playerTouch` calls `Slime#dealDamage`, which calls `victim.hurtServer(...)` directly. The
   same is true of `Pufferfish#playerTouch`. Neither goes through `setTarget`, so gating target
   selection alone cannot stop them.
3. `Animal#createAnimalAttributes` only adds `TEMPT_RANGE`; animals do not register
   `ATTACK_DAMAGE` at all, which is why a provoked cow could never hurt anything on its own.

Live-game evidence (Fabric 1.21.11 dedicated server, real client `BpmTester`, Real Peace, Hard):

| Check | Result |
|---|---|
| A size-3 slime sits on the player for 20 s | player health stays 200/200 (contact damage refused) |
| Player hits a cow once | player health drops 200 -> 198.5 (the cow fights back) |
| Cow count after the provocation | 1 -> **8** = caller + 7 reinforcements, matching the configured default |

### Any Perjury?
The first implementation recorded the grudge but gave peaceful mobs no way to act on it, and it
gated only `setTarget`, which left slimes free to keep hurting players.

### Solutions
Damage is now funnelled through `PeacePolicy#shouldRefuseDamage`, so every damage path - AI melee,
contact damage and anything else - passes the same "provoked or refused" test. Retaliation is driven
from `Mob#tick` rather than by injecting AI goals, because Mixin forbids ordinary code from
referencing classes that live in a declared mixin package.

### FACTs
Any claim about mob behaviour must be checked against every goal the class registers, not just the
most obvious one.

version: v26.0-Alpha.2

***

## {FACTTime: 2026.10.02-13:40:00} RealPeaceVerified {FACTNum 3}

GitCommitHashRange: uncommitted (first release)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\core\PeaceSweep.java +0 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\LivingEntityMixin.java +0 -0
```

### What's Happened?
The two modes were validated inside a live game rather than by reasoning alone.

### Any evidence?
A Fabric 1.21.11 dedicated server plus a Fabric 1.21.11 client (username `BpmTester`) were driven
through RCON and Despotes.

| Check | Vanilla | With the mode | Result |
|---|---|---|---|
| Wolf deals 10 damage to a chicken | chicken dies | chicken survives | Better Peace blocks friendly-versus-friendly damage |
| 2 zombies + 1 skeleton adjacent to the player for 60 s | player killed within seconds | player health stays at 100/100 | Real Peace keeps hostiles passive |
| Player hits a zombie once, then waits | - | player health falls 200 -> 191.5 -> 185.5 -> 178 -> 167 -> 155.8 while the zombie stays at 20.0 | the provoked mob retaliates, one-to-one |
| Mode switch changes world difficulty | - | `real_peace` -> Hard, `better_peace` -> Peaceful, `vanilla` -> untouched | difficulty pinning works |

### Any Founds?
End-to-end behaviour is confirmed for every claim the modes make, including the central
one-to-one-retaliation rule.

### Solutions
The mode is expressed as target-acquisition gating plus a damage gate that records a mutual grudge.

### FACTs
Peace rules can be enforced without touching mob AI goals.

version: v26.0-Alpha.1

***

## {FACTTime: 2026.10.02-12:10:00} DifficultyMechanics {FACTNum 2}

GitCommitHashRange: uncommitted (first release)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\core\PeacePolicy.java +0 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\ZombieMixin.java +0 -0
```

### What's Happened?
The mode requirements were mapped onto vanilla difficulty mechanics, and three counter-intuitive
facts were established by reading the shipped bytecode.

### Any evidence?
Findings from `javap -c` against the mapped 1.21.11 jar:

1. `Player#hurtServer` scales `DamageSource#scalesWithDifficulty()` by difficulty and forces the
   amount to zero when the world difficulty is PEACEFUL. A mode that pins the world to PEACEFUL
   therefore makes the player immune to ordinary hostile damage.
2. `ServerPlayer#tickRegeneration` grants 1 HP and 1 saturation per second **only** on PEACEFUL.
3. `NaturalSpawner` contains no difficulty branch; `spawn-monsters` is driven by
   `MinecraftServer#updateMobSpawningFlags`, which is "is the difficulty not PEACEFUL". On 1.21.11
   difficulty does not change the hostile spawn *rate*, only whether hostile mobs may spawn at all.
4. `Zombie#randomizeReinforcementsChance` sets `Attributes.SPAWN_REINFORCEMENTS_CHANCE` to a random
   value, and `Zombie#hurtServer` only applies it when the difficulty is HARD.

Together, forcing difficulty alone reproduces the requested behaviour: Better Peace -> PEACEFUL
(no hostile spawning, peace regeneration), Real Peace -> HARD (hostile spawning, no damage
nullification), and the only collateral is zombie reinforcements, which are pinned to zero.

### Any Founds?
An earlier design that added a `Player#hurtServer` hook to cancel the peaceful damage-nullification
was unnecessary and was removed: Real Peace never runs at PEACEFUL.

### Solutions
Pinning the difficulty from the overworld only, once per difficulty change, avoids multiple
dimensions fighting each other over the same world-wide value.

### FACTs
On 1.21.11 "Hard spawn density" means "hostiles are allowed to spawn", not "a higher spawn rate".

version: v26.0-Alpha.1

***

## {FACTTime: 2026.10.01-20:00:00} VanillaBehaviourDrift {FACTNum 1}

GitCommitHashRange: uncommitted (first release)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\FoxMixin.java +0 -0
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\ZombifiedPiglinMixin.java +0 -0
```

### What's Happened?
The requirement's examples ("fox versus chicken", "wolf versus sheep") no longer both exist in
1.21.11, and `Mob#setTarget` is not inherited by every mob. Both facts were found by scanning the
shipped classes rather than by assumption.

### Any evidence?
A scan of all 698 entity classes reported that only these declare `public void setTarget(LivingEntity)`:
`Mob`, `Fox`, `Creeper`, `EnderMan` and `ZombifiedPiglin` (plus unrelated AI-helper classes).
`Fox#registerGoals` targets `Animal` (which includes chickens).

> Corrected by FACT 4: the claim originally recorded here - that wolves no longer hunt sheep - was
> wrong. `Wolf#registerGoals` also registers `NonTameRandomTargetGoal(Animal)`, so wolves do hunt
> foxes, sheep, chickens and every other animal. The code was already correct because Better Peace
> suppresses friendly-mob-versus-friendly-mob conflict generically.

### Any Perjury?
The first implementation assumed "patching `Mob#setTarget` covers every mob" and used a single
`@ModifyVariable` on `Mob#hurtServer`. The in-game run rejected both: fox predation survived, and
Mixin reported `could not find any targets matching 'method_64397' in class_1308`.

### Any Founds?
Four mob classes override `setTarget`; missing any one of them silently disables the rule for that
mob. Fox was the critical omission because it is the example given for Better Peace.

### Solutions
Each overriding class gets its own thin target gate. The damage hook moved from `Mob#hurtServer` to
`LivingEntity#hurtServer`, which is where the method is actually declared.

### FACTs
Verify inheritance claims against the shipped bytecode; do not extrapolate from one version to the
next.

version: v26.0-Alpha.1

---

License: Apache-2.0

GitHub@NDBlockConnect
