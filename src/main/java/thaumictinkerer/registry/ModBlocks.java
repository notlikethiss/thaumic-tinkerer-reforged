package thaumictinkerer.registry;

import java.util.function.UnaryOperator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumictinkerer.ThaumicTinkerer;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ThaumicTinkerer.MODID);

    private ModBlocks() {
    }

    private static <B extends Block> DeferredBlock<B> withItem(DeferredBlock<B> block) {
        ModItems.ITEMS.registerSimpleBlockItem(block);
        return block;
    }

    private static <B extends Block> DeferredBlock<B> withItem(
        DeferredBlock<B> block,
        UnaryOperator<Item.Properties> itemProperties
    ) {
        ModItems.ITEMS.registerSimpleBlockItem(block, itemProperties);
        return block;
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
