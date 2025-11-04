package dev.foltz.predations.squid;

import dev.foltz.predations.PredationsMod;
import dev.foltz.predations.config.ExtraConfig;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

public final class ModSquidEntities {
    public static EntityType<PredatorySquidEntity> PREDATORY_SQUID;
    public static EntityType<PredatoryGlowSquidEntity> PREDATORY_GLOW_SQUID;

    public static void register() {
        PREDATORY_SQUID = Registry.register(
                Registries.ENTITY_TYPE,
                new Identifier("predations", "predatory_squid"),
                FabricEntityTypeBuilder.create(SpawnGroup.WATER_CREATURE, PredatorySquidEntity::new)
                        .dimensions(EntityDimensions.fixed(0.8F, 0.8F))
                        .build()
        );

        PREDATORY_GLOW_SQUID = Registry.register(
                Registries.ENTITY_TYPE,
                new Identifier("predations", "predatory_glow_squid"),
                FabricEntityTypeBuilder.create(SpawnGroup.WATER_CREATURE, PredatoryGlowSquidEntity::new)
                        .dimensions(EntityDimensions.fixed(0.8F, 0.8F))
                        .build()
        );

        
        FabricDefaultAttributeRegistry.register(PREDATORY_SQUID, PredatorySquidEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(PREDATORY_GLOW_SQUID, PredatoryGlowSquidEntity.createAttributes());

        if (ExtraConfig.get().predatorySquid.enabled) {
           
            BiomeModifications.addSpawn(BiomeSelectors.all(), SpawnGroup.WATER_CREATURE, PREDATORY_SQUID, 10, 1, 3);
            BiomeModifications.addSpawn(BiomeSelectors.all(), SpawnGroup.WATER_CREATURE, PREDATORY_GLOW_SQUID, 8, 1, 2);

            SpawnRestriction.register(
                    PREDATORY_SQUID,
                    SpawnRestriction.Location.IN_WATER,
                    Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                    SquidEntity::canSpawn
            );
            SpawnRestriction.register(
                    PREDATORY_GLOW_SQUID,
                    SpawnRestriction.Location.IN_WATER,
                    Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                    SquidEntity::canSpawn
            );
        }
    }

    private ModSquidEntities() {}
}
