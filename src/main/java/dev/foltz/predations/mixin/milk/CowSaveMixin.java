package dev.foltz.predations.mixin.milk;

import dev.foltz.predations.util.CowCooldownInterface;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class CowSaveMixin {

    private static final String NBT_LAST_MILKED = "Predations_LastMilkedTick";

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this instanceof CowCooldownInterface cow) {
            nbt.putLong(NBT_LAST_MILKED, cow.predations$getLastMilked());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this instanceof CowCooldownInterface cow) {
            if (nbt.contains(NBT_LAST_MILKED)) {
                cow.predations$setLastMilked(nbt.getLong(NBT_LAST_MILKED));
            }
        }
    }
}