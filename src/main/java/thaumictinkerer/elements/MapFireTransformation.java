package thaumictinkerer.elements;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class MapFireTransformation implements FireTransformation {
    private final Supplier<Map<Block, Block>> map;

    public MapFireTransformation(Supplier<Map<Block, Block>> map) {
        this.map = map;
    }

    @Override
    public @Nullable BlockState result(LevelReader level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof LiquidBlock && !state.getFluidState().isSource()) {
            return null;
        }
        Block result = map.get().get(block);
        return result == null ? null : result.defaultBlockState();
    }
}
