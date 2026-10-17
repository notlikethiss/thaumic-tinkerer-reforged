package thaumictinkerer.gear;

import net.minecraft.world.item.ItemStack;
import thaumcraft.item.ScribingToolsItem;

public class InfusedInkwellItem extends ScribingToolsItem {
    public static final int USES = 800;

    public InfusedInkwellItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
