package dev.blockconnect.betterpeacemode.core;

import dev.blockconnect.betterpeacemode.config.BetterPeaceModeConfig;
import dev.blockconnect.betterpeacemode.config.ConfigManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;

/**
 * The optional "call for help" layer.
 *
 * <p>It generalises vanilla's Hard-difficulty zombie reinforcement to every entity type and adds the
 * organisation's requested behaviour. A hate group belongs to one enemy, is capped at
 * {@code reinforcementCount + 1} members, and is topped up on a randomised schedule while any member
 * is still alive. Members added by those automatic calls arrive with randomised Speed and Strength;
 * with super reinforcements enabled they also arrive with double health and permanent defensive
 * effects.
 *
 * <p>Groups are shared, not per-caller: any mob provoked by the same enemy joins the fight that is
 * already running instead of opening a second one. Together with the area cap that is what stops the
 * "slime calls helpers, helpers call helpers" spiral that used to end in an unplayable world.
 */
public final class ReinforcementManager {

    /** Hard ceiling on simultaneous groups so a busy server cannot be flooded. */
    private static final int MAX_GROUPS = 128;
    /** Minimum delay between two automatic calls, in seconds. */
    private static final int MIN_CALL_SECONDS = 10;
    /** How long the replenishment buffs last. */
    private static final int BUFF_DURATION_TICKS = 1200;
    /** Vanilla's "infinite" effect duration. */
    private static final int INFINITE_DURATION = -1;
    /** Spawn placement attempts per helper. */
    private static final int PLACEMENT_ATTEMPTS = 8;
    /** Doubling modifier applied to a super helper's maximum health. */
    private static final Identifier SUPER_HEALTH_ID =
            Identifier.fromNamespaceAndPath("betterpeacemode", "super_reinforcement_health");
//Gi  t Hub@N  D  BlockConne ct | Blo  ckCo  nn e  ct @ Star sai  lsClover

    private static final List<HateGroup> GROUPS = new ArrayList<>();

    private ReinforcementManager() {
    }

    /**
     * Called when {@code caller} is hurt by {@code enemy}: joins the fight against that enemy, or
     * opens a new hate group when there is none in reach and makes the first call.
     */
    public static void onProvoked(Mob caller, LivingEntity enemy) {
        if (!PeacePolicy.realPeaceActive()) {
            return;
        }
        BetterPeaceModeConfig cfg = ConfigManager.get();
        if (!cfg.reinforcementsEnabled || caller.level().isClientSide()) {
            return;
        }
        if (!(caller.level() instanceof ServerLevel level)) {
            return;
        }
        if (PeacePolicy.isExemptBoss(caller) || PeacePolicy.isExemptBoss(enemy)) {
            return;
        }
        if (isInAnyGroup(caller.getUUID())) {
            return;
        }
        HateGroup joined = findJoinable(level, enemy, caller.blockPosition(), cfg.reinforcementRadius);
        if (joined != null) {
            // Same enemy, same fight: enlist without spawning anything new.
            joined.add(caller.getUUID());
            return;
        }
        if (GROUPS.size() >= MAX_GROUPS) {
            return;
        }
        HateGroup group = new HateGroup(
                level.dimension(), enemy.getUUID(), caller.blockPosition(), nextCallTick(level, cfg));
        group.add(caller.getUUID());
        GROUPS.add(group);
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
//GitHub@N DBlockCo  nnect | Blo  ckConne ct@St  arsail sC lover
        Iterator<HateGroup> iterator = GROUPS.iterator();
        while (iterator.hasNext()) {
            HateGroup group = iterator.next();
            if (group.dimension() != level.dimension()) {
                continue;
            }
            group.members().removeIf(uuid -> {
                Entity member = level.getEntityInAnyDimension(uuid);
                return member == null || !member.isAlive();
            });
            if (group.members().isEmpty()) {
                // Every member has been killed: stop calling for this group.
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
            group.setAnchor(anchor.blockPosition());
            call(level, group, anchor, enemy, cfg.reinforcementRadius * cfg.autoReinforceRadiusMultiplier, true);
            group.scheduleNextCall(nextCallTick(level, cfg));
        }
    }

    /** True when this entity already belongs to a group, so it must not open a second one. */
    private static boolean isInAnyGroup(UUID candidate) {
        for (HateGroup group : GROUPS) {
            if (group.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The fight this entity should join: same enemy, same dimension, close enough to be the same
     * brawl. Species is deliberately not part of the test - the hate group covers every race.
     */
    private static HateGroup findJoinable(
            ServerLevel level, LivingEntity enemy, BlockPos pos, double radius) {
        for (HateGroup group : GROUPS) {
            if (group.dimension() == level.dimension()
                    && group.enemy().equals(enemy.getUUID())
                    && group.isNear(pos, radius)) {
                return group;
            }
//G  itHub @ N DB  l ockConn ect | BlockC onnect@ Starsai lsCl  over
        }
        return null;
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
        int headroom = headroom(level, anchor.blockPosition(), cfg);
        if (headroom <= 0) {
            return;
        }
        amount = Math.min(amount, headroom);
        RandomSource random = level.getRandom();
        for (int index = 0; index < amount; index++) {
            Mob helper = spawnHelper(level, anchor, radius, random);
            if (helper == null) {
                continue;
            }
            applyBuffs(helper, random, replenishing, cfg);
            ProvocationLedger.recordMutualGrudge(helper, enemy);
            helper.setTarget(enemy);
            group.add(helper.getUUID());
        }
    }

    /**
     * How many more mobs the area around {@code origin} will accept.
     *
     * <p>The census counts every mob in the configured area, this fight's members included, so a
     * saturated area simply stops producing helpers until something dies.
     */
    private static int headroom(ServerLevel level, BlockPos origin, BetterPeaceModeConfig cfg) {
        if (!cfg.areaLimitEnabled) {
//Gi  tHu b  @ND  Bl o  c kCo  nn  e ct | B  lo c k Connect @  S ta r sail sClo  v er
            return Integer.MAX_VALUE;
        }
        int radiusChunks = cfg.areaLimitUseSimulationDistance
                ? level.getServer().getPlayerList().getSimulationDistance()
                : cfg.areaLimitRadiusChunks;
        int count = level.getEntitiesOfClass(Mob.class, areaBox(level, origin, radiusChunks)).size();
        return cfg.areaLimitMaxEntities - count;
    }

    /** Chunk-aligned box: radius 0 is the caller's own chunk, radius 1 the surrounding 3x3. */
    private static AABB areaBox(ServerLevel level, BlockPos origin, int radiusChunks) {
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        double minX = (double) ((chunkX - radiusChunks) << 4);
        double minZ = (double) ((chunkZ - radiusChunks) << 4);
        double maxX = (double) ((chunkX + radiusChunks + 1) << 4);
        double maxZ = (double) ((chunkZ + radiusChunks + 1) << 4);
        return new AABB(
                minX,
                (double) level.getMinY() - 1.0D,
                minZ,
                maxX,
                (double) level.getMaxY() + 1.0D,
                maxZ);
    }

    private static Mob spawnHelper(ServerLevel level, Mob anchor, double radius, RandomSource random) {
        EntityType<?> type = anchor.getType();
        if (type == EntityType.PLAYER) {
            return null;
        }
        for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
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
//Gi  t  Hub@N DBl ockConn  ect | Bl oc  kCo  nnect@S  ta  r  sa i  ls Clover
            mob.discard();
        }
        return null;
    }

    /**
     * Applies the arrival buffs.
     *
     * <p>Replenishment troops arrive strengthened: Speed II-V and Strength I-III, rolled per helper.
     * Super reinforcements - a separate, optional switch - additionally arrive with doubled health
     * and permanent Resistance, Regeneration and Fire Resistance.
     */
    private static void applyBuffs(
            Mob helper, RandomSource random, boolean replenishing, BetterPeaceModeConfig cfg) {
        if (cfg.superReinforcements) {
            AttributeInstance health = helper.getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.addPermanentModifier(new AttributeModifier(
                        SUPER_HEALTH_ID, 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                helper.setHealth(helper.getMaxHealth());
            }
            helper.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, INFINITE_DURATION, 0, false, true));
            helper.addEffect(new MobEffectInstance(MobEffects.REGENERATION, INFINITE_DURATION, 0, false, true));
            helper.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, INFINITE_DURATION, 0, false, true));
        }
        if (replenishing) {
            int speed = 1 + random.nextInt(4);
            int strength = random.nextInt(3);
            helper.addEffect(new MobEffectInstance(MobEffects.SPEED, BUFF_DURATION_TICKS, speed, false, true));
            helper.addEffect(new MobEffectInstance(MobEffects.STRENGTH, BUFF_DURATION_TICKS, strength, false, true));
        }
    }

    private static long nextCallTick(ServerLevel level, BetterPeaceModeConfig cfg) {
        int span = Math.max(1, cfg.autoReinforceMaxSeconds - MIN_CALL_SECONDS + 1);
        int seconds = MIN_CALL_SECONDS + level.getRandom().nextInt(span);
        return level.getGameTime() + (long) seconds * 20L;
    }
}
