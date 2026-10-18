package thaumictinkerer.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GasBlock extends thaumictinkerer.platform.TtBlock {
    protected GasBlock(Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties gas(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.NONE)
            .noCollission()
            .noOcclusion()
            .noLootTable()
            .replaceable()
            .randomTicks()
            .instabreak()
            .sound(SoundType.EMPTY)
            .pushReaction(PushReaction.DESTROY)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .isRedstoneConductor((state, level, pos) -> false);
    }

    public static boolean canHoldGas(BlockState state) {
        return state.isAir() || state.getBlock() instanceof GasBlock;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
        return true;
    }
}
