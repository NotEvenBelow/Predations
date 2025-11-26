package dev.foltz.predations.bee;

import net.minecraft.nbt.NbtCompound;

public interface BeeDataAccess {
    NbtCompound predations$getEntityData();
    int predations$getMinOccupationTicks();
    void predations$setMinOccupationTicks(int ticks);
}