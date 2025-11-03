package dev.foltz.predations.squid.ai;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.squid.HeadSuckable;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.*;
import java.util.function.Predicate;

public class HeadSuckGoal<T extends SquidEntity & HeadSuckable> extends Goal {
    private final T squid;

    private LivingEntity target;
    private int inRangeTicks;
    private int dmgTicker;
    private int overspeedTicks;
    private int psychicTicker;

    public static final Map<UUID, UUID> CLAIMED = new WeakHashMap<>();
    private static final Map<UUID, Integer> SQUID_CD = new WeakHashMap<>();

    public HeadSuckGoal(T squid) {
        this.squid = squid;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.TARGET));
    }

    @Override
    public boolean canStart() {
        ExtraConfig.PredatorySquidConfig cfg = ExtraConfig.get().predatorySquid;

        if (cfg.nudgingSquidwhenStuckonLand && !squid.isLatched() && !squid.isTouchingWater()) {
            return true;
        }

        if (!cfg.enabled) return false;

        Integer cd = SQUID_CD.get(squid.getUuid());
        if (cd != null && cd > 0) {
            SQUID_CD.put(squid.getUuid(), cd - 1);
            return false;
        }

        if (!isLowLight(cfg.lowLightLevel)) return false;

        if (target != null && isValidTarget(target, cfg)) return true;

        if (++psychicTicker >= Math.max(1, cfg.psychicGrabIntervalTicks)) {
            psychicTicker = 0;
            LivingEntity maybe = pickTarget(cfg.psychicGrabRange, cfg, true);
            if (maybe != null && squid.getRandom().nextDouble() < clamp01(cfg.psychicGrabChance)) {
                target = maybe;
                return true;
            }
        }

        target = pickTarget(cfg.latchRangeBlocks, cfg, false);
        return target != null;
    }

    @Override
    public boolean shouldContinue() {
        ExtraConfig.PredatorySquidConfig cfg = ExtraConfig.get().predatorySquid;


        if (cfg.nudgingSquidwhenStuckonLand && !squid.isLatched() && !squid.isTouchingWater()) {
            return squid.isAlive();
        }

        return cfg.enabled && squid.isAlive() && target != null && target.isAlive();
    }

    @Override
    public void start() {
        inRangeTicks = 0;
        dmgTicker = 0;
        overspeedTicks = 0;
        squid.setLatched(false);
        squid.setTongueActive(false);

        ExtraConfig.PredatorySquidConfig cfg = ExtraConfig.get().predatorySquid;
        if (target != null && squid.getBoundingBox().expand(cfg.psychicGrabRange).contains(target.getPos())) {
            CLAIMED.put(target.getUuid(), squid.getUuid());
        }
    }

    @Override
    public void stop() {
        squid.setLatched(false);
        squid.setTongueActive(false);
        squid.setNoGravity(false);
        squid.getNavigation().stop();

        if (target != null) {
            target.setNoGravity(false);
            CLAIMED.remove(target.getUuid(), squid.getUuid());
        }

        int cd = Math.max(0, ExtraConfig.get().predatorySquid.regrabCooldownTicks);
        SQUID_CD.put(squid.getUuid(), cd);

        target = null;
    }

    @Override
    public void tick() {
        ExtraConfig.PredatorySquidConfig cfg = ExtraConfig.get().predatorySquid;

        // stranded but not latched → flop toward water and skip combat
        if (!squid.isLatched() && !squid.isTouchingWater()) {
            ensureWaterIfOnLand(cfg);
            return;
        }

        if (target == null) return;
        if (!target.isAlive()) {
            target.setNoGravity(false);
            stop();
            return;
        }

        if (target instanceof PlayerEntity p &&
                dev.foltz.predations.item.TalismanImmunityTracker.isImmune(p)) {
            stop();
            return;
        }

        if (!isLowLight(cfg.lowLightLevel)) { stop(); return; }
        if (!isValidTarget(target, cfg)) { stop(); return; }
        if (cfg.blockedBySolids && !squid.canSee(target)) return;

        if (squid.isLatched()) {
            if (target.isTouchingWater()) {
                dragDownVictim(cfg);
            } else {
                double yOff = Math.max(0.1, target.getStandingEyeHeight() - 0.25);
                squid.setPosition(target.getX(), target.getY() + yOff, target.getZ());
                squid.setVelocity(Vec3d.ZERO);
            }
            applySlowness(cfg);
            doDamageAndEscape(cfg);
            return;
        }

        boolean insideOrbit = isInsideOrbit(target.getPos(), cfg.latchRangeBlocks);

        if (!insideOrbit) {
            squid.getNavigation().startMovingTo(target, 1.0);
            inRangeTicks = 0;

            if (squid.getBoundingBox().expand(cfg.psychicGrabRange).contains(target.getPos())
                    && squid.distanceTo(target) > cfg.latchRangeBlocks * 1.5) {
                tpVictimToSquid();
            }
            return;
        }

        inRangeTicks++;
        if (inRangeTicks < Math.max(1, cfg.requiredTicksInRange)) return;

        if (squid.getRandom().nextDouble() > clamp01(cfg.catchSuccessChance)) {
            stop();
            return;
        }

        if (target.hasVehicle()) {
            if (cfg.breakBoats && target.getVehicle() instanceof BoatEntity b) {
                b.kill(); target.stopRiding();
            } else if (cfg.breakMinecarts && target.getVehicle() instanceof AbstractMinecartEntity c) {
                c.kill(); target.stopRiding();
            }
        }

        beginLatchNow(cfg);
    }


    private void beginLatchNow(ExtraConfig.PredatorySquidConfig cfg) {
        squid.setLatched(true);
        squid.setTargetUuid(target.getUuid());

        squid.getNavigation().stop();
        squid.setVelocity(Vec3d.ZERO);

        tpVictimToSquid();

        if (target.isTouchingWater()) {
            dragDownVictim(cfg);
        } else {
            double yOff = Math.max(0.1, target.getStandingEyeHeight() - 0.25);
            squid.setPosition(target.getX(), target.getY() + yOff, target.getZ());
        }
        applySlowness(cfg);

        dmgTicker = cfg.tickInterval;
    }

    private void dragDownVictim(ExtraConfig.PredatorySquidConfig cfg) {
        double force = -Math.abs(cfg.downforceDrag);
        target.addVelocity(0, force, 0);
        target.velocityModified = true;

        double yOff = Math.max(0.1, target.getStandingEyeHeight() - 0.25);
        squid.setPosition(target.getX(), target.getY() + yOff, target.getZ());
        squid.setVelocity(Vec3d.ZERO);
    }

    private void applySlowness(ExtraConfig.PredatorySquidConfig cfg) {
        if (cfg.squidAndGlowSlownessLevel > 0) {
            target.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS,
                    40,
                    cfg.squidAndGlowSlownessLevel - 1,
                    false, false, true
            ));
        }
    }

    private void doDamageAndEscape(ExtraConfig.PredatorySquidConfig cfg) {
        if (!target.isAlive()) {
            target.setNoGravity(false);
            stop();
            return;
        }

        double spd = target.getVelocity().length();
        if (spd > cfg.lineBreakSpeed) {
            if (++overspeedTicks >= 20) { stop(); return; }
        } else {
            overspeedTicks = 0;
        }

        if (++dmgTicker >= Math.max(1, cfg.tickInterval)) {
            dmgTicker = 0;

            float hearts = switch (squid.getWorld().getDifficulty()) {
                case EASY -> cfg.dmgHeartsEasy;
                case HARD -> cfg.dmgHeartsHard;
                default -> cfg.dmgHeartsNormal;
            };
            float dmg = Math.max(0f, hearts) * 2f;

            if (dmg > 0f) {
                int thorns = EnchantmentHelper.getEquipmentLevel(Enchantments.THORNS, target);
                if (thorns > 0 && cfg.extraThornDamage > 0) {
                    squid.damage(squid.getDamageSources().thorns(target), (float)(dmg * cfg.extraThornDamage));
                }
                target.damage(squid.getDamageSources().mobAttack(squid), dmg);
                target.setVelocity(Vec3d.ZERO);
                if (cfg.healPct > 0f) squid.heal(dmg * cfg.healPct);

                if (cfg.squidAndGlowGiveBlindness) {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0, false, false, true));
                }

                int hungerAmp;
                if (squid.getType() == EntityType.GLOW_SQUID) {
                    if (cfg.glowWeaknessLevel > 0) {
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, cfg.glowWeaknessLevel - 1, false, false, true));
                    }
                    if (cfg.glowMiningFatigueLevel > 0) {
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 200, cfg.glowMiningFatigueLevel - 1, false, false, true));
                    }
                    // 🟢 apply strength from config
                    if (cfg.glowStrengthLevel > 0) {
                        squid.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.STRENGTH,
                                200,
                                cfg.glowStrengthLevel - 1,
                                false, false, true
                        ));
                    }
                    hungerAmp = cfg.glowHungerLevel - 1;
                } else {
                    hungerAmp = cfg.squidHungerLevel - 1;
                }

                if (hungerAmp >= 0) {
                    target.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.HUNGER,
                            200,
                            hungerAmp,
                            false, false, true
                    ));
                }
            }
        }
    }

    private void tpVictimToSquid() {
        if (target instanceof PlayerEntity p) {
            p.requestTeleport(squid.getX(), squid.getY(), squid.getZ());
        } else {
            target.requestTeleport(squid.getX(), squid.getY(), squid.getZ());
        }
    }

    private LivingEntity pickTarget(double range, ExtraConfig.PredatorySquidConfig cfg, boolean exclusivePsychic) {
        Box box = squid.getBoundingBox().expand(range);
        Predicate<LivingEntity> pred = e -> e.isAlive() && e != squid && isValidTarget(e, cfg)
                && (!cfg.blockedBySolids || squid.canSee(e));

        List<LivingEntity> list = squid.getWorld().getEntitiesByClass(LivingEntity.class, box, pred::test);

        if (exclusivePsychic) {
            list.removeIf(e -> {
                UUID tid = e.getUuid();
                UUID owner = CLAIMED.get(tid);
                return owner != null && !owner.equals(squid.getUuid());
            });
        }

        if (list.isEmpty()) return null;
        return list.get(squid.getRandom().nextInt(list.size()));
    }

    private boolean isValidTarget(LivingEntity e, ExtraConfig.PredatorySquidConfig cfg) {
        if (!e.isAlive()) return false;

        if (e instanceof PlayerEntity p) {
            if (p.isSpectator() || p.isCreative() || p.getAbilities().invulnerable) return false;


            if (dev.foltz.predations.item.TalismanImmunityTracker.isImmune(p)) {
                return false;
            }
        }

        String id = Registries.ENTITY_TYPE.getId(e.getType()).getPath();
        return !cfg.excludedTargets.contains(id);
    }

    private boolean isLowLight(int maxBlockLight) {
        return squid.getWorld().getLightLevel(squid.getBlockPos()) <= Math.max(0, maxBlockLight);
    }

    private boolean isInsideOrbit(Vec3d pos, double expandBlocks) {
        return squid.getBoundingBox().expand(Math.max(0.0, expandBlocks)).contains(pos);
    }

    private static double clamp01(double v) {
        return v < 0 ? 0 : Math.min(1, v);
    }

    private boolean hasLineOfSightToPos(Vec3d pos) {
        var ctx = new RaycastContext(
                squid.getEyePos(),
                pos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                squid
        );
        var hit = squid.getWorld().raycast(ctx);
        return hit.getType() == HitResult.Type.MISS;
    }

    // 🟢 nudging helper
    private void ensureWaterIfOnLand(ExtraConfig.PredatorySquidConfig cfg) {
        if (!cfg.nudgingSquidwhenStuckonLand) return;
        if (squid.isTouchingWater()) return;
        if (squid.age % Math.max(1, cfg.nudgingInterval) != 0) return;

        BlockPos base = squid.getBlockPos();
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;

        int r = (int) Math.max(1, cfg.nudgingRange);
        for (BlockPos pos : BlockPos.iterateOutwards(base, r, r, r)) {
            if (squid.getWorld().getFluidState(pos).isIn(FluidTags.WATER)) {
                if (!cfg.nudgePathFindThroughWall && !hasLineOfSightToPos(Vec3d.ofCenter(pos))) continue;
                double d = base.getSquaredDistance(pos);
                if (d < best) { best = d; nearest = pos.toImmutable(); }
            }
        }

        if (nearest != null) {
            Vec3d dir = new Vec3d(
                    (nearest.getX() + 0.5) - squid.getX(),
                    (nearest.getY() + 0.5) - squid.getY(),
                    (nearest.getZ() + 0.5) - squid.getZ()
            ).normalize();

            double jumpStrength = Math.max(0.3, cfg.nudgingJumpHeightVelocity);
            double forwardStrength = 0.6;

            squid.addVelocity(dir.x * forwardStrength, jumpStrength, dir.z * forwardStrength);
            squid.velocityModified = true;
        } else {
            double xVel = (squid.getRandom().nextDouble() - 0.5) * 0.4;
            double zVel = (squid.getRandom().nextDouble() - 0.5) * 0.4;
            squid.addVelocity(xVel, cfg.nudgingJumpHeightVelocity, zVel);
            squid.velocityModified = true;
        }
    }
}
