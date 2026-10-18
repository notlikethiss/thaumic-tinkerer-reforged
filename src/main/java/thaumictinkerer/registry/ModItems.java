package thaumictinkerer.registry;

import net.neoforged.bus.api.IEventBus;

public final class ModItems {
    public static final thaumictinkerer.platform.TtRegistration.Items ITEMS = new thaumictinkerer.platform.TtRegistration.Items();

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
