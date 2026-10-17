package thaumictinkerer.elements;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import thaumcraft.aspect.Aspect;

public class InfusedPotionItem extends Item {
    private final Aspect aspect;

    public InfusedPotionItem(Aspect aspect, Properties properties) {
        super(properties);
        this.aspect = aspect;
    }

    public Aspect aspect() {
        return aspect;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.literal(aspect.getName()));
    }
}
