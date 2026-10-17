package thaumictinkerer.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumictinkerer.ThaumicTinkerer;

public record ArmorStatusPayload(boolean enabled) implements CustomPacketPayload {
    public static final Type<ArmorStatusPayload> TYPE = new Type<>(ThaumicTinkerer.id("armor_status"));
    public static final StreamCodec<ByteBuf, ArmorStatusPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, ArmorStatusPayload::enabled,
        ArmorStatusPayload::new
    );

    @Override
    public Type<ArmorStatusPayload> type() {
        return TYPE;
    }
}
