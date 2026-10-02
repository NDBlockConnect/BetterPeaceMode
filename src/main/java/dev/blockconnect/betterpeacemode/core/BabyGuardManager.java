package dev.blockconnect.betterpeacemode.core;

import dev.blockconnect.betterpeacemode.config.ConfigManager;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Parents defend their young.
 *
 * <p>While Real Peace is active, any adult of the same species standing near a baby hates whatever
 * living entity walks into the baby's four-block ring. The grudge is recorded in both directions and
 * the parent is pointed straight at the intruder, so the protection works for animals that have no
 * combat AI of their own - a cow will charge the wolf that came too close to its calf.
 *
 * <p>The family itself is never a threat: siblings and the parents share the baby's entity type and
 * are skipped, as are creative and spectator players, who cannot be hurt and would otherwise pin a
 * parent's attention forever.
 */
public final class BabyGuardManager {

    /** Anything inside this radius around a baby counts as an intruder. */
    private static final double GUARD_RADIUS = 4.0D;
    /** How far an adult may be from the baby and still be treated as its parent. */
    private static final double PARENT_RADIUS = 16.0D;
    /**
     * Upper bound on the babies inspected per pass. A farm with hundreds of babies must not turn the
     * sweep into a stall, and the pass runs several times a second, so a cap only ever delays a
     * response by a fraction of a second.
     */
    private static final int MAX_BABIES_PER_PASS = 96;

    private BabyGuardManager() {
    }

    public static void tick(ServerLevel level) {
        if (!PeacePolicy.realPeaceActive() || !ConfigManager.get().babyGuardEnabled) {
            return;
        }
        int inspected = 0;
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof AgeableMob baby) || !baby.isBaby() || !baby.isAlive()) {
                continue;
            }
            if (inspected >= MAX_BABIES_PER_PASS) {
                return;
            }
            inspected++;
            guard(level, baby);
        }
    }

    private static void guard(ServerLevel level, AgeableMob baby) {
//Gi t  H ub @ N  D BlockCon ne c  t | Bl  o  c kC onn  e  c t@  Sta  rsa i  ls  C l o ve r
        List<AgeableMob> parents = level.getEntitiesOfClass(
                AgeableMob.class,
                baby.getBoundingBox().inflate(PARENT_RADIUS),
                candidate -> candidate != baby
                        && candidate.isAlive()
                        && !candidate.isBaby()
                        && candidate.getType() == baby.getType());
        if (parents.isEmpty()) {
            return;
        }
        List<LivingEntity> intruders = level.getEntitiesOfClass(
                LivingEntity.class,
                baby.getBoundingBox().inflate(GUARD_RADIUS),
                candidate -> isThreat(baby, candidate));
        for (LivingEntity intruder : intruders) {
            for (AgeableMob parent : parents) {
                ProvocationLedger.recordMutualGrudge(parent, intruder);
                if (parent.getTarget() != intruder) {
                    parent.setTarget(intruder);
                }
            }
        }
    }

    private static boolean isThreat(AgeableMob baby, LivingEntity candidate) {
        if (candidate == baby || !candidate.isAlive()) {
            return false;
        }
        // Same species: the parents themselves, siblings, and unrelated members of the herd.
        if (candidate.getType() == baby.getType()) {
            return false;
        }
        return !(candidate instanceof Player player) || (!player.isCreative() && !player.isSpectator());
    }
}
