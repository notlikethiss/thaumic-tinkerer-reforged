package thaumictinkerer.client.tablet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;
import thaumcraft.client.platform.TcRenderTypes;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.client.geom.TabletGeometry;
import thaumictinkerer.client.platform.ModelVertexSink;
import thaumictinkerer.tablet.DynamismTabletBlockEntity;

public class DynamismTabletRenderer implements BlockEntityRenderer<DynamismTabletBlockEntity, DynamismTabletRenderer.State> {
    private static final Identifier CENTER = ThaumicTinkerer.id("textures/misc/atcenter.png");
    private static final Identifier LEFT = ThaumicTinkerer.id("textures/misc/atleft.png");
    private static final Identifier RIGHT = ThaumicTinkerer.id("textures/misc/atright.png");
    private static final Identifier INDENT = ThaumicTinkerer.id("textures/misc/atfacingindent.png");
    private static final int WHITE = 0xFFFFFFFF;

    private final ItemModelResolver itemModelResolver;

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        boolean leftClick;
        Direction facing = Direction.NORTH;
        double ticks;
        float swing;
        boolean block;
        long millis;
    }

    public DynamismTabletRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DynamismTabletBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.leftClick = entity.isLeftClick();
        state.facing = entity.facing();
        state.ticks = entity.ticksExisted() + partialTicks;
        state.swing = Math.max(0.0F, entity.swingProgress() + entity.swingMod() * partialTicks);
        state.millis = System.currentTimeMillis();
        ItemStack stack = entity.heldStack();
        state.item.clear();
        state.block = stack.getItem() instanceof BlockItem;
        if (!stack.isEmpty() && entity.getLevel() != null) {
            itemModelResolver.updateForTopItem(state.item, stack.copyWithCount(1), ItemDisplayContext.GROUND, entity.getLevel(), null, (int) entity.getBlockPos().asLong());
        }
    }

    private static int rotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 270;
            case SOUTH -> 90;
            case WEST -> 0;
            default -> 180;
        };
    }

    private static float[] translation(Direction facing) {
        return switch (facing) {
            case NORTH -> new float[] {0.0F, 0.0F, -1.0F};
            case SOUTH -> new float[] {-1.0F, 0.0F, 0.0F};
            case WEST -> new float[] {0.0F, 0.0F, 0.0F};
            default -> new float[] {-1.0F, 0.0F, -1.0F};
        };
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int rotation = rotation(state.facing);
        overlay(collector, poseStack, state, CENTER, -1, 0.65, 0.13F, 0.0F);
        overlay(collector, poseStack, state, state.leftClick ? LEFT : RIGHT, 1, 1.0, state.leftClick ? 0.13F : 0.131F, 0.0F);
        overlay(collector, poseStack, state, INDENT, 0, 0.5, 0.13F, rotation + 90.0F);
        if (state.item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, rotation);
        poseStack.translate(0.1, 0.2 + Math.cos(state.millis / 600.0) / 18.0, 0.5);
        float[] translation = translation(state.facing);
        poseStack.translate(translation[0], translation[1], translation[2]);
        poseStack.scale(0.8F, 0.8F, 0.8F);
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.rotateDegrees(Axis.ZP, state.swing);
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        poseStack.translate(-state.swing / 250.0F, state.swing / 1000.0F, 0.0F);
        poseStack.mulPose(new Matrix4f().rotate((float) Math.toRadians(Math.cos(state.millis / 400.0F) * 5.0F), 0.70710677F, 0.0F, 0.70710677F));
        poseStack.translate(0.5F, 0.55F, 0.0F);
        float scale = state.block ? 2.5F : 1.5F;
        poseStack.scale(scale, scale, scale);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private void overlay(SubmitNodeCollector collector, PoseStack poseStack, State state, Identifier texture, int rotationMod, double size, float height, float forced) {
        poseStack.pushPose();
        poseStack.translate(0.5F, height, 0.5F);
        float degrees = rotationMod == 0 ? forced : (float) (state.ticks * rotationMod % 360.0);
        poseStack.rotateDegrees(Axis.YP, degrees);
        collector.submitCustomGeometry(
            poseStack,
            TcRenderTypes.translucent(texture),
            (pose, buffer) -> TabletGeometry.overlay(new ModelVertexSink(pose, buffer, WHITE, 15728880), size)
        );
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(DynamismTabletBlockEntity entity) {
        return new AABB(entity.getBlockPos()).inflate(1.0);
    }
}
