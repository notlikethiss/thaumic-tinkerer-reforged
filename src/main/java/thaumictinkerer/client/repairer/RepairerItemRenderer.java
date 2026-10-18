package thaumictinkerer.client.repairer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import thaumictinkerer.client.platform.TtItemRenderState;

public final class RepairerItemRenderer {
    private static final float ITEM_ROTATION = 180.0F;

    public void submit(PoseStack poseStack, MultiBufferSource collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        RepairerRendering.submit(collector, poseStack, lightCoords, ITEM_ROTATION, 0.0F, true, new TtItemRenderState());
    }
}
