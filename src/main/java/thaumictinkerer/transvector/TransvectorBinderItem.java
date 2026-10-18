package thaumictinkerer.transvector;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import javax.annotation.Nullable;

public class TransvectorBinderItem extends Item {
    public TransvectorBinderItem(Properties properties) {
        super(properties);
    }

    public static @Nullable GlobalPos origin(ItemStack stack) {
        return TransvectorFeature.BINDER_ORIGIN.get(stack);
    }

    public static void setOrigin(ItemStack stack, @Nullable GlobalPos origin) {
        TransvectorFeature.BINDER_ORIGIN.set(stack, origin);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = context.getItemInHand();
        BlockPos clicked = context.getClickedPos();
        BlockEntity clickedEntity = level.getBlockEntity(clicked);
        GlobalPos origin = origin(stack);
        if (origin == null) {
            if (clickedEntity instanceof TransvectorBlockEntity) {
                setOrigin(stack, GlobalPos.of(level.dimension(), clicked));
                playSound(level, clicked);
                message(player, "set");
            } else {
                message(player, "notinterf");
            }
            return InteractionResult.SUCCESS;
        }
        BlockEntity originEntity = origin.dimension().equals(level.dimension()) ? level.getBlockEntity(origin.pos()) : null;
        if (!(originEntity instanceof TransvectorBlockEntity transvector)) {
            setOrigin(stack, null);
            message(player, "notpresent");
            return InteractionResult.SUCCESS;
        }
        if (clickedEntity instanceof TransvectorInterfaceBlockEntity && transvector instanceof TransvectorInterfaceBlockEntity) {
            message(player, "interffail");
            return InteractionResult.SUCCESS;
        }
        if (!transvector.isInRange(clicked)) {
            message(player, "toofar");
            return InteractionResult.SUCCESS;
        }
        transvector.bind(clicked);
        setOrigin(stack, null);
        playSound(level, clicked);
        message(player, "complete");
        return InteractionResult.SUCCESS;
    }

    private static void playSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void message(Player player, String key) {
        player.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.connector." + key));
    }
}
