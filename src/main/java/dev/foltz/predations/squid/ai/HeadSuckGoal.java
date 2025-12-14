// Developing this gave me a headache
package dev.foltz.predations.squid.ai;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.squid.HeadSuckable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.util.math.Vec3d;
import java.util.EnumSet;

public class HeadSuckGoal<T extends SquidEntity & HeadSuckable> extends Goal {
    private final T squid;
    private LivingEntity target;

    private int startTimer;
    private int inRangeTicks;
    private int dmgTicker;
    private int overspeedTicks;
    private int psychicTicker;
    private int nudgeTimer;

    public HeadSuckGoal(T squid) {
        super();
        this.squid = squid;
        this.setControls(EnumSet.of(Control.LOOK, Control.TARGET));
    }

    @Override
    public boolean canStart() {
        ExtraConfig.PredatorySquidConfig cfg = ExtraConfig.get().predSquid;

        if (cfg.nudgingSquidwhenStuckonLand && !squid.isLatched() && !squid.isTouchingWater()) {
            return true;
        }

        if (!cfg.enabled) return false;
        if (HeadSuckTargeting.isOnCooldown(squid)) return false;

        if (++startTimer % 10 != 0) return false;

        if (!HeadSuckTargeting.isPlayerNearby(squid, cfg.aiWorkRange)) return false;
        if (!HeadSuckTargeting.isLowLight(squid, cfg.lowLightLevel)) return false;

        if (target != null) {
            if (HeadSuckTargeting.isValidTarget(target, cfg) && !HeadSuckTargeting.isClaimedByOther(target, squid)) {
                return true;
            } else {
                target = null;
            }
        }

        if (++psychicTicker >= Math.max(1, cfg.psychicGrabIntervalTicks)) {
            psychicTicker = 0;
            LivingEntity maybe = HeadSuckTargeting.pickTarget(squid, cfg.psychicGrabRange, cfg, true);
            if (maybe != null && squid.getRandom().nextDouble() < HeadSuckAbilities.clamp01(cfg.psychicGrabChance)) {
                target = maybe;
                return true;
            }
        }

        double trapRange = Math.max(cfg.latchRangeBlocks, cfg.psychicGrabRange);
        target = HeadSuckTargeting.pickTarget(squid, trapRange, cfg, true);
        return target != null;
    }

    @Override
    public boolean shouldContinue() {
        ExtraConfig.PredatorySquidConfig cfg = ExtraConfig.get().predSquid;

        if (cfg.nudgingSquidwhenStuckonLand && !squid.isLatched() && !squid.isTouchingWater()) {
            return squid.isAlive();
        }

        if (target != null && HeadSuckTargeting.isClaimedByOther(target, squid)) {
            return false;
        }

        double trapRange = Math.max(cfg.latchRangeBlocks, cfg.psychicGrabRange);
        if (target != null && squid.distanceTo(target) > trapRange * 1.5) {
            return false;
        }

        return cfg.enabled && squid.isAlive() && target != null && target.isAlive();
    }

    @Override
    public void start() {
        if (target != null && HeadSuckTargeting.isClaimedByOther(target, squid)) {
            target = null;
            return;
        }

        inRangeTicks = 0;
        dmgTicker = 0;
        overspeedTicks = 0;
        startTimer = 0;
        nudgeTimer = 0;

        squid.setLatched(false);
        squid.setTongueActive(false);

        ExtraConfig.PredatorySquidConfig cfg = ExtraConfig.get().predSquid;
        if (target != null) {
            HeadSuckTargeting.tryClaim(squid, target, cfg.psychicGrabRange);
        }
    }

    @Override
    public void stop() {
        boolean wasLatched = squid.isLatched();
        HeadSuckTargeting.cleanupTarget(squid, target, wasLatched);

        squid.setLatched(false);
        squid.setTongueActive(false);
        squid.setNoGravity(false);
        target = null;

        inRangeTicks = 0;
        dmgTicker = 0;
        overspeedTicks = 0;
        psychicTicker = 0;
        startTimer = 0;
        nudgeTimer = 0;
    }

    @Override
    public void tick() {
        ExtraConfig.PredatorySquidConfig cfg = ExtraConfig.get().predSquid;

        if (!squid.isLatched() && !squid.isTouchingWater()) {
            nudgeTimer++;
            if (nudgeTimer >= Math.max(1, cfg.nudgingInterval)) {
                HeadSuckAbilities.ensureWaterIfOnLand(squid, cfg);
                nudgeTimer = 0;
            }
            return;
        }

        if (target == null || !target.isAlive() || HeadSuckTargeting.isImmune(target)) {
            if (target != null) target.setNoGravity(false);
            stop();
            return;
        }

        if (target != null && HeadSuckTargeting.isClaimedByOther(target, squid)) {
            stop();
            return;
        }

        if (!HeadSuckTargeting.isLowLight(squid, cfg.lowLightLevel)) {
            squid.setLatched(false);
            stop();
            return;
        }

        if (!HeadSuckTargeting.isValidTarget(target, cfg)) {
            stop();
            return;
        }

        if (cfg.blockedBySolids && !squid.canSee(target)) return;

        if (squid.isLatched()) {
            if (target.isTouchingWater()) {
                HeadSuckAbilities.dragDownVictim(squid, target, cfg);
            } else {
                HeadSuckAbilities.stickToHead(squid, target);
            }
            HeadSuckAbilities.applySlowness(target, cfg);

            dmgTicker++;

            boolean breakLatch = HeadSuckAbilities.handleDamageAndEscape(squid, target, cfg, dmgTicker, overspeedTicks);
            if (breakLatch) stop();

            if (dmgTicker >= Math.max(1, cfg.attackTickInterval)) dmgTicker = 0;

            double speed = target.getVelocity().length();
            if (speed > cfg.lineBreakSpeed) overspeedTicks++; else overspeedTicks = 0;

            return;
        }

        if (target == null) { stop(); return; }

        boolean insideLatchRange = HeadSuckAbilities.isInsideOrbit(squid, target.getPos(), cfg.latchRangeBlocks);

        if (!insideLatchRange) {
            inRangeTicks = 0;
            psychicTicker++;

            if (squid.getBoundingBox().expand(cfg.psychicGrabRange).contains(target.getPos())
                    && squid.distanceTo(target) > cfg.latchRangeBlocks * 1.5) {

                if (psychicTicker >= Math.max(1, cfg.psychicGrabIntervalTicks)) {
                    HeadSuckAbilities.tpVictimToSquid(squid, target);
                    psychicTicker = 0;
                }
            }
            return;
        }

        inRangeTicks++;
        if (inRangeTicks < Math.max(1, cfg.requiredTicksInRange)) return;

        if (squid.getRandom().nextDouble() > HeadSuckAbilities.clamp01(cfg.catchSuccessChance)) {
            inRangeTicks = 0;
            return;
        }

        HeadSuckAbilities.breakVehicle(target, cfg);

        squid.setLatched(true);
        squid.setTargetUuid(target.getUuid());
        squid.getNavigation().stop();
        squid.setVelocity(Vec3d.ZERO);

        HeadSuckAbilities.tpVictimToSquid(squid, target);

        if (target.isTouchingWater()) {
            HeadSuckAbilities.dragDownVictim(squid, target, cfg);
        } else {
            HeadSuckAbilities.stickToHead(squid, target);
        }

        HeadSuckAbilities.applySlowness(target, cfg);

        dmgTicker = Math.max(1, cfg.attackTickInterval);
    }
}