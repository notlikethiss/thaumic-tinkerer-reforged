package thaumictinkerer.client.foci;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandKeys;
import thaumcraft.wand.Wands;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.foci.DeflectFocus;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class DeflectHandRenderer {
    private DeflectHandRenderer() {
    }

    @SubscribeEvent
    static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !player.isUsingItem() || player.getUsedItemHand() != event.getHand()) {
            return;
        }
        ItemStack stack = player.getUseItem();
        if (!(stack.getItem() instanceof WandItem)
            || !(Wands.getFocus(stack) instanceof DeflectFocus)
            || WandKeys.TARGET.has(stack)
            || !ItemStack.isSameItem(stack, player.getItemInHand(event.getHand()))) {
            return;
        }
        event.setCanceled(true);
        HumanoidArm arm = event.getHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean right = arm == HumanoidArm.RIGHT;
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(right ? 0.56F : -0.56F, -0.52F, -0.72F);
        minecraft.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(
            player,
            stack,
            right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
            !right,
            poseStack,
            event.getMultiBufferSource(),
            event.getPackedLight()
        );
        poseStack.popPose();
    }
}
