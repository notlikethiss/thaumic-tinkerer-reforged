package thaumictinkerer.foci;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

public record DislocatedBlock(BlockState state, CompoundTag tile, Optional<BlockState> pair) {
    public static final Codec<DislocatedBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockState.CODEC.fieldOf("state").forGetter(DislocatedBlock::state),
        CompoundTag.CODEC.optionalFieldOf("tile", new CompoundTag()).forGetter(DislocatedBlock::tile),
        BlockState.CODEC.optionalFieldOf("pair").forGetter(DislocatedBlock::pair)
    ).apply(instance, DislocatedBlock::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DislocatedBlock> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public DislocatedBlock(BlockState state, CompoundTag tile) {
        this(state, tile, Optional.empty());
    }
}
