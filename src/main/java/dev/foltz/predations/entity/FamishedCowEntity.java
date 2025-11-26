package dev.foltz.predations.entity;

import dev.foltz.predations.config.ExtraConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
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
            base = 0.25;
        }
        return Math.max(0.1, base * rare.famishedCowSpeedMultiplier);
    }

    @Override
    protected void initGoals() {
        // --- NEW LINGER AI (Idle Behavior) ---
        this.goalSelector.add(0, new SwimGoal(this));
        // Priority 5: Wander. If not attacking, it will walk around.
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
        // Priority 6: Watch Player.
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        // Priority 7: Idle Look.
        this.goalSelector.add(7, new LookAroundGoal(this));
        // --- END NEW LINGER AI ---

        // --- AGGRESSION AI ---
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.3, true));

        // TARGETING:
        // 1. Revenge: Always fight back if hit (Priority 1)
        this.targetSelector.add(1, new RevengeGoal(this));

        // 2. Hunt: Find targets.
        // Changed '10' to '40' (Reciprocal Chance).
        // This reduces how often it scans for new targets, allowing it to "linger" more often between hunts.
        this.targetSelector.add(2, new ActiveTargetGoal<>(
                this, LivingEntity.class, 40, true, true,
                target -> !(target instanceof CowEntity) // Don't eat other cows
        ));
    }

    // === Replacement Logic ===
    public static void registerSpawnReplacement(EntityType<FamishedCowEntity> type) {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            var rare = ExtraConfig.get().rareVariants;
            if (!rare.famishedCowEnabled) return;

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