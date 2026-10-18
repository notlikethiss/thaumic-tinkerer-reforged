package thaumictinkerer.platform;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import javax.annotation.Nullable;
import thaumcraft.blockentity.TcBlockEntity;

public class TtCamoBlockEntity extends TcBlockEntity {
    public static final ModelProperty<BlockState> CAMO = new ModelProperty<>();

    private @Nullable BlockState camo;

    public TtCamoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable BlockState getCamo() {
        return camo;
    }

    public void setCamo(@Nullable BlockState camo) {
        this.camo = camo;
        sync();
    }

    @Override
    public ModelData getModelData() {
        return camo == null ? ModelData.EMPTY : ModelData.builder().with(CAMO, camo).build();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        BlockState previous = camo;
        camo = input.read("camo", BlockState.CODEC).orElse(null);
        if (level != null && level.isClientSide() && !Objects.equals(previous, camo)) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("camo", BlockState.CODEC, camo);
    }
}
