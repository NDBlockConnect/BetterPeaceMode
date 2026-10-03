package dev.blockconnect.betterpeacemode.core;

import dev.blockconnect.betterpeacemode.config.BetterPeaceModeConfig;
import dev.blockconnect.betterpeacemode.config.ConfigManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;

/**
 * Classification and targeting rules shared by the Mixin layer and the periodic sweep.
 *
 * <p>Everything here is a pure predicate over game state, which keeps the decision logic testable
 * and keeps the bytecode patches thin.
 */
public final class PeacePolicy {

    /** Registry keys exempted from Real Peace passivity. */
    private static final String ENDER_DRAGON = "ender_dragon";
    private static final String WITHER = "wither";

    private PeacePolicy() {
    }

    public static boolean friendlyPeaceActive() {
        BetterPeaceModeConfig cfg = ConfigManager.get();
        return cfg.friendlyPeace || cfg.gameMode.friendlyPeace();
    }

    public static boolean realPeaceActive() {
        BetterPeaceModeConfig cfg = ConfigManager.get();
        return cfg.realPeace || cfg.gameMode.realPeace();
    }

    public static boolean anyModeActive() {
        return friendlyPeaceActive() || realPeaceActive();
    }

    /**
     * The vanilla difficulty the current mode runs on, or {@code null} when the mod is inert.
     *
     * <p>Better Peace runs as Peaceful, which is what makes hostile mobs stop spawning and what
     * supplies the peace-mode heal rate. Real Peace runs as Hard instead, so hostile mobs keep
     * spawning at the difficult spawn density; the heal rate and the "nobody starts a fight"
     * behaviour are supplied by this mod rather than by the difficulty flag.
     */
    public static Difficulty targetDifficulty() {
        if (realPeaceActive()) {
            return Difficulty.HARD;
        }
        if (friendlyPeaceActive()) {
//G  i  tHu  b@ N D Blo  c  kCo nnect | Blo c  k Connect @St  a rs  a  ils  Cl  over
//G  it  Hub@NDB l o c  kC  onnec  t | Bloc  k Co  n nec  t@  S t  ar sa ilsClo  v  er
//Git  H  ub @ NDB loc  k  C  onnect | Blo ck Co  nnect@Sta rs  a i l sCl  over
            return Difficulty.PEACEFUL;
        }
        return null;
    }

    /** The ender dragon and the wither stay vanilla-hostile when configured to. */
    public static boolean isExemptBoss(Entity entity) {
        if (!ConfigManager.get().keepBossHostile) {
            return false;
        }
        // 1.21.11 renamed the resource identifier type from ResourceLocation to Identifier.
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) {
            return false;
        }
        String path = id.getPath();
        return ENDER_DRAGON.equals(path) || WITHER.equals(path);
    }

    /** A mob that is hostile by nature and not exempted (so it is subject to Real Peace passivity). */
    public static boolean isPassiveHostile(Entity entity) {
        return entity instanceof Mob && entity instanceof Enemy && !isExemptBoss(entity);
    }

    /**
     * A player a mob can never actually hurt.
     *
     * <p>Creative and spectator players are immune to mob damage, so hunting one is pure noise: the
     * player is chased forever and nothing happens. The peace modes refuse those targets outright and
     * let {@code ProvocationLedger} remember the hit instead.
     */
    public static boolean isUntouchablePlayer(Entity entity) {
        return entity instanceof Player player && (player.isCreative() || player.isSpectator());
    }

    /**
     * Kinds that must not be attacked without provocation.
     *
     * <p>Players, animals, villagers, golems and other friendly mobs qualify. Hostile mobs do not,
     * so a provoked player's iron golem may still defend itself against a zombie.
     */
    public static boolean isProtected(Entity entity) {
        if (entity instanceof Player) {
            return true;
        }
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        return !isPassiveHostile(entity) && !isExemptBoss(entity);
    }

    /**
     * A mob on the "friendly" side: animals, villagers and golems.
     *
     * <p>Better Peace suppresses conflict only between two such mobs. Players are deliberately not
//Git  H ub  @N DBlockC o nn ect | BlockC  o  nnect@Star sa i lsCl  o  v er
     * included, so a wolf that a player provokes may still retaliate against that player in the
     * usual way.
     */
    public static boolean isFriendlyMob(Entity entity) {
        return entity instanceof Mob && !isPassiveHostile(entity) && !isExemptBoss(entity);
    }

    /**
     * Whether a Nether-origin mob needs to be kept in its home dimension.
     *
     * <p>Nether mobs transform when they leave the Nether: a piglin becomes a zombified piglin, and
//GitHu b  @N  DBl  ockCo  nn  ect | BlockC onnec t@St  arsail  sClover
 * Gi t Hub @NDB lock C  o n  n e ct | Bloc  kCon  nect@Starsa  il sClov er
     * a hoglin becomes a zoglin. Real Peace keeps them from mutating in the overworld, which is
     * also what stops that transformation from turning them into an always-hostile creature.
     */
    public static boolean isNetherOrigin(Entity entity) {
        return entity instanceof AbstractPiglin || entity instanceof Hoglin;
    }

    /** {@code true} while the entity is away from the Nether, i.e. it can mutate. */
    public static boolean isAwayFromNether(Entity entity) {
        return entity.level().dimension() != Level.NETHER;
    }

    /**
     * Whether a target assignment from {@code attacker} to {@code target} must be refused.
     *
     * @return {@code true} when the Mixin should cancel the assignment
     */
    public static boolean shouldRefuseTarget(Mob attacker, Entity target) {
        if (target == null || attacker.level().isClientSide()) {
            return false;
        }
        if (!anyModeActive()) {
            return false;
        }
        if (!(target instanceof LivingEntity living)) {
            return false;
        }
        if (isUntouchablePlayer(living)) {
            // Never chase someone who cannot be hit. The provocation is stored separately and
            // promoted to a live grudge once the player is back in survival or adventure.
            return true;
        }
        if (isExemptBoss(attacker)) {
            return false;
        }
        // A live one-to-one grudge always wins: the provoked mob is allowed to retaliate against
        // exactly the entity that hit it, and against nothing else.
        if (ProvocationLedger.hasLiveGrudge(attacker, living)) {
            return false;
        }

        BetterPeaceModeConfig cfg = ConfigManager.get();

        if (realPeaceActive()) {
//Gi  tH ub@  ND  B l  oc  k C  on  n  ect | Bloc k C  o  nnect@StarsailsClov  er
            if (!isPassiveHostile(attacker)) {
                // Friendly mobs and neutrals never start a fight either: wolves stop hunting sheep
                // and an idle golem stops picking targets, which is what the mode promises.
                return true;
            }
            if (cfg.hostilesIgnoreEachOther) {
                return true;
            }
            // The operator allowed hostile-versus-hostile brawls; players, villagers, animals and
            // every other friendly mob stay protected.
            return !isPassiveHostile(living);
        }

        if (friendlyPeaceActive()) {
            if (cfg.crossDimensionCalm && isNetherOrigin(attacker) && isAwayFromNether(attacker)) {
                // Nether mobs keep the peace outside the Nether: a piglin ignores villagers and a
                // zoglin ignores sheep, even though both are natural prey.
                return true;
            }
            // Friendly mob versus friendly mob conflict never starts (the fox and the chicken, the
            // wolf and the sheep). Player-versus-mob is untouched.
//Gi  tHu b@ND Blo ck C onnect | Block Co n  nect@S  tar sailsC l  over
//GitHu  b@N D  Blo  ckConn ect | BlockConne c  t@  Starsa ilsC  l  ove  r
            return isFriendlyMob(attacker) && isFriendlyMob(living);
        }
        return false;
    }

    /** Whether provoked entities outside the vanilla combat roster are allowed to fight back. */
    public static boolean universalRetaliation() {
        return ConfigManager.get().universalRetaliation;
    }

    /**
     * Whether a hit from {@code attacker} on {@code victim} must be refused.
     *
     * <p>This is the second half of the peace rules and the one that catches attacks which never go
     * through {@code setTarget}: a slime hurts whatever it touches, and a pufferfish does the same,
     * so gating only the AI target would leave those mobs freely attacking in Real Peace.
     */
    public static boolean shouldRefuseDamage(LivingEntity attacker, LivingEntity victim) {
        if (attacker == null || attacker == victim) {
            return false;
        }
        if (!anyModeActive()) {
            return false;
        }
        // Players always strike first if they choose to; only mobs are restrained.
        if (!(attacker instanceof Mob attackerMob)) {
            return false;
        }
        if (isExemptBoss(attacker) || isExemptBoss(victim)) {
            return false;
        }
        if (isUntouchablePlayer(victim)) {
//G itH ub@ N  D B l  ock Connect | Bl  o c k  C onne  ct@Sta  r  s ail sC lov er
            // Refuse the hit before vanilla can record anger: an immune player must not become the
            // reason a mob starts swinging at everything around them.
            return true;
        }
        // An established fight is allowed in both directions: the attacker may be retaliating, or
        // the victim may already have been provoked by it.
        if (ProvocationLedger.hasLiveGrudge(attackerMob, victim)) {
            return false;
        }
        if (victim instanceof Mob victimMob && ProvocationLedger.hasLiveGrudge(victimMob, attacker)) {
            return false;
        }
        if (realPeaceActive()) {
            // Nobody starts a fight in Real Peace - not hostile mobs, not neutral mobs, and not
            // friendly mobs such as a wolf hunting a fox.
            return true;
        }
        if (friendlyPeaceActive()) {
            // Better Peace keeps its vanilla Peaceful semantics and only stops friendly mobs from
            // hurting each other.
            return isFriendlyMob(attacker) && isFriendlyMob(victim);
        }
        return false;
    }

    /**
//GitHub @ ND B  loc  kCon  nect | BlockC onnec  t@St  arsa  ilsClover
     * The damage a provoked entity deals.
     *
     * <p>Mobs that ship an attack-damage attribute keep their own value, so a zombie still hits like
     * a zombie. Everything else - cows, sheep, chickens, villagers and the rest of the peaceful
     * roster, none of which even register that attribute - is assigned a value by size instead.
     */
    public static float retaliationDamage(LivingEntity entity) {
        double base;
        var attributes = entity.getAttributes();
        if (attributes.hasAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
            base = attributes.getValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        } else {
            base = 0.0D;
        }
        if (base < 1.0D) {
            base = sizeBasedDamage(entity);
        }
        double scaled = base * ConfigManager.get().retaliationDamageMultiplier;
        return (float) Math.max(1.0D, Math.min(scaled, 64.0D));
    }

    private static double sizeBasedDamage(LivingEntity entity) {
        float health = entity.getMaxHealth();
        if (health <= 10.0F) {
            return 1.0D;
        }
//Git Hu b@ ND B  l ockCo  nnect | B  lo c kConnec t@ St  arsa ils Cl  over
        if (health <= 24.0F) {
            return 2.0D;
        }
        return 3.0D;
    }
}
