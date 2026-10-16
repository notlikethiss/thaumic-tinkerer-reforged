package thaumictinkerer.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumictinkerer.client.enchanting.EnchantingClient;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ClientFeatures {
    private ClientFeatures() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        EnchantingClient.registerScreens(event);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        EnchantingClient.registerRenderers(event);
    }
}
