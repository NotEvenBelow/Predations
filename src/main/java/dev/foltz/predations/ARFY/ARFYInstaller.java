package dev.foltz.predations.ARFY;

import dev.foltz.predations.config.ConfigManager;
import dev.foltz.predations.mixin.entity.GoalSelectorAccessor;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;

import java.util.*;
import dev.foltz.predations.aggressionTargets.AggressionHelper; // <-- NEW IMPORT

public final class ARFYInstaller {
    private ARFYInstaller() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(ARFYInstaller::onEntitySpawn);
        System.out.println("[Predations][Init] ARFYInstaller registered");
    }

    private static void onEntitySpawn(Entity entity, ServerWorld world) {
        if (!(entity instanceof PathAwareEntity path)) return;
        MobEntity mob = (MobEntity) path;

        var goalSet = ((GoalSelectorAccessor) ((MobEntityAccessor) mob).getGoalSelector()).predations$getGoals();

        AggressionHelper.installAggressionGoals(path, mob);

        goalSet.removeIf(pg -> pg.getGoal() instanceof RunAwayGoal<?> || pg.getGoal() instanceof DetectPlayerGoal<?>);

        if (ConfigManager.isEnabled(mob)) {
            Ingredient ignore;
            if (ConfigManager.allowLure(entity)) {
                List<Item> list = ConfigManager.standardLureItems(entity.getType());
                ignore = list.isEmpty()
                        ? Ingredient.EMPTY
                        : Ingredient.ofItems(list.toArray(new Item[0]));
            } else {
                ignore = Ingredient.EMPTY;
            }

            DetectPlayerGoal<PlayerEntity> detector = new DetectPlayerGoal<>(
                    path,
                    PlayerEntity.class,
                    ignore
            );

            RunAwayGoal<PlayerEntity> runner = new RunAwayGoal<>(
                    path,
                    PlayerEntity.class,
                    ignore,
                    detector
            );

            ((MobEntityAccessor) mob).getGoalSelector().add(0, detector);
            ((MobEntityAccessor) mob).getGoalSelector().add(1, runner);
        }

        if (!ConfigManager.allowLure(entity)) {
            goalSet.removeIf(pg -> pg.getGoal() instanceof TemptGoal);
        }
    }
}