package thaumictinkerer.kami.trinkets;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.platform.Menus;
import thaumcraft.wand.focus.FocusPouchItem;

public class IchorPouchItem extends FocusPouchItem {
    public IchorPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public int pouchSize(ItemStack pouch) {
        return IchorPouchMenu.POUCH_SLOTS;
    }

    @Override
    public boolean canStore(ItemStack stack) {
        return !(stack.getItem() instanceof FocusPouchItem);
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Menus.open(serverPlayer, new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new IchorPouchMenu(containerId, inventory, player.getItemInHand(hand)),
                Component.translatable("item.thaumictinkerer.ichor_pouch")
            ));
        }
        return net.minecraft.world.InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
