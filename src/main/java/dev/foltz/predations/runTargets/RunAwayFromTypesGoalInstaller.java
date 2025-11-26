package dev.foltz.predations.runTargets;

import dev.foltz.predations.config.ConfigManager;
import dev.foltz.predations.mixin.entity.GoalSelectorAccessor;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Set;

public final class RunAwayFromTypesGoalInstaller {
    private RunAwayFromTypesGoalInstaller() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(RunAwayFromTypesGoalInstaller::onEntitySpawn);
        System.out.println("[Predations][Init] RunAwayFromTypesGoalInstaller registered");
    }

    private static void onEntitySpawn(Entity entity, ServerWorld world) {
        if (!(entity instanceof PathAwareEntity path)) return;
        MobEntity mob = (MobEntity) path;

        GoalSelector goalSelector = ((MobEntityAccessor) mob).getGoalSelector();
        var goalSet = ((GoalSelectorAccessor) goalSelector).predations$getGoals();

        Set<String> runawayTargets = ConfigManager.getRunawayTargets(path);

        if (!runawayTargets.isEmpty()) {
            DetectMobTypesGoal detector = new DetectMobTypesGoal(path, runawayTargets);
            RunAwayFromMobGoal runner = new RunAwayFromMobGoal(path, detector);

            goalSelector.add(2, detector);
            goalSelector.add(3, runner);
        }
    }
}