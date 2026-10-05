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

## {FACTTime: 2026.10.05-19:00:00} SharedConfigScreenVerified {FACTNum 17}

GitCommitHashRange: main (v26.0-Alpha.7 with ArenaMode v26.0-Alpha.2)

Files:
```
(verification only - no source changes)
```

### What's Happened?
FACT 16 had to leave one half of the ArenaMode integration open: ArenaMode contributes its Arena and
Waves pages through BetterPeaceMode's `ConfigPageRegistry`, and proving that needs a client driven to
open the shared screen. The test client was free this time.

### Any evidence?
Real client (`BpmTester4`) on the isolated server, driven entirely through the Despotes control
channel - a `key` action for `B`, `click` actions for the page selector, `screenshot` for the frames:

| Frame | What it shows |
|---|---|
| `_test-artifacts/config-page-1.png/20261005-184751-301-req.png` - 155069 B, sha256 `DE53A414E7330F40…` | the shared screen titled **BetterPeaceMode**, `Page: Rules (1/4)`, with Mode, Rule: real peace, Hostiles ignore each other, Universal retaliation, Grudge, Damage x, Nether mobs stay calm, Creative grudge carries over and Baby guard |
| `_test-artifacts/config-page-3.png/20261005-185043-488-req.png` - 219210 B, sha256 `4F4F628EEC4817E0…` | `Page: Arena (3/4)` with Arena radius, Endless, Boundary particles and the cross-reference `Waves: 2 - edit them on the Waves page` |

The page counter is the second half of the evidence: BetterPeaceMode ships two built-in pages, so
`(1/4)` means two further pages are registered by the companion mod - ArenaMode's Arena and Waves
pages - and the Arena frame shows one of them rendering with its own controls.

### Any Founds?
The client was taken over by a parallel workstream again while this ran (a fresh `bpm-l193` client
came up at 18:51), so the page-4 frame could not be captured. Frames 1/4 and 3/4 already prove both
registration and rendering, which makes the missing frame a nice-to-have rather than a gap. The
screenshots live in the gitignored `_test-artifacts/` and are referenced here by size and hash.

### FACTs
A shared extension point needs a screenshot, not just a compiling registration: the page counter
alone would have shown that four pages exist without proving that any of them renders.

version: v26.0-Alpha.7

---

## {FACTTime: 2026.10.05-18:20:00} ArenaIntegrationTagContract {FACTNum 16}

GitCommitHashRange: main (v26.0-Alpha.7 with ArenaMode v26.0-Alpha.2)

Files:
```
(verification only - no source changes)
```

### What's Happened?
ArenaMode stages fights whose size the operator chooses, and the thing that keeps the size is a tag
contract between the two mods: arena mobs carry `arenamode.arena` **and** `bpm.no_reinforcements`, and
BetterPeaceMode refuses to recruit anything carrying the second tag. This run checked that contract
on the wire, together with the session lifecycle around it.

### Any evidence?
Isolated dedicated server, ArenaMode 26.0.0-Alpha.2 + BetterPeaceMode v26.0-Alpha.7. Every hit was
issued by an invulnerable armour stand, so the provocation is real but the attacker cannot die:

| Case | Caller tags | Zombies after 12 s |
|---|---|---|
| tagged caller | `["a_zombie","bpm.no_reinforcements"]` | **1** - no recruitment |
| untagged caller | `["b_zombie"]` | **8** = caller + 7 helpers |
| the arena's contract | `["c_zombie","arenamode.arena","bpm.no_reinforcements"]` | **1** - no recruitment; tags read back exactly as sent |

Two more behaviours were visible in the same session's log:

```
[ArenaMode] wave 1/1 minecraft:zombie x1 for 120s started: spawned=1 survivors=0 aliveTotal=1
[ArenaMode] adopted untracked arena entity entity.minecraft.zombie into its wave
[ArenaMode] ending session: ownerOnline=false levelPresent=true ownerAlive=false sameLevel=false
```

That is the wave rotation, the Alpha.2 stray-adoption path and the auto-cleanup on owner disconnect,
all running against the released BetterPeaceMode jar.

### Any Founds?
The leftover stone slab from FACT 13 caught two more test mobs. Zombies summoned at y=111 inside it
suffocated, and the suffocation damage's invulnerability window made the very next `/damage` answer
`Target is invulnerable to the given damage type` - which looked exactly like the tag rule being
broken. Moving the scene to a clean platform at y=140 made both cases behave. Never place test mobs
on a coordinate another test has already built in.

**Still unverified**: the shared settings screen itself. ArenaMode contributes its Arena/Waves pages
through BetterPeaceMode's `ConfigPageRegistry`, and checking that needs a client driven to press `B`.
The shared test client was taken over by a parallel workstream (a new `bpm-l193` client came up at
18:13 while that thread's own server was running), so that half is deferred rather than skipped.

### FACTs
A cross-mod contract is worth testing on the wire, not only in the code: the tag was in both
code-bases and the behaviour still had to be proven with a tagged caller and an untagged control.

version: v26.0-Alpha.7

---

## {FACTTime: 2026.10.05-17:50:00} HateGroupSoak {FACTNum 15}

GitCommitHashRange: main (v26.0-Alpha.7, released)

Files:
```
.\_scripts\hate-group-soak.ps1 +150 -0
```

### What's Happened?
The complaint that started this whole line of work was a fight that grew until the world died:
slimes calling helpers, helpers calling more helpers, splits feeding the pile. Alpha.4 through
Alpha.7 added the rules meant to stop that; this soak keeps ten hate groups alive at once for four
minutes and watches the server instead of the fight.

### Any evidence?
Released Alpha.7 jar (sha256 `9f9976c7…`), isolated dedicated server, `areaLimitEnabled=false` so the
per-area budget cannot mask the group logic, ten invulnerable enemies with one caller each - five
zombies and five cows, all at 100 HP so nothing dies of its own accord:

| t | ticks/s | mobs | mem (MB) |
|---|---|---|---|
| 20s | 20.08 | 80 | 565 |
| 41s | 19.99 | 80 | 560 |
| 61s | 20.03 | 79 | 573 |
| 122s | 20.00 | 80 | 554 |
| 183s | 19.98 | 80 | 564 |
| 245s | 19.98 | 80 | 540 |

min ticks/s = 19.94 (target 20), peak mobs = 80, which is exactly
`groups x (reinforcementCount + 1)` = 10 x 8. The single 79 is a helper that was briefly gone and was
replaced by that group's next top-up call, which is the replenishment path doing its job.

No exception, no `Can't keep up!` warning and no memory growth in the whole run. The cows matter as
much as the zombies here: half the groups were peaceful callers, so universal retaliation, the
chase path and recruitment all ran for mobs that have no combat AI of their own.

### FACTs
"Bounded" is a claim about the ceiling, not about the first sample: hold the scene long enough for
several auto-reinforce timers to fire before believing a population is stable.

version: v26.0-Alpha.7

---

## {FACTTime: 2026.10.05-17:30:00} CreativeCarryOverVerified {FACTNum 14}

GitCommitHashRange: main (v26.0-Alpha.7, released)

Files:
```
.\_scripts\creative-carryover-test.ps1 +180 -0
```

### What's Happened?
Alpha.5 shipped two rules that had never been exercised end to end: a mob must never lock onto a
creative or spectator player, and a hit from such a player is remembered as a *pending* grudge that
is promoted the moment that player is hurtable again. Both were checked with a real client standing
in the world on the isolated dedicated server.

### Any evidence?
Staged run with a temporary debug line in the ledger, fresh client `BpmTester2`:

```
17:23:45 [BPM-DEBUG] pending recorded holder=entity.minecraft.zombie player=BpmTester2 carryOver=true
17:23:58 [BPM-DEBUG] pending promoted holder=entity.minecraft.zombie player=BpmTester2 distanceSqr=121.63 target=null
```

The promotion fired on the first sweep after the player switched to survival; the zombie then walked
from twelve blocks away to 0.8 blocks and took the player from 200 HP to 195.5 within seconds.

`_scripts/creative-carryover-test.ps1` repeats that as seven assertions:

| Case | Evidence | Result |
|---|---|---|
| the player stands on the platform | y=130, max health 20 | PASS |
| the creative player can still land the hit | provocation issued | PASS |
| a creative player is never locked onto | closest approach 12.9 blocks over 12 s | PASS |
| the creative player takes no damage | 20.0 of 20.0 | PASS |
| the player really is in survival for phase two | playerGameType=0 | PASS |
| the remembered grudge is promoted once hurtable | health 20.0 -> 0.0 | PASS |
| the zombie closes the distance in survival | closest approach 0.8 blocks | PASS |

### Any Founds?
Three harness problems produced "product is broken" results before the real cause was found:

1. **PowerShell split the commands.** `@('tp ' + $Player + ' 0 130 0', ...)` is not one string: the
   array literal turns it into `'tp '`, `'BpmTester2'`, `' 0 130 0'`, so the script sent three
   invalid commands. The teleport never happened, the gamemode never changed and the damage command
   never had an attacker - which is why three consecutive runs "proved" the carry-over was broken.
   The script now uses interpolated strings, and the ledger's debug output was the evidence that
   finally separated the two.
2. **A dead client stays dead.** A player that dies in an unattended client sits on the respawn
   screen: its player data keeps health 0, so every later phase reads nonsense (the first guard run
   reported `y=130, gameType=0, health=0`). The harness now checks for that and stops with a SKIP
   line, and the test moved to a fresh username.
3. **The test instance does not run a vanilla 20 HP player.** Reading `Health` and assuming 20 made
   the "no damage in creative" check meaningless; the harness now reads the max-health attribute
   first.

### FACTs
A red test is not evidence of a product defect until you have confirmed the test's own commands
reached the server; print what is actually sent when a harness disagrees with a hand-run command.

version: v26.0-Alpha.7

---

## {FACTTime: 2026.10.05-16:00:00} ThreeModeRegression {FACTNum 13}

GitCommitHashRange: main (v26.0-Alpha.7, released)

Files:
```
.\_scripts\mode-regression.ps1 +170 -0
.\_scripts\panic-ab-test.ps1 +120 -0
```

### What's Happened?
Alpha.5 through Alpha.7 changed the target gate, the damage gate, the panic goal and the
reinforcement placement. The three modes were re-run end to end against the released jar to make
sure none of those changes moved a rule that was already correct.

### Any evidence?
`_scripts/mode-regression.ps1` against the published Alpha.7 jar on an isolated dedicated server
(port 25566, RCON 25576). Each case is one mob-versus-mob hit whose only variable is the active mode,
plus an attacker-less control hit that must always land:

| Case | Evidence | Result |
|---|---|---|
| vanilla lets a wolf hurt a sheep | mob hit removed 3.00, control removed 3.00 | PASS |
| Better Peace pins the world to Peaceful | difficulty=Peaceful | PASS |
| Better Peace refuses wolf-on-sheep | mob hit 0.00, control 3.00 | PASS |
| Real Peace pins the world to Hard | difficulty=Hard | PASS |
| Real Peace refuses an unprovoked wolf-on-sheep | mob hit 0.00, control 3.00 | PASS |
| Real Peace keeps an unprovoked warden off a villager | villager 100.0 -> 100.0 over 15 s | PASS |
| Real Peace lets a provoked warden retaliate | cow 100.0 -> dead over 15 s | PASS |

The last two are a pair on purpose: the Alpha.6 warden gate must stop an unprovoked warden without
stopping a provoked one, and the baby guard's mutual grudge is the only way to provoke a warden
without a player online.

### Any Founds?
Two harness traps, both of which produced convincing false failures before they were understood:

1. The control hit was swallowed by the victim's own 20-tick invulnerability window, which made
   "vanilla allows a wolf to hurt a sheep" fail even though the first hit landed. The harness now
   waits 1.4 s between the two hits.
2. The warden case originally stood the villager at (4,111,0) - inside a stone slab left behind by
   the placement test. A villager suffocating inside rock loses 2 HP/s with no `last_hurt_by_mob`
   recorded, which reads exactly like a mob beating on it. The case now runs on a clean platform at
   y=125, and the warden's anger table is empty in the same run.

### FACTs
Damage arriving at 2 HP/s with no attacker recorded is suffocation, not combat; check the test
world for leftover blocks before blaming a mob.

version: v26.0-Alpha.7

---

## {FACTTime: 2026.10.05-15:20:00} ProvokedMobsKeptFighting {FACTNum 11}

GitCommitHashRange: feat/alpha7-stand-and-fight (unmerged)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\mixin\PanicGoalMixin.java +50 -0
.\src\main\java\dev\blockconnect\betterpeacemode\core\RetaliationManager.java +24 -2
```

### What's Happened?
Real Peace promises that anything which is hit fights back, and Alpha.7 was written to close the gap
the release notes had been carrying as a known limitation: provoked animals hit once and then ran.

### Any evidence?
A deterministic scenario on an isolated dedicated server (`bpm-server-1211`, port 25566, RCON
25576) with reinforcements switched off so the fight stays two-sided: the baby guard gives a cow a
live grudge against a no-AI zombie standing next to its calf, and the harness feeds the cow an
attacker-less fall hit every two seconds, which is vanilla's panic trigger. Every number is read
back over RCON.

| Build | zombie HP at 0/2/4/6/8/10 s | Outcome |
|---|---|---|
| Alpha.6 | 20.00 / 17.06 / 14.12 / 8.24 / 2.36 / 2.36 | five hits in eight seconds, then no damage for the rest of the run - the cow had left the fight |
| Alpha.7 | 20.00 / 17.06 / 14.12 / 8.24 / 5.30 / dead | keeps swinging until the target dies, while still being panicked every two seconds |

### Any Founds?
Two earlier attempts to measure this were junk, and both are worth remembering:

1. **Reinforcements confounded the first runs.** The moment the cow landed its first hit the zombie
   recruited seven helpers under the normal rules, and the horde - not the panic - is what killed the
   cow. The scenario now runs with `reinforcementsEnabled=false`.
2. **The calf was standing in the path.** With the calf between the cow and the zombie, the cow
   stopped 1.8 blocks short of its target, which looked exactly like a melee-range bug. Moving the
   calf aside showed the mob paths fine; the stall was geometry, not reach.

An intermediate attempt also switched the chase to `Navigation#createPath(entity, 0)` on the theory
that the convenience overload stops one block short. Vanilla's own `MeleeAttackGoal` uses the
convenience overload, so that theory was wrong and the change was reverted; what remained is the
re-path cadence, which is what the vanilla goal actually does differently from a per-tick call.

### Solutions
`PanicGoalMixin` cancels `shouldPanic` while the mob holds a live grudge, and the counter-attack
re-plans its path every ten ticks instead of every tick.

### FACTs
Before blaming a game mechanic for a behavioural result, make sure the test world is not fighting
back: a second wave of mobs and a baby animal standing in the path both produced convincing but
wrong conclusions.

version: v26.0-Alpha.7

---

## {FACTTime: 2026.10.05-15:28:00} HelperPlacementVerified {FACTNum 12}

GitCommitHashRange: feat/alpha7-stand-and-fight (unmerged)

Files:
```
.\src\main\java\dev\blockconnect\betterpeacemode\core\ReinforcementManager.java +46 -4
```

### What's Happened?
Helpers used to be placed at the caller's own Y, which buried them in a hillside or a slab when the
random offset landed in solid rock; a buried helper suffocates before it reaches the fight.

### Any evidence?
Isolated server, `reinforcementRadius` set to 6, a stone slab covering the eastern half of the spawn
area at helper height, one provoked zombie:

| Build | Zombies alive after 8 s | Notes |
|---|---|---|
| first version (searched up and down) | 4 of 8 | the downward search put the rest under the platform, where they fell to their deaths; fresh rotten flesh confirmed it |
| final version (searches up only, needs ground within 4 blocks) | 8 of 8 | four helpers stood on the roof at y=115 - relocated out of the slab instead of dying; the three rotten-flesh stacks still in the world were 1505 ticks old, i.e. leftovers from the failed run |

### FACTs
A placement search has to be judged by where it can send a mob, not only by whether the spot is
free: open air under a platform is free and lethal.

version: v26.0-Alpha.7

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
