package thaumictinkerer.client.funnel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.client.render.EssentiaJarRendering;
import thaumcraft.essentia.EssentiaKeys;
import thaumcraft.essentia.FilledJarItem;
import thaumcraft.registry.ModBlocks;
import thaumictinkerer.funnel.FunnelBlockEntity;

public class FunnelRenderer implements BlockEntityRenderer<FunnelBlockEntity, FunnelRenderer.State> {
    private static final int JAR_MAX_AMOUNT = 64;

    public static class State extends BlockEntityRenderState {
        boolean hasJar;
        boolean voidJar;
        int posX;
        @Nullable Aspect aspect;
        int amount;
        @Nullable Aspect filter;
    }

    public FunnelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FunnelBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        ItemStack stack = entity.getTheItem();
        state.hasJar = stack.getItem() instanceof FilledJarItem;
        state.aspect = null;
        state.amount = 0;
        state.filter = null;
        if (!state.hasJar) {
            return;
        }
        FilledJarItem item = (FilledJarItem) stack.getItem();
        state.voidJar = item.jar() == ModBlocks.VOID_JAR.get();
        state.posX = entity.getBlockPos().getX();
        AspectList aspects = item.getAspects(stack);
        if (aspects != null && aspects.size() == 1) {
            state.aspect = aspects.getAspects().get(0);
            state.amount = aspects.getAmount(state.aspect);
        }
        state.filter = EssentiaKeys.FILTER.get(stack);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.hasJar) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.115F, 0.0F);
        EssentiaJarRendering.submit(collector, poseStack, state.lightCoords, state.posX, state.aspect, state.amount, JAR_MAX_AMOUNT, state.filter, Direction.EAST);
        EssentiaJarRendering.glass(collector, poseStack, state.lightCoords, state.voidJar);
        poseStack.popPose();
    }
}
