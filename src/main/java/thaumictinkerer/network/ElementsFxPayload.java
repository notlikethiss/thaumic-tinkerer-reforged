package thaumictinkerer.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumictinkerer.ThaumicTinkerer;

public record ElementsFxPayload(int kind, double x, double y, double z, float size) implements CustomPacketPayload {
    public static final int BURST = 0;
    public static final int SPARKLE = 1;
    public static final int WISPS = 2;

    public static final Type<ElementsFxPayload> TYPE = new Type<>(ThaumicTinkerer.id("elements_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ElementsFxPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ElementsFxPayload::kind,
        ByteBufCodecs.DOUBLE, ElementsFxPayload::x,
        ByteBufCodecs.DOUBLE, ElementsFxPayload::y,
        ByteBufCodecs.DOUBLE, ElementsFxPayload::z,
        ByteBufCodecs.FLOAT, ElementsFxPayload::size,
        ElementsFxPayload::new
    );

    @Override
    public Type<ElementsFxPayload> type() {
        return TYPE;
    }
}
