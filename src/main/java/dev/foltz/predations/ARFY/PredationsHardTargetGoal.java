package dev.foltz.predations.ARFY;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Hard targeter using config IDs instead of class reflection. */
public class PredationsHardTargetGoal extends Goal {
    private static final boolean DEBUG = false;

    private final MobEntity mob;
    private final Set<String> preyIds; // lowercased, e.g. "minecraft:zombie", "zombie"
    private final int recheckTicks;
    private int ticks;

    public PredationsHardTargetGoal(MobEntity mob, Set<String> preyIds, int recheckTicks) {
        this.mob = mob;
        this.preyIds = preyIds;
        this.recheckTicks = Math.max(2, recheckTicks);
        this.setControls(EnumSet.noneOf(Control.class));
    }

    @Override
    public boolean canStart() {
        if ((++ticks % recheckTicks) != 0) return false;
        LivingEntity prey = findNearestPrey();
        if (prey == null) return false;
        mob.setTarget(prey);
        mob.setAttacking(true);
        if (DEBUG) {
            System.out.println("[Predations][HardTarget] " + mob.getType() + " -> " + prey.getType());
        }
        return true;
    }

    @Override public boolean shouldContinue() { return false; }

    private LivingEntity findNearestPrey() {
        if (preyIds.isEmpty()) return null;

        double follow = 16.0;
        var inst = mob.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
        if (inst != null) follow = Math.max(follow, inst.getValue());

        Vec3d p = mob.getPos();
        Box box = Box.of(p, follow * 2, Math.max(2.0, follow), follow * 2);

        List<LivingEntity> list = mob.getWorld().getEntitiesByClass(
                LivingEntity.class, box,
                e -> e.isAlive() && e != mob && matchesEntityId(e)
        );
        if (list.isEmpty()) return null;

        LivingEntity best = null;
        double bestD2 = Double.MAX_VALUE;
        for (LivingEntity e : list) {
            double d2 = e.squaredDistanceTo(p.x, p.y, p.z);
            if (d2 < bestD2) { bestD2 = d2; best = e; }
        }
        return best;
    }

    private boolean matchesEntityId(LivingEntity e) {
        Identifier id = Registries.ENTITY_TYPE.getId(e.getType());
        if (id == null) return false;
        String full = id.toString().toLowerCase();
        String path = id.getPath().toLowerCase();
        return preyIds.contains(full) || preyIds.contains(path);
    }
}
