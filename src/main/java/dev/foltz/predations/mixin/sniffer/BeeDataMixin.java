package dev.foltz.predations.mixin.sniffer;

import dev.foltz.predations.bee.BeeDataAccess;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.block.entity.BeehiveBlockEntity$BeeData")
public class BeeDataMixin implements BeeDataAccess {

    @Shadow @Final private NbtCompound entityData;
    @Shadow @Final @Mutable private int minOccupationTicks;

    @Override
    public NbtCompound predations$getEntityData() {
        return this.entityData;
    }

    @Override
    public int predations$getMinOccupationTicks() {
        return this.minOccupationTicks;
    }

    @Override
    public void predations$setMinOccupationTicks(int ticks) {
        this.minOccupationTicks = ticks;
    }
}