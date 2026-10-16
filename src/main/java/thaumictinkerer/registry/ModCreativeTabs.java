package thaumictinkerer.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumictinkerer.ThaumicTinkerer;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ThaumicTinkerer.MODID);

    public static final Supplier<CreativeModeTab> THAUMIC_TINKERER = TABS.register("thaumictinkerer", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.thaumictinkerer"))
        .icon(() -> new ItemStack(thaumcraft.registry.ModItems.THAUMIUM_INGOT.get()))
        .displayItems((parameters, output) -> {
            for (DeferredHolder<Item, ? extends Item> holder : ModItems.ITEMS.getEntries()) {
                output.accept(holder.get());
            }
        })
        .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
