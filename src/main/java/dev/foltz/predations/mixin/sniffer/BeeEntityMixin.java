package dev.foltz.predations.mixin.sniffer;

import dev.foltz.predations.bee.BeeEntityAccess;
import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeeEntity.class)
public abstract class BeeEntityMixin extends AnimalEntity implements BeeEntityAccess {

    @Shadow @Nullable public abstract BlockPos getFlowerPos();

    @Unique
    private static final TrackedData<Boolean> PREDATIONS_TORCHFLOWER_POLLEN = DataTracker.registerData(BeeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    protected BeeEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initTorchflowerData(CallbackInfo ci) {
        this.dataTracker.startTracking(PREDATIONS_TORCHFLOWER_POLLEN, false);
    }

    @Override
    public boolean predations$isTorchflowerPollen() {
        return this.dataTracker.get(PREDATIONS_TORCHFLOWER_POLLEN);
    }

    @Override
    public void predations$setTorchflowerPollen(boolean isTorchflower) {
        this.dataTracker.set(PREDATIONS_TORCHFLOWER_POLLEN, isTorchflower);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeTorchflowerNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("PredationsTorchflowerPollen", predations$isTorchflowerPollen());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readTorchflowerNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("PredationsTorchflowerPollen")) {
            predations$setTorchflowerPollen(nbt.getBoolean("PredationsTorchflowerPollen"));
        }
    }

    @Inject(method = "setHasNectar", at = @At("HEAD"))
    public void checkTorchflowerSource(boolean hasNectar, CallbackInfo ci) {
        if (!hasNectar) {
            predations$setTorchflowerPollen(false);
            return;
        }

        ExtraConfig.BetterSnifferRelatedFeaturesConfig config = ExtraConfig.getSnifferConfig();
        if (!config.enabled || !config.beeMoreEffectiveWhenPollenateWithTorchflower) return;

        boolean found = false;
        World world = this.getWorld();
        BlockPos flowerPos = this.getFlowerPos();

        if (flowerPos != null) {
            BlockState state = world.getBlockState(flowerPos);
            String id = Registries.BLOCK.getId(state.getBlock()).toString();
            if (id.contains("torchflower")) found = true;
        }

        if (!found) {
            BlockPos currentPos = this.getBlockPos();
            String idCurrent = Registries.BLOCK.getId(world.getBlockState(currentPos).getBlock()).toString();
            String idBelow = Registries.BLOCK.getId(world.getBlockState(currentPos.down()).getBlock()).toString();

            if (idCurrent.contains("torchflower") || idBelow.contains("torchflower")) {
                found = true;
            }
        }

        predations$setTorchflowerPollen(found);
    }
}