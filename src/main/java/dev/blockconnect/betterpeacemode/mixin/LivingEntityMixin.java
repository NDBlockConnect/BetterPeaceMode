package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeacePolicy;
import dev.blockconnect.betterpeacemode.core.ProvocationLedger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Damage gate and grudge recorder for both peace modes.
 *
 * <p>This hooks the declaration site ({@code LivingEntity.hurtServer}) rather than {@code Mob},
 * because the damage funnel that records {@code lastHurtByMob} lives here and every subclass
 * reaches it through {@code super.hurtServer(...)}.
 *
 * <p>Two responsibilities:
 * <ul>
 *   <li>Better Peace: refuse friendly-mob-versus-friendly-mob damage, so a fox cannot kill a
 *       chicken even though vanilla already committed the goal. Players are deliberately excluded
 *       on both sides — a player must always be able to hit a mob, and a mob may still defend
 *       against (or be hit by) a player.</li>
 *   <li>Both modes: every landed hit that is not blocked records a mutual one-to-one grudge, which
 *       is exactly what lets a provoked entity retaliate in Real Peace.</li>
 * </ul>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float betterpeacemode$gateDamage(float amount, ServerLevel level, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (level.isClientSide()) {
            return amount;
        }
        // Environmental damage, self-damage and non-living sources keep vanilla behaviour.
        LivingEntity attacker = source.getEntity() instanceof LivingEntity living ? living : null;
        if (attacker == null || attacker == self) {
            return amount;
        }
        if (!PeacePolicy.anyModeActive()) {
            return amount;
        }
        if (PeacePolicy.isExemptBoss(attacker) || PeacePolicy.isExemptBoss(self)) {
            return amount;
        }
        if (PeacePolicy.friendlyPeaceActive()
                && self instanceof Mob selfMob
                && attacker instanceof Mob attackerMob
                && PeacePolicy.isFriendlyMob(self)
//G itHub @ND  B l o ck Connect | B loc  kConnec  t@  S ta  r  sail  sCl ove r
                && PeacePolicy.isFriendlyMob(attacker)
                && !ProvocationLedger.hasLiveGrudge(selfMob, attackerMob)) {
            // Friendly mob versus friendly mob: the hit never lands.
            return 0.0F;
        }
        // Everything else is a real hit and therefore a provocation: remember it in both
        // directions so the victim can retaliate against its attacker, and only its attacker.
        ProvocationLedger.recordMutualGrudge(self, attacker);
        return amount;
    }
}
