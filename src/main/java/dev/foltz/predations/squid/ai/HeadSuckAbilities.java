// Developing this gave me a headache
package dev.foltz.predations.squid.ai;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class HeadSuckAbilities {

    public static void tpVictimToSquid(SquidEntity squid, LivingEntity target) {
        double x = squid.getX();
        double y = squid.getY();
        double z = squid.getZ();

        if (target instanceof PlayerEntity p) {
            p.requestTeleport(x, y, z);
        } else {
            target.refreshPositionAndAngles(x, y, z, target.getYaw(), target.getPitch());
        }
    }

    public static void safeTeleport(SquidEntity squid, LivingEntity target) {
        tpVictimToSquid(squid, target);
    }

    public static void dragDownVictim(SquidEntity squid, LivingEntity target, ExtraConfig.PredatorySquidConfig cfg) {
        double force = -Math.abs(cfg.downforceDrag);
        target.addVelocity(0, force, 0);
        target.velocityModified = true;
        stickToHead(squid, target);
    }

    public static void stickToHead(SquidEntity squid, LivingEntity target) {
        double yOff = Math.max(0.1, target.getStandingEyeHeight() - 0.25);
        squid.setPosition(target.getX(), target.getY() + yOff, target.getZ());
        squid.setVelocity(Vec3d.ZERO);
    }

    public static void applySlowness(LivingEntity target, ExtraConfig.PredatorySquidConfig cfg) {
        if (cfg.squidAndGlowSlownessLevel > 0) {
            target.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, 40, cfg.squidAndGlowSlownessLevel - 1, false, false, true
            ));
        }
    }

    public static boolean handleDamageAndEscape(SquidEntity squid, LivingEntity target, ExtraConfig.PredatorySquidConfig cfg, int dmgTicker, int overspeedTicks) {
        if (!target.isAlive()) return true;

        double spd = target.getVelocity().length();
        if (spd > cfg.lineBreakSpeed) {
            return overspeedTicks >= 20;
        }

        if (dmgTicker >= Math.max(1, cfg.attackTickInterval)) {
            float hearts = switch (squid.getWorld().getDifficulty()) {
                case EASY -> cfg.dmgHeartsEasy;
                case HARD -> cfg.dmgHeartsHard;
                default -> cfg.dmgHeartsNormal;
            };
            float dmg = Math.max(0f, hearts) * 2.0f;

            if (dmg > 0f) {
                int thorns = EnchantmentHelper.getEquipmentLevel(Enchantments.THORNS, target);
                if (thorns > 0 && cfg.extraThornDamage > 0) {
                    squid.damage(squid.getDamageSources().thorns(target), (float)(dmg * cfg.extraThornDamage));
                }
                target.damage(squid.getDamageSources().mobAttack(squid), dmg);
                target.setVelocity(Vec3d.ZERO);

                if (cfg.healPct > 0f) squid.heal(dmg * cfg.healPct);
                applyPostDamageEffects(squid, target, cfg);
            }
        }
        return false;
    }

    private static void applyPostDamageEffects(SquidEntity squid, LivingEntity target, ExtraConfig.PredatorySquidConfig cfg) {
        if (cfg.squidAndGlowGiveBlindness) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0, false, false, true));
        }

        int hungerAmp;
        if (squid.getType() == EntityType.GLOW_SQUID) {
            if (cfg.glowWeaknessLevel > 0) target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, cfg.glowWeaknessLevel - 1, false, false, true));
            if (cfg.glowMiningFatigueLevel > 0) target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 200, cfg.glowMiningFatigueLevel - 1, false, false, true));
            if (cfg.glowStrengthLevel > 0) squid.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, cfg.glowStrengthLevel - 1, false, false, true));
            hungerAmp = cfg.glowHungerLevel - 1;
        } else {
            hungerAmp = cfg.squidHungerLevel - 1;
        }

        if (hungerAmp >= 0) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 200, hungerAmp, false, false, true));
        }
    }

    public static void ensureWaterIfOnLand(SquidEntity squid, ExtraConfig.PredatorySquidConfig cfg) {
        if (!cfg.nudgingSquidwhenStuckonLand) return;

        BlockPos base = squid.getBlockPos();
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;
        int r = (int) Math.max(1, cfg.nudgingRange);

        for (BlockPos pos : BlockPos.iterateOutwards(base, r, r, r)) {
            if (squid.getWorld().getFluidState(pos).isIn(FluidTags.WATER)) {
                if (!cfg.nudgePathFindThroughWall && !hasLineOfSight(squid, Vec3d.ofCenter(pos))) continue;
                double d = base.getSquaredDistance(pos);
                if (d < best) { best = d; nearest = pos.toImmutable(); }
            }
        }

        if (nearest != null) {
            Vec3d dir = new Vec3d((nearest.getX()+0.5)-squid.getX(), (nearest.getY()+0.5)-squid.getY(), (nearest.getZ()+0.5)-squid.getZ()).normalize();
            squid.addVelocity(dir.x * 0.6, Math.max(0.3, cfg.nudgingJumpHeightVelocity), dir.z * 0.6);
        } else {
            double x = (squid.getRandom().nextDouble() - 0.5) * 0.4;
            double z = (squid.getRandom().nextDouble() - 0.5) * 0.4;
            squid.addVelocity(x, cfg.nudgingJumpHeightVelocity, z);
        }
        squid.velocityModified = true;
    }

    public static boolean hasLineOfSight(SquidEntity squid, Vec3d pos) {
        var ctx = new RaycastContext(squid.getEyePos(), pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, squid);
        return squid.getWorld().raycast(ctx).getType() == HitResult.Type.MISS;
    }

    public static boolean isInsideOrbit(SquidEntity squid, Vec3d pos, double expandBlocks) {
        return squid.getBoundingBox().expand(Math.max(0.0, expandBlocks)).contains(pos);
    }

    public static void breakVehicle(LivingEntity target, ExtraConfig.PredatorySquidConfig cfg) {
        if (!target.hasVehicle()) return;
        if (cfg.breakBoats && target.getVehicle() instanceof BoatEntity b) {
            b.kill(); target.stopRiding();
        } else if (cfg.breakMinecarts && target.getVehicle() instanceof AbstractMinecartEntity c) {
            c.kill(); target.stopRiding();
        }
    }

    public static double clamp01(double v) {
        return v < 0 ? 0 : Math.min(1, v);
    }
}