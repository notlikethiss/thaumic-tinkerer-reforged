package thaumictinkerer.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.TcBlockEntity;

public abstract class TtCropBlockEntity extends TcBlockEntity {
    public static final ModelProperty<Integer> COLOR = new ModelProperty<>();

    protected TtCropBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract Aspect getAspect();

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(COLOR, getAspect().getColor()).build();
    }
}
