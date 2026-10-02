package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeacePolicy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//G i t Hu b  @ND  B  lo ck Co  nnect | Blo  ck Conne  ct@Stars  ai lsC  lov  er
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Enforces the peace rules on target acquisition.
 *
 * <p>High-risk patch sites, described for future maintainers:
 * <ul>
 *   <li>{@code setTarget} is the funnel for both AI goals and manual assignments. Cancelling it
 *       prevents a mob from ever starting a pursuit, which is what both modes need.</li>
 *   <li>Only the classes that actually <em>declare</em> {@code setTarget} are mixed in here.
 *       {@link net.minecraft.world.entity.monster.Creeper} and
 *       {@link net.minecraft.world.entity.monster.EnderMan} override it, so they need their own
 *       mixins (see {@code CreeperMixin} / {@code EnderManMixin}); every other mob inherits the
 *       vanilla {@code Mob} implementation and is covered by this one.</li>
 * </ul>
 *
 * <p>Compatibility boundaries: both hooks delegate to {@link PeacePolicy} and become no-ops when
 * the configuration selects {@code VANILLA}, so an unused install behaves exactly like vanilla.
 */
@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void betterpeacemode$guardTarget(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (PeacePolicy.shouldRefuseTarget(self, target)) {
            ci.cancel();
        }
    }
}
