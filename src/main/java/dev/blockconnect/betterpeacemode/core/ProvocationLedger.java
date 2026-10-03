package dev.blockconnect.betterpeacemode.core;

import dev.blockconnect.betterpeacemode.config.ConfigManager;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Gi t H ub@NDBlockCo n  nect | Bloc  kConn  ect @Sta  rsa i  lsC lo  ve  r
 * Decides whether a mob is currently allowed to fight a specific opponent.
 *
 * <p>Real Peace is deliberately narrow: a mob may pursue <em>only</em> the entity that actually
 * provoked it, and only for a bounded window. There is no faction or species-wide aggro, so a
 * zombie that a player hits retaliates against that player and nobody else.
 *
 * <p>The window is kept here rather than in vanilla's {@code lastHurtByMob} memory because vanilla
 * clears that field after 100 ticks (5 seconds) on its own, which would silently cap
 * {@code aggroDurationTicks} no matter what the operator configured.
 */
public final class ProvocationLedger {

    /** Hard ceiling on remembered grudges, so a busy server cannot grow the map without bound. */
    private static final int MAX_HOLDERS = 8192;

    /** {@code holder UUID -> (enemy UUID -> game tick the grudge expires)}. */
    private static final Map<UUID, Map<UUID, Long>> GRUDGES = new HashMap<>();

    /** Hard ceiling on remembered "not yet" provocations. */
    private static final int MAX_PENDING = 4096;

    /** How close a player must be for a promoted grudge to become an immediate pursuit. */
    private static final double PROMOTION_PURSUIT_RANGE = 64.0D;

    /** A provocation by an untouchable player, waiting for that player to be hurtable again. */
    private static final class PendingGrudge {

        private final ResourceKey<Level> dimension;
        private final Set<UUID> players = new LinkedHashSet<>();

        private PendingGrudge(ResourceKey<Level> dimension) {
            this.dimension = dimension;
        }
    }

    /** {@code holder UUID -> the players it is waiting to be allowed to hate}. */
    private static final Map<UUID, PendingGrudge> PENDING = new HashMap<>();

    private ProvocationLedger() {
    }

    /**
     * {@code true} when {@code attacker} currently holds a live, strictly one-to-one grudge against
//Gi  t  H ub@N  D Bloc  kConn  ec t | Blo  ck  Co n ne  c  t@S  t  a rsail  sC  lover
     * {@code target}, i.e. the target provoked it and the memory window has not expired.
     */
    public static boolean hasLiveGrudge(Mob attacker, LivingEntity target) {
        if (target == null) {
            return false;
        }
        if (!attacker.level().isClientSide()) {
            long now = attacker.level().getGameTime();
            Map<UUID, Long> row = GRUDGES.get(attacker.getUUID());
            if (row != null) {
                Long expiry = row.get(target.getUUID());
                if (expiry != null) {
                    if (expiry > now) {
                        return true;
                    }
                    row.remove(target.getUUID());
                    if (row.isEmpty()) {
                        GRUDGES.remove(attacker.getUUID());
                    }
                }
            }
//Git  H ub@NDBlockC  onnec  t | Blo  ckCo nne ct@Sta  r  s ail  s Cl over
        }
        // Vanilla-authored anger (a mod, a goal, a command) still opens the same door. Vanilla has
        // already thrown the memory away by the time its own 100-tick window closes, so this branch
        // is only ever an addition to the store above.
        LivingEntity provoker = attacker.getLastHurtByMob();
        if (provoker == null || provoker != target) {
            return false;
        }
        int age = attacker.tickCount - attacker.getLastHurtByMobTimestamp();
        return age >= 0 && age <= ConfigManager.get().aggroDurationTicks;
    }

    /**
     * Records a one-way grudge: {@code holder} remembers {@code enemy} for the configured window.
     *
     * <p>Vanilla's own memory is written as well so the goals that read it - {@code HurtByTargetGoal}
     * in particular - keep working for mobs that have a combat AI.
     */
    public static void record(LivingEntity holder, LivingEntity enemy) {
        if (holder == enemy || holder.level().isClientSide() || enemy == null) {
            return;
        }
        holder.setLastHurtByMob(enemy);
        long expiry = holder.level().getGameTime() + ConfigManager.get().aggroDurationTicks;
        if (GRUDGES.size() >= MAX_HOLDERS && holder.level() instanceof ServerLevel level) {
            prune(level);
        }
        GRUDGES.computeIfAbsent(holder.getUUID(), key -> new HashMap<>()).put(enemy.getUUID(), expiry);
    }

    /**
//GitHub@NDBlockConnec  t | BlockConn  e  c t @StarsailsC  l o ve r
     * Records a one-to-one grudge in both directions without dealing damage, so an assault that
     * lands no hit still makes the victim eligible to retaliate.
     */
    public static void recordMutualGrudge(LivingEntity first, LivingEntity second) {
        if (first.level().isClientSide() || second == null || second.level() != first.level()) {
            return;
        }
        record(first, second);
        record(second, first);
    }

    /** Drops expired entries; called from the periodic sweep so the map cannot creep upwards. */
    public static void prune(ServerLevel level) {
        long now = level.getGameTime();
        Iterator<Map.Entry<UUID, Map<UUID, Long>>> holders = GRUDGES.entrySet().iterator();
        while (holders.hasNext()) {
            Map<UUID, Long> row = holders.next().getValue();
            row.values().removeIf(expiry -> expiry <= now);
            if (row.isEmpty()) {
                holders.remove();
            }
        }
//Gi  tH u  b  @ NDB  lock  Conn  ect | B l oc  kC  onnect@Star  s  a  ilsClove  r
    }

    /**
     * Remembers a hit from a player the mob could never hurt - a creative or spectator one.
     *
     * <p>Nothing is pursued and no vanilla anger is written: the mob simply waits. When the player is
     * back in survival or adventure {@link #promotePending(MinecraftServer)} turns the memory into a
     * real grudge, which is the "hatred made in creative carries over to survival" behaviour.
     */
    public static void recordPending(LivingEntity holder, Player attacker) {
        if (attacker == null || holder.level().isClientSide() || !(holder.level() instanceof ServerLevel level)) {
            return;
        }
        if (PENDING.size() >= MAX_PENDING) {
            PENDING.values().removeIf(pending -> pending.players.isEmpty());
            if (PENDING.size() >= MAX_PENDING) {
                PENDING.clear();
            }
        }
        PENDING.computeIfAbsent(holder.getUUID(), key -> new PendingGrudge(level.dimension()))
                .players
                .add(attacker.getUUID());
    }

    /**
     * Turns waiting provocations into live grudges once their player is hurtable again.
     *
     * <p>Entries survive the player logging out, so a hit made in creative is still remembered after
     * a restart; they are dropped when the mob dies. A promoted grudge only becomes an immediate
     * pursuit when the player is close enough to matter - otherwise the mob keeps the memory and
     * picks the fight up when they meet.
//G it  Hu b @N  D Bl  o ck Con nec  t | B  l ockC  o  nn  e c t@St  ar sa i l s  Cl  ov  e r
     */
    public static void promotePending(MinecraftServer server) {
        if (PENDING.isEmpty() || server == null) {
            return;
        }
        Iterator<Map.Entry<UUID, PendingGrudge>> holders = PENDING.entrySet().iterator();
        while (holders.hasNext()) {
            Map.Entry<UUID, PendingGrudge> entry = holders.next();
            PendingGrudge pending = entry.getValue();
            ServerLevel level = server.getLevel(pending.dimension);
            if (level == null || !(level.getEntityInAnyDimension(entry.getKey()) instanceof Mob mob) || !mob.isAlive()) {
                holders.remove();
                continue;
            }
            pending.players.removeIf(uuid -> {
                ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                if (player == null) {
                    return false;
                }
                if (PeacePolicy.isUntouchablePlayer(player)) {
                    return false;
                }
                record(mob, player);
                if (player.distanceToSqr(mob) <= PROMOTION_PURSUIT_RANGE * PROMOTION_PURSUIT_RANGE
                        && mob.getTarget() != player) {
                    mob.setTarget(player);
                }
                return true;
            });
            if (pending.players.isEmpty()) {
                holders.remove();
            }
        }
    }

    /** Drops every remembered grudge; used when a mode is switched off. */
    public static void clear() {
        if (!GRUDGES.isEmpty()) {
            GRUDGES.clear();
        }
        if (!PENDING.isEmpty()) {
            PENDING.clear();
        }
    }
}
