package thaumictinkerer.mobilizer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public class RelayBlock extends thaumictinkerer.platform.TtBlock implements EntityBlock {
    public RelayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RelayBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != MobilizerFeature.RELAY_ENTITY.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> RelayBlockEntity.tick(tickLevel, pos, tickState, (RelayBlockEntity) entity);
    }
}
