package thaumictinkerer.transvector;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumictinkerer.camo.CamoBlock;

public class TransvectorInterfaceBlock extends CamoBlock {
    public TransvectorInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransvectorInterfaceBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != TransvectorFeature.INTERFACE_ENTITY.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> TransvectorInterfaceBlockEntity.tick(tickLevel, pos, tickState, (TransvectorInterfaceBlockEntity) entity);
    }
}
