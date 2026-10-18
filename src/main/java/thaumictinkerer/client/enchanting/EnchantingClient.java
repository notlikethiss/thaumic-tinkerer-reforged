package thaumictinkerer.client.enchanting;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.enchanting.EnchantingFeature;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class EnchantingClient {
    private EnchantingClient() {
    }

    @SubscribeEvent
    static void registerTints(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> new SpellClothTint().calculate(stack, null, null), EnchantingFeature.SPELL_CLOTH.get());
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(EnchantingFeature.ENCHANTER_MENU.get(), EnchanterScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(EnchantingFeature.ENCHANTER_ENTITY.get(), EnchanterRenderer::new);
    }
}
