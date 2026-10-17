package thaumictinkerer.attractor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import thaumcraft.platform.Menus;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;

public class AttractorBlock extends Block implements EntityBlock {
    public static final BooleanProperty PULL = BooleanProperty.create("pull");

    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0);

    private final boolean corporeal;

    public AttractorBlock(Properties properties, boolean corporeal) {
        super(properties.noOcclusion());
        this.corporeal = corporeal;
        registerDefaultState(stateDefinition.any().setValue(PULL, true));
    }

    public boolean corporeal() {
        return corporeal;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PULL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand == InteractionHand.MAIN_HAND && stack.getItem() instanceof WandItem) {
            if (level.getBlockEntity(pos) instanceof CorporealAttractorBlockEntity attractor && player instanceof ServerPlayer serverPlayer) {
                Menus.open(serverPlayer, attractor);
                level.playSound(null, pos, ModSounds.KEY.get(), SoundSource.BLOCKS, 1.0F, 0.5F);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            level.setBlock(pos, state.cycle(PULL), Block.UPDATE_CLIENTS);
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return corporeal ? new CorporealAttractorBlockEntity(pos, state) : new AttractorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        BlockEntityType<?> expected = corporeal ? AttractorFeature.CORPOREAL_ENTITY.get() : AttractorFeature.KINETIC_ENTITY.get();
        if (type != expected) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> ((AttractorBlockEntity) entity).tick(tickLevel, pos, tickState);
    }
}
