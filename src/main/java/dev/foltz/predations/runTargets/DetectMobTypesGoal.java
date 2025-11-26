package dev.foltz.predations.runTargets;

import dev.foltz.predations.config.ConfigManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DetectMobTypesGoal extends Goal {
    private static final int CHECK_COOLDOWN_TICKS = 10;

    private final PathAwareEntity mob;
    private final Set<String> attackerIdsLower;
    private LivingEntity targetEntity;
    private long fleeUntilTick = 0L;
    private int checkTimer;

    public DetectMobTypesGoal(PathAwareEntity mob, Set<String> attackerIdsLower) {
        this.mob = mob;
        this.attackerIdsLower = attackerIdsLower;
        this.setControls(EnumSet.noneOf(Goal.Control.class));
        this.checkTimer = mob.getRandom().nextInt(CHECK_COOLDOWN_TICKS);
    }

    public boolean isNeutralized() {
        return this.mob.isLeashed() && ConfigManager.leashingStopTheRunning(this.mob);
    }

    public boolean shouldFlee() {
        return this.mob.getWorld().getTime() < this.fleeUntilTick;
    }

    public LivingEntity getTarget() {
        return this.targetEntity;
    }

    @Override
    public boolean canStart() {
        if (isNeutralized()) {
            return false;
        }

        this.checkTimer--;
        if (this.checkTimer > 0) {
            return false;
        }
        this.checkTimer = CHECK_COOLDOWN_TICKS;

        // Check the *base* 'enabled' config for the mob
        if (!ConfigManager.isEnabled(this.mob)) {
            return false;
        }

        final double baseMaxDistance = ConfigManager.safeDistance(this.mob);
        this.targetEntity = findClosestTarget(baseMaxDistance);

        return this.targetEntity != null;
    }

    @Override
    public boolean shouldContinue() {
        if (isNeutralized()) {
            return false;
        }

        if (this.targetEntity == null || !this.targetEntity.isAlive()) {
            return false;
        }

        double maxDistance = ConfigManager.safeDistance(this.mob);

        if (this.mob.distanceTo(this.targetEntity) > maxDistance) {
            return false;
        }

        if (ConfigManager.requireLineOfSightToRun(this.mob)) {
            return this.mob.canSee(this.targetEntity);
        }

        return true;
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

    private LivingEntity findClosestTarget(double range) {
        TargetPredicate predicate = TargetPredicate.createAttackable()
                .setBaseMaxDistance(range)
                .setPredicate(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test);

        if (!ConfigManager.requireLineOfSightToRun(this.mob)) {
            predicate.ignoreVisibility();
        }

        List<LivingEntity> candidates = mob.getWorld().getEntitiesByClass(
                LivingEntity.class,
                mob.getBoundingBox().expand(range, (range + 1f) / 2f, range),
                e -> {
                    // Filter by the ID set
                    if (!e.isAlive()) return false;
                    var id = Registries.ENTITY_TYPE.getId(e.getType());
                    if (id == null) return false;
                    String full = id.toString().toLowerCase(Locale.ROOT);
                    String path = id.getPath().toLowerCase(Locale.ROOT);
                    return attackerIdsLower.contains(full) || attackerIdsLower.contains(path);
                }
        );
        return mob.getWorld().getClosestEntity(
                candidates,
                predicate,
                mob, mob.getX(), mob.getY(), mob.getZ()
        );
    }
}