package dev.foltz.predations.sniffer;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PitcherPlantEffectsState extends PersistentState {

    public final Map<BlockPos, String> plantEffects = new HashMap<>();

    private final Map<Long, List<BlockPos>> spatialCache = new HashMap<>();

    public static PitcherPlantEffectsState getServerState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                PitcherPlantEffectsState::fromNbt,
                PitcherPlantEffectsState::new,
                "predations_pitcher_plants"
        );
    }


    public void addEffect(BlockPos pos, String data) {
        plantEffects.put(pos, data);
        long chunkKey = ChunkPos.toLong(pos);
        spatialCache.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(pos);
        this.markDirty();
    }

    public void removeEffect(BlockPos pos) {
        if (plantEffects.remove(pos) != null) {
            long chunkKey = ChunkPos.toLong(pos);
            List<BlockPos> list = spatialCache.get(chunkKey);
            if (list != null) {
                list.remove(pos);
                if (list.isEmpty()) spatialCache.remove(chunkKey);
            }
            this.markDirty();
        }
    }

    public List<BlockPos> getPlantsInChunk(long chunkKey) {
        return spatialCache.getOrDefault(chunkKey, new ArrayList<>());
    }

    private void rebuildCache() {
        spatialCache.clear();
        for (BlockPos pos : plantEffects.keySet()) {
            long chunkKey = ChunkPos.toLong(pos);
            spatialCache.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(pos);
        }
    }


    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (Map.Entry<BlockPos, String> entry : plantEffects.entrySet()) {
            NbtCompound tag = new NbtCompound();
            tag.putLong("pos", entry.getKey().asLong());
            tag.putString("val", entry.getValue());
            list.add(tag);
        }
        nbt.put("data", list);
        return nbt;
    }

    public static PitcherPlantEffectsState fromNbt(NbtCompound nbt) {
        PitcherPlantEffectsState state = new PitcherPlantEffectsState();
        if (nbt.contains("data", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("data", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound tag = list.getCompound(i);
                state.plantEffects.put(BlockPos.fromLong(tag.getLong("pos")), tag.getString("val"));
            }
        }
        state.rebuildCache();
        return state;
    }
}