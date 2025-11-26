package dev.foltz.predations.entity;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.item.ModItems; // <-- ADDED IMPORT
import dev.foltz.predations.mixin.villager.ZombieVillagerEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class ThrownPotionOfCuringEntity extends ThrownItemEntity {

    public ThrownPotionOfCuringEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public ThrownPotionOfCuringEntity(World world, LivingEntity owner) {
        super(ModEntities.THROWN_POTION_OF_CURING, owner, world);
    }

    public ThrownPotionOfCuringEntity(World world, double x, double y, double z) {
        super(ModEntities.THROWN_POTION_OF_CURING, x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        // --- FIX: Return the custom item, not vanilla SPLASH_POTION ---
        return ModItems.SPLASH_POTION_OF_CURING;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (this.getWorld().isClient) {
            return;
        }

        // Get config
        double range = ExtraConfig.getVillagerConfig().customCureSplashRange;

        // Play splash sound
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_SPLASH_POTION_BREAK, this.getSoundCategory(), 1.0f, this.getWorld().random.nextFloat() * 0.1f + 0.9f);

        // Spawn particles
        this.getWorld().createExplosion(null, this.getX(), this.getY(), this.getZ(), 0.0f, World.ExplosionSourceType.NONE);

        // Apply curing effect
        this.applyCureSplash(range);

        this.discard();
    }

    private void applyCureSplash(double range) {
        Box box = this.getBoundingBox().expand(range, range / 2.0, range);
        List<ZombieVillagerEntity> list = this.getWorld().getNonSpectatingEntities(ZombieVillagerEntity.class, box);

        UUID ownerUuid = this.getOwner() != null ? this.getOwner().getUuid() : null;

        if (!list.isEmpty()) {
            for (ZombieVillagerEntity zombieVillager : list) {
                // Check if it's not already curing
                if (!zombieVillager.isConverting()) {

                    ((ZombieVillagerEntityAccessor) zombieVillager).invokeSetConverting(
                            ownerUuid,
                            this.getWorld().random.nextInt(2401) + 3600
                    );

                    this.getWorld().sendEntityStatus(zombieVillager, (byte) 16);
                }
            }
        }
    }
}