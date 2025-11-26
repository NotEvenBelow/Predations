package dev.foltz.predations.guano;

import dev.foltz.predations.PredationsMod;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class FertilizerNetworking {
    public static final Identifier PACKET_ID = new Identifier(PredationsMod.MODID, "fertilizer_update");

    public static void sendUpdate(ServerPlayerEntity player, BlockPos pos, double multiplier) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeDouble(multiplier);
        ServerPlayNetworking.send(player, PACKET_ID, buf);
    }

    // FIXED: Now accepts 'World' as a parameter
    public static void sendToTracking(World world, Chunk chunk, BlockPos pos, double multiplier) {
        if (world instanceof ServerWorld serverWorld) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos);
            buf.writeDouble(multiplier);

            // Send to all players tracking this chunk
            for (ServerPlayerEntity player : PlayerLookup.tracking(serverWorld, chunk.getPos())) {
                ServerPlayNetworking.send(player, PACKET_ID, buf);
            }
        }
    }
}