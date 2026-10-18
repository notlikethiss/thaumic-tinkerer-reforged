package thaumictinkerer.kami;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.VisDiscountGear;

public class IchorclothArmorItem extends net.minecraft.world.item.ArmorItem implements VisDiscountGear {
    private final int visDiscount;

    public IchorclothArmorItem(Properties properties, int visDiscount, thaumcraft.platform.TcArmorMaterial material, thaumcraft.platform.ArmorSlot slot) {
        super(thaumcraft.platform.Registration.armorMaterial(material), thaumcraft.platform.Registration.armorType(slot), properties);
        this.visDiscount = visDiscount;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
        return visDiscount;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, builder, flag);
        builder.add(Component.translatable("tc.visdiscount").append(": " + visDiscount + "%").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
