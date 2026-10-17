package thaumictinkerer.kami.gateway;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record PearlLocation(ResourceKey<Level> dimension, BlockPos pos) {
    public static final Codec<PearlLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Level.RESOURCE_KEY_CODEC.fieldOf("dim").forGetter(PearlLocation::dimension),
        BlockPos.CODEC.fieldOf("pos").forGetter(PearlLocation::pos)
    ).apply(instance, PearlLocation::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PearlLocation> STREAM_CODEC = StreamCodec.composite(
        ResourceKey.streamCodec(Registries.DIMENSION), PearlLocation::dimension,
        BlockPos.STREAM_CODEC, PearlLocation::pos,
        PearlLocation::new
    );
}
