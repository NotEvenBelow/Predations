package dev.foltz.predations.squid;

import dev.foltz.predations.config.ExtraConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class PeakComedySquidSpawnHandler {
    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof SquidEntity squid)) return;

            double squidChance = ExtraConfig.get().predSquid.peakComedySquidChance;
            if (squidChance > 0 && world.getRandom().nextDouble() < squidChance) {
                BlockPos pos = squid.getBlockPos();
                squid.discard();

                double glowChance = ExtraConfig.get().predSquid.peakComedyGlowSquidChance;
                if (glowChance > 0 && world.getRandom().nextDouble() < glowChance) {
                    ModSquidEntities.PREDATORY_GLOW_SQUID.spawn(
                            world, null, null, pos,
                            SpawnReason.NATURAL, false, false
                    );
                    return;
                }

                ModSquidEntities.PREDATORY_SQUID.spawn(
                        world, null, null, pos,
                        SpawnReason.NATURAL, false, false
                );
            }
        });
    }
}
