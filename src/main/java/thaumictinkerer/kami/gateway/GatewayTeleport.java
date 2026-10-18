package thaumictinkerer.kami.gateway;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import thaumcraft.platform.Teleport;
import thaumcraft.registry.ModSounds;
import thaumictinkerer.network.GatewayFxPayload;
import thaumictinkerer.platform.TtNetwork;

public final class GatewayTeleport {
    public static final double LANDING_HEIGHT = 1.6;
    private static final double FX_RANGE = 64.0;

    private GatewayTeleport() {
    }

    public static boolean teleport(ServerPlayer player, PearlLocation location) {
        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(location.dimension())) {
            player.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.noDest"));
            return false;
        }
        BlockPos pos = location.pos();
        if (!(level.getBlockEntity(pos) instanceof WarpGateBlockEntity destination)) {
            player.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.noDest"));
            return false;
        }
        if (destination.isLocked()) {
            player.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.noTeleport"));
            return false;
        }
        burst(level, player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WAND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.stopRiding();
        Teleport.to(player, level, new Vec3(pos.getX() + 0.5, pos.getY() + LANDING_HEIGHT, pos.getZ() + 0.5), player.getYRot(), player.getXRot());
        burst(level, player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WAND.get(), SoundSource.PLAYERS, 1.0F, 0.1F);
        return true;
    }

    private static void burst(ServerLevel level, ServerPlayer player) {
        TtNetwork.sendToNear(level, null, player.getX(), player.getY(), player.getZ(), FX_RANGE, new GatewayFxPayload(player.getX(), player.getY(), player.getZ()));
    }
}
