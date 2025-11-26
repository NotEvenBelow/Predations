package dev.foltz.predations.mixin.fox;

import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FoxEntity.class)
public interface FoxEntityAccessor {
    @Invoker("setSleeping")
    void invokeSetSleeping(boolean sleeping);

    @Invoker("setAggressive")
    void invokeSetAggressive(boolean aggressive);
}