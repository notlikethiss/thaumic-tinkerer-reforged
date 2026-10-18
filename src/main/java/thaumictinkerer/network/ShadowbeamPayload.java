package thaumictinkerer.network;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import thaumictinkerer.ThaumicTinkerer;

public record ShadowbeamPayload(List<Vec3> path) implements CustomPacketPayload {
    public static final int MAX_POINTS = 512;

    public static final Type<ShadowbeamPayload> TYPE = new Type<>(ThaumicTinkerer.id("shadowbeam"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShadowbeamPayload> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.composite(ByteBufCodecs.DOUBLE, Vec3::x, ByteBufCodecs.DOUBLE, Vec3::y, ByteBufCodecs.DOUBLE, Vec3::z, Vec3::new).apply(ByteBufCodecs.list(MAX_POINTS)), ShadowbeamPayload::path,
        ShadowbeamPayload::new
    );

    @Override
    public Type<ShadowbeamPayload> type() {
        return TYPE;
    }
}
