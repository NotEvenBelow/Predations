package dev.foltz.predations.mixin.rabies;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.rabiesEffect.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChorusFruitItem.class)
public class RabiesChorusMixin {

    @Inject(method = "finishUsing", at = @At("RETURN"))
    private void onEatChorus(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!world.isClient) {
            if (ExtraConfig.getRabiesConfig().chorusFruitRemovesRabies) {
                if (user.hasStatusEffect(ModEffects.RABIES)) {
                    user.removeStatusEffect(ModEffects.RABIES);
                }
            }
        }
    }
}