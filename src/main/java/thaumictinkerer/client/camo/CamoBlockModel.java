package thaumictinkerer.client.camo;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import thaumictinkerer.camo.CamoBlock;
import thaumictinkerer.camo.CamoBlockEntity;

public class CamoBlockModel extends BakedModelWrapper<BakedModel> {
    public CamoBlockModel(BakedModel fallback) {
        super(fallback);
    }

    private static @Nullable BlockState disguise(ModelData data) {
        BlockState state = data.get(CamoBlockEntity.CAMO);
        return state == null || !CamoBlock.isRenderableCamo(state) ? null : state;
    }

    private BakedModel model(@Nullable BlockState state) {
        return state == null ? originalModel : Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType) {
        BlockState disguise = disguise(data);
        return model(disguise).getQuads(disguise == null ? state : disguise, side, random, data, renderType);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return model(disguise(data)).getParticleIcon(data);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        BlockState disguise = disguise(data);
        return model(disguise).getRenderTypes(disguise == null ? state : disguise, random, data);
    }

}
