package dev.foltz.predations.pufferfish;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.WeakHashMap;

public class PufferfishJackpotTracker {
    private static final WeakHashMap<ServerPlayerEntity, Boolean> jackpotMap = new WeakHashMap<>();

    public static void setJackpot(ServerPlayerEntity player, boolean value) {
        jackpotMap.put(player, value);
    }

    public static boolean didJackpot(ServerPlayerEntity player) {
        return jackpotMap.getOrDefault(player, false);
    }

    public static void remove(ServerPlayerEntity player) {
        jackpotMap.remove(player);
    }
}
