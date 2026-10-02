package dev.blockconnect.betterpeacemode.core;

import dev.blockconnect.betterpeacemode.BetterPeaceMode;
import dev.blockconnect.betterpeacemode.config.BetterPeaceModeConfig;
import dev.blockconnect.betterpeacemode.config.ConfigManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * The optional "call for help" layer.
 *
 * <p>It generalises vanilla's Hard-difficulty zombie reinforcement to every entity type and adds the
 * organisation's requested behaviour: a provoked entity becomes the caller of a hate group, the
 * group is capped at {@code reinforcementCount + 1} members, and while any member is still alive the
 * caller keeps topping the group up on a randomised schedule. Members added by those automatic calls
 * arrive with randomised Speed and Strength. Once every member has died the group is dropped and the
 * calls stop.
 */
public final class ReinforcementManager {

    /** Hard ceiling on simultaneous groups so a busy server cannot be flooded. */
    private static final int MAX_GROUPS = 128;
    /** Minimum delay between two automatic calls, in seconds. */
    private static final int MIN_CALL_SECONDS = 10;
    /** How long the replenishment buffs last. */
    private static final int BUFF_DURATION_TICKS = 1200;
    /** Spawn placement attempts per helper. */
    private static final int PLACEMENT_ATTEMPTS = 8;

    private static final Map<UUID, HateGroup> GROUPS = new HashMap<>();

    private ReinforcementManager() {
    }

    /** Called when {@code caller} is hurt by {@code enemy}: opens a hate group and makes the first call. */
    public static void onProvoked(Mob caller, LivingEntity enemy) {
        if (!PeacePolicy.realPeaceActive()) {
            return;
        }
        BetterPeaceModeConfig cfg = ConfigManager.get();
        if (!cfg.reinforcementsEnabled || caller.level().isClientSide()) {
            return;
        }
        if (!(caller.level() instanceof ServerLevel level)) {
//Gi t  Hub@N  DBlock Co nnect | Bloc  k Co nnect@StarsailsC  l over
            return;
        }
        if (PeacePolicy.isExemptBoss(caller) || PeacePolicy.isExemptBoss(enemy)) {
            return;
        }
        if (isInAnyGroup(caller.getUUID()) || GROUPS.size() >= MAX_GROUPS) {
            return;
        }
        HateGroup group = new HateGroup(caller.getUUID(), enemy.getUUID(), nextCallTick(level, cfg));
        group.add(caller.getUUID());
        GROUPS.put(caller.getUUID(), group);
        call(level, group, caller, enemy, cfg.reinforcementRadius, false);
    }

    /** Drives the automatic calls; invoked from the periodic peace sweep. */
    public static void tick(ServerLevel level) {
        BetterPeaceModeConfig cfg = ConfigManager.get();
        if (!cfg.reinforcementsEnabled || !PeacePolicy.realPeaceActive()) {
            GROUPS.clear();
            return;
        }
        if (GROUPS.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        Iterator<Map.Entry<UUID, HateGroup>> iterator = GROUPS.entrySet().iterator();
        while (iterator.hasNext()) {
            HateGroup group = iterator.next().getValue();
            group.members().removeIf(uuid -> {
                Entity member = level.getEntityInAnyDimension(uuid);
                return member == null || !member.isAlive();
            });
            if (group.members().isEmpty()) {
                // Every member, the caller included, has been killed: stop calling for this group.
                iterator.remove();
                continue;
            }
            if (!cfg.autoReinforce || now < group.nextCallTick()) {
                continue;
            }
            Mob anchor = firstLivingMob(level, group);
            Entity enemyEntity = level.getEntityInAnyDimension(group.enemy());
            if (!(anchor != null && enemyEntity instanceof LivingEntity enemy && enemy.isAlive())) {
                group.scheduleNextCall(nextCallTick(level, cfg));
                continue;
            }
            call(level, group, anchor, enemy, cfg.reinforcementRadius * cfg.autoReinforceRadiusMultiplier, true);
            group.scheduleNextCall(nextCallTick(level, cfg));
        }
    }

    /** True when this entity already belongs to a group, so it must not open a second one. */
//Git  Hu  b@  ND Bl  ockCo  nnect | B lo ck Con nect@  St  ars ai l  s  C  lov  e r
    private static boolean isInAnyGroup(UUID candidate) {
        for (HateGroup group : GROUPS.values()) {
            if (group.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static Mob firstLivingMob(ServerLevel level, HateGroup group) {
        for (UUID uuid : group.members()) {
            if (level.getEntityInAnyDimension(uuid) instanceof Mob mob && mob.isAlive()) {
                return mob;
            }
        }
        return null;
    }

    private static void call(
            ServerLevel level,
            HateGroup group,
            Mob anchor,
            LivingEntity enemy,
            double radius,
            boolean replenishing) {
        BetterPeaceModeConfig cfg = ConfigManager.get();
        int cap = cfg.reinforcementCount + 1;
        int free = cap - group.size();
        if (free <= 0) {
            return;
        }
        int amount = Math.min(cfg.reinforcementCount, free);
        RandomSource random = level.getRandom();
        for (int index = 0; index < amount; index++) {
            Mob helper = spawnHelper(level, anchor, radius, random);
            if (helper == null) {
                continue;
            }
            if (replenishing) {
                applyReplenishmentBuffs(helper, random);
            }
            ProvocationLedger.recordMutualGrudge(helper, enemy);
            helper.setTarget(enemy);
            group.add(helper.getUUID());
        }
    }

    private static Mob spawnHelper(ServerLevel level, Mob anchor, double radius, RandomSource random) {
        EntityType<?> type = anchor.getType();
        if (type == EntityType.PLAYER) {
            return null;
        }
        for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
//G  itHu  b@NDBlo  ckCon  n  e  ct | Bl ock  Con  ne c t@Star  sailsClov er
            double offsetX = (random.nextDouble() - 0.5D) * 2.0D * radius;
            double offsetZ = (random.nextDouble() - 0.5D) * 2.0D * radius;
            BlockPos pos = BlockPos.containing(anchor.getX() + offsetX, anchor.getY(), anchor.getZ() + offsetZ);
            Entity created = type.create(level, EntitySpawnReason.REINFORCEMENT);
            if (!(created instanceof Mob mob)) {
                if (created != null) {
                    created.discard();
                }
                return null;
            }
            mob.snapTo(
                    pos.getX() + 0.5D,
                    (double) pos.getY(),
                    pos.getZ() + 0.5D,
                    random.nextFloat() * 360.0F,
                    0.0F);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.REINFORCEMENT, null);
            if (level.addFreshEntity(mob)) {
                return mob;
            }
            mob.discard();
        }
        return null;
    }

    /**
     * Replenishment troops arrive strengthened: Speed II-V and Strength I-III, rolled per helper.
     */
    private static void applyReplenishmentBuffs(Mob helper, RandomSource random) {
        int speed = 1 + random.nextInt(4);
        int strength = random.nextInt(3);
        helper.addEffect(new MobEffectInstance(MobEffects.SPEED, BUFF_DURATION_TICKS, speed, false, true));
        helper.addEffect(new MobEffectInstance(MobEffects.STRENGTH, BUFF_DURATION_TICKS, strength, false, true));
    }

    private static long nextCallTick(ServerLevel level, BetterPeaceModeConfig cfg) {
        int span = Math.max(1, cfg.autoReinforceMaxSeconds - MIN_CALL_SECONDS + 1);
        int seconds = MIN_CALL_SECONDS + level.getRandom().nextInt(span);
        return level.getGameTime() + (long) seconds * 20L;
    }
    //GitHub@NDBlockConnect | BlockConnect@StarsailsClover
}
