package thaumictinkerer.gear;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import thaumcraft.research.Knowledge;
import thaumcraft.research.ResearchManager;

public class ShareTomeItem extends Item {
    public ShareTomeItem(Properties properties) {
        super(properties);
    }

    public static @Nullable ShareData data(ItemStack stack) {
        return GearFeature.SHARE.get(stack);
    }

    public static List<String> snapshot(Player player) {
        return new ArrayList<>(Knowledge.get(player).research());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        ShareData data = data(stack);
        if (data == null) {
            GearFeature.SHARE.set(stack, new ShareData(player.getGameProfile().name(), snapshot(player)));
            player.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.shareTome.write"));
            return InteractionResult.SUCCESS;
        }
        ServerPlayer owner = serverPlayer.level().getServer().getPlayerList().getPlayerByName(data.player());
        List<String> research = owner == null ? data.research() : snapshot(owner);
        for (String key : research) {
            ResearchManager.complete(serverPlayer, key, false);
        }
        player.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.shareTome.sync"));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        ShareData data = data(stack);
        builder.accept(data == null
            ? Component.translatable("ttinkerer.ttmisc.shareTome.noAssign")
            : Component.translatable("ttinkerer.ttmisc.shareTome.playerName", data.player()));
    }
}
