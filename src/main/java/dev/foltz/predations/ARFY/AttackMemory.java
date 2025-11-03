package dev.foltz.predations.ARFY;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public final class AttackMemory {
    private static final class Record {
        long untilTick;
        WeakReference<LivingEntity> attackerRef;
        Vec3d lastKnownPos;
        long lastUpdateTick;
    }
    private static final Map<LivingEntity, Record> MAP = new WeakHashMap<>();
    private AttackMemory() {}

    public static void mark(LivingEntity prey, long nowTick, int windowTicks, LivingEntity attacker) {
        if (prey == null || attacker == null) return;
        Record r = MAP.computeIfAbsent(prey, k -> new Record());
        r.untilTick = nowTick + Math.max(1, windowTicks);
        r.attackerRef = new WeakReference<>(attacker);
        r.lastKnownPos = attacker.getPos();
        r.lastUpdateTick = nowTick;
    }

    public static boolean isActive(LivingEntity prey, long nowTick) {
        Record r = MAP.get(prey);
        return r != null && nowTick <= r.untilTick;
    }

    public static Vec3d attackerPos(LivingEntity prey, long nowTick) {
        Record r = MAP.get(prey);
        return (r != null) ? r.lastKnownPos : null;
    }

    public static void maybeRefresh(LivingEntity prey, long nowTick, int cooldownTicks) {
        Record r = MAP.get(prey);
        if (r == null) return;
        if (nowTick - r.lastUpdateTick < cooldownTicks) return;
        LivingEntity a = r.attackerRef != null ? r.attackerRef.get() : null;
        if (a != null && a.isAlive()) {
            r.lastKnownPos = a.getPos();
            r.lastUpdateTick = nowTick;
        }
    }
}
