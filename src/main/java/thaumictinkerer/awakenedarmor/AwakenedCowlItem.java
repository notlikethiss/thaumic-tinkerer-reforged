package thaumictinkerer.awakenedarmor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.gear.Goggles;
import thaumcraft.node.Revealer;

public class AwakenedCowlItem extends AwakenedArmorItem implements Goggles, Revealer {
    public AwakenedCowlItem(Properties properties, int visDiscount) {
        super(properties, visDiscount);
    }

    @Override
    public boolean showNodes(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean showIngamePopups(ItemStack stack, LivingEntity entity) {
        return true;
    }
}
