package dev.foltz.predations.guano;

import net.minecraft.util.math.BlockPos;

public interface FertilizerAccess {
    // Bone Meal / Guano (Persistent)
    void predations$setFertilizer(BlockPos pos, double multiplier);
    double predations$getFertilizer(BlockPos pos);
    void predations$removeFertilizer(BlockPos pos);
    boolean predations$isFertilized(BlockPos pos);

    // Sniffer Boost (Temporary)
    void predations$setSnifferBoost(BlockPos pos, long expiryTime);
    long predations$getSnifferBoostExpiry(BlockPos pos);
}