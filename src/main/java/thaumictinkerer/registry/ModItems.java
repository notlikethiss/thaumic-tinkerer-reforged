package thaumictinkerer.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumictinkerer.ThaumicTinkerer;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ThaumicTinkerer.MODID);

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
