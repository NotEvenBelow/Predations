package dev.foltz.predations.cow;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;

public class PanicKickGoal extends net.minecraft.entity.ai.goal.Goal {

    private final MobEntity mob;
    private long activeUntil = 0L;
    private long nextKick = 0L;
    private float lastHealth = -1f;

    public PanicKickGoal(MobEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!mob.isAlive()) return false;
        if (mob instanceof PassiveEntity pe && pe.isBaby()) return false;

        var angry = ExtraConfig.angryFor(mob);
        if (!ExtraConfig.angryEnabled() || angry == null || !angry.enabled) {
            syncHealth();
            return false;
        }

        long now = mob.getWorld().getTime();

        // 1. Check Physical Damage (Health Drop)
        detectDamageAndRefreshWindow();

        // 2. RESTORED: Check AttackMemory (Milking Trigger)
        // If AttackMemory is active, ensure the kick window is open for at least 20 ticks
        if (AttackMemory.isActive(mob, now)) {
            // We set a minimum active time to allow the goal to start
            if (activeUntil < now + 20) {
                activeUntil = now + 20;
            }
        }

        if (now >= activeUntil) return false;
        if (now < nextKick) return false;

        return findKickTarget() != null;
    }

    @Override
    public boolean shouldContinue() {
        return false;
    }

    @Override
    public void start() {
        LivingEntity target = findKickTarget();
        if (target == null) return;

        World world = mob.getWorld();
        long now = world.getTime();
        var angry = ExtraConfig.angryFor(mob);

        int cd = (angry != null && angry.kickCooldownTicks != null)
                ? Math.max(1, angry.kickCooldownTicks)
                : Math.max(1, ExtraConfig.angryDefaultKickCooldown());
        nextKick = now + cd;

        // Note: AttackMemory.mark MUST be called by Milking/Damage trigger,
        // using the desired duration (runTimeinTick). We don't mark here unless needed.

        // Damage Calculation
        float hearts;
        Difficulty diff = world.getDifficulty();
        if (angry != null) {
            switch (diff) {
                case PEACEFUL -> hearts = 0f;
                case EASY -> hearts = (angry.kickDamageHeartsEasy != null) ? angry.kickDamageHeartsEasy : 1.0f;
                case HARD -> hearts = (angry.kickDamageHeartsHard != null) ? angry.kickDamageHeartsHard : 2.0f;
                default -> hearts = (angry.kickDamageHeartsNormal != null) ? angry.kickDamageHeartsNormal : 1.5f;
            }
        } else {
            hearts = 1.5f;
        }
        float damage = hearts * 2.0f;

        // Knockback Calculation
        double hVel = (angry != null && angry.kickHorizontalVelocity != null) ? angry.kickHorizontalVelocity : 0.28;
        double vVel = (angry != null && angry.kickVerticalVelocity != null) ? angry.kickVerticalVelocity : 1.2;

        Vec3d dirVec = target.getPos().subtract(mob.getPos());
        Vec3d horizontalDir = new Vec3d(dirVec.x, 0, dirVec.z).normalize();
        double kbX = horizontalDir.x * hVel;
        double kbZ = horizontalDir.z * hVel;

        // Apply Hit
        target.damage(mob.getDamageSources().mobAttack(mob), damage);
        Vec3d tv = target.getVelocity();
        target.setVelocity(tv.x + kbX, Math.max(tv.y, vVel), tv.z + kbZ);
        target.velocityModified = true;

        mob.swingHand(Hand.MAIN_HAND, true);
        mob.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 0.9f + mob.getRandom().nextFloat() * 0.2f);
    }

    private void detectDamageAndRefreshWindow() {
        float hp = mob.getHealth();
        if (lastHealth < 0f) { lastHealth = hp; return; }

        if (hp < lastHealth) {
            var angry = ExtraConfig.angryFor(mob);

            // Uses kickActiveWindowTicks for the aggression window
            int kickWindow = (angry != null && angry.kickActiveWindowTicks != null)
                    ? angry.kickActiveWindowTicks
                    : 120;

            activeUntil = mob.getWorld().getTime() + kickWindow;
        }
        lastHealth = hp;
    }

    private void syncHealth() {
        lastHealth = mob.getHealth();
    }

    private LivingEntity findKickTarget() {
        double kickRange = getKickRange();
        double kickRangeSq = kickRange * kickRange;

        World world = mob.getWorld();
        Box box = mob.getBoundingBox().expand(kickRange, 1.5, kickRange);
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, box, le -> candidateFilter(le, kickRangeSq));
        if (entities.isEmpty()) return null;

        LivingEntity best = null;
        double bestD2 = Double.MAX_VALUE;

        for (LivingEntity le : entities) {
            double d2 = mob.squaredDistanceTo(le);
            if (d2 < bestD2) {
                bestD2 = d2;
                best = le;
            }
        }
        return best;
    }

    private boolean candidateFilter(LivingEntity le, double rangeSq) {
        if (le == null || le == mob || !le.isAlive()) return false;
        if (le.getType() == mob.getType()) return false;
        if (le instanceof PlayerEntity p) {
            if (p.isSpectator() || p.isCreative()) return false;
        }
        return mob.squaredDistanceTo(le) <= rangeSq;
    }

    private boolean canSeeTarget(LivingEntity target) {
        // ... raycast logic ... (Removed here to keep logic clean)
        return true; // Simplified for close range kick to avoid raycast failures
    }

    private double getKickRange() {
        var angry = ExtraConfig.angryFor(mob);
        return (angry != null && angry.kickingRange != null) ? angry.kickingRange : 3.5;
    }
}