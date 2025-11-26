package dev.foltz.predations.mixin.sniffer;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.sniffer.PitcherPlantEffectsState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow public abstract ItemStack getStack();

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickPitcherAbsorption(CallbackInfo ci) {
        if (this.getWorld().isClient) return;
        if (this.age % 10 != 0) return;

        ItemStack stack = this.getStack();
        if (!(stack.getItem() instanceof PotionItem)) return;

        World world = this.getWorld();

        Box box = this.getBoundingBox();
        BlockPos foundPos = null;
        BlockState foundState = null;

        for (BlockPos testPos : BlockPos.iterate(
                (int) Math.floor(box.minX), (int) Math.floor(box.minY), (int) Math.floor(box.minZ),
                (int) Math.floor(box.maxX), (int) Math.floor(box.maxY), (int) Math.floor(box.maxZ))) {

            BlockState state = world.getBlockState(testPos);
            if (state.isOf(Blocks.PITCHER_CROP) || state.isOf(Blocks.PITCHER_PLANT)) {
                foundPos = testPos.toImmutable();
                foundState = state;
                break;
            }
        }

        if (foundPos == null) return;

        BlockPos keyPos = foundPos;
        if (foundState.contains(Properties.DOUBLE_BLOCK_HALF)) {
            if (foundState.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
                keyPos = foundPos.down();
            }
        }
        keyPos = keyPos.toImmutable();

        ExtraConfig.BetterSnifferRelatedFeaturesConfig config = ExtraConfig.getSnifferConfig();
        if (!config.enabled) return;

        PitcherPlantEffectsState effectsState = PitcherPlantEffectsState.getServerState((ServerWorld) world);

        if (effectsState.plantEffects.containsKey(keyPos)) return;

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
            System.out.println("DEBUG: SAVING EFFECT " + appliedId + " to " + keyPos);
            effectsState.addEffect(keyPos, appliedId + ":" + appliedAmp);

            world.playSound(null, keyPos, SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.BLOCKS, 1.0f, 1.0f);
            this.discard();
        } else {
            world.playSound(null, keyPos, SoundEvents.INTENTIONALLY_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            this.discard();
        }
    }
}