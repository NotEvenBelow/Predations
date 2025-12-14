package dev.foltz.predations.mixin.sniffer;

import dev.foltz.predations.access.DamageTrackerAccess;
import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.sniffer.PitcherPlantEffectsState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class PitcherPlantWorldMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickPitcherManager(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;

        if (world.getTime() % 20 != 0) return;

        ExtraConfig.BetterSnifferRelatedFeaturesConfig config = ExtraConfig.getSnifferConfig();
        if (!config.enabled) return;

        PitcherPlantEffectsState state = PitcherPlantEffectsState.getServerState(world);
        if (state.plantEffects.isEmpty()) return;

        Set<BlockPos> toRemove = new HashSet<>();

        for (BlockPos plantPos : state.plantEffects.keySet()) {
            if (!world.isChunkLoaded(plantPos)) continue;

            BlockState blockState = world.getBlockState(plantPos);
            if (!blockState.isOf(Blocks.PITCHER_CROP) && !blockState.isOf(Blocks.PITCHER_PLANT)) {
                toRemove.add(plantPos);
            }
        }

        if (!toRemove.isEmpty()) {
            for (BlockPos pos : toRemove) {
                state.removeEffect(pos);
            }
        }

        if (state.plantEffects.isEmpty()) return;


        int r = config.pitcherEffectGiveInRadius;
        int rSq = r * r;
        int minLight = config.pitcherEffectGiveInLightLevel;

        int chunkRadius = (r >> 4) + 1;

        for (ServerPlayerEntity player : world.getPlayers()) {

            long lastDamage = ((DamageTrackerAccess) player).predations$getLastDamageTime();
            if (lastDamage > 0 && (world.getTime() - lastDamage < config.pitcherEffectCancelWhenDamagedInTicks)) {
                continue;
            }

            BlockPos playerPos = player.getBlockPos();
            ChunkPos playerChunk = player.getChunkPos();

            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {

                    long chunkKey = ChunkPos.toLong(playerChunk.x + dx, playerChunk.z + dz);
                    List<BlockPos> plantsInChunk = state.getPlantsInChunk(chunkKey);

                    if (plantsInChunk.isEmpty()) continue;

                    for (BlockPos plantPos : plantsInChunk) {

                        if (Math.abs(playerPos.getY() - plantPos.getY()) > r) continue;

                        double distX = playerPos.getX() - plantPos.getX();
                        double distZ = playerPos.getZ() - plantPos.getZ();
                        if ((distX * distX + distZ * distZ) > rSq) continue;

                        if (world.getLightLevel(plantPos) < minLight) continue;

                        String data = state.plantEffects.get(plantPos);
                        if (data == null) continue;

                        int lastColonIndex = data.lastIndexOf(':');
                        if (lastColonIndex > 0 && lastColonIndex < data.length() - 1) {
                            try {
                                String effectId = data.substring(0, lastColonIndex);
                                int amp = Integer.parseInt(data.substring(lastColonIndex + 1));

                                StatusEffect effect = Registries.STATUS_EFFECT.get(new Identifier(effectId));
                                if (effect != null) {
                                    player.addStatusEffect(new StatusEffectInstance(effect, 80, amp, true, false));
                                }
                            } catch (Exception e) {}
                        }
                    }
                }
            }
        }
    }
}