package thaumictinkerer.mechanism;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class ItemHolderBlock extends thaumictinkerer.platform.TtBlock implements EntityBlock {
    protected ItemHolderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !(level.getBlockEntity(pos) instanceof ItemHolderBlockEntity holder)) {
            return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!holder.isEmpty()) {
            take(level, player, holder);
            return net.minecraft.world.ItemInteractionResult.SUCCESS;
        }
        if (!stack.isEmpty() && holder.accepts(stack)) {
            if (!level.isClientSide()) {
                holder.setTheItem(stack.copyWithCount(1));
                stack.consume(1, player);
            }
            return net.minecraft.world.ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof ItemHolderBlockEntity holder && !holder.isEmpty()) {
            take(level, player, holder);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, param);
    }

    private static void take(Level level, Player player, ItemHolderBlockEntity holder) {
        if (level.isClientSide()) {
            return;
        }
        ItemStack taken = holder.removeTheItem();
        if (!player.getInventory().add(taken)) {
            player.drop(taken,  false);
        }
    }
}
