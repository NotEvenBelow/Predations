package dev.foltz.predations.squid;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.passive.SchoolingFishEntity;

import java.util.Set;

public final class FishFleeInstaller {
    private FishFleeInstaller() {}

    private static final Set<EntityType<?>> FISH_TYPES = Set.of(
            EntityType.COD, EntityType.SALMON, EntityType.TROPICAL_FISH, EntityType.PUFFERFISH
    );

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            var cfg = ExtraConfig.get().predatorySquid;
            if (!cfg.fishFleeSquidLikePlayer) return;
            if (!(entity instanceof SchoolingFishEntity fish)) return;
            if (!FISH_TYPES.contains(entity.getType())) return;

            int dist = Math.max(1, cfg.fishFleeDistance);
            var goalSel = ((MobEntityAccessor) fish).getGoalSelector();
            goalSel.add(1, new FleeEntityGoal<>(fish, PredatorySquidEntity.class, dist, 1.2, 1.6));
            goalSel.add(1, new FleeEntityGoal<>(fish, PredatoryGlowSquidEntity.class, dist, 1.2, 1.6));
        });
    }
}
