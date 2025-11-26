package dev.foltz.predations.mixin.sniffer;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.sniffer.PitcherPlantEffectsState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Item.class)
public class PitcherPlantInteractionMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void onInteract(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block != Blocks.PITCHER_CROP && block != Blocks.PITCHER_PLANT) return;

        if (state.contains(Properties.DOUBLE_BLOCK_HALF)) {
            if (state.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
                pos = pos.down();
            }
        }

        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();

        if (player != null && player.isSneaking() && stack.getItem() instanceof PotionItem) {

            if (world.isClient) {
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }

            ExtraConfig.BetterSnifferRelatedFeaturesConfig config = ExtraConfig.getSnifferConfig();
            if (!config.enabled) return;

            PitcherPlantEffectsState effectsState = PitcherPlantEffectsState.getServerState((ServerWorld) world);
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);

            String appliedId = null;
            int appliedAmp = 0;
            boolean foundValid = false;

            for (StatusEffectInstance effect : effects) {
                String id = Registries.STATUS_EFFECT.getId(effect.getEffectType()).toString();

                if (config.pitcherEffects.containsKey(id)) {
                    int potionAmp = effect.getAmplifier();
                    int configCap = config.pitcherEffects.get(id);

                    appliedAmp = Math.min(potionAmp, configCap);
                    appliedId = id;
                    foundValid = true;
                    break;
                }
            }

            if (foundValid) {
                effectsState.plantEffects.put(pos, appliedId + ":" + appliedAmp);
                player.sendMessage(Text.of("§a[Predations] Plant absorbed " + appliedId + " (Level " + (appliedAmp + 1) + ")"), true);
            } else {
                if (effectsState.plantEffects.containsKey(pos)) {
                    effectsState.plantEffects.remove(pos);
                    player.sendMessage(Text.of("§e[Predations] Plant effects cleansed."), true);
                } else {
                    player.sendMessage(Text.of("§7[Predations] Potion consumed (No valid effect applied)."), true);
                }
            }
            effectsState.markDirty();

            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
                if (stack.isEmpty()) {
                    player.setStackInHand(context.getHand(), new ItemStack(Items.GLASS_BOTTLE));
                } else if (!player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE))) {
                    player.dropItem(new ItemStack(Items.GLASS_BOTTLE), false);
                }
            }

            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}