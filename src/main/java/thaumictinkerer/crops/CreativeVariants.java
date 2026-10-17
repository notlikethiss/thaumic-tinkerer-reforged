package thaumictinkerer.crops;

import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;

public interface CreativeVariants {
    void addCreativeStacks(Consumer<ItemStack> output);
}
