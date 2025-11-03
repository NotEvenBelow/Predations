package dev.foltz.predations.ARFY;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class RunAwayGoal<T extends LivingEntity> extends Goal {
    private static final int FLEE_RANGE_HORIZONTAL = 16;
    private static final int FLEE_RANGE_VERTICAL = 7;

    private static final double NODE_REACHED_DISTANCE_SQ = 1.0 * 1.0;

    private final PathAwareEntity mob;
    private final Class<T> classToRunFrom;
    private final Ingredient ignoreIfHolding;
    private final EntityNavigation nav;

    private final float farSpeed, nearSpeed;
    private final int distance;
    private final float ratio;

    private final float safeMult;
    private final int repathCooldown;
    private final int lingerTicks;

    private final TargetPredicate withinRangePredicate;
    private Path fleePath;
    private T targetEntity;

    private long lastRepathTick = 0L;
    private long lastSeenTick = 0L;
    private Vec3d lastThreatPos;

    public RunAwayGoal(PathAwareEntity mob,
                       Class<T> classToRunFrom,
                       Ingredient ignoreIfHolding,
                       float farSpeed, float nearSpeed, int distance, float ratio,
                       float safeMult, int repathCooldown, int lingerTicks) {
        this.mob = mob;
        this.classToRunFrom = classToRunFrom;
        this.ignoreIfHolding = ignoreIfHolding;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        this.distance = Math.max(1, distance);
        this.ratio = ratio;
        this.safeMult = Math.max(1f, safeMult);
        this.repathCooldown = Math.max(1, repathCooldown);
        this.lingerTicks = Math.max(0, lingerTicks);

        this.nav = mob.getNavigation();
        this.withinRangePredicate = TargetPredicate.createAttackable()
                .setPredicate(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test);
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        var candidates = mob.getWorld().getEntitiesByClass(
                classToRunFrom,
                mob.getBoundingBox().expand(distance, (distance + 1f) / 2f, distance),
                e -> true
        );
        targetEntity = mob.getWorld().getClosestEntity(
                candidates,
                withinRangePredicate.setBaseMaxDistance(distance),
                mob, mob.getX(), mob.getY(), mob.getZ());

        if (targetEntity == null) return false;

        if (!ignoreIfHolding.isEmpty()) {
            for (var stack : targetEntity.getHandItems()) {
                if (ignoreIfHolding.test(stack)) return false;
            }
        }

        lastSeenTick = mob.getWorld().getTime();

        Vec3d away = NoPenaltyTargeting.findFrom(mob, FLEE_RANGE_HORIZONTAL, FLEE_RANGE_VERTICAL, targetEntity.getPos());
        if (away == null) return false;
        if (targetEntity.squaredDistanceTo(away.x, away.y, away.z) <= targetEntity.squaredDistanceTo(mob)) return false;

        fleePath = nav.findPathTo(away.x, away.y, away.z, 0);
        lastThreatPos = targetEntity.getPos();
        return fleePath != null;
    }

    @Override
    public boolean shouldContinue() {
        if (nav.isIdle()) return false;

        final long now = mob.getWorld().getTime();
        if (targetEntity != null && targetEntity.isAlive()) {
            double maxSafe = Math.max(distance, distance * safeMult);
            double d2 = mob.squaredDistanceTo(targetEntity);
            if (d2 < maxSafe * maxSafe) {
                lastSeenTick = now;
                return true;
            }
        }
        return (now - lastSeenTick) < lingerTicks;
    }

    @Override public void start() { if (fleePath != null) nav.startMovingAlong(fleePath, farSpeed); }
    @Override public void stop()  { targetEntity = null; fleePath = null; lastThreatPos = null; }

    @Override
    public void tick() {
        // FIX: 1.20.1 uses Path#getCurrentNodePos() instead of getNodePos(idx)
        Path path = this.nav.getCurrentPath();
        if (path != null && !path.isFinished()) {
            BlockPos targetNodeBlockPos = path.getCurrentNodePos();
            if (targetNodeBlockPos != null) {
                Vec3d mobPos = this.mob.getPos();
                double dx = mobPos.x - (targetNodeBlockPos.getX() + 0.5);
                double dz = mobPos.z - (targetNodeBlockPos.getZ() + 0.5);
                double distSq = (dx * dx) + (dz * dz);
                if (distSq < NODE_REACHED_DISTANCE_SQ) {
                    path.next();
                }
            }
        }

        // original logic continues
        final long now = mob.getWorld().getTime();

        if (now - lastRepathTick >= repathCooldown) {
            Vec3d threatPos = (targetEntity != null && targetEntity.isAlive()) ? targetEntity.getPos() : lastThreatPos;
            lastThreatPos = threatPos;
            if (threatPos != null) {
                Vec3d away = NoPenaltyTargeting.findFrom(mob, FLEE_RANGE_HORIZONTAL, FLEE_RANGE_VERTICAL, threatPos);
                if (away != null) {
                    Path p = nav.findPathTo(away.x, away.y, away.z, 0);
                    if (p != null) {
                        fleePath = p;
                        nav.startMovingAlong(fleePath, farSpeed);
                    }
                }
            }
            lastRepathTick = now;
        }

        if (targetEntity != null) {
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
}
