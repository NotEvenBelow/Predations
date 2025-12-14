// Developing this gave me a headache
package dev.foltz.predations.squid.ai;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Box;

import java.util.*;
import java.util.function.Predicate;

public class HeadSuckTargeting {
    public static final Map<LivingEntity, Boolean> ALREADY_SUCKED = new WeakHashMap<>();

    public static final Map<LivingEntity, UUID> CLAIMED = new WeakHashMap<>();
    private static final Map<UUID, Integer> SQUID_CD = new WeakHashMap<>();

    public static boolean isOnCooldown(SquidEntity squid) {
        Integer cd = SQUID_CD.get(squid.getUuid());
        if (cd != null && cd > 0) {
            SQUID_CD.put(squid.getUuid(), cd - 1);
            return true;
        }
        return false;
    }

    public static boolean isPlayerNearby(SquidEntity squid, double range) {
        return squid.getWorld().getClosestPlayer(squid, range) != null;
    }

    public static boolean isLowLight(SquidEntity squid, int maxLight) {
        return squid.getWorld().getLightLevel(squid.getBlockPos()) <= Math.max(0, maxLight);
    }

    public static boolean isImmune(LivingEntity target) {
        if (target instanceof PlayerEntity p) {
            return dev.foltz.predations.item.TalismanImmunityTracker.isImmune(p);
        }
        return false;
    }

    public static boolean isClaimedByOther(LivingEntity target, SquidEntity squid) {
        UUID owner = CLAIMED.get(target);
        return owner != null && !owner.equals(squid.getUuid());
    }

    public static LivingEntity pickTarget(SquidEntity squid, double range, ExtraConfig.PredatorySquidConfig cfg, boolean ignoredExclusiveParam) {
        Box box = squid.getBoundingBox().expand(range);

        Predicate<LivingEntity> pred = e -> e.isAlive() && e != squid && isValidTarget(e, cfg)
                && (!cfg.blockedBySolids || (e.isTouchingWater() && squid.isTouchingWater()) || squid.canSee(e));

        List<LivingEntity> list = squid.getWorld().getEntitiesByClass(LivingEntity.class, box, pred::test);

        list.removeIf(e -> isClaimedByOther(e, squid));

        if (list.isEmpty()) return null;
        return list.get(squid.getRandom().nextInt(list.size()));
    }

    public static boolean isValidTarget(LivingEntity e, ExtraConfig.PredatorySquidConfig cfg) {
        if (!e.isAlive()) return false;

        if (ALREADY_SUCKED.containsKey(e)) return false;

        if (e instanceof PlayerEntity p) {
            if (p.isSpectator() || p.isCreative() || p.getAbilities().invulnerable) return false;
            if (isImmune(p)) return false;
        }

        String id = Registries.ENTITY_TYPE.getId(e.getType()).getPath();
        return !cfg.excludedTargets.contains(id);
    }

    public static void tryClaim(SquidEntity squid, LivingEntity target, double range) {
        if (target != null && !isClaimedByOther(target, squid) && squid.getBoundingBox().expand(range).contains(target.getPos())) {
            CLAIMED.put(target, squid.getUuid());
        }
    }

    public static void cleanupTarget(SquidEntity squid, LivingEntity target, boolean wasLatched) {
        if (target != null) {
            UUID owner = CLAIMED.get(target);
            if (owner != null && owner.equals(squid.getUuid())) {
                CLAIMED.remove(target);
            }

            if (wasLatched) {
                ALREADY_SUCKED.put(target, true);
            }
        }

        int cd = Math.max(0, ExtraConfig.get().predSquid.regrabCooldownTicks);
        SQUID_CD.put(squid.getUuid(), cd);
    }

    public static void releaseTarget(SquidEntity squid, LivingEntity target, boolean wasSuccessful) {
        cleanupTarget(squid, target, wasSuccessful);
    }
}