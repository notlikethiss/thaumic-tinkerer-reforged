package thaumictinkerer.client.awakenedarmor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import thaumcraft.client.platform.ItemGeometry;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.awakenedarmor.AwakenedArmorFeature;
import thaumictinkerer.client.geom.WingsGeometry;

public class WingsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier TEXTURE = ThaumicTinkerer.id("textures/model/ichorgem1.png");
    private static final int WHITE = 0xFFFFFFFF;

    public WingsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        if (!state.chestEquipment.is(AwakenedArmorFeature.ROBES.get()) || state.isInvisible) {
            return;
        }
        boolean flying = false;
        if (Minecraft.getInstance().level != null) {
            Entity entity = Minecraft.getInstance().level.getEntity(state.id);
            flying = entity instanceof Player player && player.getAbilities().flying;
        }
        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        wing(collector, poseStack, lightCoords, WingsGeometry.LEFT_PIVOT_X, WingsGeometry.leftAngle(flying, state.ageInTicks), true);
        wing(collector, poseStack, lightCoords, WingsGeometry.RIGHT_PIVOT_X, WingsGeometry.rightAngle(flying, state.ageInTicks), false);
        poseStack.popPose();
    }

    private static void wing(SubmitNodeCollector collector, PoseStack poseStack, int light, float pivotX, float angle, boolean left) {
        poseStack.pushPose();
        poseStack.translate(pivotX * WingsGeometry.PIXEL, WingsGeometry.PIVOT_Y * WingsGeometry.PIXEL, WingsGeometry.PIVOT_Z * WingsGeometry.PIXEL);
        poseStack.rotateDegrees(Axis.YP, (float) Math.toDegrees(angle));
        if (left) {
            ItemGeometry.solid(collector.order(1), poseStack, TEXTURE, false, sink -> WingsGeometry.leftWing(sink::vertex, WHITE, light));
        } else {
            ItemGeometry.solid(collector.order(1), poseStack, TEXTURE, false, sink -> WingsGeometry.rightWing(sink::vertex, WHITE, light));
        }
        poseStack.popPose();
    }
}
