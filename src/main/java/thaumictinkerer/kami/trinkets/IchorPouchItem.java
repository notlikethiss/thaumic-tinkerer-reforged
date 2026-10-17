package thaumictinkerer.kami.trinkets;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.platform.Menus;
import thaumcraft.wand.focus.FocusPouchItem;

public class IchorPouchItem extends FocusPouchItem {
    public IchorPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Menus.open(serverPlayer, new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new IchorPouchMenu(containerId, inventory, player.getItemInHand(hand)),
                Component.translatable("item.thaumictinkerer.ichor_pouch")
            ));
        }
        return InteractionResult.SUCCESS;
    }
}
