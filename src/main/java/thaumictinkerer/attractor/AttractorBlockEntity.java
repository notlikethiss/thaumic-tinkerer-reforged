package thaumictinkerer.attractor;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.fx.Fx;

public class AttractorBlockEntity extends TcBlockEntity {
    public static final float STRENGTH = 0.25F;
    public static final int PULL_COLOR = 2;
    public static final int PUSH_COLOR = 4;

    public AttractorBlockEntity(BlockPos pos, BlockState state) {
        this(AttractorFeature.KINETIC_ENTITY.get(), pos, state);
    }

    protected AttractorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean applies(Entity entity) {
        return entity instanceof ItemEntity;
    }

    public static int redstone(Level level, BlockPos pos) {
        return level.getBestNeighborSignal(pos);
    }

    public static AABB range(BlockPos pos, int redstone) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        double range = redstone / 2;
        return new AABB(x - range, pos.getY(), z - range, x + range, y + range, z + range);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        int redstone = redstone(level, pos);
        if (redstone <= 0) {
            return;
        }
        boolean pull = state.getValue(AttractorBlock.PULL);
        float speed = pull ? STRENGTH : -STRENGTH;
        Vec3 origin = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, range(pos, redstone), this::applies);
        for (Entity entity : entities) {
            double x = entity.getX();
            double y = entity.getY() + (entity instanceof ItemEntity ? entity.getBbHeight() / 2.0 : 0.0);
            double z = entity.getZ();
            double distanceSquared = pull ? origin.distanceToSqr(x, y, z) : 1.1;
            if (distanceSquared > 1.0) {
                if (!level.isClientSide()) {
                    pullTowards(entity, origin, speed);
                }
                Fx.get().sparkle(level, (float) x, (float) y, (float) z, 1.5F, pull ? PULL_COLOR : PUSH_COLOR, 0.0F);
            }
        }
    }

    public static void pullTowards(Entity entity, Vec3 origin, float modifier) {
        Vec3 center = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ());
        Vec3 difference = origin.subtract(center);
        if (difference.length() > 1.0) {
            difference = difference.normalize();
        }
        entity.setDeltaMovement(difference.scale(modifier));
        entity.needsSync = true;
    }
}
