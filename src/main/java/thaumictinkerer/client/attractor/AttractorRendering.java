package thaumictinkerer.client.attractor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.platform.TcRenderTypes.Kind;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.client.geom.AttractorGeometry;
import thaumictinkerer.client.platform.ModelVertexSink;

public final class AttractorRendering {
    private static final Identifier KINETIC_PULL = ThaumicTinkerer.id("textures/model/magnets.png");
    private static final Identifier KINETIC_PUSH = ThaumicTinkerer.id("textures/model/magnetn.png");
    private static final Identifier CORPOREAL_PULL = ThaumicTinkerer.id("textures/model/mobmagnets.png");
    private static final Identifier CORPOREAL_PUSH = ThaumicTinkerer.id("textures/model/mobmagnetn.png");
    private static final Identifier RING = Identifier.fromNamespaceAndPath("thaumcraft", "textures/item/lightningring.png");
    private static final int RING_FRAMES = 16;
    private static final int RING_LIGHT = 225;
    private static final int WHITE = 0xFFFFFFFF;

    private AttractorRendering() {
    }

    private static Identifier texture(boolean corporeal, boolean pull) {
        if (corporeal) {
            return pull ? CORPOREAL_PULL : CORPOREAL_PUSH;
        }
        return pull ? KINETIC_PULL : KINETIC_PUSH;
    }

    public static void submit(SubmitNodeCollector collector, PoseStack poseStack, boolean corporeal, boolean pull, int redstone, int light, long ticks, long millis) {
        collector.submitCustomGeometry(
            poseStack,
            TcRenderTypes.of(Kind.ENTITY_CUTOUT, texture(corporeal, pull)),
            (pose, buffer) -> AttractorGeometry.model(new ModelVertexSink(pose, buffer, WHITE, light))
        );
        int frame = (int) Math.floorMod(ticks, (long) RING_FRAMES);
        int red = pull ? 0 : 255;
        int blue = pull ? 255 : 0;
        int color = (230 << 24) | (red << 16) | blue;
        float scale = redstone / 15.0F * 0.7F + (redstone == 0 ? 0.0F : 0.4F);
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.rotateDegrees(Axis.XP, 90.0F);
        poseStack.translate(0.0F, 0.0F, -0.6F);
        for (int index = 0; index < 3; index++) {
            for (int side = 0; side < 2; side++) {
                poseStack.scale(1.0F, -1.0F, 1.0F);
                poseStack.scale(scale, scale, scale);
                collector.submitCustomGeometry(
                    poseStack,
                    TcRenderTypes.translucent(RING),
                    (pose, buffer) -> AttractorGeometry.ring(new ModelVertexSink(pose, buffer, color, RING_LIGHT), frame, RING_FRAMES)
                );
            }
            poseStack.translate(0.0, 0.0, -(Math.cos(millis / 500.0F) + 1.0) * 0.09 - 0.1);
        }
        poseStack.popPose();
    }
}
