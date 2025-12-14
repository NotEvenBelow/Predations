package dev.foltz.predations.pufferfish;

import dev.foltz.predations.PredationsMod;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PufferfishJackpotAdvancement {
    public static final Identifier ADVANCEMENT_ID = new Identifier(PredationsMod.MODID, "pufferfish_jackpot");

    public static void grant(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            System.err.println("Cannot grant advancement: Player server is null.");
            return;
        }

        Advancement advancement = server.getAdvancementLoader().get(ADVANCEMENT_ID);
        if (advancement == null) {
            System.err.println("Could not find advancement: " + ADVANCEMENT_ID);
            return;
        }

        AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);

        if (!progress.isDone()) {
            for (String criterion : progress.getUnobtainedCriteria()) {
                player.getAdvancementTracker().grantCriterion(advancement, criterion);
            }
        }
    }
}