package dev.foltz.predations.ARFY;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public class RunAwayFromTypesGoal extends Goal {
    private static final int FLEE_RANGE_HORIZONTAL = 16;
    private static final int FLEE_RANGE_VERTICAL = 7;

    private final PathAwareEntity mob;
    private final Set<String> attackerIdsLower;
    private final EntityNavigation nav;

    private final float farSpeed, nearSpeed;
    private final int distance;
    private final float ratio;

    private final TargetPredicate withinRangePredicate;
    private Path fleePath;
    private LivingEntity targetEntity;

    public RunAwayFromTypesGoal(PathAwareEntity mob,
                                Set<String> attackerIdsLower,
                                float farSpeed, float nearSpeed, int distance, float ratio) {
        this.mob = mob;
        this.attackerIdsLower = attackerIdsLower;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        this.distance = Math.max(1, distance);
        this.ratio = ratio;

        this.nav = mob.getNavigation();
        this.withinRangePredicate = TargetPredicate.createAttackable()
                .setPredicate(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test);
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (attackerIdsLower.isEmpty()) return false;

        var world = mob.getWorld();
        var box = mob.getBoundingBox().expand(distance, (distance + 1f) / 2f, distance);

        var candidates = world.getEntitiesByClass(LivingEntity.class, box, e -> {
            var id = Registries.ENTITY_TYPE.getId(e.getType());
            if (id == null) return false;
            String full = id.toString().toLowerCase(Locale.ROOT);
            String path = id.getPath().toLowerCase(Locale.ROOT);
            return attackerIdsLower.contains(full) || attackerIdsLower.contains(path);
        });

        targetEntity = world.getClosestEntity(
                candidates,
                withinRangePredicate.setBaseMaxDistance(distance),
                mob, mob.getX(), mob.getY(), mob.getZ());

        if (targetEntity == null) return false;

        Vec3d away = NoPenaltyTargeting.findFrom(mob, FLEE_RANGE_HORIZONTAL, FLEE_RANGE_VERTICAL, targetEntity.getPos());
        if (away == null) return false;
        if (targetEntity.squaredDistanceTo(away.x, away.y, away.z) <= targetEntity.squaredDistanceTo(mob)) return false;

        fleePath = nav.findPathTo(away.x, away.y, away.z, 0);
        return fleePath != null;
    }

    @Override public boolean shouldContinue() { return !nav.isIdle(); }
    @Override public void start() { nav.startMovingAlong(fleePath, farSpeed); }
    @Override public void stop()  { targetEntity = null; }

    @Override
    public void tick() {
        if (targetEntity == null) return;
        float maxDist = distance;
        double d2 = Math.min(mob.squaredDistanceTo(targetEntity), maxDist * maxDist);
        double d = Math.sqrt(d2);
        double delta = 1 - d / maxDist;

        float speed = (ratio == 0)
                ? (d2 == 0 ? nearSpeed : (float) MathHelper.lerp(delta, farSpeed, nearSpeed))
                : (delta >= ratio ? nearSpeed : farSpeed);
        nav.setSpeed(speed);
    }
}
