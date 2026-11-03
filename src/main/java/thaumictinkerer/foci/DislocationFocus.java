package thaumictinkerer.foci;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusUtils;
import thaumictinkerer.network.FociEffectPayload;

public class DislocationFocus extends TtFocus {
    private static final AspectList COST = new AspectList().add(Aspect.ENTROPY, 500).add(Aspect.ORDER, 500).add(Aspect.EARTH, 100);
    private static final AspectList COST_TILE = new AspectList().add(Aspect.ENTROPY, 2500).add(Aspect.ORDER, 2500).add(Aspect.EARTH, 500);
    private static final AspectList COST_SPAWNER = new AspectList().add(Aspect.ENTROPY, 10000).add(Aspect.ORDER, 10000).add(Aspect.EARTH, 5000);

    public DislocationFocus(Properties properties) {
        super(properties, "focusdislocation", true, false);
    }

    public static AspectList cost(@Nullable BlockEntity blockEntity) {
        if (blockEntity == null) {
            return COST.copy();
        }
        if (blockEntity instanceof SpawnerBlockEntity || blockEntity instanceof TrialSpawnerBlockEntity) {
            return COST_SPAWNER.copy();
        }
        return COST_TILE.copy();
    }

    public static boolean isBlacklisted(Level level, BlockPos pos, BlockState state) {
        return state.isAir()
            || state.is(Blocks.PISTON_HEAD)
            || state.is(Blocks.MOVING_PISTON)
            || state.is(FocusUtils.PORTABLE_HOLE_BLACKLIST)
            || state.getDestroySpeed(level, pos) == -1.0F;
    }

    private static @Nullable Direction pairDirection(BlockState state) {
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
        }
        if (state.getBlock() instanceof AbstractBedBlock) {
            return AbstractBedBlock.getConnectedDirection(state);
        }
        return null;
    }

    private static boolean isMainPart(BlockState state) {
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
        }
        if (state.getBlock() instanceof AbstractBedBlock) {
            return state.getValue(AbstractBedBlock.PART) == BedPart.FOOT;
        }
        return true;
    }

    private static BlockState otherPart(BlockState state) {
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return state.cycle(BlockStateProperties.DOUBLE_BLOCK_HALF);
        }
        return state.cycle(AbstractBedBlock.PART);
    }

    private static boolean canPlaceAt(ServerLevel level, Player player, ItemStack wand, BlockPos pos, Direction face) {
        return level.isInWorldBounds(pos)
            && level.getBlockState(pos).canBeReplaced()
            && player.mayUseItemAt(pos, face, wand)
            && level.mayInteract(player, pos);
    }

    public static @Nullable DislocatedBlock held(ItemStack focus) {
        return FociFeature.DISLOCATED.get(focus);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        DislocatedBlock held = held(focus);
        return held == null ? "DISLOCATION" : "DISLOCATION" + held.state().getBlock().getDescriptionId() + "-0";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0xFFB200;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos clicked = blockHit.getBlockPos();
        Direction face = blockHit.getDirection();
        if (!player.mayUseItemAt(clicked, face, wand) || !serverLevel.mayInteract(player, clicked)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack focus = Wands.getFocusItem(wand);
        DislocatedBlock held = held(focus);
        if (held != null) {
            place(serverLevel, player, wand, focus, held, clicked, face);
        } else {
            pickUp(serverLevel, player, wand, focus, clicked);
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean place(ServerLevel level, Player player, ItemStack wand, ItemStack focus, DislocatedBlock held, BlockPos clicked, Direction face) {
        BlockPos target = clicked.relative(face);
        BlockState state = held.state();
        BlockState pair = held.pair().orElse(null);
        if (!isMainPart(state)) {
            pair = state;
            state = otherPart(state);
        }
        Direction pairDirection = pairDirection(state);
        BlockPos pairPos = pair == null || pairDirection == null ? null : target.relative(pairDirection);
        if (!canPlaceAt(level, player, wand, target, face)
            || pairPos != null && !canPlaceAt(level, player, wand, pairPos, face)
            || !state.canSurvive(level, target)) {
            return false;
        }
        if (!level.setBlock(target, state, Block.UPDATE_ALL)) {
            return false;
        }
        if (pairPos != null) {
            level.setBlock(pairPos, DoublePlantBlock.copyWaterloggedFrom(level, pairPos, pair), Block.UPDATE_ALL);
        }
        if (!held.tile().isEmpty()) {
            CompoundTag tag = held.tile().copy();
            tag.putInt("x", target.getX());
            tag.putInt("y", target.getY());
            tag.putInt("z", target.getZ());
            BlockEntity blockEntity = BlockEntity.loadStatic(target, state, tag, level.registryAccess());
            if (blockEntity != null) {
                level.setBlockEntity(blockEntity);
                level.sendBlockUpdated(target, state, state, Block.UPDATE_ALL);
            }
        }
        FociFeature.DISLOCATED.remove(focus);
        Wands.setFocus(wand, focus);
        FociEffects.send(player, FociEffectPayload.PLACE_BURST, target);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WAND.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        return true;
    }

    public static boolean pickUp(ServerLevel level, Player player, ItemStack wand, ItemStack focus, BlockPos clicked) {
        BlockPos pos = clicked;
        BlockState state = level.getBlockState(pos);
        if (isBlacklisted(level, pos, state)) {
            return false;
        }
        Direction pairDirection = pairDirection(state);
        if (!isMainPart(state)) {
            BlockPos mainPos = pos.relative(pairDirection);
            BlockState main = level.getBlockState(mainPos);
            if (!main.is(state.getBlock()) || !isMainPart(main) || !level.mayInteract(player, mainPos)) {
                return false;
            }
            pos = mainPos;
            state = main;
            pairDirection = pairDirection(state);
        }
        BlockPos pairPos = null;
        BlockState pair = null;
        if (pairDirection != null) {
            BlockPos otherPos = pos.relative(pairDirection);
            BlockState other = level.getBlockState(otherPos);
            if (other.is(state.getBlock()) && !isMainPart(other)) {
                if (!level.mayInteract(player, otherPos)) {
                    return false;
                }
                pairPos = otherPos;
                pair = other;
            }
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        CompoundTag tile = blockEntity == null ? new CompoundTag() : blockEntity.saveWithFullMetadata(level.registryAccess());
        WandItem item = (WandItem) wand.getItem();
        if (!item.consumeAllVis(wand, player, cost(blockEntity), true, false)) {
            return false;
        }
        FociFeature.DISLOCATED.set(focus, new DislocatedBlock(state, tile, Optional.ofNullable(pair)));
        Wands.setFocus(wand, focus);
        if (blockEntity != null) {
            level.removeBlockEntity(pos);
        }
        if (pairPos != null) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            level.setBlock(pairPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.updateNeighborsAt(pos, state.getBlock());
            Blocks.AIR.defaultBlockState().updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        FociEffects.send(player, FociEffectPayload.PICK_BURST, pos);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), state.getSoundType().getBreakSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WAND.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        return true;
    }
}
