package dev.foltz.predations.mixin.guanoAndBoneMeal;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PhantomEntity.class)
public class PhantomEntityMixin extends FlyingEntity {

    protected PhantomEntityMixin(EntityType<? extends FlyingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "mobTick", at = @At("HEAD"), cancellable = true)
    private void handlePhantomLogic(CallbackInfo ci) {
        ExtraConfig.BatGuanoAndBoneMealConfig config = ExtraConfig.getBatGuanoConfig();

        if (!config.enabled) return;

        if (config.phantomCanLeash) {
            Entity holder = this.getHoldingEntity();
            if (holder != null) {
                ci.cancel(); // Stop AI
                double dist = this.distanceTo(holder);
                if (dist > 5.0) {
                    Vec3d pull = holder.getPos().subtract(this.getPos()).normalize().multiply(0.4);
                    Vec3d currentVel = this.getVelocity();
                    this.setVelocity(currentVel.add(pull).multiply(0.9));
                }
            }
        }

        if (!this.getWorld().isClient) {
            if (this.age > 0 && this.age % config.phantomGuanoRollIntervalInTick == 0) {
                if (this.random.nextDouble() < config.phantomGuanoDropChance) {
                    this.dropStack(new ItemStack(ModItems.GUANO));
                    this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 0.8F, 0.5F);
                }
            }
        }
    }
}