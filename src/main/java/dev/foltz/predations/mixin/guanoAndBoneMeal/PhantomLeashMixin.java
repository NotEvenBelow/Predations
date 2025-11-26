package dev.foltz.predations.mixin.guanoAndBoneMeal;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class PhantomLeashMixin {

    @Inject(method = "canBeLeashedBy", at = @At("HEAD"), cancellable = true)
    private void predations$checkPhantomCanLeash(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PhantomEntity) {
            ExtraConfig.BatGuanoAndBoneMealConfig config = ExtraConfig.getBatGuanoConfig();

            if (config.enabled) {
                if (config.phantomCanLeash) {
                    cir.setReturnValue(true);
                } else {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}