package dev.foltz.predations.mixin.guanoAndBoneMeal;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.guano.FertilizerAccess;
import dev.foltz.predations.guano.FertilizerNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class BoneMealMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void onUseBoneMeal(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof CropBlock) {
            ExtraConfig.BatGuanoAndBoneMealConfig config = ExtraConfig.getBatGuanoConfig();
            if (config.enabled && config.vanillaBoneMealSpeedUpCropGrowthInsteadofInstantGrowth) {

                if (!world.isClient) {
                    Chunk chunk = world.getChunk(pos);
                    if (chunk instanceof FertilizerAccess access) {
                        if (access.predations$isFertilized(pos)) {
                            cir.setReturnValue(ActionResult.PASS);
                        } else {
                            double multiplier = config.vanillaBoneMealSpeedUpCropGrowthInsteadofInstantGrowthMultiplier;
                            access.predations$setFertilizer(pos, multiplier);

                            if (context.getPlayer() instanceof ServerPlayerEntity serverPlayer) {
                                FertilizerNetworking.sendUpdate(serverPlayer, pos, multiplier);
                            }

                            if (!context.getPlayer().getAbilities().creativeMode) {
                                context.getStack().decrement(1);
                            }

                            world.syncWorldEvent(1505, pos, 0);

                            cir.setReturnValue(ActionResult.SUCCESS);
                        }
                    }
                } else {
                    cir.setReturnValue(ActionResult.SUCCESS);
                }
            }
        }
    }
}