package thaumictinkerer.kami;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.VisDiscountGear;

public class IchorclothArmorItem extends Item implements VisDiscountGear {
    private final int visDiscount;

    public IchorclothArmorItem(Properties properties, int visDiscount) {
        super(properties);
        this.visDiscount = visDiscount;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
        return visDiscount;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        builder.accept(Component.translatable("tc.visdiscount").append(": " + visDiscount + "%").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
