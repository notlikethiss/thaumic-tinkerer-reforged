package thaumictinkerer.client.camo;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jspecify.annotations.Nullable;
import thaumictinkerer.camo.CamoBlock;
import thaumictinkerer.camo.CamoBlockEntity;

public class CamoBlockModel implements DynamicBlockStateModel {
    private final BlockStateModel fallback;

    public CamoBlockModel(BlockStateModel fallback) {
        this.fallback = fallback;
    }

    private static @Nullable BlockState camo(BlockAndTintGetter level, BlockPos pos) {
        BlockState camo = level.getModelData(pos).get(CamoBlockEntity.CAMO);
        return camo == null || !CamoBlock.isRenderableCamo(camo) ? null : camo;
    }

    private BlockStateModel model(@Nullable BlockState camo) {
        return camo == null ? fallback : Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(camo);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return null;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        BlockState camo = camo(level, pos);
        model(camo).collectParts(level, pos, camo == null ? state : camo, random, parts);
    }

    @Override
    public Material.Baked particleMaterial() {
        return fallback.particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        BlockState camo = camo(level, pos);
        return model(camo).particleMaterial(level, pos, camo == null ? state : camo);
    }

    @Override
    public int materialFlags() {
        return fallback.materialFlags();
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        BlockState camo = camo(level, pos);
        return model(camo).materialFlags(level, pos, camo == null ? state : camo);
    }

    public record Unbaked(BlockStateModel.Unbaked fallback) implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStateModel.Unbaked.CODEC.fieldOf("fallback").forGetter(Unbaked::fallback)
        ).apply(instance, Unbaked::new));

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new CamoBlockModel(fallback.bake(baker));
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            fallback.resolveDependencies(resolver);
        }

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }
    }
}
