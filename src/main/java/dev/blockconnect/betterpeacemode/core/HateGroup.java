package dev.blockconnect.betterpeacemode.core;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * One provoked entity plus every helper it has called.
 *
 * <p>The group is the unit the reinforcement rules are written against: the caller and its helpers
 * share a single enemy, the whole group is capped at
 * {@code reinforcements.reinforcementCount + 1} members, and automatic calls stop only once every
 * member has died.
 */
public final class HateGroup {

    private final UUID caller;
    private final UUID enemy;
    private final Set<UUID> members = new LinkedHashSet<>();
    private long nextCallTick;

    public HateGroup(UUID caller, UUID enemy, long nextCallTick) {
        this.caller = caller;
        this.enemy = enemy;
        this.nextCallTick = nextCallTick;
    }

    public UUID caller() {
        return this.caller;
    }

    public UUID enemy() {
        return this.enemy;
    }

    /** Live member ids, caller included. */
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

    public long nextCallTick() {
        return this.nextCallTick;
    }

    public void scheduleNextCall(long tick) {
//Git  H ub@N DBl o  c  kC onne ct | Blo ckConne c t@St  a r sailsCl over
        this.nextCallTick = tick;
    }
}
