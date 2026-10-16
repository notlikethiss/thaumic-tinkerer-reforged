package thaumictinkerer.client.enchanting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumictinkerer.enchanting.EnchanterBlockEntity;

public class EnchanterRenderer implements BlockEntityRenderer<EnchanterBlockEntity, EnchanterRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState tool = new ItemStackRenderState();
        final ItemStackRenderState wand = new ItemStackRenderState();
        float ticks;
    }

    public EnchanterRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EnchanterBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.tool.clear();
        state.wand.clear();
        if (entity.getLevel() == null) {
            return;
        }
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        state.ticks = (viewer == null ? 0 : viewer.tickCount) + partialTicks;
        ItemStack tool = entity.getTool();
        if (!tool.isEmpty()) {
            itemModelResolver.updateForTopItem(state.tool, tool.copyWithCount(1), ItemDisplayContext.FIXED, entity.getLevel(), null, (int) entity.getBlockPos().asLong());
        }
        ItemStack wand = entity.getWand();
        if (!wand.isEmpty()) {
            itemModelResolver.updateForTopItem(state.wand, wand.copyWithCount(1), ItemDisplayContext.FIXED, entity.getLevel(), null, (int) entity.getBlockPos().asLong() + 1);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.tool.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.4F, 0.765F, 0.6F);
            poseStack.rotateDegrees(Axis.XP, -90.0F);
            poseStack.rotateDegrees(Axis.ZP, 30.0F);
            poseStack.scale(0.7F, 0.7F, 0.7F);
            state.tool.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        if (!state.wand.isEmpty()) {
            double millis = state.ticks * 50.0;
            poseStack.pushPose();
            poseStack.translate(0.7F, 0.78F + (float) (Math.cos(millis / 1000.0) - 1.2) / 40.0F, 0.35F);
            poseStack.rotateDegrees(Axis.XP, -90.0F);
            poseStack.rotateDegrees(Axis.ZP, -70.0F);
            poseStack.rotateDegrees(Axis.XP, (float) Math.cos(millis / 500.0) * 5.0F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            state.wand.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(EnchanterBlockEntity entity) {
        return new AABB(entity.getBlockPos()).expandTowards(0.0, 0.5, 0.0);
    }
}
