package dev.foltz.predations.mixin.ARFY;

import dev.foltz.predations.ARFY.INeutralizable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class ArfyNeutralizableMixin extends LivingEntity implements INeutralizable {

    @Unique
    private static final TrackedData<Boolean> IS_ARFY_NEUTRALIZED = DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    protected ArfyNeutralizableMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTrackerMixin(CallbackInfo ci) {
        this.dataTracker.startTracking(IS_ARFY_NEUTRALIZED, false);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("IsArfyNeutralized", this.isArfyNeutralized());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        this.setArfyNeutralized(nbt.getBoolean("IsArfyNeutralized"));
    }

    @Unique
    @Override
    public boolean isArfyNeutralized() {
        return this.dataTracker.get(IS_ARFY_NEUTRALIZED);
    }

    @Unique
    @Override
    public void setArfyNeutralized(boolean neutralized) {
        this.dataTracker.set(IS_ARFY_NEUTRALIZED, neutralized);
    }
}