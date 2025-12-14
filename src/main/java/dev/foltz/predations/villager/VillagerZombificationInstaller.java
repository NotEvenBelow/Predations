package dev.foltz.predations.villager;

import dev.foltz.predations.config.ExtraConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

public final class VillagerZombificationInstaller {
    private VillagerZombificationInstaller() {}

    private static final String ZOMBIFIED_TAG = "predations.zombified";

    private static boolean IS_CURING = false;

    public static void setIsCuring(boolean isCuring) {
        IS_CURING = isCuring;
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(VillagerZombificationInstaller::onEntitySpawn);

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ExtraConfig.VillagerConfig villagerConfig = ExtraConfig.getVillagerConfig();

            if (villagerConfig.disableCuring) {
                if (!player.getStackInHand(hand).isOf(Items.GOLDEN_APPLE)) {
                    return ActionResult.PASS;
                }

                if (entity instanceof ZombieVillagerEntity zombieVillager) {
                    if (zombieVillager.hasStatusEffect(StatusEffects.WEAKNESS)) {
                        return ActionResult.FAIL;
                    }
                }
            }

            return ActionResult.PASS;
        });

        System.out.println("[Predations][Init] VillagerZombificationInstaller registered");
    }

    private static void onEntitySpawn(Entity entity, ServerWorld world) {
        double zombifyChance = ExtraConfig.getVillagerConfig().zombificationChance;
        if (zombifyChance <= 0) {
            return;
        }

        if (entity instanceof VillagerEntity villager) {

            if (entity.getCommandTags().contains(ZOMBIFIED_TAG)) {
                return;
            }

            if (IS_CURING) {
                entity.addCommandTag(ZOMBIFIED_TAG);
                return;
            }

            if (world.getRandom().nextFloat() < zombifyChance) {
                entity.addCommandTag(ZOMBIFIED_TAG);
                villager.convertTo(EntityType.ZOMBIE_VILLAGER, true);
            }
        }
    }
}