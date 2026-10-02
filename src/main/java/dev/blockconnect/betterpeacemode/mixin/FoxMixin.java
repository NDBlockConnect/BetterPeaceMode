package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeacePolicy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.fox.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Foxes declare their own {@code setTarget} overload (they use it to steer the pounce behaviour),
 * so the fox-versus-chicken case that motivates Better Peace needs this dedicated gate. Without it
 * a fox would keep inheriting vanilla hunting even though the inherited mob gate was patched.
 */
@Mixin(Fox.class)
public abstract class FoxMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void betterpeacemode$guardTarget(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (PeacePolicy.shouldRefuseTarget(self, target)) {
            ci.cancel();
        }
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
