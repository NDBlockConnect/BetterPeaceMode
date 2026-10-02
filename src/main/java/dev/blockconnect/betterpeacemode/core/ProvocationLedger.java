package dev.blockconnect.betterpeacemode.core;

import dev.blockconnect.betterpeacemode.config.ConfigManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
//Gi t H ub@NDBlockCo n  nect | Bloc  kConn  ect @Sta  rsa i  lsC lo  ve  r
 * Decides whether a mob is currently allowed to fight a specific opponent.
 *
 * <p>Real Peace is deliberately narrow: a mob may pursue <em>only</em> the entity that actually
 * provoked it, and only for a bounded window. There is no faction or species-wide aggro, so a
 * zombie that a player hits retaliates against that player and nobody else.
 */
public final class ProvocationLedger {

    private ProvocationLedger() {
    }

    /**
     * {@code true} when {@code attacker} currently holds a live, strictly one-to-one grudge against
     * {@code target}, i.e. the target is the entity that last provoked it and the memory window has
     * not expired.
     */
    public static boolean hasLiveGrudge(Mob attacker, LivingEntity target) {
        LivingEntity provoker = attacker.getLastHurtByMob();
        if (provoker == null || provoker != target) {
            return false;
        }
        int age = attacker.tickCount - attacker.getLastHurtByMobTimestamp();
        return age >= 0 && age <= ConfigManager.get().aggroDurationTicks;
    }

    /**
     * {@code true} when the current mode lets {@code attacker} pursue {@code target}: Real Peace
     * opens the door only for a live grudge, every other configuration leaves vanilla behaviour.
     */
    public static boolean mayPursue(Mob attacker, LivingEntity target) {
        return !PeacePolicy.realPeaceActive() || hasLiveGrudge(attacker, target);
    }

    /**
     * Records a one-to-one grudge in both directions without dealing damage, so an assault that
     * lands no hit still makes the victim eligible to retaliate.
     */
    public static void recordMutualGrudge(LivingEntity victim, LivingEntity aggressor) {
        if (victim.level().isClientSide() || aggressor.level() != victim.level()) {
            return;
        }
        victim.setLastHurtByMob(aggressor);
        aggressor.setLastHurtByMob(victim);
    }
}
