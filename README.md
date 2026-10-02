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
- Nether mobs stay calm outside the Nether and do not mutate: piglins do not turn into zombified
  piglins and hoglins do not turn into zoglins, and while they are away from home they will not
  start a fight - though they will still retaliate if attacked.
- Hard difficulty's zombie reinforcement swarm is disabled, so combat stays strictly one-to-one.
- The player keeps the Peaceful regeneration rate (1 HP and 1 saturation per second while hurt),
  which the Hard difficulty alone would not provide.

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
  "aggroDurationTicks": 600
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

## 4 Commands

Requires permission level 2 (gamemasters) or higher, so single-player worlds and server operators
both work.

| Command | Effect |
| :--- | :--- |
| `/betterpeace status` | Print the active mode, difficulty and rule flags. |
| `/betterpeace mode <vanilla\|better_peace\|real_peace>` | Switch mode and persist it. |
| `/betterpeace reload` | Re-read the config file from disk. |
| `/betterpeace save` | Write the current in-memory config to disk. |

## 5 How it works

The mode is expressed as a small set of vanilla entry points rather than as a rewrite of mob AI:

- Target acquisition (`Mob#setTarget`, plus the four subclasses that override it - fox, creeper,
  enderman and zombified piglin) is gated, which stops a mob from ever starting a pursuit.
- Damage application (`LivingEntity#hurtServer`) is gated, which stops friendly-versus-friendly hits
  from landing and records the one-to-one grudge in both directions.
- A once-per-second server sweep re-checks live targets, pins the difficulty for the active mode,
  keeps Nether mobs from mutating, and bounds the cost to a fixed budget.
- Zombie reinforcements are pinned to zero so no fight can escalate into a swarm.

## 6 Build

```powershell
$env:JAVA_HOME = "<a JDK 25>"      # Loom 1.18 needs JDK 25 to run
.\gradlew.bat build                # the mod itself still targets Java 21
```

Output: `build/libs/BetterPeaceMode-v26.0-Alpha.1-JE-1.21.11-Fabric.jar`.

## 7 Compatibility

- Minecraft 1.21.11, Fabric Loader 0.19.5+ and Fabric API (the command surface uses the Fabric
  command callback).
- Client and dedicated server both work; the rules are installed from the common entry point.
- Works alongside other mods. The patches touch only well-known vanilla methods and become no-ops
  when the selected mode is `vanilla`.

---

License: Apache-2.0

GitHub@NDBlockConnect
