package dev.foltz.predations.aggressionTargets;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;

import dev.foltz.predations.config.ConfigManager;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import dev.foltz.predations.mixin.entity.GoalSelectorAccessor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class AggressionHelper {
    private AggressionHelper() {}

    public static Set<String> normalizePreyIds(Collection<String> ids) {
        Set<String> normalized = new HashSet<>();
        for (String s : ids) {
            if (s != null && !s.isBlank()) {
                normalized.add(s.trim().toLowerCase(Locale.ROOT));
            }
        }
        return normalized;
    }

    public static void installAggressionGoals(PathAwareEntity path, MobEntity mob) {
        Set<String> preyIds = ConfigManager.aggressionTargetsFor(mob);

        GoalSelector targetSelector = ((MobEntityAccessor) mob).getTargetSelector();
        GoalSelector goalSelector = ((MobEntityAccessor) mob).getGoalSelector();

        ((GoalSelectorAccessor) targetSelector).predations$getGoals()
                .removeIf(pg -> pg.getGoal() instanceof PredationsHardTargetGoal);

        // 2. Ensure Passive Mobs have an Attack Goal
        if (!(mob instanceof HostileEntity)) {
            boolean hasAttack = goalSelector.getGoals().stream()
                    .anyMatch(pg -> pg.getGoal() instanceof MeleeAttackGoal);

            if (!hasAttack) {
                goalSelector.add(2, new MeleeAttackGoal(path, 1.2, true));
            }
        }

        boolean hasWander = goalSelector.getGoals().stream()
                .anyMatch(pg -> pg.getGoal() instanceof WanderAroundGoal);

        if (!hasWander) {
            goalSelector.add(7, new WanderAroundFarGoal(path, 1.0));
        }

        if (preyIds != null && !preyIds.isEmpty()) {
            Set<String> normalizedPreyIds = normalizePreyIds(preyIds);

            targetSelector.add(2, new PredationsHardTargetGoal(mob, normalizedPreyIds, 20));
        }
    }
}