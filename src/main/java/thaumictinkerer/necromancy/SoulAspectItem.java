package thaumictinkerer.necromancy;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import thaumcraft.aspect.Aspect;

public class SoulAspectItem extends Item {
    public enum Form {
        BASE,
        CONDENSED,
        INFUSED
    }

    private final Aspect aspect;
    private final Form form;

    public SoulAspectItem(Properties properties, Aspect aspect, Form form) {
        super(properties);
        this.aspect = aspect;
        this.form = form;
    }

    public Aspect aspect() {
        return aspect;
    }

    public Form form() {
        return form;
    }

    public static boolean isSoul(ItemStack stack) {
        return stack.getItem() instanceof SoulAspectItem;
    }

    public static boolean isInfused(ItemStack stack) {
        return stack.getItem() instanceof SoulAspectItem item && item.form == Form.INFUSED;
    }

    public static Aspect aspectOf(ItemStack stack) {
        return ((SoulAspectItem) stack.getItem()).aspect;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        builder.accept(Component.literal(aspect.getName()));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return form == Form.INFUSED || super.isFoil(stack);
    }
}
