package thaumictinkerer.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BrightNitorItem extends Item {
    public BrightNitorItem(Properties properties) {
        super(properties);
    }

    public static void placeNitorGas(Level level, BlockPos pos, boolean strong) {
        if (level.isClientSide()) {
            return;
        }
        BlockState existing = level.getBlockState(pos);
        if (existing.is(Blocks.AIR) || existing.is(Blocks.CAVE_AIR) || existing.is(GasFeature.NITOR_GAS.get())) {
            level.setBlock(pos, GasFeature.NITOR_GAS.get().defaultBlockState().setValue(NitorGasBlock.STRONG, strong), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        placeNitorGas(level, new BlockPos((int) Math.floor(owner.getX()), (int) owner.getY() + 1, (int) Math.floor(owner.getZ())), false);
        super.inventoryTick(stack, level, owner, slot);
    }
}
