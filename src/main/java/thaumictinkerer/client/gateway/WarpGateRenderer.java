package thaumictinkerer.client.gateway;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;
import thaumcraft.client.platform.TcRenderTypes;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.client.geom.SpinningCubesGeometry;
import thaumictinkerer.client.platform.ModelVertexSink;
import thaumictinkerer.kami.gateway.WarpGateBlockEntity;

public class WarpGateRenderer implements BlockEntityRenderer<WarpGateBlockEntity, WarpGateRenderer.State> {
    private static final Identifier WHITE = ThaumicTinkerer.id("textures/misc/white.png");
    private static final int SOLID = 0xFFFFFFFF;
    private static final int TRAIL = 0x33FFFFFF;
    private static final int LIGHT = 15728880;
    private static final float HALF_TURN = (float) Math.PI;
    private static final float DIAGONAL = 0.70710677F;

    public static class State extends BlockEntityRenderState {
        double ticks;
    }

    public WarpGateRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WarpGateBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.ticks = (entity.getLevel() == null ? 0L : entity.getLevel().getGameTime()) + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 2.5, 0.5);
        poseStack.mulPose(new Matrix4f().rotate(HALF_TURN, DIAGONAL, 0.0F, DIAGONAL));
        collector.submitCustomGeometry(
            poseStack,
            TcRenderTypes.translucent(WHITE),
            (pose, buffer) -> SpinningCubesGeometry.build(
                state.ticks,
                new ModelVertexSink(pose, buffer, SOLID, LIGHT),
                new ModelVertexSink(pose, buffer, TRAIL, LIGHT)
            )
        );
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(WarpGateBlockEntity entity) {
        return new AABB(entity.getBlockPos()).expandTowards(0.0, 3.0, 0.0).inflate(1.0);
    }
}
