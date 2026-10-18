package thaumictinkerer.test;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public final class TtTestSupport {
    private TtTestSupport() {
    }

    public static ServerPlayer mockServerPlayer(GameTestHelper helper, GameType type) {
        ServerPlayer player = thaumcraft.test.TestSupport.mockServerPlayer(helper);
        player.setGameMode(type);
        player.moveTo(net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1))));
        for (int tick = 0; tick < 61; tick++) {
            player.tick();
        }
        return player;
    }
}
