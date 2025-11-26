package dev.foltz.predations.mixin.rabies;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.rabiesEffect.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class RabiesFoodMixin {
    @Inject(method = "applyFoodEffects", at = @At("HEAD"))
    private void checkRabiesCure(ItemStack stack, World world, LivingEntity targetEntity, CallbackInfo ci) {
        if (world.isClient) return;


        if (!ExtraConfig.getRabiesConfig().enabled) return;

        boolean shouldCure = false;

        // bro finally has a use
        if (stack.isOf(Items.CHORUS_FRUIT)) {
            if (ExtraConfig.getRabiesConfig().chorusFruitRemovesRabies) {
                shouldCure = true;
            }
        }

        else if (stack.isOf(Items.GOLDEN_APPLE)) {
            if (ExtraConfig.getRabiesConfig().goldenAppleRemovesRabies) {
                shouldCure = true;
            }
        }

        else if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            if (ExtraConfig.getRabiesConfig().enchantedGoldenAppleRemovesRabies) {
                shouldCure = true;
            }
        }
        if (shouldCure && targetEntity.hasStatusEffect(ModEffects.RABIES)) {
            targetEntity.removeStatusEffect(ModEffects.RABIES);
        }
    }
}