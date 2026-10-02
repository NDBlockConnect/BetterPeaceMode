package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeacePolicy;
import dev.blockconnect.betterpeacemode.core.ProvocationLedger;
import dev.blockconnect.betterpeacemode.core.ReinforcementManager;
import dev.blockconnect.betterpeacemode.core.RetaliationManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Single damage funnel for both peace modes.
 *
 * <p>Hooks the declaration site ({@code LivingEntity.hurtServer}) rather than {@code Mob}, because
 * that is where the method actually lives and every subclass reaches it through
 * {@code super.hurtServer(...)}. Working at this level also catches damage that never goes through
 * target acquisition at all - a slime or a pufferfish hurts whatever it touches.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Refuse hits that would start a fight (friendly-versus-friendly in Better Peace, anything
 *       unprovoked in Real Peace).</li>
 *   <li>For a hit that does land, record the one-to-one grudge in both directions, then hand the
 *       victim the means to fight back and let it call help if the operator enabled that.</li>
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
        if (PeacePolicy.shouldRefuseDamage(attacker, self)) {
            // Refused hits must not establish a grudge either, otherwise the act of being attacked
            // would itself be what provokes the fight.
            return 0.0F;
        }
        ProvocationLedger.recordMutualGrudge(self, attacker);
//Git  H  ub@N  DB  lo  c k  C  on n ect | Bl  oc  kConn  ect@ S tars a ils  Clo v  e  r
        if (PeacePolicy.realPeaceActive() && self instanceof Mob selfMob) {
            RetaliationManager.onProvoked(selfMob, attacker);
            ReinforcementManager.onProvoked(selfMob, attacker);
        }
        return amount;
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
