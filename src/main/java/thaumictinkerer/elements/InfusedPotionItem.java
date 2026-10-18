package thaumictinkerer.elements;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.aspect.Aspect;

public class InfusedPotionItem extends net.minecraft.world.item.PotionItem {
    private final Aspect aspect;

    public InfusedPotionItem(Aspect aspect, Properties properties) {
        super(properties);
        this.aspect = aspect;
    }

    public Aspect aspect() {
        return aspect;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> builder, TooltipFlag flag) {
        builder.add(Component.literal(aspect.getName()));
    }
}
