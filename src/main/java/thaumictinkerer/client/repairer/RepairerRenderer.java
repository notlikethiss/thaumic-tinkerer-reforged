package thaumictinkerer.client.repairer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import thaumictinkerer.client.platform.TtBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import thaumictinkerer.client.platform.TtBlockEntityRenderState;

import thaumictinkerer.client.platform.TtItemRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumictinkerer.repairer.RepairerBlockEntity;

public class RepairerRenderer extends TtBlockEntityRenderer<RepairerBlockEntity, RepairerRenderer.State> {

    public static class State extends TtBlockEntityRenderState {
        final TtItemRenderState item = new TtItemRenderState();
        float rotation;
        float ticks;
        boolean tookLastTick;
    }

    public RepairerRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(RepairerBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, @Nullable Object breakProgress) {
        super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.rotation = RepairerRendering.rotation(entity.orientation());
        state.ticks = entity.ticksExisted() + partialTicks;
        state.tookLastTick = entity.tookLastTick();
        ItemStack stack = entity.getTheItem();
        state.item.clear();
        if (!stack.isEmpty() && entity.getLevel() != null) {
            TtItemRenderState.updateForTopItem(state.item, stack.copyWithCount(1), ItemDisplayContext.GROUND, entity.getLevel(), null, (int) entity.getBlockPos().asLong());
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, MultiBufferSource collector, Object camera) {
        RepairerRendering.submit(collector, poseStack, state.lightCoords, state.rotation, state.ticks, state.tookLastTick, state.item);
    }
}
