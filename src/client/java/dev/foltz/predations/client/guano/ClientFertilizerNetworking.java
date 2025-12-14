package dev.foltz.predations.client.guano;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import dev.foltz.predations.guano.FertilizerAccess;
import dev.foltz.predations.guano.FertilizerNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class ClientFertilizerNetworking {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(FertilizerNetworking.PACKET_ID, ClientFertilizerNetworking::onReceive);
    }

    private static void onReceive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        BlockPos pos = buf.readBlockPos();
        double multiplier = buf.readDouble();

        client.execute(() -> {
            if (client.world != null) {
                Chunk chunk = client.world.getChunk(pos);
                if (chunk instanceof FertilizerAccess access) {
                    if (multiplier > 0) {
                        access.predations$setFertilizer(pos, multiplier);
                    } else {
                        access.predations$removeFertilizer(pos);
                    }
                }
            }
        });
    }
}