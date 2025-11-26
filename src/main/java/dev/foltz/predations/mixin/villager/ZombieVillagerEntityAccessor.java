package dev.foltz.predations.mixin.villager;

import net.minecraft.entity.mob.ZombieVillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.UUID;

@Mixin(ZombieVillagerEntity.class)
public interface ZombieVillagerEntityAccessor {

    @Invoker("setConverting")
    void invokeSetConverting(UUID uuid, int time);
}