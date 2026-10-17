package thaumictinkerer.client.crops;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import thaumictinkerer.crops.InfusedGrainBlockEntity;

public final class CropAspectTint implements BlockTintSource {
    @Override
    public int color(BlockState state) {
        return 0xFFFFFFFF;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        Integer color = level.getModelData(pos).get(InfusedGrainBlockEntity.COLOR);
        return color == null ? 0xFFFFFFFF : ARGB.opaque(color);
    }
}
