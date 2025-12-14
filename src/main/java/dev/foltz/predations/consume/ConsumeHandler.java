package dev.foltz.predations.consume;

import dev.foltz.predations.config.ConfigManager;
import dev.foltz.predations.mixin.entity.GoalSelectorAccessor;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents; // CHANGED
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;

public final class ConsumeHandler {
    private ConsumeHandler() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(ConsumeHandler::onEntityLoad);
        System.out.println("[Predations][Init] ConsumeHandler registered");
    }

    private static void onEntityLoad(Entity entity, ServerWorld world) {
        if (!(entity instanceof PathAwareEntity path)) return;
        MobEntity mob = (MobEntity) path;

        if (!ConfigManager.consumeEnabled(mob)) return;

        var goalSel = ((MobEntityAccessor) mob).getGoalSelector();
        var goals = ((GoalSelectorAccessor) goalSel).predations$getGoals();

        boolean has = goals.stream().anyMatch(pg -> pg.getGoal() instanceof ConsumeNearbyItemsGoal);
        if (!has) {
            goalSel.add(2, new ConsumeNearbyItemsGoal(path));
        }
    }
}