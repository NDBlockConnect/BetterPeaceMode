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

    public void normalize() {
        if (this.gameMode == null) {
            this.gameMode = GameMode.VANILLA;
        }
        if (this.aggroDurationTicks < 20) {
            this.aggroDurationTicks = 20;
        }
        if (this.aggroDurationTicks > 24000) {
            this.aggroDurationTicks = 24000;
        }
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
