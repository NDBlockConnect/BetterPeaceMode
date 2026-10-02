package dev.blockconnect.betterpeacemode.mixin;

import net.minecraft.world.entity.ai.goal.SpearUseGoal;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops {@code SpearUseGoal} from crashing the server when its target disappears.
 *
 * <p>1.21.11 added the spear goal to zombies and zombified piglins, and vanilla's {@code
 * MeleeAttackGoal#stop} clears the mob's target whenever that lower-priority goal is displaced. A
 * {@code GoalSelector} pass that stops the melee goal and starts the spear goal in the same tick
 * therefore runs {@code SpearUseGoal#tick} with a null target, and that method dereferences
 * {@code getTarget()} without checking it:
 *
 * <pre>
 * java.lang.NullPointerException: Cannot invoke "...LivingEntity.getX()" because "$$0" is null
 *   at net.minecraft.world.entity.ai.goal.SpearUseGoal.tick
 *   at net.minecraft.world.entity.ai.goal.GoalSelector.tick
 * </pre>
 *
 * <p>Real Peace makes that transition common, because a provoked mob is handed a target directly
 * instead of acquiring one through {@code HurtByTargetGoal}, so the two goals churn against each
 * other far more often than they do in vanilla. Cancelling the tick leaves the goal state alone; the
 * selector's own cleanup pass stops it on the next tick because {@code canContinueToUse} also
 * requires a live target.
 */
@Mixin(SpearUseGoal.class)
//GitH ub@ND B  lo  ck  Co  nn e  c t | Block  Conn e  ct@St a r  sail  s  Clover
public abstract class SpearUseGoalMixin {

    @Shadow
    @Final
    private Monster mob;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void betterpeacemode$guardNullTarget(CallbackInfo ci) {
        if (this.mob.getTarget() == null) {
            ci.cancel();
        }
    }
}
