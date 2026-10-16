package thaumictinkerer.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumictinkerer.ThaumicTinkerer;

public record FociEffectPayload(int kind, int entityId, double x, double y, double z) implements CustomPacketPayload {
    public static final int PICK_BURST = 0;
    public static final int PLACE_BURST = 1;
    public static final int SMELT_BEAM = 2;
    public static final int SMELT_PROGRESS = 3;
    public static final int SMELT_DONE = 4;

    public static final Type<FociEffectPayload> TYPE = new Type<>(ThaumicTinkerer.id("foci_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FociEffectPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, FociEffectPayload::kind,
        ByteBufCodecs.VAR_INT, FociEffectPayload::entityId,
        ByteBufCodecs.DOUBLE, FociEffectPayload::x,
        ByteBufCodecs.DOUBLE, FociEffectPayload::y,
        ByteBufCodecs.DOUBLE, FociEffectPayload::z,
        FociEffectPayload::new
    );

    @Override
    public Type<FociEffectPayload> type() {
        return TYPE;
    }
}
