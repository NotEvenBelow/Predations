package dev.foltz.predations.mixin.rabies;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.rabiesEffect.RabiesTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageTracker.class)
public class RabiesDeathMessageMixin {

    @Shadow @Final private LivingEntity entity;

    @Inject(method = "getDeathMessage", at = @At("HEAD"), cancellable = true)
    private void predations$customHydrophobiaMessage(CallbackInfoReturnable<Text> cir) {
        if (this.entity instanceof RabiesTracker tracker) {
            int ticks = tracker.getRabiesTicks();
            int threshold = ExtraConfig.getRabiesConfig().rabiesHydrophobiaStartTime;

            if (ticks > threshold) {
                if (this.entity.getRecentDamageSource() != null && this.entity.getRecentDamageSource().isOf(DamageTypes.STARVE)) {

                    Text customMessage = Text.literal(this.entity.getDisplayName().getString() + " died from Hydrophobia");
                    cir.setReturnValue(customMessage);
                }
            }
        }
    }
}