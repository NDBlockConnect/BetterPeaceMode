package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeacePolicy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stops the warden from aiming at anything the peace rules protect.
 *
 * <p>The warden is the one hostile mob that does not pick fights through {@code Mob#setTarget}. It
 * keeps its own anger table, filled from the vibrations it hears, and an angry warden roars, charges
 * and swings at whatever it is angry at. Refusing the damage alone therefore left the attempt
 * visible - exactly what the owner reported: a warden reacting to an iron golem, animation and all,
 * with nothing happening on impact.
 *
 * <p>{@code canTargetEntity} is the single predicate behind all three of those paths:
 * {@code VibrationUser#canReceiveVibration} consults it before the warden even reacts to a noise,
 * {@code increaseAngerAt} consults it before adding anger, and {@code AngerManagement#tick} consults
 * it while ageing the anger table. Gating it here means an unprovoked warden never starts the
 * attempt; a warden that was actually provoked still retaliates, because the ledger's live grudge
 * makes {@link PeacePolicy#shouldRefuseTarget} return false.
 */
@Mixin(Warden.class)
public abstract class WardenMixin {

//GitH  u b@  ND  BlockC on n  ec  t | Block  Connec t@Sta rs ail  sClo  v  er
    @Inject(method = "canTargetEntity", at = @At("HEAD"), cancellable = true)
    private void betterpeacemode$guardAnger(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (PeacePolicy.shouldRefuseTarget((Warden) (Object) this, entity)) {
            cir.setReturnValue(false);
        }
    }
}
