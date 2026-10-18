package thaumictinkerer.awakenedarmor;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thaumictinkerer.kami.IchorclothArmorItem;

public class AwakenedArmorItem extends IchorclothArmorItem {
    public AwakenedArmorItem(Properties properties, int visDiscount, thaumcraft.platform.TcArmorMaterial material, thaumcraft.platform.ArmorSlot slot) {
        super(properties, visDiscount, material, slot);
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        ItemStack stack = player.getItemInHand(hand);
        boolean disabled = !AwakenedArmor.isPieceDisabled(stack);
        AwakenedArmorFeature.PIECE_DISABLED.set(stack, disabled);
        if (player instanceof ServerPlayer serverPlayer) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
            serverPlayer.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.awakenedArmor" + (disabled ? 1 : 0)), true);
        }
        return net.minecraft.world.InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, builder, flag);
        if (AwakenedArmor.isPieceDisabled(stack)) {
            builder.add(Component.translatable("ttinkerer.ttmisc.awakenedArmor1"));
        }
    }
}
