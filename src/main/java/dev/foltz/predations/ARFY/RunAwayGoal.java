package dev.foltz.predations.ARFY;

import dev.foltz.predations.config.ConfigManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class RunAwayGoal<T extends LivingEntity> extends Goal {
    private static final int FLEE_RANGE_HORIZONTAL = 16;
    private static final int FLEE_RANGE_VERTICAL = 7;

    private final PathAwareEntity mob;
    private final EntityNavigation nav;

    private final DetectPlayerGoal<T> detectorGoal;

    private final int repathCooldown = 20;

    private Path fleePath;
    private T targetEntity;

    private long lastRepathTick = 0L;
    private Vec3d lastThreatPos;

    private long stuckSinceTick = 0L;

    public RunAwayGoal(PathAwareEntity mob,
                       Class<T> classToRunFrom,
                       Ingredient ignoreIfHolding,
                       DetectPlayerGoal<T> detectorGoal) {
        this.mob = mob;
        this.nav = mob.getNavigation();
        this.detectorGoal = detectorGoal;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    private boolean isNeutralized() {
        return this.detectorGoal.isNeutralized();
    }

    @Override
    public boolean canStart() {
        if (isNeutralized()) {
            return false;
        }
        return this.detectorGoal.shouldFlee() && (this.detectorGoal.getTarget() != null || this.mob.getAttacker() != null);
    }

    @Override
    public boolean shouldContinue() {
        if (isNeutralized()) {
            return false;
        }

        if (this.mob.getAttacker() != null && (this.mob.getWorld().getTime() - this.mob.getLastAttackedTime() < 100)) {
            return true;
        }

        final long now = mob.getWorld().getTime();
        if (this.stuckSinceTick > 0L) {
            long stuckDuration = now - this.stuckSinceTick;
            if (stuckDuration > 40L) {
                return false;
            }
        }

        return this.detectorGoal.shouldFlee();
    }

    @Override
    public void start() {
        this.stuckSinceTick = 0L;
        this.lastRepathTick = 0L;

        this.targetEntity = this.detectorGoal.getTarget();

        LivingEntity attacker = this.mob.getAttacker();
        if (this.targetEntity == null && attacker != null) {
            try {
                this.targetEntity = (T) attacker;
            } catch (ClassCastException ignored) {}
        }

        if (this.targetEntity != null) {
            this.lastThreatPos = this.targetEntity.getPos();
        }

        findAndStartPath();
    }

    @Override
    public void stop() {
        this.targetEntity = null;
        this.fleePath = null;
        this.stuckSinceTick = 0L;

        try {
            this.nav.stop();
            double runSpeed = ConfigManager.runSpeed(this.mob);
            this.nav.setSpeed((float)(runSpeed - (runSpeed - 1.0)));
        } catch (UnsupportedOperationException ignored) {}
    }

    private boolean findAndStartPath() {
        this.lastRepathTick = mob.getWorld().getTime();

        this.targetEntity = this.detectorGoal.getTarget();

        if (this.targetEntity == null && this.mob.getAttacker() != null) {
            this.lastThreatPos = this.mob.getAttacker().getPos();
        } else if (targetEntity != null && targetEntity.isAlive()) {
            this.lastThreatPos = targetEntity.getPos();
        }

        if (this.lastThreatPos == null) return false;

        Vec3d away = NoPenaltyTargeting.findFrom(mob, FLEE_RANGE_HORIZONTAL, FLEE_RANGE_VERTICAL, this.lastThreatPos);
        if (away != null) {
            Path p = nav.findPathTo(away.x, away.y, away.z, 0);
            if (p != null) {
                this.fleePath = p;
                nav.startMovingAlong(fleePath, 1.0);
                this.stuckSinceTick = 0L;
                return true;
            }
        }

        if (this.stuckSinceTick == 0L) {
            this.stuckSinceTick = mob.getWorld().getTime();
        }
        return false;
    }

    @Override
    public void tick() {
        final long now = mob.getWorld().getTime();

        if (this.mob.isLeashed()) {
            try {
                double runSpeed = ConfigManager.runSpeed(this.mob);
                this.nav.setSpeed((float)(runSpeed - (runSpeed - 1.0)));
                this.nav.stop();
            } catch (UnsupportedOperationException ignored) {}
            return;
        }

        double speedMultiplier;
        LivingEntity currentThreat = this.detectorGoal.getTarget();

        if (currentThreat == null) {
            currentThreat = this.mob.getAttacker();
        }

        if (currentThreat != null && currentThreat.isAlive()) {
            final double baseSpeedMultiplier = ConfigManager.runSpeed(this.mob);
            final double nearBlock = ConfigManager.runNearPlayerBlock(this.mob);
            final double nearMult = ConfigManager.nearPlayerSpeedMultiplier(this.mob);

            if (mob.distanceTo(currentThreat) < nearBlock) {
                speedMultiplier = baseSpeedMultiplier * nearMult;
            } else {
                speedMultiplier = baseSpeedMultiplier;
            }
        }
        else if (this.detectorGoal.shouldFlee() || this.mob.getAttacker() != null) {
            speedMultiplier = ConfigManager.runSpeed(this.mob);
        }
        else {
            speedMultiplier = 1.0;
        }

        try {
            this.nav.setSpeed((float)speedMultiplier);
        } catch (UnsupportedOperationException ignored) { }

        if (nav.isIdle() || now - lastRepathTick >= this.repathCooldown) {
            findAndStartPath();
        }

        if (nav.isIdle()) {
            if (stuckSinceTick == 0L) {
                stuckSinceTick = now;
            }
        } else {
            stuckSinceTick = 0L;
        }
    }
}