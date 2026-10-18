package thaumictinkerer.client.awakenedarmor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.client.platform.ItemGeometry;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.awakenedarmor.AwakenedArmorFeature;
import thaumictinkerer.client.geom.WingsGeometry;

public class WingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE = ThaumicTinkerer.id("textures/model/ichorgem1.png");
    private static final int WHITE = 0xFFFFFFFF;

    public WingsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource collector, int lightCoords, AbstractClientPlayer player, float limbSwing, float limbAmount, float partialTick, float ageInTicks, float yRot, float xRot) {
        if (!player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).is(AwakenedArmorFeature.ROBES.get()) || player.isInvisible()) {
            return;
        }
        boolean flying = player.getAbilities().flying;
        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        wing(collector, poseStack, lightCoords, WingsGeometry.LEFT_PIVOT_X, WingsGeometry.leftAngle(flying, ageInTicks), true);
        wing(collector, poseStack, lightCoords, WingsGeometry.RIGHT_PIVOT_X, WingsGeometry.rightAngle(flying, ageInTicks), false);
        poseStack.popPose();
    }

    private static void wing(MultiBufferSource collector, PoseStack poseStack, int light, float pivotX, float angle, boolean left) {
        poseStack.pushPose();
        poseStack.translate(pivotX * WingsGeometry.PIXEL, WingsGeometry.PIVOT_Y * WingsGeometry.PIXEL, WingsGeometry.PIVOT_Z * WingsGeometry.PIXEL);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(angle)));
        if (left) {
            ItemGeometry.solid(collector, poseStack, TEXTURE, false, sink -> WingsGeometry.leftWing(sink::vertex, WHITE, light));
        } else {
            ItemGeometry.solid(collector, poseStack, TEXTURE, false, sink -> WingsGeometry.rightWing(sink::vertex, WHITE, light));
        }
        poseStack.popPose();
    }
}
