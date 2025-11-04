package dev.foltz.predations.mixin.entity;

import dev.foltz.predations.ARFY.PredationsHardTargetGoal;
import dev.foltz.predations.config.ConfigManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import java.util.Set;

@Mixin(MobEntity.class)
public abstract class MobAggressionMixin {
    private MobAggressionMixin(EntityType<?> type, World world) {}

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void predations$injectAggression(CallbackInfo ci) {
        MobEntity self = (MobEntity)(Object)this;
        Set<String> preyIds = ConfigManager.aggressionTargetsFor(self);
        if (preyIds == null || preyIds.isEmpty()) return;

        // skip tamed/sitting wolves
        if (self instanceof WolfEntity wolf && (wolf.isTamed() || wolf.isSitting())) return;

        GoalSelector goalSel = ((MobEntityAccessor) self).getGoalSelector();
        GoalSelector targetSel = ((MobEntityAccessor) self).getTargetSelector();

        // ensure melee goal
        if (self instanceof PathAwareEntity pathAware) {
            boolean hasMelee = ((GoalSelectorAccessor) goalSel).predations$getGoals()
                    .stream().anyMatch(pg -> pg.getGoal() instanceof MeleeAttackGoal);
            if (!hasMelee) goalSel.add(2, new MeleeAttackGoal(pathAware, 1.2, true));
        }

        Set<String> lowered = preyIds.stream()
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());

        ((GoalSelectorAccessor) targetSel).predations$getGoals()
                .removeIf(pg -> pg.getGoal() instanceof PredationsHardTargetGoal);

        targetSel.add(3, new PredationsHardTargetGoal(self, lowered, 10));

        Identifier id = Registries.ENTITY_TYPE.getId(self.getType());
        System.out.println("[Predations][Aggro] attached to " + (id != null ? id : "unknown") + " prey=" + lowered);
    }
}
