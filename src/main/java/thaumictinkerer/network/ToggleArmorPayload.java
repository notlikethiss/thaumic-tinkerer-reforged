package thaumictinkerer.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.awakenedarmor.AwakenedArmor;
import thaumictinkerer.awakenedarmor.AwakenedArmorFeature;
import thaumictinkerer.platform.TtNetwork;

public record ToggleArmorPayload(boolean enabled) implements CustomPacketPayload {
    public static final Type<ToggleArmorPayload> TYPE = new Type<>(ThaumicTinkerer.id("toggle_armor"));
    public static final StreamCodec<ByteBuf, ToggleArmorPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, ToggleArmorPayload::enabled,
        ToggleArmorPayload::new
    );

    public static void handle(ToggleArmorPayload payload, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !AwakenedArmor.wearsAny(serverPlayer)) {
            return;
        }
        if (AwakenedArmor.isEnabled(serverPlayer) == payload.enabled()) {
            return;
        }
        AwakenedArmorFeature.ARMOR_ENABLED.set(serverPlayer, payload.enabled());
        TtNetwork.sendToPlayer(serverPlayer, new ArmorStatusPayload(payload.enabled()));
    }

    @Override
    public Type<ToggleArmorPayload> type() {
        return TYPE;
    }
}
