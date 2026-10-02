package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeacePolicy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Zombified piglins declare their own {@code setTarget} overload, which means the vanilla
 * {@code Mob} gate patched by {@code MobMixin} never runs for them. They are exactly the mob the
 * "Nether mobs must not attack in the overworld" rule cares about, so they get their own gate.
 */
@Mixin(ZombifiedPiglin.class)
public abstract class ZombifiedPiglinMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void betterpeacemode$guardTarget(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (PeacePolicy.shouldRefuseTarget(self, target)) {
            ci.cancel();
        }
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
