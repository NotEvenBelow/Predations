package dev.foltz.predations.consume;

import dev.foltz.predations.config.ConfigManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.WeakHashMap;

public class ConsumeNearbyItemsGoal extends Goal {
    private static final WeakHashMap<PathAwareEntity, Long> LAST = new WeakHashMap<>();
    private final PathAwareEntity mob;

    public ConsumeNearbyItemsGoal(PathAwareEntity mob) {
        this.mob = mob;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (!ConfigManager.consumeEnabled(mob)) return false;
        long now = mob.getWorld().getTime();
        long last = LAST.getOrDefault(mob, 0L);
        return (now - last) >= ConfigManager.consumeDelayTicks(mob);
    }

    @Override
    public void start() {
        double r = ConfigManager.consumeRadius(mob);
        Box box = mob.getBoundingBox().expand(r, r, r);

        var list = mob.getWorld().getEntitiesByClass(ItemEntity.class, box, ie -> ie.isAlive() && !ie.getStack().isEmpty());
        if (list.isEmpty()) return;

        for (ItemEntity ie : list) {
            ItemStack stack = ie.getStack();
            Identifier iid = Registries.ITEM.getId(stack.getItem());
            if (!ConfigManager.isConsumableItem(mob, iid)) continue;

            // consume exactly 1
            stack.decrement(1);
            if (stack.isEmpty()) ie.discard();

            if (mob.getHealth() < mob.getMaxHealth()) {
                // heal if not full
                float hearts = ConfigManager.consumeHealHearts(mob);
                mob.heal(Math.max(0f, hearts) * 2f);
            } else {
                // already full health → give buffs
                mob.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 800, 0)); // 10s, level 1
                mob.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 800, 0));    // 10s, level 1
            }

            // sound
            mob.getWorld().playSound(
                    null,
                    mob.getBlockPos(),
                    SoundEvents.ENTITY_GENERIC_EAT,
                    SoundCategory.NEUTRAL,
                    0.8f, 1.0f
            );

            LAST.put(mob, mob.getWorld().getTime());
            break;
        }
    }

    @Override
    public boolean shouldContinue() {
        return false;
    }
}
