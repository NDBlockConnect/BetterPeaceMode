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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
 *   <li>Cancel hits that would start a fight (friendly-versus-friendly in Better Peace, anything
 *       unprovoked in Real Peace).</li>
 *   <li>For a hit that does land, record the grudge in both directions, then hand the victim the
 *       means to fight back and let it call help if the operator enabled that.</li>
 * </ul>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void betterpeacemode$gateDamage(
            ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (level.isClientSide()) {
            return;
        }
        // Environmental damage, self-damage and non-living sources keep vanilla behaviour.
        LivingEntity attacker = source.getEntity() instanceof LivingEntity living ? living : null;
        if (attacker == null || attacker == self) {
            return;
        }
        if (!PeacePolicy.anyModeActive()) {
            return;
        }
        if (PeacePolicy.shouldRefuseDamage(attacker, self)) {
            // Zeroing the amount is not enough. Vanilla's hurt path has no "amount <= 0" exit, so a
            // zeroed hit would still consume the invulnerability window, run the knockback, and -
            // the part that used to break Real Peace - call resolveMobResponsibleForDamage, which
//Git  Hub@N  DB lockCon  nect | Bl o c k  C onn ect@Star sa ils Clover
            // records the victim's anger toward the slime that merely touched it. That anger was
            // then read back as a live grudge, so the next touch landed real damage and both sides
            // started recruiting. Cancelling the hit leaves no trace at all.
            cir.setReturnValue(false);
            return;
        }
        ProvocationLedger.recordMutualGrudge(self, attacker);
        if (PeacePolicy.realPeaceActive() && self instanceof Mob selfMob) {
            RetaliationManager.onProvoked(selfMob, attacker);
            ReinforcementManager.onProvoked(selfMob, attacker);
        }
    }
}
