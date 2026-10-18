package thaumictinkerer.client.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumictinkerer.platform.TtNetwork;

public final class TtClientNetwork {
    private TtClientNetwork() {
    }

    public static <T extends CustomPacketPayload> void handle(CustomPacketPayload.Type<T> type, TtNetwork.Handler<T> handler) {
        TtNetwork.setClientHandler(type, payload -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                handler.handle(payload, player);
            }
        });
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
