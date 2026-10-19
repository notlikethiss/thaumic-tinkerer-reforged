package thaumictinkerer.registry;

import java.util.HashSet;
import java.util.Set;
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
import thaumictinkerer.crops.CreativeVariants;
import thaumictinkerer.kami.KamiFeature;
import thaumictinkerer.kami.WandVariant;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ThaumicTinkerer.MODID);
    private static final Set<DeferredHolder<Item, ? extends Item>> HIDDEN = new HashSet<>();

    public static final Supplier<CreativeModeTab> THAUMIC_TINKERER = TABS.register("thaumictinkerer", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.thaumictinkerer"))
        .icon(() -> new ItemStack(KamiFeature.ICHOR.get()))
        .displayItems((parameters, output) -> {
            for (WandVariant variant : WandVariant.values()) {
                if (variant.available()) {
                    output.accept(variant.creativeStack());
                }
            }
            for (DeferredHolder<Item, ? extends Item> holder : ModItems.ITEMS.getEntries()) {
                if (HIDDEN.contains(holder)) {
                    continue;
                }
                if (holder.get() instanceof CreativeVariants variants) {
                    variants.addCreativeStacks(output::accept);
                } else {
                    output.accept(holder.get());
                }
            }
        })
        .build());

    private ModCreativeTabs() {
    }

    public static void hide(DeferredHolder<Item, ? extends Item> holder) {
        HIDDEN.add(holder);
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
