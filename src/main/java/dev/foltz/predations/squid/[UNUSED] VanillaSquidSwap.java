/** Might be needed in the future**/


/** package dev.foltz.predations.squid;

import dev.foltz.predations.config.ExtraConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;

public final class VanillaSquidSwap {
    private VanillaSquidSwap() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((Entity entity, ServerWorld world) -> {
            // Off switch: only if your feature is enabled
            if (!ExtraConfig.get().predatorySquid.enabled) return;

            // Only swap exact vanilla types.
            if (entity.getType() == EntityType.SQUID) {
                swap(entity, world, ModSquidEntities.PREDATORY_SQUID);
            } else if (entity.getType() == EntityType.GLOW_SQUID) {
                swap(entity, world, ModSquidEntities.PREDATORY_GLOW_SQUID);
            }
        });
    }

    private static void swap(Entity vanilla, ServerWorld world, EntityType<?> replacementType) {
        var replacement = replacementType.create(world);
        if (replacement == null) return;

        replacement.refreshPositionAndAngles(
                vanilla.getX(), vanilla.getY(), vanilla.getZ(),
                vanilla.getYaw(), vanilla.getPitch()
        );
        replacement.setVelocity(vanilla.getVelocity());

        if (vanilla.hasCustomName()) replacement.setCustomName(vanilla.getCustomName());
        replacement.setCustomNameVisible(vanilla.isCustomNameVisible());

        // spawn and remove original
        if (world.spawnEntity(replacement)) vanilla.discard();
    }
}
**/