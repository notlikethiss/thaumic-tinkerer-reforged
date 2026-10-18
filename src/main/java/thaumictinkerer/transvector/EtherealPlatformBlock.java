package thaumictinkerer.transvector;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumictinkerer.camo.CamoBlock;
import thaumictinkerer.camo.CamoBlockEntity;

public class EtherealPlatformBlock extends CamoBlock {
    public static final double PLAYER_OFFSET = 0.38;

    public EtherealPlatformBlock(Properties properties) {
        super(properties);
    }

    public static boolean solidFor(@Nullable Entity entity, BlockPos pos) {
        if (entity == null) {
            return false;
        }
        boolean player = entity instanceof Player;
        return entity.getY() > pos.getY() + (player ? PLAYER_OFFSET : 0.0) && (!player || !entity.isShiftKeyDown());
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext && solidFor(entityContext.getEntity(), pos)) {
            return Shapes.block();
        }
        return Shapes.empty();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CamoBlockEntity(TransvectorFeature.PLATFORM_ENTITY.get(), pos, state);
    }
}
