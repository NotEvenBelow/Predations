package dev.foltz.predations.mixin.fox;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.rabiesEffect.ModEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class FoxRabiesMixin {

    private static final String INFECTED_TAG = "predations.infected_with_rabies";

    @Inject(method = "tryAttack", at = @At("RETURN"))
    private void onTryAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof FoxEntity)) {
            return;
        }

        if (!cir.getReturnValue()) {
            return;
        }

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        FoxEntity fox = (FoxEntity) (Object) this;
        ExtraConfig.RabiesConfig config = ExtraConfig.getRabiesConfig();

        double chance;
        if (fox.getCommandTags().contains(INFECTED_TAG)) {
            chance = config.naturalAggressiveFoxRabiesBitChance;

            if (fox.getRandom().nextDouble() < chance) {
                livingTarget.addStatusEffect(new StatusEffectInstance(ModEffects.RABIES, Integer.MAX_VALUE, 0));

                if (livingTarget instanceof FoxEntity) {
                    livingTarget.addCommandTag(INFECTED_TAG);
                }
            }
        }
    }
}