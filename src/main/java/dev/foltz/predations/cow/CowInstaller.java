package dev.foltz.predations.cow;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.mixin.entity.GoalSelectorAccessor;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.server.world.ServerWorld;

public final class CowInstaller {
    private CowInstaller() {}

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.isClient) return;
            handleCows(world);
        });
    }

    private static void handleCows(ServerWorld world) {
        for (var e : world.iterateEntities()) {
            if (!(e instanceof CowEntity cow)) continue;
            MobEntity mob = cow;

            if (ExtraConfig.angryEnabled()) {
                var angry = ExtraConfig.angryFor(mob);
                if (angry != null && angry.enabled) {
                    EntityAttributeInstance maxHpAttr = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                    if (maxHpAttr != null) {
                        double defaultHearts = 9.0;
                        double hearts = (angry.maxHearts != null) ? angry.maxHearts : defaultHearts;
                        double wantMax = Math.max(1.0, hearts * 2.0);

                        if (Math.abs(maxHpAttr.getBaseValue() - wantMax) > 1e-6) {
                            maxHpAttr.setBaseValue(wantMax);
                            mob.setHealth((float) wantMax);
                            mob.timeUntilRegen = 0;
                        }
                    }
                }
            }

            var goals = ((GoalSelectorAccessor) ((MobEntityAccessor) mob).getGoalSelector()).predations$getGoals();

            boolean hasKick = goals.stream().anyMatch(pg -> pg.getGoal() instanceof PanicKickGoal);
            if (!hasKick && ExtraConfig.angryFor(mob) != null) {
                ((MobEntityAccessor) mob).getGoalSelector().add(1, new dev.foltz.predations.cow.PanicKickGoal(mob));
            }

            var angry = ExtraConfig.angryFor(mob);
            if (angry != null && angry.enabled) {
                double speed = (angry.runSpeed != null) ? angry.runSpeed : 1.5;

                final int OUR_FLEE_PRIORITY = 0;

                boolean hasOurFleeGoal = goals.stream()
                        .anyMatch(pg -> pg.getPriority() == OUR_FLEE_PRIORITY && pg.getGoal() instanceof EscapeDangerGoal);

                if (!hasOurFleeGoal) {
                    goals.removeIf(pg -> pg.getGoal() instanceof EscapeDangerGoal);

                    ((MobEntityAccessor) mob).getGoalSelector().add(
                            OUR_FLEE_PRIORITY,
                            new EscapeDangerGoal((PathAwareEntity) mob, speed)
                    );
                }
            }
        }
    }
}