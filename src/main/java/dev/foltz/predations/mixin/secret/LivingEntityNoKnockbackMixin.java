package dev.foltz.predations.mixin.secret;

import dev.foltz.predations.secret.KnockbackContext;
import dev.foltz.predations.secret.KnockbackHelper;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LivingEntity.class)
public abstract class LivingEntityNoKnockbackMixin {

    @Inject(method = "takeKnockback(DDD)V", at = @At("HEAD"), cancellable = true)
    private void arfy$cancelKB(double strength, double x, double z, CallbackInfo ci) {
        if (KnockbackHelper.noKnockbackAll() && KnockbackContext.isNoKB()) {
            ci.cancel();
        }
    }
}
