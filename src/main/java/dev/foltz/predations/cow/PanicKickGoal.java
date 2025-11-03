package dev.foltz.predations.cow;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
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

public class PanicKickGoal extends Goal {
    private static final int ACTIVE_WINDOW_TICKS = 6 * 20;

    private static final double SEARCH_RANGE = 2.75;
    // private static final double KICK_RANGE   = 2.1;  //

    private static final double HZ_PUSH = 0.28;  // horizontal shove
    private static final double VY_MIN  = 0.50;  // min upward impulse
    private static final double VY_MAX  = 0.85;  // cap upward impulse

    private final MobEntity mob;
    private long  activeUntil = 0L;   // when the current kick window ends
    private long  nextKick    = 0L;   // per-cooldown gate
    private float lastHealth  = -1f;  // for damage detection

    public PanicKickGoal(MobEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!mob.isAlive()) return false;
        if (mob instanceof PassiveEntity pe && pe.isBaby()) return false;

        // Must be an "angry" mob type (so cow/mooshroom when configured)
        var angry = ExtraConfig.angryFor(mob);
        if (!ExtraConfig.angryEnabled() || angry == null || !angry.enabled) {
            syncHealth(); // still keep lastHealth sane
            return false;
        }

        detectDamageAndRefreshWindow();

        long now = mob.getWorld().getTime();
        if (now >= activeUntil) return false;
        if (now < nextKick)     return false;
        LivingEntity target = findKickTarget();
        return target != null;
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
        long  now   = world.getTime();
        var   angry = ExtraConfig.angryFor(mob);

        int cd = angry != null && angry.kickCooldownTicks != null
                ? Math.max(1, angry.kickCooldownTicks)
                : Math.max(1, ExtraConfig.angryDefaultKickCooldown());
        nextKick = now + cd;

        // Damage (hearts -> health) by difficulty from config
        float hearts;
        Difficulty diff = world.getDifficulty();
        if (angry != null) {
            switch (diff) {
                case PEACEFUL -> hearts = 0f;
                case EASY     -> hearts = angry.kickDamageHeartsEasy;
                case HARD     -> hearts = angry.kickDamageHeartsHard;
                default       -> hearts = angry.kickDamageHeartsNormal;
            }
        } else {
            hearts = (diff == Difficulty.HARD) ? 1.5f : (diff == Difficulty.EASY ? 1.0f : 1.5f);
        }
        float damage = hearts * 2.0f;

        Vec3d away = target.getPos().subtract(mob.getPos());
        Vec3d dir  = new Vec3d(away.x, 0, away.z).normalize();
        double kbX = dir.x * HZ_PUSH;
        double kbZ = dir.z * HZ_PUSH;

        double vy = 1.2;

        target.damage(mob.getDamageSources().mobAttack(mob), damage);

        Vec3d tv = target.getVelocity();
        target.setVelocity(tv.x + kbX, Math.max(tv.y, vy), tv.z + kbZ);
        target.velocityModified = true;

        // FX
        mob.swingHand(Hand.MAIN_HAND, true);
        mob.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 0.9f + mob.getRandom().nextFloat() * 0.2f);
    }

    // ---------- internals ----------

    /** Detect new damage since last check and extend the window. */
    private void detectDamageAndRefreshWindow() {
        float hp = mob.getHealth();
        if (lastHealth < 0f) { lastHealth = hp; return; }
        if (hp < lastHealth) {
            // Got hurt -> refresh 6s window
            long now = mob.getWorld().getTime();
            activeUntil = now + ACTIVE_WINDOW_TICKS;
        }
        lastHealth = hp;
    }

    private void syncHealth() {
        float hp = mob.getHealth();
        if (lastHealth < 0f) lastHealth = hp;
        else lastHealth = hp;
    }

    private LivingEntity findKickTarget() {
        World w = mob.getWorld();
        Box box = mob.getBoundingBox().expand(SEARCH_RANGE, 1.25, SEARCH_RANGE);
        List<LivingEntity> list = w.getEntitiesByClass(LivingEntity.class, box, this::candidateFilter);
        if (list.isEmpty()) return null;

        LivingEntity best = null;
        double bestD2 = Double.MAX_VALUE;
        Vec3d eye = mob.getEyePos();
        double kickRange = getKickRange();

        for (LivingEntity le : list) {
            double d2 = mob.squaredDistanceTo(le);
            if (d2 > (kickRange * kickRange)) continue;

            HitResult hr = w.raycast(new RaycastContext(
                    eye, le.getEyePos(),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mob
            ));
            if (hr.getType() == HitResult.Type.BLOCK) continue;

            if (d2 < bestD2) { bestD2 = d2; best = le; }
        }
        return best;
    }

    private boolean candidateFilter(LivingEntity le) {
        if (le == null || le == mob || !le.isAlive()) return false;

        if (le.getType() == mob.getType()) return false;

        if (le instanceof PlayerEntity p) {
            if (p.isSpectator() || p.isCreative()) return false;
        }

        return mob.squaredDistanceTo(le) <= (SEARCH_RANGE * SEARCH_RANGE);
    }

    private static double clamp(double v, double lo, double hi) {
        return (v < lo) ? lo : (v > hi ? hi : v);
    }

    private double getKickRange() {
        var angry = ExtraConfig.angryFor(mob);
        if (angry != null) {
            return angry.kickingRange;
        }
        return 2.1; // fallback default
    }
}
