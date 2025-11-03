package dev.foltz.predations.mixin.fox;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes foxes periodically drop FOX_FEATHER like chickens lay eggs.
 * Uses ExtraConfig.get().foxItems.FoxFeatherDropRollTickInterval and FoxFeatherDropChance.
 */
@Mixin(FoxEntity.class)
public abstract class FoxEntityFeatherMixin extends AnimalEntity {
    @Unique private int predations$featherTimer;

    protected FoxEntityFeatherMixin(EntityType<? extends AnimalEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void predations$initTimer(EntityType<? extends FoxEntity> type, World world, CallbackInfo ci) {
        predations$resetTimer();
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void predations$tickFeather(CallbackInfo ci) {
        // server-side only
        if (this.getWorld().isClient()) return;

        var cfgRoot = ExtraConfig.get();
        if (cfgRoot == null || cfgRoot.foxItems == null) return;
        var cfg = cfgRoot.foxItems;

        // baby foxes don’t lay; dead or sleeping fine either way—adjust if you want
        if (this.isBaby() || !this.isAlive()) return;

        // countdown
        if (--predations$featherTimer <= 0) {
            predations$resetTimer();

            // roll chance (cfg is in %, e.g. 5.0 = 5%)
            if (this.getRandom().nextDouble() * 100.0 < cfg.FoxFeatherDropChance) {
                // sound like chicken egg (use your own if you prefer)
                this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 0.3F,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.2F + 1.0F);

                // drop the item
                this.dropItem(ModItems.FOX_FEATHER);
            }
        }
    }

    @Unique
    private void predations$resetTimer() {
        var cfgRoot = ExtraConfig.get();
        int interval = 1200; // fallback ~1 minute
        if (cfgRoot != null && cfgRoot.foxItems != null) {
            interval = Math.max(1, cfgRoot.foxItems.FoxFeatherDropRollTickInterval);
        }
        predations$featherTimer = interval;
    }
}
