package thaumictinkerer.gas;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GasBottleItem extends Item {
    public static final int RELEASE_LEVEL = 4;

    private final Supplier<? extends SpreadingGasBlock> gas;

    public GasBottleItem(Supplier<? extends SpreadingGasBlock> gas, Properties properties) {
        super(properties);
        this.gas = gas;
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos pos = new BlockPos((int) player.getX(), (int) player.getY() + 1, (int) player.getZ());
        boolean free = GasBlock.canHoldGas(level.getBlockState(pos));
        stack.consume(1, player);
        if (level instanceof ServerLevel serverLevel) {
            float pitch = 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, pitch);
            if (free) {
                gas.get().release(serverLevel, pos, RELEASE_LEVEL);
            }
        }
        return net.minecraft.world.InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
