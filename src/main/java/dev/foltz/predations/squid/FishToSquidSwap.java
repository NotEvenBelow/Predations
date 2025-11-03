package dev.foltz.predations.squid;

import dev.foltz.predations.config.ExtraConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;

import java.util.Set;

public final class FishToSquidSwap {
    private FishToSquidSwap() {}

    private static final Set<EntityType<?>> FISH_TYPES = Set.of(
            EntityType.COD, EntityType.SALMON, EntityType.TROPICAL_FISH, EntityType.PUFFERFISH
    );

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((Entity entity, ServerWorld world) -> {
            var cfg = ExtraConfig.get().predatorySquid;
            if (cfg.replaceChance <= 0) return;

            if (FISH_TYPES.contains(entity.getType())) {
                if (world.random.nextDouble() < cfg.replaceChance) {
                    boolean glow = world.random.nextFloat() < cfg.glowSquidChance;
                    EntityType<?> type = glow ? EntityType.GLOW_SQUID : EntityType.SQUID;
                    swap(entity, world, type);
                }
            }
        });
    }

    private static void swap(Entity fish, ServerWorld world, EntityType<?> squidType) {
        var squid = squidType.create(world);
        if (squid == null) return;

        squid.refreshPositionAndAngles(fish.getX(), fish.getY(), fish.getZ(), fish.getYaw(), fish.getPitch());
        squid.setVelocity(fish.getVelocity());

        if (fish.hasCustomName()) {
            squid.setCustomName(fish.getCustomName());
            squid.setCustomNameVisible(fish.isCustomNameVisible());
        }
        if (world.spawnEntity(squid)) {
            fish.discard();
        }
    }
}
