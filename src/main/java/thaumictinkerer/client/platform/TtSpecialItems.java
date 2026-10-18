package thaumictinkerer.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.attractor.AttractorFeature;
import thaumictinkerer.repairer.RepairerFeature;
import thaumictinkerer.client.attractor.AttractorItemRenderer;
import thaumictinkerer.client.repairer.RepairerItemRenderer;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class TtSpecialItems extends BlockEntityWithoutLevelRenderer {
    private final AttractorItemRenderer kinetic = new AttractorItemRenderer(false);
    private final AttractorItemRenderer corporeal = new AttractorItemRenderer(true);
    private final RepairerItemRenderer repairer = new RepairerItemRenderer();

    private TtSpecialItems() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @SubscribeEvent
    static void register(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private TtSpecialItems renderer;
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new TtSpecialItems();
                }
                return renderer;
            }
        }, AttractorFeature.KINETIC_ATTRACTOR.get().asItem(), AttractorFeature.CORPOREAL_ATTRACTOR.get().asItem(), RepairerFeature.REPAIRER.get().asItem());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (stack.is(RepairerFeature.REPAIRER.get().asItem())) {
            repairer.submit(pose, buffers, light, overlay, stack.hasFoil(), 0);
        } else if (stack.is(AttractorFeature.CORPOREAL_ATTRACTOR.get().asItem())) {
            corporeal.submit(pose, buffers, light, overlay, stack.hasFoil(), 0);
        } else {
            kinetic.submit(pose, buffers, light, overlay, stack.hasFoil(), 0);
        }
    }
}
