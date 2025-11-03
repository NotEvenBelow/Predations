/** UNUSED

package dev.foltz.predations.squid;

import dev.foltz.predations.config.ExtraConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public final class SquidFloatHandler {
    // tune these to change rise speed
    private static final double UP_ACCEL = 0.06;  // per-tick upward accel
    private static final double VY_CAP   = 0.25;  // max upward speed

    // stay ~1 block below the surface (0 = surface, 1 = one block below)
    private static final int TARGET_DEPTH_BELOW_SURFACE = 1;

    private SquidFloatHandler() {}

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(SquidFloatHandler::tickWorld);
    }

    private static void tickWorld(ServerWorld world) {
        var cfg = ExtraConfig.get().predSquid;
        if (!cfg.squidFloatAtNight) return;
        if (!world.isNight()) return;

        for (Entity e : world.iterateEntities()) {
            if (!(e instanceof SquidEntity squid) || (e instanceof GlowSquidEntity)) continue;
            if (!squid.isAlive() || !squid.isTouchingWater()) continue;

            // must be exposed to sky and have a reachable water surface
            BlockPos surface = findWaterSurface(world, squid.getBlockPos());
            if (surface == null) continue; // no open sky above / blocked
            int targetY = surface.getY() - TARGET_DEPTH_BELOW_SURFACE;

            // if already at/above target depth, stop pushing up
            if (squid.getY() >= targetY + 0.05) continue;

            // nudge up
            var v = squid.getVelocity();
            double vy = Math.min(v.y + UP_ACCEL, VY_CAP);
            squid.setVelocity(v.x, vy, v.z);
            squid.velocityModified = true;
        }
    }

    /**
     * Returns the first AIR block above a continuous column of WATER
     * starting from 'start'. If anything solid (not water/air) appears
     * before air, returns null. This approximates "exposed to sky".
     */
    /**private static BlockPos findWaterSurface(ServerWorld world, BlockPos start) {
        // must start in water
        if (!isWater(world.getBlockState(start))) return null;

        // scan upward up to the world top
        int topY = world.getTopY();
        BlockPos.Mutable m = start.mutableCopy();

        while (m.getY() < topY) {
            m.move(0, 1, 0);
            BlockState bs = world.getBlockState(m);
            if (isWater(bs)) {
                continue; // still in water
            }
            if (bs.isAir()) {
                // we reached air: check that there’s no solid cover all the way up
                if (columnToSkyIsClear(world, m.up())) {
                    return m.toImmutable();
                } else {
                    return null; // something solid above -> not exposed
                }
            }
            // hit something that is neither water nor air → blocked
            return null;
        }
        return null;
    }

    private static boolean columnToSkyIsClear(ServerWorld world, BlockPos from) {
        int topY = world.getTopY();
        BlockPos.Mutable m = from.mutableCopy();
        while (m.getY() < topY) {
            if (!world.getBlockState(m).isAir()) return false;
            m.move(0, 1, 0);
        }
        return true;
    }

    private static boolean isWater(BlockState bs) {
        return bs.isOf(Blocks.WATER);
    }
}

**/