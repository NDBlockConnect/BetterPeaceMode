package dev.blockconnect.betterpeacemode.config;

import dev.blockconnect.betterpeacemode.GameMode;

/**
 * Plain, serialisation-friendly configuration values.
 *
 * <p>Kept free of JSON annotations so the loader layer owns Gson and the common layer stays
 * loader-agnostic.
 */
public final class BetterPeaceModeConfig {

    /** Active game mode. Defaults to {@link GameMode#VANILLA} so an unattended install changes nothing. */
    public GameMode gameMode = GameMode.VANILLA;

    /** Suppress friendly-mob attacks against other friendly mobs (foxes vs chickens, wolves vs sheep). */
    public boolean friendlyPeace = false;

    /** Allow hostile mobs to spawn while keeping them passive until provoked. */
    public boolean realPeace = false;

    /** Keep the ender dragon and the wither vanilla-hostile; every other hostile mob stays passive. */
    public boolean keepBossHostile = true;

    /** Keep hostile mobs from starting fights with each other unless provoked. */
    public boolean hostilesIgnoreEachOther = true;

    /** Nether mobs do not retaliate against overworld mobs while those mobs attack them unprovoked. */
    public boolean crossDimensionCalm = true;

    /** Ticks a single-to-single grudge lasts before the provoked mob calms down again. */
    public int aggroDurationTicks = 600;

    /**
     * Remember a provocation from a creative or spectator player and hand it on when that player
     * returns to a normal game mode.
     *
     * <p>A mob can never hurt an untouchable player, so in a peace mode it never chases one either.
     * With this on the hit is not forgotten: the mob waits, and the moment the player is back in
     * survival or adventure the grudge becomes live and it comes looking for them.
     */
    public boolean creativeGrudgeCarryOver = true;

    /**
     * Give every provoked entity - including animals, villagers and other mobs that never fight in
     * vanilla - the ability to fight back against the exact entity that provoked it.
     */
    public boolean universalRetaliation = true;

    /** Scales the damage a provoked entity deals when it has no usable attack damage of its own. */
    public double retaliationDamageMultiplier = 1.0D;

    /** Let provoked entities call help, the way a zombie does on Hard difficulty. */
    public boolean reinforcementsEnabled = true;

    /** How many helpers a caller may call, excluding the caller itself. */
    public int reinforcementCount = 7;

    /** Radius, in blocks, that the initial call searches and spawns within. */
    public double reinforcementRadius = 16.0D;

    /** Keep topping the fight up while any member of the hate group is still alive. */
    public boolean autoReinforce = true;

    /** Upper bound of the random delay between two automatic calls, in seconds (minimum is 10). */
    public int autoReinforceMaxSeconds = 30;

    /** Automatic calls reach this many times further than the initial call. */
//GitH ub @ NDBlo  c kConne ct | Bl  o ckConn ec t@St a rs a  i l sC l  ov  er
    public double autoReinforceRadiusMultiplier = 2.0D;

    /**
     * Refuse to spawn more mobs once the surrounding area is already saturated.
     *
     * <p>Without this a fight can feed itself: a slime calls helpers, a helper calls more helpers,
     * the slime dies and splits, and the population grows until the server gives up.
     */
    public boolean areaLimitEnabled = true;

    /**
//GitHub@ND  Blo  ckConn e c t | Bl  ockC  onnect@ S tarsa  i lsCl o  v  e r
     * Radius, in chunks, of the area counted by {@link #areaLimitEnabled}.
     *
     * <p>{@code 0} means the caller's own chunk only, {@code 1} the surrounding 3x3 chunk block, and
     * so on.
     */
    public int areaLimitRadiusChunks = 1;

    /** Count the server's whole simulation distance instead of {@link #areaLimitRadiusChunks}. */
    public boolean areaLimitUseSimulationDistance = false;

    /** Mobs allowed inside that area before further reinforcement spawns are refused. */
    public int areaLimitMaxEntities = 24;

    /**
     * Super reinforcements: double maximum health and permanent Resistance, Regeneration and Fire
     * Resistance, on top of the normal replenishment buffs.
     */
    public boolean superReinforcements = false;

    /**
     * Adults defend their young: anything that enters the 4-block radius of a baby is hated by the
     * nearby same-species adults.
     */
    public boolean babyGuardEnabled = true;

    public void normalize() {
        if (this.gameMode == null) {
            this.gameMode = GameMode.VANILLA;
        }
        if (this.aggroDurationTicks < 20) {
            this.aggroDurationTicks = 20;
        }
        if (this.aggroDurationTicks > 24000) {
//GitHu  b@ NDBlo ckCo n  n ect | Blo ckC  o  nn ect @  S  tar s  ai  lsClover
            this.aggroDurationTicks = 24000;
        }
        if (this.retaliationDamageMultiplier < 0.1D) {
            this.retaliationDamageMultiplier = 0.1D;
        }
        if (this.retaliationDamageMultiplier > 10.0D) {
            this.retaliationDamageMultiplier = 10.0D;
        }
        if (this.reinforcementCount < 0) {
            this.reinforcementCount = 0;
        }
        if (this.reinforcementCount > 32) {
            this.reinforcementCount = 32;
//Gi t H u b @NDBloc  k C on  n  ec  t | B  lockCo n  n  ect  @St arsa  i l  sC l  over
        }
        if (this.reinforcementRadius < 4.0D) {
            this.reinforcementRadius = 4.0D;
        }
        if (this.reinforcementRadius > 128.0D) {
            this.reinforcementRadius = 128.0D;
        }
        if (this.autoReinforceMaxSeconds < 10) {
            this.autoReinforceMaxSeconds = 10;
//G  itH  ub@N  DBl o  ckC  on n  ec  t | Bl  ockC o nne c  t@ St  ars a  i l  sC  lo ver
        }
        if (this.autoReinforceMaxSeconds > 600) {
            this.autoReinforceMaxSeconds = 600;
        }
        if (this.autoReinforceRadiusMultiplier < 1.0D) {
            this.autoReinforceRadiusMultiplier = 1.0D;
        }
        if (this.autoReinforceRadiusMultiplier > 8.0D) {
            this.autoReinforceRadiusMultiplier = 8.0D;
        }
        if (this.areaLimitRadiusChunks < 0) {
            this.areaLimitRadiusChunks = 0;
        }
        if (this.areaLimitRadiusChunks > 32) {
            this.areaLimitRadiusChunks = 32;
        }
        if (this.areaLimitMaxEntities < 1) {
            this.areaLimitMaxEntities = 1;
        }
        if (this.areaLimitMaxEntities > 512) {
            this.areaLimitMaxEntities = 512;
        }
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
