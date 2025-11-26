/** ritual*, function inside item/ModItem.java, again wtf is wrong with my file organization **/
package dev.foltz.predations.rirtualNetwork;

import dev.foltz.predations.PredationsMod;
import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.item.ModItems;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

public class ModPackets {
    public static final Identifier LIFE_RITUAL_PACKET_ID = new Identifier(PredationsMod.MODID, "life_ritual_use");

    public static void registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(LIFE_RITUAL_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            String targetName = buf.readString();

            server.execute(() -> {
                ExtraConfig.VillagerConfig config = ExtraConfig.getVillagerConfig();
                if (config == null || !config.LifeRitualFunctionEnabled) {
                    player.sendMessage(Text.literal("Action Failed: Life Ritual is disabled on this server."), false);
                    return;
                }

                if (!player.getMainHandStack().isOf(ModItems.LIFE_RITUAL) && !player.getOffHandStack().isOf(ModItems.LIFE_RITUAL)) {
                    return;
                }

                ServerPlayerEntity targetPlayer = server.getPlayerManager().getPlayer(targetName);

                boolean success = false;

                if (targetPlayer != null) {
                    if (targetPlayer.interactionManager.getGameMode() == GameMode.SPECTATOR) {
                        targetPlayer.changeGameMode(GameMode.SURVIVAL);
                        targetPlayer.sendMessage(Text.literal("You have been revived by " + player.getName().getString() + "!"), false);
                        player.sendMessage(Text.literal("Life Ritual successful. " + targetName + " has been revived."), true);
                        success = true;
                    } else {
                        player.sendMessage(Text.literal("Life Ritual has failed to be consumed: Target is not in Spectator mode."), false);
                    }
                } else {
                    player.sendMessage(Text.literal("Life Ritual has failed to be consumed: Player '" + targetName + "' not found."), false);
                }

                if (success && !player.isCreative()) {
                    if (player.getMainHandStack().isOf(ModItems.LIFE_RITUAL)) {
                        player.getMainHandStack().decrement(1);
                    } else if (player.getOffHandStack().isOf(ModItems.LIFE_RITUAL)) {
                        player.getOffHandStack().decrement(1);
                    }
                }
            });
        });
    }
}