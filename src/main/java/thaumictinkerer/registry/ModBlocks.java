package thaumictinkerer.registry;

import java.util.function.UnaryOperator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class ModBlocks {
    public static final thaumictinkerer.platform.TtRegistration.Blocks BLOCKS = new thaumictinkerer.platform.TtRegistration.Blocks();

    private ModBlocks() {
    }

    public static <B extends Block> DeferredBlock<B> withItem(DeferredBlock<B> block) {
        ModItems.ITEMS.registerSimpleBlockItem(block);
        return block;
    }

    public static <B extends Block> DeferredBlock<B> withItem(
        DeferredBlock<B> block,
        UnaryOperator<Item.Properties> itemProperties
    ) {
        ModItems.ITEMS.registerSimpleBlockItem(block, itemProperties.apply(new Item.Properties()));
        return block;
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
