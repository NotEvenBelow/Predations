package dev.foltz.predations.mixin.kuru;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.kuru_status.KuruStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class KuruFoodMixin {

    @Inject(method = "applyFoodEffects", at = @At("HEAD"))
    private void checkKuruCure(ItemStack stack, World world, LivingEntity targetEntity, CallbackInfo ci) {
        if (world.isClient) return;

        boolean shouldCure = false;

        // bro finally has a use
        if (stack.isOf(Items.CHORUS_FRUIT)) {
            if (ExtraConfig.getKuruConfig().chorusFruitRemovesKuru) {
                shouldCure = true;
            }
        }
        else if (stack.isOf(Items.GOLDEN_APPLE)) {
            if (ExtraConfig.getKuruConfig().goldenAppleRemovesKuru) {
                shouldCure = true;
            }
        }
        else if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            if (ExtraConfig.getKuruConfig().enchantedGoldenAppleRemovesKuru) {
                shouldCure = true;
            }
        }

        if (shouldCure && targetEntity.hasStatusEffect(KuruStatusEffects.KURU)) {
            targetEntity.removeStatusEffect(KuruStatusEffects.KURU);
        }
    }
}