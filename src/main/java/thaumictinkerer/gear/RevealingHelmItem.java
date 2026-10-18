package thaumictinkerer.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.gear.Goggles;
import thaumcraft.item.gear.Repairable;
import thaumcraft.node.Revealer;
import thaumcraft.wand.VisDiscountGear;

public class RevealingHelmItem extends net.minecraft.world.item.ArmorItem implements Revealer, Goggles, VisDiscountGear, Repairable {
    public static final int VIS_DISCOUNT = 5;

    public RevealingHelmItem(Properties properties) {
        super(thaumcraft.platform.Registration.armorMaterial(GearFeature.REVEALING_ARMOR), net.minecraft.world.item.ArmorItem.Type.HELMET, properties);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, builder, flag);
        builder.add(Component.translatable("tc.visdiscount").append(": " + VIS_DISCOUNT + "%").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
