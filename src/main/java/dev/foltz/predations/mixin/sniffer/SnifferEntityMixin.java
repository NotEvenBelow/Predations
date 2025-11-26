package dev.foltz.predations.mixin.sniffer;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.guano.FertilizerAccess;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnifferEntity.class)
public abstract class SnifferEntityMixin extends AnimalEntity {

    protected SnifferEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "mobTick", at = @At("HEAD"))
    private void applyCropBoostLogic(CallbackInfo ci) {
        if (this.getWorld().isClient) return;

        ExtraConfig.BetterSnifferRelatedFeaturesConfig config = ExtraConfig.getSnifferConfig();

        if (!config.enabled || !config.snifferBoostCropGrowth) return;

        if (this.age % config.snifferCropGrowthInterval == 0) {

            int radius = config.snifferCropGrowthRadius;
            BlockPos center = this.getBlockPos();
            World world = this.getWorld();
            long expiryTime = world.getTime() + config.snifferCropGrowthDurationInTicks;

            for (int x = -radius; x <= radius; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = center.add(x, y, z);

                        if (world.getBlockState(pos).getBlock() instanceof CropBlock) {
                            Chunk chunk = world.getChunk(pos);
                            if (chunk instanceof FertilizerAccess access) {
                                access.predations$setSnifferBoost(pos, expiryTime);
                            }
                        }
                    }
                }
            }
        }
    }
}