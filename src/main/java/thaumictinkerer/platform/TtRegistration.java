package thaumictinkerer.platform;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumictinkerer.ThaumicTinkerer;

public final class TtRegistration {
    private TtRegistration() {
    }

    public static final class Items extends DeferredRegister.Items {
        public Items() {
            super(ThaumicTinkerer.MODID);
        }

        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> factory, UnaryOperator<Item.Properties> properties) {
            return register(name, () -> factory.apply(properties.apply(new Item.Properties())));
        }
    }

    public static final class Blocks extends DeferredRegister.Blocks {
        public Blocks() {
            super(ThaumicTinkerer.MODID);
        }

        public <B extends Block> DeferredBlock<B> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends B> factory, UnaryOperator<BlockBehaviour.Properties> properties) {
            return register(name, () -> factory.apply(properties.apply(BlockBehaviour.Properties.of())));
        }
    }
}
