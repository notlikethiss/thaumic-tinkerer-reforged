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
    static void registerTints(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(ThaumicTinkerer.id("spell_cloth"), SpellClothTint.MAP_CODEC);
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(EnchantingFeature.ENCHANTER_MENU.get(), EnchanterScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(EnchantingFeature.ENCHANTER_ENTITY.get(), EnchanterRenderer::new);
    }
}
