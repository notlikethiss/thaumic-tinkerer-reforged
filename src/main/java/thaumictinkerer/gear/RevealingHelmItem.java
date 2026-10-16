package thaumictinkerer.gear;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.gear.Goggles;
import thaumcraft.item.gear.Repairable;
import thaumcraft.node.Revealer;
import thaumcraft.wand.VisDiscountGear;

public class RevealingHelmItem extends Item implements Revealer, Goggles, VisDiscountGear, Repairable {
    public static final int VIS_DISCOUNT = 5;

    public RevealingHelmItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean showNodes(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean showIngamePopups(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
        return VIS_DISCOUNT;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        builder.accept(Component.translatable("tc.visdiscount").append(": " + VIS_DISCOUNT + "%").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
