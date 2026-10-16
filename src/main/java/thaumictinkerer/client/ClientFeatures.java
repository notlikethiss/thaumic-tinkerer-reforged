package thaumictinkerer.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumictinkerer.client.enchanting.EnchantingClient;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import thaumictinkerer.client.funnel.FunnelRenderer;
import thaumictinkerer.client.repairer.RepairerRenderer;
import thaumictinkerer.funnel.FunnelFeature;
import thaumictinkerer.repairer.RepairerFeature;
import thaumictinkerer.attractor.AttractorFeature;
import thaumictinkerer.client.attractor.AttractorRenderer;
import thaumictinkerer.client.attractor.CorporealAttractorScreen;
import thaumictinkerer.client.tablet.DynamismTabletRenderer;
import thaumictinkerer.client.tablet.DynamismTabletScreen;
import thaumictinkerer.tablet.TabletFeature;

public final class ClientFeatures {
    private ClientFeatures() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        EnchantingClient.registerScreens(event);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(FunnelFeature.FUNNEL_ENTITY.get(), FunnelRenderer::new);
        event.registerBlockEntityRenderer(RepairerFeature.REPAIRER_ENTITY.get(), RepairerRenderer::new);
        EnchantingClient.registerRenderers(event);
        event.register(AttractorFeature.CORPOREAL_MENU.get(), CorporealAttractorScreen::new);
        event.register(TabletFeature.TABLET_MENU.get(), DynamismTabletScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AttractorFeature.KINETIC_ENTITY.get(), AttractorRenderer::new);
        event.registerBlockEntityRenderer(AttractorFeature.CORPOREAL_ENTITY.get(), AttractorRenderer::new);
        event.registerBlockEntityRenderer(TabletFeature.TABLET_ENTITY.get(), DynamismTabletRenderer::new);
    }
}
