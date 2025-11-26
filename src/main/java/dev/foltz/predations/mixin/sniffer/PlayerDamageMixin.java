package dev.foltz.predations.mixin.sniffer;

import dev.foltz.predations.access.DamageTrackerAccess;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerDamageMixin implements DamageTrackerAccess {
    @Unique
    private long lastDamageTime = -100000;

    @Override
    public long predations$getLastDamageTime() {
        return this.lastDamageTime;
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void onDamageTaken(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            this.lastDamageTime = ((PlayerEntity)(Object)this).getWorld().getTime();
        }
    }
}