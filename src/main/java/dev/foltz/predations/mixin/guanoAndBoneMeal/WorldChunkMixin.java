package dev.foltz.predations.mixin.guanoAndBoneMeal;

import dev.foltz.predations.guano.FertilizerAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(WorldChunk.class)
public class WorldChunkMixin implements FertilizerAccess {

    @Unique
    private final Map<BlockPos, Double> predations$fertilizerMap = new HashMap<>();

    @Unique
    private final Map<BlockPos, Long> predations$snifferBoostMap = new HashMap<>();

    @Override
    public void predations$setFertilizer(BlockPos pos, double multiplier) {
        predations$fertilizerMap.put(pos.toImmutable(), multiplier);
        ((WorldChunk)(Object)this).setNeedsSaving(true);
    }

    @Override
    public double predations$getFertilizer(BlockPos pos) {
        return predations$fertilizerMap.getOrDefault(pos, 0.0);
    }

    @Override
    public boolean predations$isFertilized(BlockPos pos) {
        return predations$fertilizerMap.containsKey(pos);
    }

    @Override
    public void predations$removeFertilizer(BlockPos pos) {
        predations$fertilizerMap.remove(pos);
        ((WorldChunk)(Object)this).setNeedsSaving(true);
    }

    @Override
    public void predations$setSnifferBoost(BlockPos pos, long expiryTime) {
        predations$snifferBoostMap.put(pos.toImmutable(), expiryTime);
    }

    @Override
    public long predations$getSnifferBoostExpiry(BlockPos pos) {
        return predations$snifferBoostMap.getOrDefault(pos, 0L);
    }
}