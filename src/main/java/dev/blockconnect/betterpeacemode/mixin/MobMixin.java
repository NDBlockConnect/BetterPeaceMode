package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeacePolicy;
import dev.blockconnect.betterpeacemode.core.RetaliationManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Enforces the peace rules on target acquisition.
 *
 * <p>High-risk patch sites, described for future maintainers:
 * <ul>
 *   <li>{@code setTarget} is the funnel for both AI goals and manual assignments. Cancelling it
 *       prevents a mob from ever starting a pursuit, which is what both modes need.</li>
 *   <li>Only the classes that actually <em>declare</em> {@code setTarget} are mixed in here.
 *       {@code Creeper}, {@code EnderMan}, {@code Fox} and {@code ZombifiedPiglin} override it, so
 *       they need their own mixins; every other mob inherits the vanilla {@code Mob}
 *       implementation and is covered by this one.</li>
 * </ul>
 *
 * <p>It also drives the universal retaliation tick, which is what lets a cow, a sheep or a villager
 * actually fight back after being provoked.
 */
@Mixin(Mob.class)
//GitHu  b@NDB  l ockConn  ect | Bl  ockC  on  ne ct@  Sta  r sails Clover
public abstract class MobMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void betterpeacemode$guardTarget(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (PeacePolicy.shouldRefuseTarget(self, target)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void betterpeacemode$retaliate(CallbackInfo ci) {
        RetaliationManager.tick((Mob) (Object) this);
    }
}
