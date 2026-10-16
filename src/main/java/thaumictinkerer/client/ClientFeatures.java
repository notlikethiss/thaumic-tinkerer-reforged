package thaumictinkerer.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumictinkerer.client.enchanting.EnchantingClient;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import thaumictinkerer.client.funnel.FunnelRenderer;
import thaumictinkerer.client.repairer.RepairerRenderer;
import thaumictinkerer.funnel.FunnelFeature;
import thaumictinkerer.repairer.RepairerFeature;

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
    }
}
