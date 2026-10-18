package thaumictinkerer.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public abstract class TtBlockEntityRenderer<B extends BlockEntity, S extends TtBlockEntityRenderState> implements BlockEntityRenderer<B> {
    public abstract S createRenderState();

    public void extractRenderState(B entity, S state, float partialTicks, Vec3 camera, Object progress) {
    }

    public abstract void submit(S state, PoseStack pose, MultiBufferSource buffers, Object camera);

    @Override
    public final void render(B entity, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        S state = createRenderState();
        state.lightCoords = light;
        extractRenderState(entity, state, partialTicks, Vec3.ZERO, null);
        submit(state, pose, buffers, null);
    }
}
