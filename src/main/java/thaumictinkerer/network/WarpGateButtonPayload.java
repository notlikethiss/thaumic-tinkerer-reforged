package thaumictinkerer.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.kami.gateway.WarpGateMenu;

public record WarpGateButtonPayload(boolean locked) implements CustomPacketPayload {
    public static final Type<WarpGateButtonPayload> TYPE = new Type<>(ThaumicTinkerer.id("warp_gate_button"));
    public static final StreamCodec<ByteBuf, WarpGateButtonPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, WarpGateButtonPayload::locked,
        WarpGateButtonPayload::new
    );

    public static void handle(WarpGateButtonPayload payload, Player player) {
        if (player instanceof ServerPlayer && player.containerMenu instanceof WarpGateMenu menu && menu.stillValid(player)) {
            menu.setLocked(payload.locked());
        }
    }

    @Override
    public Type<WarpGateButtonPayload> type() {
        return TYPE;
    }
}
