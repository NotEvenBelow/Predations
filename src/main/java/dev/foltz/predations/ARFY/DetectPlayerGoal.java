package dev.foltz.predations.ARFY;

import dev.foltz.predations.config.ConfigManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;

import java.util.EnumSet;
import java.util.List;

public class DetectPlayerGoal<T extends LivingEntity> extends Goal {
    private static final int CHECK_COOLDOWN_TICKS = 10;
    private static final double IMPRINT_RANGE = 5.0;

    private final PathAwareEntity mob;
    private final Class<T> classToDetect;
    private final Ingredient ignoreIfHolding;
    private T targetEntity;
    private long fleeUntilTick = 0L;
    private int checkTimer;

    private final boolean isImprinted;

    public DetectPlayerGoal(PathAwareEntity mob, Class<T> classToDetect, Ingredient ignoreIfHolding) {
        this.mob = mob;
        this.classToDetect = classToDetect;
        this.ignoreIfHolding = ignoreIfHolding;
        this.setControls(EnumSet.noneOf(Goal.Control.class));
        this.checkTimer = mob.getRandom().nextInt(CHECK_COOLDOWN_TICKS);
        this.isImprinted = this.checkImprinting();
    }

    private boolean checkImprinting() {
        if (this.mob.isBaby()) {
            T nearbyPlayer = findClosestTarget(IMPRINT_RANGE);
            return nearbyPlayer != null;
        }
        return false;
    }

    public boolean isNeutralized() {
        if (this.mob instanceof INeutralizable neutralMob) {
            if (this.mob.isLeashed()) {
                neutralMob.setArfyNeutralized(true);
            }
            return neutralMob.isArfyNeutralized();
        }
        return false;
    }

    public boolean shouldFlee() {
        if (this.mob.getAttacker() != null && (this.mob.getWorld().getTime() - this.mob.getLastAttackedTime() < 100)) {
            return true;
        }
        return this.mob.getWorld().getTime() < this.fleeUntilTick;
    }

    public T getTarget() {
        return this.targetEntity;
    }

    @Override
    public boolean canStart() {
        if (this.isImprinted || isNeutralized()) {
            return false;
        }

        if (this.mob.getAttacker() != null) {
            LivingEntity attacker = this.mob.getAttacker();
            if (this.classToDetect.isAssignableFrom(attacker.getClass()) && !isLured((T)attacker)) {
                this.targetEntity = (T) attacker;
                return true;
            }
        }

        this.checkTimer--;
        if (this.checkTimer > 0) {
            return false;
        }
        this.checkTimer = CHECK_COOLDOWN_TICKS;

        if (!ConfigManager.isEnabled(this.mob)) {
            return false;
        }

        final double baseMaxDistance = ConfigManager.safeDistance(this.mob);
        this.targetEntity = findClosestTarget(baseMaxDistance);

        if (this.targetEntity != null) {
            if (this.targetEntity.isSneaking()) {
                double reducedDistance = baseMaxDistance * (1.0 - ConfigManager.shiftingReduceDetectRangeByPercent(this.mob));
                if (this.mob.distanceTo(this.targetEntity) > reducedDistance) {
                    this.targetEntity = null;
                }
            }
        }

        return this.targetEntity != null && !isLured(this.targetEntity);
    }

    @Override
    public boolean shouldContinue() {
        if (this.isImprinted || isNeutralized()) {
            return false;
        }

        if (this.mob.getAttacker() != null && this.mob.isAlive()) {
            return true;
        }

        if (this.targetEntity == null || !this.targetEntity.isAlive()) {
            return false;
        }

        double maxDistance = ConfigManager.safeDistance(this.mob);

        if (this.targetEntity.isSneaking()) {
            maxDistance = maxDistance * (1.0 - ConfigManager.shiftingReduceDetectRangeByPercent(this.mob));
        }

        if (this.mob.distanceTo(this.targetEntity) > maxDistance) {
            return false;
        }

        if (ConfigManager.requireLineOfSightToRun(this.mob)) {
            if (!this.mob.canSee(this.targetEntity)) {
                return false;
            }
        }

        return !isLured(this.targetEntity);
    }

    @Override
    public void start() {
        this.fleeUntilTick = this.mob.getWorld().getTime() + 1000L;
    }

    @Override
    public void stop() {
        if (isNeutralized()) {
            this.fleeUntilTick = 0L;
        }
        else {
            final int lingerTicks = ConfigManager.getLingerTicks(this.mob);
            this.fleeUntilTick = this.mob.getWorld().getTime() + lingerTicks;
        }
        this.targetEntity = null;
    }

    private boolean isLured(T target) {
        if (ConfigManager.allowLure(this.mob) && !ignoreIfHolding.isEmpty()) {
            for (var stack : target.getHandItems()) {
                if (ignoreIfHolding.test(stack)) return true;
            }
        }
        return false;
    }

    private T findClosestTarget(double range) {
        TargetPredicate predicate = TargetPredicate.createAttackable()
                .setBaseMaxDistance(range)
                .setPredicate(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test);

        if (!ConfigManager.requireLineOfSightToRun(this.mob)) {
            predicate.ignoreVisibility();
        }

        List<T> candidates = mob.getWorld().getEntitiesByClass(
                classToDetect,
                mob.getBoundingBox().expand(range, (range + 1f) / 2f, range),
                e -> true
        );
        return mob.getWorld().getClosestEntity(
                candidates,
                predicate,
                mob, mob.getX(), mob.getY(), mob.getZ()
        );
    }
}