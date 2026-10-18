package thaumictinkerer.client.attractor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;

public final class AttractorItemRenderer {
    private final boolean corporeal;
    public AttractorItemRenderer(boolean corporeal) { this.corporeal = corporeal; }

    public void submit(PoseStack poseStack, MultiBufferSource collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        long ticks = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
        AttractorRendering.submit(collector, poseStack, corporeal, true, 15, lightCoords, ticks, System.currentTimeMillis());
    }
}
