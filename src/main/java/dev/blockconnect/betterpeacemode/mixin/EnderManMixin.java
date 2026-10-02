package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeacePolicy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Endermen declare their own {@code setTarget} overload (they use it to react to being looked at
 * and to teleport away), so the target gate has to be applied to this class as well.
 */
@Mixin(EnderMan.class)
public abstract class EnderManMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void betterpeacemode$guardTarget(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (PeacePolicy.shouldRefuseTarget(self, target)) {
            ci.cancel();
        }
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
