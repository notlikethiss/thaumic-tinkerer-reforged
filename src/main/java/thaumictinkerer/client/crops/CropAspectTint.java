package thaumictinkerer.client.crops;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import thaumictinkerer.crops.InfusedGrainBlockEntity;

public final class CropAspectTint {
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (level != null && pos != null && level.getBlockEntity(pos) instanceof InfusedGrainBlockEntity grain) {
            return 0xFF000000 | grain.getAspect().getColor();
        }
        return 0xFFFFFFFF;
    }
}
