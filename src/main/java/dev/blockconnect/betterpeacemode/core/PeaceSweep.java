package dev.blockconnect.betterpeacemode.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

/**
 * Boundary enforcement that runs once per second on each server level.
 *
 * <p>The Mixin hooks stop fights from starting, but a mob can still hold a stale target if the
 * mode changed mid-fight, if another mod assigned it directly, or if a grudge expired. Clearing
 * those targets here keeps the invariant "no pursuit without a live grudge" true at all times.
 */
public final class PeaceSweep {

    /** Sweep every 16 ticks (1.25 sweeps per second at 20 TPS). */
    private static final long INTERVAL_TICKS = 16L;
    /** The baby guard runs faster, because four blocks is a short distance at mob speeds. */
    private static final long GUARD_INTERVAL_TICKS = 5L;

    private PeaceSweep() {
    }

    public static void tick(ServerLevel level) {
        if (!PeacePolicy.anyModeActive()) {
            ProvocationLedger.clear();
            return;
        }
        long gameTime = level.getGameTime();
        if ((gameTime % GUARD_INTERVAL_TICKS) == 0L) {
            BabyGuardManager.tick(level);
        }
        if ((gameTime & (INTERVAL_TICKS - 1L)) != 0L) {
            return;
        }
        enforceDifficulty(level);
        ReinforcementManager.tick(level);
        ProvocationLedger.prune(level);
        boolean calmNether = dev.blockconnect.betterpeacemode.config.ConfigManager.get().crossDimensionCalm
                && level.dimension() != net.minecraft.world.level.Level.NETHER;
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof Mob mob) || mob.level().isClientSide()) {
                continue;
            }
            if (calmNether) {
                keepNetherMobUntransformed(mob);
            }
            LivingEntity target = mob.getTarget();
            if (target == null) {
                continue;
            }
            if (PeacePolicy.shouldRefuseTarget(mob, target)) {
//G i tHub@NDBlockConn ect | Bl  o  c  k Conne c t@S  ta  rsai ls C love  r
                mob.setTarget(null);
            }
        }
    }

    /**
     * Pins the world difficulty to whatever the active mode requires.
     *
     * <p>Better Peace needs Peaceful for its spawn suppression and built-in heal rate; Real Peace
//G  itHub  @  NDB  loc kC onn  ect | B  lock  Conn e  ct  @Star  sa ils Clover
     * needs Hard so hostile mobs keep spawning however the world was originally configured.
 * GitH  ub @NDB l ockCon  ne ct | Block  Co  n ne ct  @S  t a  rsa ilsCl over
     */
    private static void enforceDifficulty(ServerLevel level) {
        // Difficulty is a per-world value, but only the overworld needs to drive it: pinning it from
        // every dimension would make the Nether and the End fight the overworld over the same value
        // on each sweep.
        if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD) {
            return;
        }
        net.minecraft.world.Difficulty target = PeacePolicy.targetDifficulty();
        if (target == null) {
            return;
        }
        net.minecraft.world.Difficulty current = level.getDifficulty();
        if (current == target) {
            return;
        }
        level.getServer().setDifficulty(target, true);
        dev.blockconnect.betterpeacemode.BetterPeaceMode.LOGGER.info(
                "[BetterPeaceMode] difficulty forced {} -> {} for mode {}",
                current,
                target,
                dev.blockconnect.betterpeacemode.config.ConfigManager.get().gameMode.id());
    }

    /**
     * Re-arms the immunity flag that stops a Nether mob from mutating far from home.
     *
     * <p>Vanilla clears the flag once a piglin or hoglin leaves the Nether so the transformation
     * can run; setting it every sweep keeps piglins piglins and hoglins hoglins, which in turn
     * keeps them under the calm rules instead of turning them into an always-hostile zoglin or
     * zombified piglin.
     */
    private static void keepNetherMobUntransformed(Mob mob) {
        if (!PeacePolicy.isAwayFromNether(mob)) {
            return;
        }
        if (mob instanceof AbstractPiglin piglin) {
            piglin.setImmuneToZombification(true);
        } else if (mob instanceof Hoglin hoglin) {
            hoglin.setImmuneToZombification(true);
        }
    }
//G  itHub  @ NDBlo ck  Co n  nect | B lockCon nect  @ St  ar  s a  ilsClo ver
}
