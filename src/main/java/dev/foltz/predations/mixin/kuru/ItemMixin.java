package dev.foltz.predations.mixin.kuru;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.kuru.KuruHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Shadow public abstract FoodComponent getFoodComponent();

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void preventMeatEatingAtStage2(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {

        if (world.isClient) {
            return;
        }

        if (!ExtraConfig.getKuruConfig().unableToEatAnyMeatsAtStage2) {
            return;
        }

        ItemStack stack = user.getStackInHand(hand);

        if (stack.isFood()) {
            FoodComponent food = this.getFoodComponent();
            if (food != null && food.isMeat()) {

                int currentStage = KuruHandler.getKuruStage(user);

                if (currentStage >= 2) {
                    cir.setReturnValue(TypedActionResult.fail(stack));
                }
            }
        }
    }
}