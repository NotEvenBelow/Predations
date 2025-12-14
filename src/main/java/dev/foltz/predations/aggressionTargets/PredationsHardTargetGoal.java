package dev.foltz.predations.aggressionTargets;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.util.Set;

public class PredationsHardTargetGoal extends ActiveTargetGoal<LivingEntity> {

    private int unseenTicks = 0;

    public PredationsHardTargetGoal(MobEntity mob, Set<String> preyIds, int reciprocalChance) {

        super(mob, LivingEntity.class, reciprocalChance, true, true, (target) -> isPrey(target, preyIds));
    }

    @Override
    public void start() {
        super.start();
        this.unseenTicks = 0;
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return false;


        if (!super.shouldContinue()) return false;

        if (target instanceof PlayerEntity) return false;

        if (this.mob.getVisibilityCache().canSee(target)) {
            this.unseenTicks = 0;
        } else {
            if (++this.unseenTicks > 60) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void stop() {
        super.stop();
        if (this.mob.getTarget() != null && !(this.mob.getTarget() instanceof PlayerEntity)) {
            this.mob.setTarget(null);
        }
        this.unseenTicks = 0;
    }

    private static boolean isPrey(LivingEntity target, Set<String> preyIds) {
        if (preyIds == null || preyIds.isEmpty()) return false;
        Identifier id = Registries.ENTITY_TYPE.getId(target.getType());
        if (id == null) return false;

        String full = id.toString().toLowerCase();
        String path = id.getPath().toLowerCase();
        return preyIds.contains(full) || preyIds.contains(path);
    }
}