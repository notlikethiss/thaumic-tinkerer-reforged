package thaumictinkerer.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thaumictinkerer.network.FociEffectPayload;
import thaumictinkerer.platform.TtNetwork;

public final class FociEffects {
    private FociEffects() {
    }

    public static void send(Player player, int kind, BlockPos pos) {
        if (player instanceof ServerPlayer serverPlayer) {
            TtNetwork.sendToTrackingAndSelf(serverPlayer, new FociEffectPayload(kind, player.getId(), pos.getX(), pos.getY(), pos.getZ()));
        }
    }
}
