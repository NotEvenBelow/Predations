package dev.foltz.predations.mixin.wolf;

import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfEntity.class)
public class WolfTameMixin {

    private static final String INFECTED_TAG = "predations.infected_with_rabies";

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        WolfEntity wolf = (WolfEntity) (Object) this;

        if (wolf.getCommandTags().contains(INFECTED_TAG)) {
            ItemStack itemStack = player.getStackInHand(hand);
            Item item = itemStack.getItem();


            if (item == Items.BONE || wolf.isBreedingItem(itemStack)) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}