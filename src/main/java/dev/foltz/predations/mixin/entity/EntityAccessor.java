package dev.foltz.predations.mixin.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("stepHeight") float predations$getStepHeight();
    @Accessor("stepHeight") void predations$setStepHeight(float value);
}
