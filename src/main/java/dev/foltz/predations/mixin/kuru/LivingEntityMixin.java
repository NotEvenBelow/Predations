package dev.foltz.predations.mixin.kuru;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.kuru_status.KuruStatusEffects;
import dev.foltz.predations.rabiesEffect.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow @Final private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

    @Shadow protected void onStatusEffectRemoved(StatusEffectInstance effect) {
        throw new AssertionError("Mixin shadow method called");
    }

    @Inject(method = "clearStatusEffects", at = @At("HEAD"), cancellable = true)
    private void predations$handleCustomMilkCures(CallbackInfoReturnable<Boolean> cir) {
        boolean cureKuru = ExtraConfig.getKuruConfig().milkCanCureKuru;
        boolean cureRabies = ExtraConfig.getRabiesConfig().milkRemoveRabies;

        if (cureKuru && cureRabies) {
            return;
        }

        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getWorld().isClient) {
            cir.setReturnValue(false);
            return;
        }

        boolean removedAny = false;
        Iterator<StatusEffectInstance> iterator = this.activeStatusEffects.values().iterator();

        while(iterator.hasNext()) {
            StatusEffectInstance instance = iterator.next();
            StatusEffect type = instance.getEffectType();

            boolean isKuru = (type == KuruStatusEffects.KURU);
            boolean isRabies = (type == ModEffects.RABIES);

            if (isKuru && !cureKuru) continue;

            if (isRabies && !cureRabies) continue;

            this.onStatusEffectRemoved(instance);
            iterator.remove();
            removedAny = true;
        }

        cir.setReturnValue(removedAny);
    }
}