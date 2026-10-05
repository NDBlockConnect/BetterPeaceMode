package dev.blockconnect.betterpeacemode.mixin;

import dev.blockconnect.betterpeacemode.core.PeacePolicy;
import dev.blockconnect.betterpeacemode.core.ProvocationLedger;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps a provoked mob in the fight instead of letting it run away from it.
 *
 * <p>Real Peace promises that anything which is hit fights back - a cow that a player punches turns
 * around and charges. Vanilla disagrees: {@code PanicGoal} sees any damage in the panic-causing tag
 * and makes the animal sprint away, so the counter-attack landed one hit and then spent its time
 * running. Every panic variant in 1.21.11 - the tamable, mount, fox, panda, rabbit and turtle
 * subclasses included - reaches {@code shouldPanic}, so cancelling it here covers the whole roster:
 *
 * <ul>
 *   <li>{@code MountPanicGoal} and {@code FoxPanicGoal} call {@code super.shouldPanic()};</li>
 *   <li>{@code TurtlePanicGoal} overrides {@code canUse} but still calls {@code shouldPanic()};</li>
 *   <li>the rest only override {@code tick}, which is never reached once the goal cannot start.</li>
 * </ul>
 *
 * <p>The suppression only applies while the mob holds a live one-to-one grudge against a living
 * target, i.e. exactly while {@code RetaliationManager} is driving it to attack. Unprovoked panic
 * (fire, falls, unrelated damage) and Better Peace are untouched.
 */
@Mixin(PanicGoal.class)
public abstract class PanicGoalMixin {

    @Shadow
    @Final
    private PathfinderMob mob;

    @Inject(method = "shouldPanic", at = @At("HEAD"), cancellable = true)
    private void betterpeacemode$standAndFight(CallbackInfoReturnable<Boolean> cir) {
        if (!PeacePolicy.realPeaceActive() || !PeacePolicy.universalRetaliation()) {
            return;
        }
        LivingEntity enemy = this.mob.getTarget();
        if (enemy == null || !enemy.isAlive()) {
            return;
        }
        if (ProvocationLedger.hasLiveGrudge(this.mob, enemy)) {
            cir.setReturnValue(false);
        }
    }
}
//Git  Hub  @NDB lock Conne c t | Bl  ockCon ne  ct@  St a rs ailsClov er
