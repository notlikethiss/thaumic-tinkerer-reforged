package thaumictinkerer.platform;

import net.minecraft.world.item.Item;
import thaumcraft.platform.TcToolMaterial;

public class TtToolItem extends Item {
    private final int enchantmentValue;

    public TtToolItem(Properties properties, TcToolMaterial material) {
        super(properties);
        enchantmentValue = material.enchantability();
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }
}
