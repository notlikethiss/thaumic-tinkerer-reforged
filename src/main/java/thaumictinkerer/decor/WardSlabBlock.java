package thaumictinkerer.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlocks;

public class WardSlabBlock extends SlabBlock implements EntityBlock {
    public WardSlabBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WardedSlabBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != DecorFeature.WARDED_SLAB.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> WardedSlabBlockEntity.serverTick(tickLevel, pos, (WardedSlabBlockEntity) entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        TravelEffect.apply(level, pos, entity);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.hasNeighborSignal(pos)) {
            for (int index = 0; index < 4; index++) {
                Fx.get().blockRunes(level, pos.getX(), pos.getY() + 0.7F, pos.getZ(), 0.2F + random.nextFloat() * 0.4F, random.nextFloat() * 0.3F, 0.8F + random.nextFloat() * 0.2F, 20, -0.02F);
            }
        } else if (passableAbove(level, pos)) {
            for (int index = 0; index < 6; index++) {
                Fx.get().blockRunes(level, pos.getX(), pos.getY() + 0.7F, pos.getZ(), 0.9F + random.nextFloat() * 0.1F, random.nextFloat() * 0.3F, random.nextFloat() * 0.3F, 24, -0.02F);
            }
        } else {
            AABB area = new AABB(pos).inflate(1.0);
            for (Entity entity : level.getEntities((Entity) null, area)) {
                if (entity instanceof LivingEntity living && !(living instanceof Player)) {
                    Fx.get().blockRunes(level, pos.getX(), pos.getY() + 0.6F + random.nextFloat() * Math.max(0.8F, living.getEyeHeight()), pos.getZ(), 0.6F + random.nextFloat() * 0.4F, 0.0F, 0.3F + random.nextFloat() * 0.7F, 20, 0.0F);
                    break;
                }
            }
        }
    }

    private static boolean passableAbove(Level level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        boolean passable = above.getCollisionShape(level, pos.above()).isEmpty();
        boolean firstClear = !above.is(ModBlocks.WARDING_AURA.get());
        boolean secondClear = !level.getBlockState(pos.above(2)).is(ModBlocks.WARDING_AURA.get());
        return passable && (firstClear || secondClear);
    }
}
