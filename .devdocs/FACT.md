<p style="font-size:28px;text-align:center;"><b>BetterPeaceMode FACT</b></p>

> Development facts for BetterPeaceMode, newest first. Each entry records what was checked, how it
> was checked, and what the evidence was.

---

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
Separately, `Wolf#registerGoals` targets only `Player` and `AbstractSkeleton`, so wolves no longer
hunt sheep in 1.21.11, while `Fox#registerGoals` targets `Animal` (which includes chickens).

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
