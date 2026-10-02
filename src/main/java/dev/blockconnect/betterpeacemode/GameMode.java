package dev.blockconnect.betterpeacemode;

/**
 * The two game modes provided by BetterPeaceMode.
 *
 * <p>Neither mode touches the vanilla difficulty enum. Both are layered on top of whatever
 * difficulty the world already uses, which keeps worlds shareable with vanilla clients and
 * keeps the vanilla difficulty screen untouched.
 */
public enum GameMode {
    /** Vanilla behaviour: the mod is installed but does nothing. */
    VANILLA(0, "vanilla"),
    /**
     * Vanilla Peaceful, plus friendly-mob conflict suppression.
     *
     * <p>Hostile mobs still do not spawn (that is ordinary Peaceful). What changes is that
     * friendly mobs no longer attack each other: a fox stops hunting chickens and a wolf
     * stops hunting sheep.
     */
    BETTER_PEACE(1, "better_peace"),
    /**
     * {@link #BETTER_PEACE}, plus hostile mobs spawn normally but stay passive until provoked.
     *
     * <p>Hostile entities ignore players, villagers and each other unless provoked. Once a
     * fight starts it is strictly one-to-one: the victim retaliates against its attacker only.
     */
    REAL_PEACE(2, "real_peace");

    private final int tier;
    private final String id;

    GameMode(int tier, String id) {
        this.tier = tier;
        this.id = id;
    }

    /** Ordering weight; a higher tier implies all lower-tier rules. */
    public int tier() {
        return this.tier;
    }

    /** Stable lowercase identifier used by the config file and commands. */
    public String id() {
        return this.id;
    }

    public boolean friendlyPeace() {
        return this.tier >= BETTER_PEACE.tier;
    }

    public boolean realPeace() {
        return this == REAL_PEACE;
    }

    /** Parses a case-insensitive id, accepting the common {@code real-peace} spelling. */
    public static GameMode byId(String raw) {
        if (raw == null) {
            return VANILLA;
//GitH ub @ ND Bl  o ckConn ec  t | Bl  o  c kC onnect@  Sta rs ailsClove  r
        }
        String normalized = raw.trim().toLowerCase(java.util.Locale.ROOT).replace('-', '_');
        for (GameMode mode : values()) {
            if (mode.id.equals(normalized)) {
                return mode;
            }
        }
        return VANILLA;
    }
}
