package thaumictinkerer.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class OrderFireTransformation implements FireTransformation {
    private final Map<Block, Optional<Block>> results = new ConcurrentHashMap<>();

    public OrderFireTransformation() {
        reset();
    }

    public void reset() {
        results.clear();
        for (Block block : new Block[]{Blocks.STONE, Blocks.GLASS, Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL}) {
            results.put(block, Optional.empty());
        }
    }

    @Override
    public @Nullable BlockState result(LevelReader level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        Block block = state.getBlock();
        Optional<Block> result = results.get(block);
        if (result == null) {
            result = Optional.ofNullable(compute(serverLevel, pos, state));
            results.put(block, result);
        }
        return result.map(Block::defaultBlockState).orElse(null);
    }

    @Override
    public int decayChance(LevelReader level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 1;
        }
        int dropSize = drops(serverLevel, pos, state).size();
        if (dropSize == 2) {
            return 2;
        }
        if (dropSize >= 3) {
            return 1;
        }
        return 3;
    }

    private static List<ItemStack> drops(ServerLevel level, BlockPos pos, BlockState state) {
        return Block.getDrops(state, level, pos, level.getBlockEntity(pos));
    }

    private static @Nullable Block compute(ServerLevel level, BlockPos pos, BlockState state) {
        List<ItemStack> drops = drops(level, pos, state);
        if (drops.isEmpty() || !allEqual(drops)) {
            return null;
        }
        ItemStack stack = drops.get(0);
        ItemStack blockStack = craft(level, stack);
        if (blockStack.isEmpty()) {
            ItemStack ingot = smelt(level, stack);
            if (!ingot.isEmpty()) {
                blockStack = craft(level, ingot);
            }
        }
        if (blockStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() != Blocks.AIR) {
            return blockItem.getBlock();
        }
        return null;
    }

    private static boolean allEqual(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (!ItemStack.isSameItemSameComponents(stack, stacks.get(0))) {
                return false;
            }
        }
        return true;
    }

    private static ItemStack craft(ServerLevel level, ItemStack stack) {
        List<ItemStack> grid = new ArrayList<>();
        for (int index = 0; index < 9; index++) {
            grid.add(stack.copyWithCount(1));
        }
        CraftingInput input = CraftingInput.of(3, 3, grid);
        return level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level)
            .map(holder -> holder.value().assemble(input))
            .orElse(ItemStack.EMPTY);
    }

    private static ItemStack smelt(ServerLevel level, ItemStack stack) {
        SingleRecipeInput input = new SingleRecipeInput(stack.copyWithCount(1));
        return level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level)
            .map(holder -> holder.value().assemble(input))
            .orElse(ItemStack.EMPTY);
    }
}
