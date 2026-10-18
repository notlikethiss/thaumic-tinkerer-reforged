package thaumictinkerer.client.attractor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import thaumictinkerer.client.platform.TtBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import thaumictinkerer.client.platform.TtBlockEntityRenderState;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumictinkerer.attractor.AttractorBlock;
import thaumictinkerer.attractor.AttractorBlockEntity;
import thaumictinkerer.attractor.CorporealAttractorBlockEntity;

public class AttractorRenderer extends TtBlockEntityRenderer<AttractorBlockEntity, AttractorRenderer.State> {
    public static class State extends TtBlockEntityRenderState {
        boolean corporeal;
        boolean pull = true;
        int redstone;
        long ticks;
        long millis;
    }

    public AttractorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AttractorBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, @Nullable Object breakProgress) {
        super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.corporeal = entity instanceof CorporealAttractorBlockEntity;
        state.pull = entity.getBlockState().getValue(AttractorBlock.PULL);
        state.redstone = entity.getLevel() == null ? 15 : AttractorBlockEntity.redstone(entity.getLevel(), entity.getBlockPos());
        state.ticks = entity.getLevel() == null ? 0 : entity.getLevel().getGameTime();
        state.millis = System.currentTimeMillis();
    }

    @Override
    public void submit(State state, PoseStack poseStack, MultiBufferSource collector, Object camera) {
        AttractorRendering.submit(collector, poseStack, state.corporeal, state.pull, state.redstone, state.lightCoords, state.ticks, state.millis);
    }
}
