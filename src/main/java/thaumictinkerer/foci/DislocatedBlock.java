package thaumictinkerer.foci;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

public record DislocatedBlock(BlockState state, CompoundTag tile) {
    public static final Codec<DislocatedBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockState.CODEC.fieldOf("state").forGetter(DislocatedBlock::state),
        CompoundTag.CODEC.optionalFieldOf("tile", new CompoundTag()).forGetter(DislocatedBlock::tile)
    ).apply(instance, DislocatedBlock::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DislocatedBlock> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
