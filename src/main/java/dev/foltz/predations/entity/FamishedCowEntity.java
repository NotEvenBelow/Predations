package dev.foltz.predations.entity;

import dev.foltz.predations.config.ExtraConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class FamishedCowEntity extends CowEntity {
    public FamishedCowEntity(EntityType<? extends CowEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        double baseHp = 16.0;
        double baseSpeed = resolveFamishedBaseSpeed();

        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, baseHp)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, baseSpeed)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    private static double resolveFamishedBaseSpeed() {
        var extras = ExtraConfig.get();
        var rare = extras.rareVariants;

        double base;
        if (rare.famishedCowBaseSpeed != null) {
            base = rare.famishedCowBaseSpeed;
        } else {
            var angry = extras.angryMobs.entities.get("cow");
            if (angry != null && angry.panicNearSpeed != null) {
                base = angry.panicNearSpeed * 0.2;
            } else {
                base = 0.25;
            }
        }
        return Math.max(0.1, base * rare.famishedCowSpeedMultiplier);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.3, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(
                this, LivingEntity.class, 10, true, true,
                target -> !(target instanceof CowEntity)
        ));
    }

    // === replacement logic ===
    public static void registerSpawnReplacement(EntityType<FamishedCowEntity> type) {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            // server-only; event supplies ServerWorld already
            var rare = ExtraConfig.get().rareVariants;
            if (!rare.famishedCowEnabled) return;

            // only vanilla cow; do NOT replace mooshroom
            if (entity.getType() != EntityType.COW) return;
            if (entity instanceof FamishedCowEntity) return;

            double chance = Math.max(0.0, Math.min(1.0, rare.famishedCowChance));
            if (world.random.nextDouble() >= chance) return;

            var famished = type.create(world);
            if (famished == null) return;

            famished.refreshPositionAndAngles(
                    entity.getX(), entity.getY(), entity.getZ(),
                    entity.getYaw(), entity.getPitch()
            );
            entity.discard();
            world.spawnEntity(famished);
        });
    }
}
