package thaumictinkerer.elements;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public interface FireTransformation {
    @Nullable BlockState result(LevelReader level, BlockPos pos, BlockState state);

    default int decayChance(LevelReader level, BlockPos pos, BlockState state) {
        return 1;
    }
}
