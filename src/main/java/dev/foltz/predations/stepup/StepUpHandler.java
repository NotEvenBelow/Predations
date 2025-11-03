package dev.foltz.predations.stepup;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.mixin.entity.EntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public final class StepUpHandler {
    private StepUpHandler() {}

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(StepUpHandler::tick);
        System.out.println("[Predations][Init] StepUpHandler registered");
    }

    private static void tick(ServerWorld world) {
        var cfg = ExtraConfig.get().stepUp;
        float defaultHeight = cfg.defaultHeight;
        Map<String, ExtraConfig.StepPerEntity> map = cfg.entities;

        for (Entity e : world.iterateEntities()) {
            if (!(e instanceof MobEntity)) continue;

            var id = Registries.ENTITY_TYPE.getId(e.getType());
            if (id == null) continue;

            ExtraConfig.StepPerEntity entry = map.get(id.toString());
            if (entry == null || entry.enabled == null || !entry.enabled) continue;

            float newHeight = entry.height != null ? entry.height : defaultHeight;
            float cur = ((EntityAccessor) e).predations$getStepHeight();

            if (Math.abs(cur - newHeight) > 0.01f) {
                ((EntityAccessor) e).predations$setStepHeight(newHeight);
            }
        }
    }
}
