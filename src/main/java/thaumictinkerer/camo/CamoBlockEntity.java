package thaumictinkerer.camo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumictinkerer.platform.TtCamoBlockEntity;

public class CamoBlockEntity extends TtCamoBlockEntity {
    public CamoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
