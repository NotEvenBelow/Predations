package dev.foltz.predations.mixin.guanoAndBoneMeal;

import dev.foltz.predations.guano.FertilizerAccess;
import dev.foltz.predations.guano.FertilizerNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(AbstractBlock.class)
public class CropBlockMixin {

    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    private void clearFertilizerOnBreak(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        if (world.isClient) return;

        if (!(state.getBlock() instanceof CropBlock)) {
            return;
        }

        if (!state.isOf(newState.getBlock())) {
            Chunk chunk = world.getChunk(pos);

            if (chunk instanceof FertilizerAccess access) {
                if (access.predations$isFertilized(pos)) {
                    access.predations$removeFertilizer(pos);

                    FertilizerNetworking.sendToTracking(world, chunk, pos, 0.0);
                }
            }
        }
    }
}