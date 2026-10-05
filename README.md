<p style="font-size:28px;text-align:center;"><b>BetterPeaceMode</b></p>

> Two additional peace-oriented game modes for Minecraft Java Edition: **Better Peace** and **Real Peace**.

> [!NOTE]
> Built for Minecraft **1.21.11** on Fabric. Both modes are layering rules on top of the vanilla
> difficulty system: nothing about the difficulty screen or the saved world format is changed, so a
> world can be opened with or without this mod.

---

## 1 Introduction

Vanilla Peaceful removes hostile mobs entirely, and vanilla Easy/Hard leave every hostile mob free
to start fights. BetterPeaceMode fills the space between those two extremes with two selectable
modes:

| Mode | Difficulty used | Hostile spawning | Who may start a fight |
| :--- | :--- | :--- | :--- |
| `vanilla` | untouched | vanilla | vanilla |
| `better_peace` | Peaceful | none | nobody (friendly mobs ignore each other) |
| `real_peace` | Hard | normal, difficult spawn density | nobody, until something is provoked |

## 2 Modes

### 2.1 Better Peace

Vanilla Peaceful, plus one rule: **friendly mobs no longer attack each other**. A fox stops hunting
chickens, wolves stop hunting sheep, and any other animal-versus-animal predation is suppressed.
Player-versus-mob conflict is untouched, so the world still behaves like the Peaceful everyone
knows.

### 2.2 Real Peace

Better Peace, plus hostile mobs are allowed back:

- Hostile mobs spawn normally, at the difficult spawn density, which means the world stays
  populated the way a Hard-difficulty world does.
- **No hostile entity starts a fight.** Zombies, skeletons, creepers, spiders, endermen and piglins
  ignore players, villagers, animals and each other while they are unprovoked.
- The ender dragon and the wither are exempt: they remain hostile.
- **Provocation creates a one-to-one grudge.** The moment an entity is actually hit, it remembers
  exactly who hit it and may retaliate against that entity and only that entity, for a bounded
  window (600 ticks by default). Hitting any entity therefore starts a fight.
- **Every entity can fight back.** Hostile mobs already know how; cows, sheep, chickens, villagers
  and every other peaceful mob are given the ability the first time they are provoked. Mobs that
  ship an attack-damage value keep it, and the rest are assigned one by size (1 for small, 2 for
  medium, 3 for large), scaled by `retaliationDamageMultiplier`. A provoked mob also *stays* in the
  fight: vanilla's panic goal is suppressed while its grudge is live, so a cow that a player punches
  charges back instead of sprinting away, and it keeps closing the distance and swinging until its
  attacker dies or the grudge expires.
- **Provoked entities call for help.** By default the caller spawns up to 7 helpers of its own type
  within 16 blocks, giving a hate group of 8 including itself. While any member of the group is
  still alive it keeps topping the group up on a randomised 10-30 s schedule, reaching twice the
  radius, and replenishment arrivals carry randomised Speed II-V and Strength I-III. Once every
  member has died the group is dropped and the calls stop.
  Helpers are placed in a spot that actually fits - the search goes upwards and only accepts
  ground within a few blocks - so nobody is buried inside a hillside or dropped off a ledge.
- **One group per enemy, shared by every race.** A hate group belongs to the entity being fought,
  not to the caller: any mob provoked by the same attacker joins the fight that already exists
  instead of opening a second one. Four zombies that a player hits in the same brawl therefore form
  one group of four plus seven helpers, not four groups of eight.
- **The area around a call has an entity budget.** Before spawning helpers the mod counts the mobs
  in a chunk-aligned area around the caller (one chunk by default, or the whole server simulation
  distance if you prefer) and refuses to add more once the configured maximum is reached. A
  saturated area - a full animal pen, a village, a mob farm - simply stops producing helpers until
  something dies.
- **Super reinforcements (optional).** With `superReinforcements` enabled every helper arrives with
  double maximum health and indefinite Resistance, Regeneration and Fire Resistance on top of the
  normal replenishment buffs. Vanilla refuses Regeneration on undead helpers, so zombies and
  skeletons keep the other two.
- **Parents guard their young.** Any adult of the same species within 16 blocks of a baby hates
  whatever living entity comes within 4 blocks of that baby, and attacks it - a cow will charge the
  wolf that walked up to its calf. Siblings and herd mates are never treated as threats, and
  creative or spectator players are ignored so a parent cannot lock onto someone it can never hurt.
- **Nobody picks a fight they cannot win.** A mob never chases a creative or spectator player: it
  cannot hurt them, so the peace modes refuse the target outright instead of letting a zombie follow
  an invulnerable player around forever. With `creativeGrudgeCarryOver` on, the provocation is not
  forgotten either - the mob waits, and the moment that player is back in survival or adventure the
  grudge goes live and it comes looking.
- **Idle mobs stay idle.** In Real Peace a mob may only acquire a target through a live grudge, so a
  wolf stops hunting sheep and an iron golem stops picking fights. The single exception is the
  opt-in hostile-versus-hostile brawl when `hostilesIgnoreEachOther` is turned off.
- **Anger-driven mobs are gated too.** The warden does not pick fights through target selection - it
  fills an anger table from the vibrations it hears, and an angry warden roars, charges and swings.
  That table is now gated at its single predicate, so an unprovoked warden no longer reacts to (or
  swings at) a protected mob; a warden that was actually hit still retaliates.
- Nether mobs stay calm outside the Nether and do not mutate: piglins do not turn into zombified
  piglins and hoglins do not turn into zoglins, and while they are away from home they will not
  start a fight - though they will still retaliate if attacked.
- Hard difficulty's zombie reinforcement swarm is disabled, so combat stays strictly one-to-one.
- The player keeps the Peaceful regeneration rate (1 HP and 1 saturation per second while hurt),
  which the Hard difficulty alone would not provide.
- Attacks that never go through AI target selection are covered too: a slime or a pufferfish hurts
  whatever it touches, and those contact hits are cancelled outright while the creature is
  unprovoked - no damage, no knockback, no invulnerability window, and no grudge.

## 3 Configuration

`config/betterpeacemode.json` is created on first launch:

```json
{
  "gameMode": "VANILLA",
  "friendlyPeace": false,
  "realPeace": false,
  "keepBossHostile": true,
  "hostilesIgnoreEachOther": true,
  "crossDimensionCalm": true,
  "aggroDurationTicks": 600,
  "universalRetaliation": true,
  "retaliationDamageMultiplier": 1.0,
  "reinforcementsEnabled": true,
  "reinforcementCount": 7,
  "reinforcementRadius": 16.0,
  "autoReinforce": true,
  "autoReinforceMaxSeconds": 30,
  "autoReinforceRadiusMultiplier": 2.0,
  "areaLimitEnabled": true,
  "areaLimitRadiusChunks": 1,
  "areaLimitUseSimulationDistance": false,
  "areaLimitMaxEntities": 24,
  "superReinforcements": false,
  "babyGuardEnabled": true
}
```

| Field | Meaning |
| :--- | :--- |
| `gameMode` | `VANILLA`, `BETTER_PEACE` or `REAL_PEACE`. This is the normal way to select a mode. |
| `friendlyPeace` / `realPeace` | Fine-grained overrides, useful when a pack wants to combine rules. |
| `keepBossHostile` | Keep the ender dragon and the wither hostile in Real Peace. |
| `hostilesIgnoreEachOther` | Keep hostile mobs from starting fights with each other. Turning it off still protects players, villagers and animals. |
| `crossDimensionCalm` | Keep Nether mobs calm and unmutated away from the Nether. |
| `aggroDurationTicks` | How long a one-to-one grudge lasts, clamped to 20-24000. |
| `creativeGrudgeCarryOver` | Remember a hit from a creative or spectator player and turn it into a real grudge once that player can be hurt again. |
| `universalRetaliation` | Let every provoked entity fight back, including peaceful mobs that have no combat AI. |
| `retaliationDamageMultiplier` | Scales the damage a provoked entity deals. |
| `reinforcementsEnabled` | Let provoked entities call helpers of their own type. |
| `reinforcementCount` | Helpers per call; the hate group caps at this value + 1 (the caller). |
| `reinforcementRadius` | Radius, in blocks, of the initial call. |
| `autoReinforce` | Keep topping the group up while any member is alive. |
| `autoReinforceMaxSeconds` | Upper bound of the randomised delay between automatic calls (min 10 s). |
| `autoReinforceRadiusMultiplier` | Automatic calls reach this many times further than the initial call. |
| `areaLimitEnabled` | Refuse reinforcement spawns once the area around the caller is saturated. |
| `areaLimitRadiusChunks` | Radius of that area in chunks; `0` is the caller's own chunk, `1` the surrounding 3x3. |
| `areaLimitUseSimulationDistance` | Use the server's whole simulation distance instead of the fixed radius. |
| `areaLimitMaxEntities` | Mobs allowed inside the area before further helper spawns are refused. |
| `superReinforcements` | Helpers arrive with double health and indefinite Resistance, Regeneration and Fire Resistance. |
| `babyGuardEnabled` | Adults hate anything that comes within 4 blocks of a baby of their own species. |

## 4 Commands

Requires permission level 2 (gamemasters) or higher, so single-player worlds and server operators
both work.

| Command | Effect |
| :--- | :--- |
| `/betterpeace status` | Print the active mode, difficulty and rule flags. |
| `/betterpeace mode <vanilla\|better_peace\|real_peace>` | Switch mode and persist it. |
| `/betterpeace reload` | Re-read the config file from disk. |
| `/betterpeace save` | Write the current in-memory config to disk. |

In single-player the same settings are available from an in-game screen, opened with the mod's
key binding (default `B`). On a dedicated server the client screen edits the local config, so
operators should use the command or the server's config file instead.

That screen is also the **shared settings window for the BlockConnect Minecraft line**. Companion
mods register their own pages through the client API
(`dev.blockconnect.betterpeacemode.client.api.ConfigPageRegistry`), so installing several of them
still means one window and one key binding. The page selector shows every page and its position, and
each page is written against a small layout context so the controls line up with the built-in ones.

An entity carrying the scoreboard tag `bpm.no_reinforcements` is excluded from the recruitment
system entirely - the hook staged fights such as the ArenaMode gladiator arena use to keep a wave at
the size the operator asked for.

## 5 How it works

The mode is expressed as a small set of vanilla entry points rather than as a rewrite of mob AI:

- Target acquisition (`Mob#setTarget`, plus the four subclasses that override it - fox, creeper,
  enderman and zombified piglin) is gated, which stops a mob from ever starting a pursuit.
- Damage application (`LivingEntity#hurtServer`) is gated, which cancels friendly-versus-friendly and
  unprovoked hits before any of vanilla's hurt bookkeeping runs, and records the one-to-one grudge in
  both directions for the hits that do land.
- A once-per-second server sweep re-checks live targets, pins the difficulty for the active mode,
  keeps Nether mobs from mutating, prunes expired grudges and bounds the cost to a fixed budget. A
  faster pass on the same sweep drives the baby guard.
- The grudge itself is kept by the mod rather than by vanilla's `lastHurtByMob`, because vanilla
  forgets that field after 100 ticks and would silently cap the configured window at five seconds.
- `SpearUseGoal#tick` is guarded against a null target. 1.21.11 hands zombies and zombified piglins
  that goal, and vanilla's `MeleeAttackGoal#stop` clears the target when it is displaced, which
  crashes the server when it happens to a zombie that is fighting a creative-mode player.
- Zombie reinforcements are pinned to zero so no fight can escalate into a swarm.

## 6 Build

```powershell
$env:JAVA_HOME = "<a JDK 25>"      # Loom 1.18 needs JDK 25 to run
.\gradlew.bat build                # the mod itself still targets Java 21
```

Output: `build/libs/BetterPeaceMode-v26.0-Alpha.4-JE-1.21.11-Fabric.jar`.

## 7 Compatibility

- Minecraft 1.21.11, Fabric Loader 0.19.3+ and Fabric API (the command surface uses the Fabric
  command callback).
- Client and dedicated server both work; the rules are installed from the common entry point.
- Works alongside other mods. The patches touch only well-known vanilla methods and become no-ops
  when the selected mode is `vanilla`.

---

License: Apache-2.0

GitHub@NDBlockConnect
