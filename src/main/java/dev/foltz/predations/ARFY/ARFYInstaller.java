package dev.foltz.predations.ARFY;

import dev.foltz.predations.config.ConfigManager;
import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.consume.ConsumeNearbyItemsGoal;
import dev.foltz.predations.mixin.entity.GoalSelectorAccessor;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.*;

public final class ARFYInstaller {
    private static final WeakHashMap<LivingEntity, Float> LAST_HEALTH = new WeakHashMap<>();
    private static final String TAG_RUN_FROM_PLAYER   = "predations_run_from_player";
    private static final String TAG_RUN_FROM_HOSTILES = "predations_run_from_hostiles";
    private static final int AGGRO_REAPPLY_TICKS = 20;

    private ARFYInstaller() {}

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!world.isClient) tickWorld(world);
        });
        System.out.println("[Predations][Init] ARFYInstaller registered");
    }

    private static void tickWorld(ServerWorld world) {
        long now = world.getTime();

        // === A) Aggression ===
        if (now % AGGRO_REAPPLY_TICKS == 0) {
            for (var e : world.iterateEntities()) {
                if (!(e instanceof PathAwareEntity path)) continue;
                MobEntity mob = (MobEntity) path;

                Set<String> preyIds = ConfigManager.aggressionTargetsFor(mob);
                if (preyIds == null || preyIds.isEmpty()) continue;

                // normalize
                Set<String> lowered = new HashSet<>();
                for (String s : preyIds) {
                    if (s != null && !s.isBlank())
                        lowered.add(s.trim().toLowerCase(Locale.ROOT));
                }

                GoalSelector goalSel = ((MobEntityAccessor) mob).getGoalSelector();
                if (((GoalSelectorAccessor) goalSel).predations$getGoals()
                        .stream().noneMatch(pg -> pg.getGoal() instanceof MeleeAttackGoal))
                    goalSel.add(2, new MeleeAttackGoal(path, 1.2, true));

                GoalSelector targetSel = ((MobEntityAccessor) mob).getTargetSelector();
                ((GoalSelectorAccessor) targetSel).predations$getGoals()
                        .removeIf(pg -> pg.getGoal() instanceof PredationsHardTargetGoal);
                targetSel.add(3, new PredationsHardTargetGoal(mob, lowered, 10));
            }
        }

        // === B) flee / consume ===
        for (var e : world.iterateEntities()) {
            if (!(e instanceof PathAwareEntity path)) continue;
            MobEntity mob = (MobEntity) path;
            if (!ConfigManager.isEnabled(mob)) continue;

            // flee on attack
            if (ExtraConfig.fleeOnAttackEnabled() && !e.getCommandTags().contains(TAG_RUN_FROM_HOSTILES)) {
                GoalSelector gs = ((MobEntityAccessor) mob).getGoalSelector();
                var goals = ((GoalSelectorAccessor) gs).predations$getGoals();
                goals.removeIf(pg -> pg.getGoal().getClass().getName().equals("net.minecraft.entity.ai.goal.PanicGoal"));
                ExtraConfig.AngryMob angry = ExtraConfig.angryFor(e);
                float far  = angry != null && angry.panicFarSpeed != null ? angry.panicFarSpeed : 1.5f;
                float near = angry != null && angry.panicNearSpeed != null ? angry.panicNearSpeed : 2.8f;
                int dist   = angry != null && angry.panicDistance != null ? angry.panicDistance : 15;
                float ratio= angry != null && angry.panicRatio != null ? angry.panicRatio : 0.5f;
                gs.add(1, new RunAwayFromHostilesOnAttackGoal(path, far, near, dist, ratio));
                e.addCommandTag(TAG_RUN_FROM_HOSTILES);
            }

            // flee player
            if (ExtraConfig.fleeFromPlayerEnabled()) {
                var set = ((GoalSelectorAccessor) ((MobEntityAccessor) mob).getGoalSelector()).predations$getGoals();
                boolean has = set.stream().anyMatch(pg -> pg.getGoal() instanceof RunAwayGoal<?>);
                if (!has) {
                    net.minecraft.recipe.Ingredient ignore;
                    if (ConfigManager.allowLure(e)) {
                        List<net.minecraft.item.Item> list = ConfigManager.standardLureItems(e.getType());
                        ignore = list.isEmpty()
                                ? net.minecraft.recipe.Ingredient.EMPTY
                                : net.minecraft.recipe.Ingredient.ofItems(list.toArray(new net.minecraft.item.ItemConvertible[0]));
                    } else ignore = net.minecraft.recipe.Ingredient.EMPTY;

                    ((MobEntityAccessor) mob).getGoalSelector().add(
                            3,
                            new RunAwayGoal<>(
                                    path,
                                    PlayerEntity.class,
                                    ignore,
                                    ConfigManager.farSpeed(e),
                                    ConfigManager.nearSpeed(e),
                                    ConfigManager.distance(e),
                                    ConfigManager.ratio(e),
                                    ExtraConfig.fleeFromPlayerSafeMult(),
                                    ExtraConfig.fleeFromPlayerRepathCd(),
                                    ExtraConfig.fleeFromPlayerLinger()
                            )
                    );
                    e.addCommandTag(TAG_RUN_FROM_PLAYER);
                }
            }



            // remove tempt
            if (!ConfigManager.allowLure(e)) {
                var set = ((GoalSelectorAccessor) ((MobEntityAccessor) mob).getGoalSelector()).predations$getGoals();
                set.removeIf(pg -> pg.getGoal() instanceof TemptGoal);
            }

            // track attack memory
            float prev = LAST_HEALTH.getOrDefault(mob, mob.getHealth());
            float cur = mob.getHealth();
            if (cur < prev) {
                LivingEntity atk = mob.getAttacker();
                if (atk != null && atk.isAlive())
                    AttackMemory.mark(mob, now, ExtraConfig.fleeWindowTicks(), atk);
            }
            LAST_HEALTH.put(mob, cur);
        }
    }



    public static float fleeSafeMult() { return ExtraConfig.get().fleeOnAttack.safeDistanceMultiplier; }
    public static int fleeRepathCooldown() { return ExtraConfig.get().fleeOnAttack.repathCooldownTicks; }
}
