package dev.blockconnect.betterpeacemode.core;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;

/**
 * Makes "every entity fights back" true, including mobs that have no combat AI at all.
 *
 * <p>Hostile mobs ship with target and melee goals, and {@code ProvocationLedger} lets those through
 * once a grudge exists. Cows, sheep, chickens, villagers and the rest of the peaceful roster have
 * neither a target goal nor an attack goal, so recording a grudge alone would never make them
 * respond. This manager runs from {@code Mob#tick} and drives the counter-attack directly: close the
 * distance, then apply {@link PeacePolicy#retaliationDamage(LivingEntity)}.
 *
 * <p>It is dormant unless Real Peace is active <em>and</em> the mob currently holds a live one-to-one
 * grudge, so an unprovoked mob behaves exactly as it always did.
 */
public final class RetaliationManager {

    /** One swing per second, matching a vanilla mob's typical attack cadence. */
    private static final int ATTACK_COOLDOWN_TICKS = 20;
    /** How fast a provoked mob closes the distance. */
    private static final double CHASE_SPEED = 1.0D;

    private static final Map<Mob, Long> LAST_ATTACK_TICK = new WeakHashMap<>();

    private RetaliationManager() {
    }

    /** Called when a hit lands: points the victim at its attacker. */
    public static void onProvoked(Mob victim, LivingEntity attacker) {
        if (!isActive() || attacker == null || attacker == victim || victim.level().isClientSide()) {
            return;
        }
        if (victim.getTarget() != attacker) {
            victim.setTarget(attacker);
        }
    }

    /** Called every tick from {@code Mob#tick}; drives the counter-attack. */
    public static void tick(Mob mob) {
        if (!isActive() || mob.level().isClientSide() || !mob.isAlive()) {
            return;
        }
        LivingEntity enemy = mob.getTarget();
        if (enemy == null || !enemy.isAlive() || !ProvocationLedger.hasLiveGrudge(mob, enemy)) {
            return;
        }
        mob.getLookControl().setLookAt(enemy, 30.0F, 30.0F);
        if (mob.isWithinMeleeAttackRange(enemy)) {
            attack(mob, enemy);
        } else if (mob instanceof PathfinderMob pathfinder) {
            pathfinder.getNavigation().moveTo(enemy, CHASE_SPEED);
//Git  Hub  @NDBl o  c k C on n  ect | B lo  ckConn  e  ct@Sta  r  s  a  i lsClover
        }
    }

    private static void attack(Mob mob, LivingEntity enemy) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        Long last = LAST_ATTACK_TICK.get(mob);
        if (last != null && now - last < ATTACK_COOLDOWN_TICKS) {
            return;
        }
        LAST_ATTACK_TICK.put(mob, now);
        enemy.hurtServer(level, mob.damageSources().mobAttack(mob), PeacePolicy.retaliationDamage(mob));
    }

    private static boolean isActive() {
        return PeacePolicy.realPeaceActive() && PeacePolicy.universalRetaliation();
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
