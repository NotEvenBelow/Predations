package dev.foltz.predations.item;

import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TalismanImmunityTracker {
    private static final Map<UUID, Integer> timers = new HashMap<>();

    public static void tick() {
        timers.replaceAll((id, time) -> time > 0 ? time - 1 : 0);
    }

    public static void setImmune(PlayerEntity player, int ticks) {
        timers.put(player.getUuid(), ticks);
    }

    public static boolean isImmune(PlayerEntity player) {
        return timers.getOrDefault(player.getUuid(), 0) > 0;
    }
}
