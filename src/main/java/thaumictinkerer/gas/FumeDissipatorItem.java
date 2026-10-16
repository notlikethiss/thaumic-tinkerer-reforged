package thaumictinkerer.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.registry.ModSounds;

public class FumeDissipatorItem extends Item {
    public static final int RADIUS = 3;

    public FumeDissipatorItem(Properties properties) {
        super(properties);
    }

    public static int dissipate(Level level, int centerX, int centerY, int centerZ) {
        int removed = 0;
        for (int x = centerX - RADIUS; x < centerX + RADIUS; x++) {
            for (int y = centerY - RADIUS; y < centerY + RADIUS; y++) {
                for (int z = centerZ - RADIUS; z < centerZ + RADIUS; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getBlockState(pos).getBlock() instanceof GasBlock) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        removed++;
                    }
                }
            }
        }
        return removed;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel) {
            dissipate(level, (int) player.getX(), (int) player.getY(), (int) player.getZ());
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WAND.get(), SoundSource.PLAYERS, 0.2F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }
}
