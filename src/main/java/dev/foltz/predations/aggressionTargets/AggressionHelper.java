package dev.foltz.predations.aggressionTargets;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.registry.Registries;
import dev.foltz.predations.config.ConfigManager;
import dev.foltz.predations.mixin.entity.MobEntityAccessor;
import dev.foltz.predations.mixin.entity.GoalSelectorAccessor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.util.Identifier;

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

        if (preyIds != null && !preyIds.isEmpty()) {
            Set<String> normalizedPreyIds = normalizePreyIds(preyIds);
            if (goalSelector.getGoals().stream().noneMatch(pg -> pg.getGoal() instanceof MeleeAttackGoal)) {
                goalSelector.add(2, new MeleeAttackGoal(path, 1.2, true));
            }
            targetSelector.add(3, new PredationsHardTargetGoal(mob, normalizedPreyIds, 10));
        }
    }

    public static boolean matchesEntityId(LivingEntity e, Set<String> preyIds) {
        if (preyIds.isEmpty()) return false;
        Identifier id = Registries.ENTITY_TYPE.getId(e.getType());
        if (id == null) return false;
        String full = id.toString().toLowerCase();
        String path = id.getPath().toLowerCase();
        return preyIds.contains(full) || preyIds.contains(path);
    }
}