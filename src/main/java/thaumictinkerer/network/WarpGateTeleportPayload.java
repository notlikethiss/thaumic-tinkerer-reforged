package thaumictinkerer.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.kami.gateway.WarpGateBlockEntity;

public record WarpGateTeleportPayload(BlockPos gate, int index) implements CustomPacketPayload {
    public static final Type<WarpGateTeleportPayload> TYPE = new Type<>(ThaumicTinkerer.id("warp_gate_teleport"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WarpGateTeleportPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, WarpGateTeleportPayload::gate,
        ByteBufCodecs.VAR_INT, WarpGateTeleportPayload::index,
        WarpGateTeleportPayload::new
    );

    public static void handle(WarpGateTeleportPayload payload, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !serverPlayer.level().isLoaded(payload.gate())) {
            return;
        }
        if (serverPlayer.level().getBlockEntity(payload.gate()) instanceof WarpGateBlockEntity gate) {
            gate.teleportPlayer(serverPlayer, payload.index());
        }
    }

    @Override
    public Type<WarpGateTeleportPayload> type() {
        return TYPE;
    }
}
