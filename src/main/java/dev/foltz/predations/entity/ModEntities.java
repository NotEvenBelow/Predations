package dev.foltz.predations.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
    public static final EntityType<FamishedCowEntity> FAMISHED_COW =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    new Identifier("predations", "famished_cow"),
                    FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, FamishedCowEntity::new)
                            .dimensions(EntityDimensions.fixed(0.9f, 1.4f))
                            .trackRangeBlocks(64).trackedUpdateRate(3)
                            .build()
            );

    public static void register() {

        FabricDefaultAttributeRegistry.register(FAMISHED_COW, FamishedCowEntity.createAttributes());

        FamishedCowEntity.registerSpawnReplacement(FAMISHED_COW);
    }
}
