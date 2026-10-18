package thaumictinkerer.client.repairer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import thaumictinkerer.client.platform.TtItemRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.client.platform.ItemGeometry;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.client.geom.RepairerGeometry;

public final class RepairerRendering {
    private static final ResourceLocation MODEL = ThaumicTinkerer.id("textures/model/repairer.png");
    private static final ResourceLocation REPAIR = ThaumicTinkerer.id("textures/misc/repair.png");
    private static final ResourceLocation REPAIR_OFF = ThaumicTinkerer.id("textures/misc/repairoff.png");
    private static final int WHITE = 0xFFFFFFFF;
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float GROUND_LIFT = 0.125F;

    private RepairerRendering() {
    }

    public static void submit(
        MultiBufferSource collector,
        PoseStack poseStack,
        int light,
        float rotation,
        float ticks,
        boolean tookLastTick,
        TtItemRenderState item
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        ItemGeometry.solid(collector, poseStack, MODEL, true, sink -> RepairerGeometry.body(sink::vertex, WHITE, light));
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation * 2.0F));
        ItemGeometry.solid(collector, poseStack, MODEL, true, sink -> RepairerGeometry.glass(sink::vertex, WHITE, light));
        poseStack.popPose();
        poseStack.popPose();

        float spin = ticks * 0.75F % 360.0F;
        if (!item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F + (float) Math.sin(ticks / 10.0F) * 0.05F - GROUND_LIFT, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(spin));
            item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.975F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        ItemGeometry.solid(collector, poseStack, tookLastTick ? REPAIR : REPAIR_OFF, true, sink -> RepairerGeometry.overlay(sink::vertex, 1.25F, WHITE, FULL_BRIGHT));
        poseStack.popPose();
    }

    public static float rotation(Direction orientation) {
        return switch (orientation) {
            case NORTH -> 0.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 90.0F;
        };
    }
}
