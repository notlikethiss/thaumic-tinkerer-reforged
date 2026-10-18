package thaumictinkerer.test;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;

public final class TtGameTestHelper extends GameTestHelper {
    public TtGameTestHelper(GameTestHelper helper) {
        super(helper.testInfo);
    }

    @Override
    public BlockPos absolutePos(BlockPos pos) {
        return super.absolutePos(pos.above());
    }

    @Override
    public Vec3 absoluteVec(Vec3 pos) {
        return super.absoluteVec(pos.add(0, 1, 0));
    }

    @Override
    public BlockPos relativePos(BlockPos pos) {
        return super.relativePos(pos).below();
    }

    @Override
    public Vec3 relativeVec(Vec3 pos) {
        return super.relativeVec(pos).subtract(0, 1, 0);
    }
}
