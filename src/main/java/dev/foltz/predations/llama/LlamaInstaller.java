package dev.foltz.predations.llama;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.mixin.entity.GoalSelectorAccessor;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.server.world.ServerWorld;

public final class LlamaInstaller {
    private LlamaInstaller() {}

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.isClient) return;
            handleLlamas(world);
        });
    }

    private static void handleLlamas(ServerWorld world) {
        for (var e : world.iterateEntities()) {
            if (!(e instanceof LlamaEntity llama)) continue;

            ExtraConfig.KickingLlamaConfig config = ExtraConfig.get().kickingLlama;
            if (!config.enabled) continue;

            MobEntity mob = llama;
            var selector = ((MobEntityAccessor) mob).getGoalSelector();
            var goals = ((GoalSelectorAccessor) selector).predations$getGoals();

            goals.removeIf(pg -> {
                var g = pg.getGoal();
                return g instanceof MeleeAttackGoal || g instanceof AttackGoal;
            });

            boolean hasKick = goals.stream().anyMatch(pg -> pg.getGoal() instanceof LlamaKickGoal);
            if (!hasKick) {
                selector.add(0, new LlamaKickGoal(mob));
            }
        }
    }
}