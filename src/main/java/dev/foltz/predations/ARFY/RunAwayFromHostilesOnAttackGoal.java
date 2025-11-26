/**package dev.foltz.predations.ARFY;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class RunAwayFromHostilesOnAttackGoal extends Goal {
    private final PathAwareEntity mob;
    private final EntityNavigation nav;
    private final float farSpeed, nearSpeed;
    private final int distance;
    private final float ratio;
    private final float safeDistance;
    private final int repathCooldown;

    private Path fleePath;
    private long lastRepathTick = 0L;
    private Vec3d cachedThreatPos = null;

    public RunAwayFromHostilesOnAttackGoal(PathAwareEntity mob, float farSpeed, float nearSpeed, int distance, float ratio) {
        this.mob = mob;
        this.nav = mob.getNavigation();
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        this.distance = Math.max(1, distance);
        this.ratio = ratio;


        float fleeSafeMult = 1.5f;
        int fleeRepathCooldown = 20;

        this.safeDistance = Math.max(this.distance, (float) (this.distance * fleeSafeMult));
        this.repathCooldown = fleeRepathCooldown;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        long now = mob.getWorld().getTime();
        if (!AttackMemory.isActive(mob, now)) return false;

        AttackMemory.maybeRefresh(mob, now, repathCooldown);
        cachedThreatPos = AttackMemory.attackerPos(mob, now);
        if (cachedThreatPos == null) return false;
        if (mob.squaredDistanceTo(cachedThreatPos) >= (double) (safeDistance * safeDistance)) return false;
        return buildOrRefreshPath(now);
    }

    @Override
    public boolean shouldContinue() {
        if (nav.isIdle()) return false;
        long now = mob.getWorld().getTime();
        if (!AttackMemory.isActive(mob, now)) return false;
        if (cachedThreatPos == null) return true;
        return mob.squaredDistanceTo(cachedThreatPos) < (double) (safeDistance * safeDistance);
    }

    @Override public void start() { if (fleePath != null) nav.startMovingAlong(fleePath, farSpeed); }
    @Override public void stop()  { fleePath = null; cachedThreatPos = null; }

    @Override
    public void tick() {
        long now = mob.getWorld().getTime();

        if (now - lastRepathTick >= repathCooldown) {
            AttackMemory.maybeRefresh(mob, now, repathCooldown);
            Vec3d threat = AttackMemory.attackerPos(mob, now);
            if (threat != null) cachedThreatPos = threat;

            if (cachedThreatPos != null && mob.squaredDistanceTo(cachedThreatPos) < (double) (safeDistance * safeDistance)) {
                buildOrRefreshPath(now);
            }
        }

        if (cachedThreatPos != null) {
            double maxD = distance;
            double d2 = Math.min(mob.squaredDistanceTo(cachedThreatPos), maxD * maxD);
            double d = Math.sqrt(d2);
            double delta = 1 - d / maxD;

            float speed = (ratio == 0f)
                    ? (d2 == 0 ? nearSpeed : (float) MathHelper.lerp(delta, farSpeed, nearSpeed))
                    : (delta >= ratio ? nearSpeed : farSpeed);
            nav.setSpeed(speed);
        }
    }

    private boolean buildOrRefreshPath(long now) {
        if (cachedThreatPos == null) return false;

        Vec3d awayDir = mob.getPos().subtract(cachedThreatPos).normalize();
        Vec3d desired = mob.getPos().add(awayDir.multiply(Math.max(8.0, distance * 0.75)));

        Vec3d candidate = NoPenaltyTargeting.findTo(mob, 16, 7, desired, Math.PI / 2);
        if (candidate == null) candidate = NoPenaltyTargeting.findFrom(mob, 16, 7, cachedThreatPos);

        if (candidate != null) {
            Path p = nav.findPathTo(candidate.x, candidate.y, candidate.z, 0);
            if (p != null) {
                fleePath = p;
                nav.startMovingAlong(fleePath, farSpeed);
                lastRepathTick = now;
                return true;
            }
        }
        lastRepathTick = now;
        return true;
    }
} **/