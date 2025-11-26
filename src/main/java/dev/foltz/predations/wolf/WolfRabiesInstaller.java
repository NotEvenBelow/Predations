package dev.foltz.predations.wolf;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.server.world.ServerWorld;

public final class WolfRabiesInstaller {
    private WolfRabiesInstaller() {}

    private static final String INFECTED_TAG = "predations.infected_with_rabies";

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(WolfRabiesInstaller::onEntitySpawn);
        System.out.println("[Predations][Init] WolfRabiesInstaller registered");
    }

    private static void onEntitySpawn(Entity entity, ServerWorld world) {
        if (!(entity instanceof WolfEntity wolf)) {
            return;
        }

        try {
            boolean alreadyInfected = wolf.getCommandTags().contains(INFECTED_TAG);
            boolean becomingInfected = false;

            if (!alreadyInfected) {
                double chance = ExtraConfig.getRabiesConfig().naturalAggressiveWolfSpawnChance;
                if (world.getRandom().nextDouble() < chance) {
                    becomingInfected = true;
                }
            }

            if (alreadyInfected || becomingInfected) {
                if (becomingInfected) {
                    wolf.addCommandTag(INFECTED_TAG);
                }

                wolf.setTamed(false);
                wolf.setSitting(false);
                wolf.setAngerTime(Integer.MAX_VALUE);

                ((MobEntityAccessor)wolf).getTargetSelector().add(0, new ActiveTargetGoal<>(
                        wolf,
                        LivingEntity.class,
                        10,
                        true,
                        false,
                        (target) -> {
                            if (target instanceof WolfEntity targetWolf) {
                                return !targetWolf.getCommandTags().contains(INFECTED_TAG);
                            }
                            return true;
                        }
                ));
            }
        } catch (Exception e) {
            System.err.println("[Predations] Error in WolfRabiesInstaller: " + e.getMessage());
        }
    }
}