package dev.foltz.predations.mixin.secret;

import dev.foltz.predations.secret.KnockbackContext;
import dev.foltz.predations.secret.KnockbackHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PlayerEntity.class)
public abstract class PlayerAttackNoKnockbackMixin {

    @Inject(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void predations$noKBStart(Entity target, CallbackInfo ci) {
        if (!KnockbackHelper.noKnockbackAll()) return;

        PlayerEntity self = (PlayerEntity)(Object)this;
        ItemStack stack = self.getMainHandStack();

        boolean emptyHand = stack.isEmpty();
        boolean holdingBlock = !emptyHand && stack.getItem() instanceof BlockItem;
        boolean whitelisted = !emptyHand && KnockbackHelper.isWhitelistedKB(stack);

        if (emptyHand || holdingBlock || !whitelisted) {
            KnockbackContext.enableNoKB();
        }
    }

    @Inject(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
    private void predations$noKBEnd(Entity target, CallbackInfo ci) {
        KnockbackContext.disableNoKB();
    }
}
