package thaumictinkerer.client.trinkets;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import thaumictinkerer.kami.trinkets.TrinketsFeature;

public final class TrinketsClient {
    private TrinketsClient() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(TrinketsFeature.ICHOR_POUCH_MENU.get(), IchorPouchScreen::new);
    }
}
