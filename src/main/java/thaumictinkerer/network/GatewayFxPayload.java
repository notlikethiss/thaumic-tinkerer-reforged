package thaumictinkerer.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumictinkerer.ThaumicTinkerer;

public record GatewayFxPayload(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<GatewayFxPayload> TYPE = new Type<>(ThaumicTinkerer.id("gateway_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GatewayFxPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, GatewayFxPayload::x,
        ByteBufCodecs.DOUBLE, GatewayFxPayload::y,
        ByteBufCodecs.DOUBLE, GatewayFxPayload::z,
        GatewayFxPayload::new
    );

    @Override
    public Type<GatewayFxPayload> type() {
        return TYPE;
    }
}
