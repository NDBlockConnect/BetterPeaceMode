package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeaceSweep;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drives the periodic peace sweep.
 *
 * <p>Hooking the server level tick instead of a per-mob tick keeps mod-to-mod {@code Mob#tick}
 * injections out of the way and bounds the sweep cost to a fixed budget per second.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void betterpeacemode$peaceSweep(CallbackInfo ci) {
        PeaceSweep.tick((ServerLevel) (Object) this);
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
