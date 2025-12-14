package dev.foltz.predations.mixin.golem;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(MeleeAttackGoal.class)
public abstract class IronGolemReachMixin {

    private static final Field MOB_FIELD;

    static {
        Field foundField = null;
        try {
            for (Field f : MeleeAttackGoal.class.getDeclaredFields()) {
                if (MobEntity.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    foundField = f;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[Predations] Failed to reflect Mob field in MeleeAttackGoal.");
            e.printStackTrace();
        }
        MOB_FIELD = foundField;
    }

    @Inject(method = "getSquaredMaxAttackDistance", at = @At("RETURN"), cancellable = true)
    private void extendGolemReach(LivingEntity target, CallbackInfoReturnable<Double> cir) {
        if (MOB_FIELD == null) return;

        try {
            Object rawMob = MOB_FIELD.get(this);
            if (rawMob == null) return;
            if (!(rawMob instanceof IronGolemEntity)) return;

            ExtraConfig.VillagerConfig config = ExtraConfig.getVillagerConfig();
            if (config == null) return;
            if (!config.ironGolemChangeEnabled) return;

            double vanillaReachSq = cir.getReturnValue();
            double verticalRange = config.ironGolemReachRangeVertically;

            MobEntity mob = (MobEntity) rawMob;

            double yDiff = Math.abs(mob.getY() - target.getY());

            if (yDiff <= verticalRange) {
                if (mob.canSee(target)) {
                    double newReachSq = vanillaReachSq + (yDiff * yDiff) + 2.0;
                    cir.setReturnValue(newReachSq);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}