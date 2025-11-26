package dev.foltz.predations.mixin.rabies;

import dev.foltz.predations.rabiesEffect.RabiesStatusEffect;
import dev.foltz.predations.rabiesEffect.RabiesTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class RabiesEntityMixin extends Entity implements RabiesTracker {

    @Unique
    private int rabiesTicks = 0;

    public RabiesEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

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

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    public void injectWriteMethod(NbtCompound nbt, CallbackInfo ci) {
        if (rabiesTicks > 0) {
            nbt.putInt("PredationsRabiesTicks", rabiesTicks);
        }
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