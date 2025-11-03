package dev.foltz.predations.consume;

import dev.foltz.predations.config.ConfigManager;
import dev.foltz.predations.mixin.entity.GoalSelectorAccessor;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Standalone tick handler for attaching ConsumeNearbyItemsGoal to eligible mobs.
 * No dependency on ARFYInstaller.
 */
public final class ConsumeHandler {
    private ConsumeHandler() {}

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(ConsumeHandler::tickWorld);
        System.out.println("[Predations][Init] ConsumeHandler registered");
    }

    private static void tickWorld(ServerWorld world) {
        for (var e : world.iterateEntities()) {
            if (!(e instanceof PathAwareEntity path)) continue;
            MobEntity mob = (MobEntity) path;

            if (!ConfigManager.consumeEnabled(mob)) continue;

            var goalSel = ((MobEntityAccessor) mob).getGoalSelector();
            var goals = ((GoalSelectorAccessor) goalSel).predations$getGoals();

            boolean has = goals.stream().anyMatch(pg -> pg.getGoal() instanceof ConsumeNearbyItemsGoal);
            if (!has) {
                goalSel.add(2, new ConsumeNearbyItemsGoal(path));
            }
        }
    }
}
