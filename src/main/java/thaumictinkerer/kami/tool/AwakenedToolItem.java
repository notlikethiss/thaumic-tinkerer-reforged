package thaumictinkerer.kami.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.item.tool.ToolHarvest;

public class AwakenedToolItem extends Item {
    private static final float SHEARS_LEAVES = 15.0F;
    private static final float SHEARS_WOOL = 5.0F;

    private final ToolKind kind;

    public AwakenedToolItem(Properties properties, ToolKind kind) {
        super(properties);
        this.kind = kind;
    }

    public ToolKind kind() {
        return kind;
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            ToolHandler.changeMode(player.getItemInHand(hand));
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        boolean result = super.mineBlock(stack, level, state, pos, owner);
        if (kind != ToolKind.SWORD && level instanceof ServerLevel serverLevel && owner instanceof ServerPlayer player && !ToolHarvest.isHarvesting()) {
            ToolHandler.breakExtra(kind, stack, serverLevel, player, pos, state, ToolHarvest.getFace(player));
        }
        return result;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float speed = super.getDestroySpeed(stack, state);
        if (kind != ToolKind.AXE) {
            return speed;
        }
        if (state.is(BlockTags.LEAVES) || state.is(Blocks.COBWEB) || state.getBlock() instanceof VineBlock) {
            return Math.max(speed, SHEARS_LEAVES);
        }
        if (state.is(BlockTags.WOOL)) {
            return Math.max(speed, SHEARS_WOOL);
        }
        return speed;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, builder, flag);
        builder.add(ToolHandler.modeName(kind, ToolHandler.getMode(stack)));
    }
}
