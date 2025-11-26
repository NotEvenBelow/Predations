package dev.foltz.predations.mixin.golem;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.passive.IronGolemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class IronGolemSuffocationMixin {

    @Inject(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void predations$cancelIronGolemSuffocation(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        if (!((Object)this instanceof IronGolemEntity)) {
            return;
        }

        ExtraConfig.VillagerConfig config = ExtraConfig.getVillagerConfig();

        if (config != null && config.ironGolemChangeEnabled && config.ironGolemCannotSuffocate) {

            if (source.isOf(DamageTypes.IN_WALL)) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}