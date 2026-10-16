package thaumictinkerer.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.attractor.CorporealAttractorMenu;

public record MobMagnetButtonPayload(boolean adult) implements CustomPacketPayload {
    public static final Type<MobMagnetButtonPayload> TYPE = new Type<>(ThaumicTinkerer.id("mob_magnet_button"));
    public static final StreamCodec<ByteBuf, MobMagnetButtonPayload> STREAM_CODEC = ByteBufCodecs.BOOL.map(MobMagnetButtonPayload::new, MobMagnetButtonPayload::adult);

    public static void handle(MobMagnetButtonPayload payload, Player player) {
        if (player instanceof ServerPlayer && player.containerMenu instanceof CorporealAttractorMenu menu && menu.stillValid(player)) {
            menu.setAdult(payload.adult());
        }
    }

    @Override
    public Type<MobMagnetButtonPayload> type() {
        return TYPE;
    }
}
