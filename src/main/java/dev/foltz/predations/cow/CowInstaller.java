package dev.foltz.predations.cow;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.mixin.entity.GoalSelectorAccessor;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
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
            MobEntity mob = cow; // safe cast

            // ---------------- angry health sync ----------------
            if (ExtraConfig.angryEnabled()) {
                var angry = ExtraConfig.angryFor(mob);
                if (angry != null && angry.enabled) {
                    EntityAttributeInstance maxHpAttr = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                    if (maxHpAttr != null) {
                        double wantMax = Math.max(1.0, angry.maxHearts * 2.0);
                        if (Math.abs(maxHpAttr.getBaseValue() - wantMax) > 1e-6) {
                            maxHpAttr.setBaseValue(wantMax);
                            mob.setHealth((float) wantMax);
                            mob.timeUntilRegen = 0;
                        }
                    }
                }
            }

            // ---------------- panic kick ----------------
            var goals = ((GoalSelectorAccessor) ((MobEntityAccessor) mob).getGoalSelector()).predations$getGoals();
            boolean hasKick = goals.stream().anyMatch(pg -> pg.getGoal() instanceof PanicKickGoal);
            if (!hasKick && ExtraConfig.angryFor(mob) != null) {
                ((MobEntityAccessor) mob).getGoalSelector().add(1, new PanicKickGoal(mob));
            }

            // ---------------- panic flee overrides ----------------
            var angry = ExtraConfig.angryFor(mob);
            if (angry != null && angry.enabled) {
                float far   = angry.panicFarSpeed   != null ? angry.panicFarSpeed   : 1.5f;
                float near  = angry.panicNearSpeed  != null ? angry.panicNearSpeed  : 2.8f;
                int   dist  = angry.panicDistance   != null ? angry.panicDistance   : 15;
                float ratio = angry.panicRatio      != null ? angry.panicRatio      : 0.5f;

                // remove vanilla panic goal
                goals.removeIf(pg -> pg.getGoal().getClass().getSimpleName().equals("PanicGoal"));

                // inject custom flee
                ((MobEntityAccessor) mob).getGoalSelector().add(
                        2,
                        new dev.foltz.predations.ARFY.RunAwayFromHostilesOnAttackGoal((net.minecraft.entity.mob.PathAwareEntity) mob, far, near, dist, ratio)
                );
            }
        }
    }
}
