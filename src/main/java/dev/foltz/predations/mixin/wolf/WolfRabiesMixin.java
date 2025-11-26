package dev.foltz.predations.mixin.wolf;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.rabiesEffect.ModEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.WolfEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfEntity.class)
public class WolfRabiesMixin {

    private static final String INFECTED_TAG = "predations.infected_with_rabies";

    @Inject(method = "tryAttack", at = @At("RETURN"))
    private void onTryAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        WolfEntity wolf = (WolfEntity) (Object) this;

        if (wolf.isTamed()) {
            return;
        }

        ExtraConfig.RabiesConfig config = ExtraConfig.getRabiesConfig();

        double chance;
        if (wolf.getCommandTags().contains(INFECTED_TAG)) {
            chance = config.naturalAggressiveWolfRabiesBitChance;
        } else {
            chance = config.nonNaturalAggressiveWolfRabiesBitChance;
        }

        if (wolf.getRandom().nextDouble() < chance) {
            livingTarget.addStatusEffect(new StatusEffectInstance(ModEffects.RABIES, Integer.MAX_VALUE, 0));

            if (livingTarget instanceof WolfEntity) {
                livingTarget.addCommandTag(INFECTED_TAG);
            }
        }
    }
}