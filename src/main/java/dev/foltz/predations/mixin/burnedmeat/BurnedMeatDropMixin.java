package dev.foltz.predations.mixin.burnedmeat;

import dev.foltz.predations.item.BurnedMeatHelper;
import dev.foltz.predations.item.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class BurnedMeatDropMixin {
    @Inject(
            method = "drop(Lnet/minecraft/entity/damage/DamageSource;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void predations$burnedMeatOnly(DamageSource source, CallbackInfo ci) {
        if (!BurnedMeatHelper.isEnabled()) return;
        if (source == null || !source.isIn(DamageTypeTags.IS_FIRE)) return;

        LivingEntity self = (LivingEntity)(Object)this;
        var cfg = BurnedMeatHelper.getEntry(self);
        if (cfg == null) return;

        ci.cancel();
        Random rng = self.getRandom();
        int count = BurnedMeatHelper.rollCount(cfg, rng);
        for (int i = 0; i < count; i++) {
            self.dropItem(ModItems.BURNED_MEAT);
        }
    }
}
