package thaumictinkerer.awakenedarmor;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import thaumictinkerer.kami.IchorclothArmorItem;

public class AwakenedArmorItem extends IchorclothArmorItem {
    public AwakenedArmorItem(Properties properties, int visDiscount) {
        super(properties, visDiscount);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
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
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        if (AwakenedArmor.isPieceDisabled(stack)) {
            builder.accept(Component.translatable("ttinkerer.ttmisc.awakenedArmor1"));
        }
    }
}
