package thaumictinkerer.platform;

import net.neoforged.bus.api.IEventBus;

public final class TtPlatform {
    private TtPlatform() {
    }

    public static void register(IEventBus bus) {
        TtStackData.register(bus);
        TtAttachments.register(bus);
        TtTransfer.register(bus);
    }
}
