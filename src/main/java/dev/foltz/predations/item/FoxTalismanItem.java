package dev.foltz.predations.item;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.squid.HeadSuckable;
import dev.foltz.predations.squid.ai.HeadSuckTargeting; // [NEW IMPORT]
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Queue;

public class FoxTalismanItem extends Item {
    public FoxTalismanItem(Settings settings) {
        super(settings.maxCount(1)); // unstackable
        ServerTickEvents.END_SERVER_TICK.register(server -> tickTasks());
    }

    // --------
    private static final Queue<DelayedTask> TASKS = new ArrayDeque<>();
    private static void schedule(Runnable r, int delayTicks) { TASKS.add(new DelayedTask(r, delayTicks)); }
    private static void tickTasks() {
        var it = TASKS.iterator();
        while (it.hasNext()) {
            var t = it.next();
            if (--t.delay <= 0) { t.action.run(); it.remove(); }
        }
    }
    private static class DelayedTask { Runnable action; int delay; DelayedTask(Runnable a, int d){action=a;delay=d;} }
    // ----------------------------------------------------

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            var cfg = ExtraConfig.get().foxItems;
            if (!cfg.FoxTalismanFunctionEnabled) return TypedActionResult.pass(stack);

            user.getItemCooldownManager().set(this, Math.max(1, cfg.TalismanCooldowninSecond) * 20);

            int immunityTicks = 20 * ExtraConfig.get().foxItems.TalismanSquidImmunityTimeInSecond;
            TalismanImmunityTracker.setImmune(user, immunityTicks);

            cfg.TalismanEffects.forEach(entry -> {
                if ("squid_immunity".equals(entry.effectId)) {
                    return;
                }
                var effect = Registries.STATUS_EFFECT.get(new Identifier(entry.effectId));
                if (effect != null) {
                    user.addStatusEffect(new StatusEffectInstance(
                            effect,
                            entry.durationSeconds * 20,
                            entry.amplifier,
                            false, false, true
                    ));
                }
            });

            forceUnlatchNearbySquid(user, 8.0);

            // spaced glass sounds: 0, 0.3s, 0.6s
            for (int i = 0; i < 3; i++) {
                int delay = i * 6; // 6 ticks = 0.3s
                float pitch = 1.0f + 0.05f * i;
                schedule(() -> world.playSound(null, user.getBlockPos(),
                        SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0f, pitch), delay);
            }

            // durability
            int maxUses = Math.max(1, cfg.TalismanUseTime);
            stack.setDamage(stack.getDamage() + 1);

            // last-use texture swap using CustomModelData
            if (maxUses > 1) {
                if (stack.getDamage() == maxUses - 1) {
                    stack.getOrCreateNbt().putInt("CustomModelData", 1);
                } else {
                    stack.getOrCreateNbt().remove("CustomModelData");
                }
            } else {
                stack.getOrCreateNbt().remove("CustomModelData");
            }

            if (stack.getDamage() >= maxUses) {
                stack.decrement(1);
                user.sendToolBreakStatus(hand);
            }
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    private static void forceUnlatchNearbySquid(PlayerEntity user, double radius) {
        var world = user.getWorld();
        Box box = user.getBoundingBox().expand(radius);
        var squids = world.getEntitiesByClass(SquidEntity.class, box, s -> s.isAlive());

        for (var s : squids) {
            if (s instanceof HeadSuckable hs) {
                var tgt = hs.getTargetUuid();
                if (hs.isLatched() && tgt != null && tgt.equals(user.getUuid())) {
                    HeadSuckTargeting.releaseTarget(s, user, false);

                    hs.setLatched(false);
                    hs.setTongueActive(false);
                    hs.setTargetUuid(null);
                    s.setNoGravity(false);
                    s.getNavigation().stop();

                    s.addVelocity(0, 0.4, 0);
                    s.velocityModified = true;
                }
            }
        }
        user.setNoGravity(false);
    }
}