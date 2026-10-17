package thaumictinkerer.crops;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectProvidingItem;

public class InfusedGrainItem extends Item implements AspectProvidingItem {
    private final Aspect aspect;

    public InfusedGrainItem(Aspect aspect, Properties properties) {
        super(properties);
        this.aspect = aspect;
    }

    public Aspect getAspect() {
        return aspect;
    }

    @Override
    public AspectList getAspects(ItemStack stack) {
        return new AspectList().add(aspect, 1).add(Aspect.CROP, 1);
    }

    @Override
    public void setAspects(ItemStack stack, AspectList aspects) {
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        builder.accept(aspect.getDescription());
    }
}
