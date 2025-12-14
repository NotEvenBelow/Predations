package dev.foltz.predations.guano;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class GuanoFertilizerItem extends Item {

    public GuanoFertilizerItem(Settings settings) {
        super(settings);
    }

    private void consumeWithChance(ItemStack stack, PlayerEntity player, double chance) {
        if (player.getAbilities().creativeMode) return;
        if (player.getRandom().nextDouble() < chance) {
            stack.decrement(1);
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        boolean isFertilizable = state.getBlock() instanceof Fertilizable;
        if (!isFertilizable) {
            return ActionResult.PASS;
        }

        ExtraConfig.BatGuanoAndBoneMealConfig config = ExtraConfig.getBatGuanoConfig();
        if (!config.enabled) return ActionResult.PASS;

        if (config.guanoFertilizerInstantFinishCropGrowth) {
            if (state.getBlock() instanceof CropBlock crop) {
                int maxAge = crop.getMaxAge();

                int currentAge = crop.getAge(state);

                if (currentAge >= maxAge) {
                    return ActionResult.PASS;
                }

                if (!world.isClient) {
                    world.setBlockState(pos, crop.withAge(maxAge));
                    world.syncWorldEvent(1505, pos, 0);
                }

                if (context.getPlayer() != null) {
                    context.getPlayer().getItemCooldownManager().set(this, 5);
                    consumeWithChance(context.getStack(), context.getPlayer(), config.guanoFertilizerConsumeChance);
                }
                return ActionResult.success(world.isClient);
            }

            ItemStack dummyStack = context.getStack().copy();
            if (BoneMealItem.useOnFertilizable(dummyStack, world, pos)) {
                if (!world.isClient) {
                    world.syncWorldEvent(1505, pos, 0);
                }
                if (context.getPlayer() != null) {
                    context.getPlayer().getItemCooldownManager().set(this, 5);
                    consumeWithChance(context.getStack(), context.getPlayer(), config.guanoFertilizerConsumeChance);
                }
                return ActionResult.success(world.isClient);
            }
        }

        // --- MODE B: SPEED UP ---
        else {
            if (world.isClient) {
                return ActionResult.SUCCESS;
            }

            Chunk chunk = world.getChunk(pos);
            if (chunk instanceof FertilizerAccess access) {
                if (access.predations$isFertilized(pos)) {
                    return ActionResult.PASS;
                } else {
                    double multiplier = config.guanoFertilizerCropGrowthMultiplier;
                    access.predations$setFertilizer(pos, multiplier);

                    PlayerEntity player = context.getPlayer();
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        FertilizerNetworking.sendToTracking(world, chunk, pos, multiplier);
                    }

                    if (player != null) {
                        player.getItemCooldownManager().set(this, 5);
                        consumeWithChance(context.getStack(), player, config.guanoFertilizerConsumeChance);
                    }

                    world.syncWorldEvent(1505, pos, 0);

                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }
}