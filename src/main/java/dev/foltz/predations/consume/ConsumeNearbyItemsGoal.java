package dev.foltz.predations.consume;

import dev.foltz.predations.config.ConfigManager;
import dev.foltz.predations.config.ConfigManager.ConsumptionEffectEntry;
import static dev.foltz.predations.config.ConfigManager.getFullHealthBuffs;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.List;
import java.util.WeakHashMap;

public class ConsumeNearbyItemsGoal extends Goal {
    private static final WeakHashMap<PathAwareEntity, Long> LAST = new WeakHashMap<>();
    private final PathAwareEntity mob;
    private ItemEntity targetItem;

    public ConsumeNearbyItemsGoal(PathAwareEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.noneOf(Control.class));
    }

    @Override
    public boolean canStart() {
        if (!ConfigManager.consumeEnabled(mob)) return false;

        long now = mob.getWorld().getTime();
        long last = LAST.getOrDefault(mob, 0L);
        if ((now - last) < ConfigManager.consumeDelayTicks(mob)) return false;


        this.targetItem = findConsumable();
        return this.targetItem != null;
    }

    @Override
    public boolean shouldContinue() {
        return false; // One-shot action: Eat instantly, then finish.
    }

    @Override
    public void start() {
        if (targetItem == null || !targetItem.isAlive()) return;

        ItemStack stack = targetItem.getStack();
        Identifier iid = Registries.ITEM.getId(stack.getItem());

        if (!ConfigManager.isConsumableItem(mob, iid)) return;

        stack.decrement(1);
        if (stack.isEmpty()) targetItem.discard();

        if (mob.getHealth() < mob.getMaxHealth()) {
            float hearts = ConfigManager.consumeHealHearts(mob);
            mob.heal(Math.max(0f, hearts) * 2f);
        } else {
            List<ConsumptionEffectEntry> buffs = getFullHealthBuffs(mob);
            for (var entry : buffs) {
                if (entry.effectId == null || entry.effectId.isEmpty()) continue;

                StatusEffect effect = Registries.STATUS_EFFECT.get(new Identifier(entry.effectId));
                if (effect != null) {
                    mob.addStatusEffect(new StatusEffectInstance(
                            effect,
                            entry.durationSeconds * 20,
                            entry.amplifier,
                            false, true, true
                    ));
                }
            }
        }

        // --- SOUND ---
        mob.getWorld().playSound(
                null,
                mob.getBlockPos(),
                SoundEvents.ENTITY_GENERIC_EAT,
                SoundCategory.NEUTRAL,
                0.8f, 1.0f
        );

        // --- RESET ---
        LAST.put(mob, mob.getWorld().getTime());
        this.targetItem = null;
    }

    private ItemEntity findConsumable() {
        double r = ConfigManager.consumeRadius(mob);
        Box box = mob.getBoundingBox().expand(r, r, r);

        var list = mob.getWorld().getEntitiesByClass(ItemEntity.class, box, ie -> ie.isAlive() && !ie.getStack().isEmpty());

        for (ItemEntity ie : list) {
            ItemStack stack = ie.getStack();
            Identifier iid = Registries.ITEM.getId(stack.getItem());
            if (ConfigManager.isConsumableItem(mob, iid)) {
                return ie;
            }
        }
        return null;
    }
}