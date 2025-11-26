package dev.foltz.predations.rabiesEffect;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

public class RabiesStatusEffect extends StatusEffect {
    public RabiesStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x5A6C31);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!ExtraConfig.getRabiesConfig().enabled) {
            return;
        }

        if (!(entity instanceof PlayerEntity) && entity.getHealth() > 1.0F) {
            entity.damage(entity.getDamageSources().magic(), 1.0F);
        }

        if (!entity.getWorld().isClient) {
            if (entity instanceof RabiesTracker tracker) {

                tracker.incrementRabiesTicks();

                int limit = ExtraConfig.getRabiesConfig().timeSurviveWithRabiesEffectTillKilledInTick;

                if (tracker.getRabiesTicks() > limit) {
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 3, false, false));
                }
            }
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}