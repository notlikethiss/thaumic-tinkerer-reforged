package thaumictinkerer.transvector;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import thaumictinkerer.camo.CamoBlockEntity;

public abstract class TransvectorBlockEntity extends CamoBlockEntity {
    private @Nullable BlockPos target;

    protected TransvectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract int maxDistance();

    protected boolean tileRequiredAtLink() {
        return true;
    }

    public @Nullable BlockPos targetPos() {
        return target;
    }

    public boolean isBound() {
        return target != null;
    }

    public void bind(BlockPos pos) {
        target = pos.immutable();
        sync();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    public boolean isInRange(BlockPos pos) {
        return Math.abs(pos.getX() - worldPosition.getX()) <= maxDistance()
            && Math.abs(pos.getY() - worldPosition.getY()) <= maxDistance()
            && Math.abs(pos.getZ() - worldPosition.getZ()) <= maxDistance();
    }

    public final @Nullable BlockEntity getTarget() {
        if (target == null || level == null || !level.isLoaded(target)) {
            return null;
        }
        BlockEntity entity = level.getBlockEntity(target);
        if ((entity == null && tileRequiredAtLink()) || !isInRange(target)) {
            if (!level.isClientSide()) {
                target = null;
                setChanged();
            }
            return null;
        }
        return entity;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        target = input.read("target", BlockPos.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("target", BlockPos.CODEC, target);
    }
}
