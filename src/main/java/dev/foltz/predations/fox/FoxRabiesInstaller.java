package dev.foltz.predations.fox;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.mixin.entity.GoalSelectorAccessor;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import dev.foltz.predations.mixin.fox.FoxEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class FoxRabiesInstaller {
    private FoxRabiesInstaller() {}

    private static final String INFECTED_TAG = "predations.infected_with_rabies";

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(FoxRabiesInstaller::onEntitySpawn);
        System.out.println("[Predations][Init] FoxRabiesInstaller registered");
    }

    private static void onEntitySpawn(Entity entity, ServerWorld world) {
        if (!(entity instanceof FoxEntity fox)) {
            return;
        }

        try {
            // 1. Determine Infection Status
            boolean alreadyInfected = fox.getCommandTags().contains(INFECTED_TAG);
            boolean becomingInfected = false;

            // Only roll for new infection if not already infected
            if (!alreadyInfected) {
                double chance = ExtraConfig.getRabiesConfig().naturalAggressiveFoxSpawnChance;
                if (world.getRandom().nextDouble() < chance) {
                    becomingInfected = true;
                }
            }

            // 2. Apply Logic if Infected (Existing OR New)
            if (alreadyInfected || becomingInfected) {

                // Apply tag if it's new
                if (becomingInfected) {
                    fox.addCommandTag(INFECTED_TAG);
                }

                // --- FORCE STATES ---
                fox.setSitting(false);
                ((FoxEntityAccessor) fox).invokeSetSleeping(false);
                fox.setCrouching(false);
                ((FoxEntityAccessor) fox).invokeSetAggressive(true);

                // --- PURGE COWARDICE (This now runs for ALL infected foxes) ---
                GoalSelector goalSelector = ((MobEntityAccessor)fox).getGoalSelector();
                Set<PrioritizedGoal> goals = ((GoalSelectorAccessor)goalSelector).predations$getGoals();

                List<PrioritizedGoal> toRemove = new ArrayList<>();
                for (PrioritizedGoal pg : goals) {
                    Goal g = pg.getGoal();
                    String className = g.getClass().getName();
                    // Check for Fleeing, Panic, and Avoidance
                    if (g instanceof FleeEntityGoal || g instanceof EscapeDangerGoal ||
                            className.contains("Avoid") || className.contains("Panic") ||
                            className.contains("Sit") || className.contains("Sleep")) {
                        toRemove.add(pg);
                    }
                }

                // Remove the peaceful goals
                for (PrioritizedGoal pg : toRemove) {
                    goalSelector.remove(pg.getGoal());
                }


                goalSelector.add(1, new MeleeAttackGoal(fox, 0.6, true));

                // Priority 0: Target Selection
                ((MobEntityAccessor)fox).getTargetSelector().add(0, new ActiveTargetGoal<>(
                        fox,
                        LivingEntity.class,
                        10,
                        true,
                        false,
                        (target) -> {
                            if (target instanceof FoxEntity targetFox) {
                                return !targetFox.getCommandTags().contains(INFECTED_TAG);
                            }
                            return true;
                        }
                ));
            }
        } catch (Exception e) {
            System.err.println("[Predations] Error in FoxRabiesInstaller: " + e.getMessage());
        }
    }
}