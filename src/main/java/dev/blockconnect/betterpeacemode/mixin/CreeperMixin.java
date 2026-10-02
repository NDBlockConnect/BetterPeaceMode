package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeacePolicy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Creeper declares its own {@code setTarget} overload, so it does not inherit the guarded vanilla
 * implementation and needs the same target gate applied separately.
 */
@Mixin(Creeper.class)
public abstract class CreeperMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void betterpeacemode$guardTarget(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (PeacePolicy.shouldRefuseTarget(self, target)) {
            ci.cancel();
        }
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
