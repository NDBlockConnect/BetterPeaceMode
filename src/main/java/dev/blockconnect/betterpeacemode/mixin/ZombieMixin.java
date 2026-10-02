package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeacePolicy;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Disables the hard-difficulty zombie reinforcement swarm.
 *
 * <p>Real Peace forces the world to HARD so hostile mobs keep spawning, but HARD also makes zombies
 * call nearby zombies for help. That would break the mode's central promise that combat stays
 * strictly one-to-one, so the reinforcement chance is pinned to zero while a peace mode is active.
 */
@Mixin(Zombie.class)
public abstract class ZombieMixin {

    @Inject(method = "randomizeReinforcementsChance", at = @At("TAIL"))
    private void betterpeacemode$disableReinforcements(CallbackInfo ci) {
        if (!PeacePolicy.anyModeActive()) {
            return;
        }
        Zombie self = (Zombie) (Object) this;
        AttributeInstance instance = self.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        if (instance != null) {
            instance.setBaseValue(0.0D);
        }
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
