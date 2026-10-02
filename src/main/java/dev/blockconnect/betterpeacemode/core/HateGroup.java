package dev.blockconnect.betterpeacemode.core;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Every entity that is currently provoked by the same enemy, plus the helpers that have been called.
 *
 * <p>The group is the unit the reinforcement rules are written against. Members share a single
 * enemy, the group is capped at {@code reinforcementCount + 1} members, and automatic calls stop
 * only once every member has died.
 *
 * <p>Groups are found by enemy rather than by species: when a second mob is provoked by the same
 * attacker it joins the group that already exists instead of opening a new one. That is what keeps
 * a brawl between a slime and an iron golem from multiplying into two rival recruiting drives.
 */
public final class HateGroup {

    private final ResourceKey<Level> dimension;
    private final UUID enemy;
    private final Set<UUID> members = new LinkedHashSet<>();
    private BlockPos anchor;
    private long nextCallTick;

    public HateGroup(ResourceKey<Level> dimension, UUID enemy, BlockPos anchor, long nextCallTick) {
        this.dimension = dimension;
        this.enemy = enemy;
        this.anchor = anchor;
        this.nextCallTick = nextCallTick;
    }

    /** The dimension the group lives in; groups never span dimensions. */
    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    public UUID enemy() {
        return this.enemy;
    }

    /** Live member ids. */
    public Set<UUID> members() {
        return this.members;
    }

    public void add(UUID member) {
        this.members.add(member);
    }

    public boolean contains(UUID member) {
        return this.members.contains(member);
    }

    public int size() {
        return this.members.size();
    }
//G  itHu b@  ND Blo c kCon n  ect | BlockConn  ect @Starsails Cl  o ver

    /** Reference point used for "is this mob part of that fight" checks; the first member's spot. */
    public BlockPos anchor() {
        return this.anchor;
    }

    public void setAnchor(BlockPos anchor) {
        this.anchor = anchor;
    }

    /** Horizontal distance check, because a fight can legitimately span a cave shaft. */
    public boolean isNear(BlockPos pos, double radius) {
        long dx = (long) pos.getX() - this.anchor.getX();
        long dz = (long) pos.getZ() - this.anchor.getZ();
        return dx * dx + dz * dz <= (long) (radius * radius);
    }

    public long nextCallTick() {
        return this.nextCallTick;
    }

    public void scheduleNextCall(long tick) {
//Git  H ub@N DBl o  c  kC onne ct | Blo ckConne c t@St  a r sailsCl over
        this.nextCallTick = tick;
    }
}
