package thaumictinkerer.kami.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.item.tool.ToolHarvest;

public final class ToolHandler {
    public static final int MODES = 3;
    public static final CustomModelData DEFAULT_MODE = modeData(0);

    private static final int RANGE_HORIZONTAL = 24;
    private static final int RANGE_VERTICAL = 48;
    private static final int MAX_TREE_BREAKS = 2048;

    private ToolHandler() {
    }

    private static CustomModelData modeData(int mode) {
        return new CustomModelData(mode);
    }

    public static int getMode(ItemStack stack) {
        CustomModelData data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        Float value = data == null ? null : (float) data.value();
        return value == null ? 0 : Math.floorMod(value.intValue(), MODES);
    }

    public static void setMode(ItemStack stack, int mode) {
        stack.set(DataComponents.CUSTOM_MODEL_DATA, modeData(mode));
    }

    public static int getNextMode(int mode) {
        return mode == MODES - 1 ? 0 : mode + 1;
    }

    public static void changeMode(ItemStack stack) {
        setMode(stack, getNextMode(getMode(stack)));
    }

    public static Component modeName(ToolKind kind, int mode) {
        return Component.translatable("ttinkerer.ttmisc.mode." + kind.type() + "." + mode);
    }

    public static void breakExtra(ToolKind kind, ItemStack stack, ServerLevel level, ServerPlayer player, BlockPos origin, BlockState state, Direction face) {
        int mode = getMode(stack);
        if (mode == 0 || !kind.matches(state)) {
            return;
        }
        switch (kind) {
            case PICKAXE -> {
                if (mode == 1) {
                    square(kind, stack, level, player, origin, face);
                } else {
                    line(kind, stack, level, player, origin, face);
                }
            }
            case SHOVEL -> {
                if (mode == 1) {
                    square(kind, stack, level, player, origin, face);
                } else {
                    iterate(kind, stack, level, player, origin, 0, -8, 0, 1, 8, 1);
                }
            }
            case AXE -> {
                if (mode == 1) {
                    square(kind, stack, level, player, origin, face);
                } else {
                    tree(level, player, origin, state);
                }
            }
            case SWORD -> {
            }
        }
    }

    private static void square(ToolKind kind, ItemStack stack, ServerLevel level, ServerPlayer player, BlockPos origin, Direction face) {
        boolean doX = face.getStepX() == 0;
        boolean doY = face.getStepY() == 0;
        boolean doZ = face.getStepZ() == 0;
        iterate(kind, stack, level, player, origin, doX ? -2 : 0, doY ? -1 : 0, doZ ? -2 : 0, doX ? 3 : 1, doY ? 4 : 1, doZ ? 3 : 1);
    }

    private static void line(ToolKind kind, ItemStack stack, ServerLevel level, ServerPlayer player, BlockPos origin, Direction face) {
        int offsetX = -face.getStepX();
        int offsetY = -face.getStepY();
        int offsetZ = -face.getStepZ();
        iterate(
            kind, stack, level, player, origin,
            offsetX >= 0 ? 0 : -10, offsetY >= 0 ? 0 : -10, offsetZ >= 0 ? 0 : -10,
            offsetX > 0 ? 10 : 1, offsetY > 0 ? 10 : 1, offsetZ > 0 ? 10 : 1
        );
    }

    private static void iterate(ToolKind kind, ItemStack stack, ServerLevel level, ServerPlayer player, BlockPos origin, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        for (int offsetX = startX; offsetX < endX; offsetX++) {
            for (int offsetY = startY; offsetY < endY; offsetY++) {
                for (int offsetZ = startZ; offsetZ < endZ; offsetZ++) {
                    if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
                        continue;
                    }
                    breakExtraBlock(kind, stack, level, player, origin.offset(offsetX, offsetY, offsetZ));
                }
            }
        }
        AABB area = new AABB(origin.getX() + startX, origin.getY() + startY, origin.getZ() + startZ, origin.getX() + endX, origin.getY() + endY, origin.getZ() + endZ);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            item.setPos(player.getX(), player.getY() + 1, player.getZ());
        }
    }

    private static void breakExtraBlock(ToolKind kind, ItemStack stack, ServerLevel level, ServerPlayer player, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !kind.matches(state)) {
            return;
        }
        if (state.requiresCorrectToolForDrops() && !stack.isCorrectToolForDrops(state)) {
            return;
        }
        ToolHarvest.destroyUnlessProtected(player, pos, -1);
    }

    private static void tree(ServerLevel level, ServerPlayer player, BlockPos origin, BlockState state) {
        if (!state.is(BlockTags.LOGS)) {
            return;
        }
        Block block = state.getBlock();
        int breaks = 0;
        while (level.getBlockState(origin).is(block) && breaks++ < MAX_TREE_BREAKS) {
            BlockPos furthest = findFurthest(level, origin, block);
            if (!ToolHarvest.destroyUnlessProtected(player, furthest, -1)) {
                break;
            }
        }
        AABB area = new AABB(origin.getX() - 5, origin.getY() - 1, origin.getZ() - 5, origin.getX() + 5, origin.getY() + 64, origin.getZ() + 5);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            item.setPos(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5);
            item.tickCount += 20;
        }
    }

    private static BlockPos findFurthest(ServerLevel level, BlockPos origin, Block block) {
        BlockPos last = origin;
        double lastDistance = 0.0;
        boolean improved = true;
        while (improved) {
            improved = false;
            search:
            for (int offsetX = -2; offsetX <= 2; offsetX++) {
                for (int offsetY = 2; offsetY >= -2; offsetY--) {
                    for (int offsetZ = -2; offsetZ <= 2; offsetZ++) {
                        if (Math.abs(last.getX() + offsetX - origin.getX()) > RANGE_HORIZONTAL
                            || Math.abs(last.getY() + offsetY - origin.getY()) > RANGE_VERTICAL
                            || Math.abs(last.getZ() + offsetZ - origin.getZ()) > RANGE_HORIZONTAL) {
                            return last;
                        }
                        BlockPos candidate = last.offset(offsetX, offsetY, offsetZ);
                        BlockState candidateState = level.getBlockState(candidate);
                        if (candidateState.is(block) && candidateState.is(BlockTags.LOGS) && candidateState.getDestroySpeed(level, candidate) >= 0.0F) {
                            double distance = candidate.distSqr(origin);
                            if (distance > lastDistance) {
                                lastDistance = distance;
                                last = candidate;
                                improved = true;
                                break search;
                            }
                        }
                    }
                }
            }
        }
        return last;
    }
}
