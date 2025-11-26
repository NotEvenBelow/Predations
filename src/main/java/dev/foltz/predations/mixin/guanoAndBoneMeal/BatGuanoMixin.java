package dev.foltz.predations.mixin.guanoAndBoneMeal;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BatEntity.class)
public abstract class BatGuanoMixin extends AmbientEntity {

    protected BatGuanoMixin(EntityType<? extends AmbientEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "mobTick", at = @At("HEAD"))
    private void handleGuanoDrop(CallbackInfo ci) {
        if (this.getWorld().isClient) return;

        ExtraConfig.BatGuanoAndBoneMealConfig config = ExtraConfig.getBatGuanoConfig();
        if (!config.enabled) return;

        if (this.age > 0 && this.age % config.batGuanoRollIntervalInTick == 0) {
            if (this.random.nextDouble() < config.batGuanoDropChance) {
                this.dropStack(new ItemStack(ModItems.GUANO));
                this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 0.5F, 1.5F); // Small plop sound
            }
        }
    }
}