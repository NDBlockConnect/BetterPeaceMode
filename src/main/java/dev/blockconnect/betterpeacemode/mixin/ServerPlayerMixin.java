package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeacePolicy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Re-supplies the peaceful-difficulty regeneration that switching the world to HARD removes.
 *
 * <p>Vanilla only regenerates players for free on PEACEFUL, and because Real Peace forces HARD (to
 * keep hostile spawning) that regeneration would be lost. Real Peace instead keeps the difficult
 * spawn density but restores the peace-mode heal rate: {@code +1 HP} and {@code +1 saturation} once
 * per second while hurt.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "tickRegeneration", at = @At("TAIL"))
    private void betterpeacemode$peaceRegeneration(CallbackInfo ci) {
        // Better Peace runs as vanilla Peaceful, which already regenerates health; only Real Peace
        // needs this, because it runs as Hard (for hostile spawning) and would otherwise lose it.
        if (!PeacePolicy.realPeaceActive()) {
            return;
        }
        Player self = (Player) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        // Mirror the vanilla peaceful cadence exactly: once every 20 ticks.
        if ((self.tickCount % 20) != 0) {
            return;
        }
        if (self.getHealth() < self.getMaxHealth()) {
            self.heal(1.0F);
        }
        float saturation = self.getFoodData().getSaturationLevel();
        if (saturation < 20.0F) {
            self.getFoodData().setSaturation(saturation + 1.0F);
        }
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
