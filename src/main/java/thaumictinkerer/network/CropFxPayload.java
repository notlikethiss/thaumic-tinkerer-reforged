package thaumictinkerer.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumictinkerer.ThaumicTinkerer;

public record CropFxPayload(BlockPos from, BlockPos to, int color) implements CustomPacketPayload {
    public static final Type<CropFxPayload> TYPE = new Type<>(ThaumicTinkerer.id("crop_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CropFxPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, CropFxPayload::from,
        BlockPos.STREAM_CODEC, CropFxPayload::to,
        ByteBufCodecs.INT, CropFxPayload::color,
        CropFxPayload::new
    );

    @Override
    public Type<CropFxPayload> type() {
        return TYPE;
    }
}
