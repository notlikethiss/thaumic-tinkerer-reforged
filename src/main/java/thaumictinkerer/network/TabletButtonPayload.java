package thaumictinkerer.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.tablet.DynamismTabletMenu;

public record TabletButtonPayload(boolean leftClick, boolean redstone) implements CustomPacketPayload {
    public static final Type<TabletButtonPayload> TYPE = new Type<>(ThaumicTinkerer.id("tablet_button"));
    public static final StreamCodec<ByteBuf, TabletButtonPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, TabletButtonPayload::leftClick,
        ByteBufCodecs.BOOL, TabletButtonPayload::redstone,
        TabletButtonPayload::new
    );

    public static void handle(TabletButtonPayload payload, Player player) {
        if (player instanceof ServerPlayer && player.containerMenu instanceof DynamismTabletMenu menu && menu.stillValid(player)) {
            menu.setModes(payload.leftClick(), payload.redstone());
        }
    }

    @Override
    public Type<TabletButtonPayload> type() {
        return TYPE;
    }
}
