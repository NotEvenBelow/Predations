package dev.foltz.predations.mixin.rabies;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.rabiesEffect.RabiesStatusEffect;
import dev.foltz.predations.rabiesEffect.RabiesTracker;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class RabiesEntityMixin implements RabiesTracker {

    // ==========================================================
    //                    RABIES TRACKER
    // ==========================================================

    @Unique
    private int rabiesTicks = 0;

    @Override
    public int getRabiesTicks() {
        return this.rabiesTicks;
    }

    @Override
    public void setRabiesTicks(int ticks) {
        this.rabiesTicks = ticks;
    }

    @Override
    public void incrementRabiesTicks() {
        this.rabiesTicks++;
    }

    // ==========================================================
    //                 HYDROPHOBIA
    // ==========================================================

    @Inject(method = "tick", at = @At("HEAD"))
    public void predations$rabiesTick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!self.getWorld().isClient) {
            int symptomStartTick = ExtraConfig.getRabiesConfig().rabiesHydrophobiaStartTime;

            if (this.rabiesTicks > symptomStartTick) {
                if (self.isInsideWaterOrBubbleColumn()) {
                    if (this.rabiesTicks % 20 == 0) {
                        self.damage(self.getDamageSources().starve(), 2.0f);
                    }
                }
            }
        }
    }

    // ==========================================================
    //                 no drinking
    // ==========================================================

    @Inject(method = "setCurrentHand", at = @At("HEAD"), cancellable = true)
    public void predations$preventDrinking(Hand hand, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!self.getWorld().isClient) {
            int symptomStartTick = ExtraConfig.getRabiesConfig().rabiesHydrophobiaStartTime;

            if (this.rabiesTicks > symptomStartTick) {
                ItemStack stack = self.getStackInHand(hand);

                if (stack.getUseAction() == UseAction.DRINK) {

                    ci.cancel();

                    if (self instanceof PlayerEntity player) {
                        player.sendMessage(Text.literal("Your throat is rejecting the fluid").formatted(Formatting.RED), true);
                    }
                }
            }
        }
    }

    // ==========================================================
    //                 data
    // ==========================================================

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    public void injectWriteMethod(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("PredationsRabiesTicks", rabiesTicks);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    public void injectReadMethod(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("PredationsRabiesTicks")) {
            this.rabiesTicks = nbt.getInt("PredationsRabiesTicks");
        }
    }

    @Inject(method = "onStatusEffectRemoved", at = @At("HEAD"))
    public void onRemoveEffect(StatusEffectInstance effect, CallbackInfo ci) {
        if (effect.getEffectType() instanceof RabiesStatusEffect) {
            this.rabiesTicks = 0;
        }
    }
}