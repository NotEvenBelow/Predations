package dev.foltz.predations.mixin.guanoAndBoneMeal;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.guano.FertilizerAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public abstract class CropGrowthSpeedMixin {

    @Shadow public abstract void applyGrowth(World world, BlockPos pos, BlockState state);
    @Shadow public abstract int getMaxAge();
    @Shadow protected abstract IntProperty getAgeProperty();
    @Shadow protected static float getAvailableMoisture(Block block, BlockView world, BlockPos pos) { return 0; }

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void applyFertilizerSpeedBoost(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        Chunk chunk = world.getChunk(pos);
        if (chunk instanceof FertilizerAccess access) {
            double totalMultiplier = 1.0;

            if (ExtraConfig.getBatGuanoConfig().enabled) {
                double fertMult = access.predations$getFertilizer(pos);
                if (fertMult > 1.0) {
                    totalMultiplier *= fertMult;
                }
            }

            ExtraConfig.BetterSnifferRelatedFeaturesConfig snifferConfig = ExtraConfig.getSnifferConfig();

            if (snifferConfig.enabled && snifferConfig.snifferBoostCropGrowth) {
                long expiry = access.predations$getSnifferBoostExpiry(pos);
                if (world.getTime() < expiry) {
                    totalMultiplier *= snifferConfig.snifferCropGrowthMultiplier;
                }
            }

            if (totalMultiplier <= 1.0) return;

            int extraCycles = (int) totalMultiplier - 1;
            double remainder = totalMultiplier - 1 - extraCycles;
            if (random.nextDouble() < remainder) {
                extraCycles++;
            }

            for (int i = 0; i < extraCycles; i++) {
                BlockState currentState = world.getBlockState(pos);
                if (!(currentState.getBlock() instanceof CropBlock)) break;

                int age = currentState.get(this.getAgeProperty());
                if (age >= this.getMaxAge()) break;

                if (world.getBaseLightLevel(pos, 0) >= 9) {
                    float moisture = getAvailableMoisture((Block)(Object)this, world, pos);
                    if (random.nextInt((int)(25.0F / moisture) + 1) == 0) {
                        this.applyGrowth(world, pos, currentState);
                    }
                }
            }
        }
    }
}