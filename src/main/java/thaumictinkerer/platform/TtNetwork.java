package thaumictinkerer.platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import thaumictinkerer.ThaumicTinkerer;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID)
public final class TtNetwork {
    private static final List<Consumer<PayloadRegistrar>> REGISTRATIONS = new ArrayList<>();
    private static final Map<CustomPacketPayload.Type<?>, Consumer<?>> CLIENT_HANDLERS = new HashMap<>();

    private TtNetwork() {
    }

    @FunctionalInterface
    public interface Handler<T extends CustomPacketPayload> {
        void handle(T payload, Player player);
    }

    public static <T extends CustomPacketPayload> void toClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        REGISTRATIONS.add(registrar -> registrar.playToClient(type, codec, (payload, context) -> dispatch(type, payload)));
    }

    public static <T extends CustomPacketPayload> void setClientHandler(CustomPacketPayload.Type<T> type, Consumer<T> handler) {
        CLIENT_HANDLERS.put(type, handler);
    }

    @SuppressWarnings("unchecked")
    private static <T extends CustomPacketPayload> void dispatch(CustomPacketPayload.Type<T> type, T payload) {
        Consumer<T> handler = (Consumer<T>) CLIENT_HANDLERS.get(type);
        if (handler != null) {
            handler.accept(payload);
        }
    }

    public static <T extends CustomPacketPayload> void toServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Handler<T> handler) {
        REGISTRATIONS.add(registrar -> registrar.playToServer(type, codec, (payload, context) -> handler.handle(payload, context.player())));
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        for (Consumer<PayloadRegistrar> registration : REGISTRATIONS) {
            registration.accept(registrar);
        }
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToTracking(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    public static void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    public static void sendToNear(ServerLevel level, ServerPlayer excluded, double x, double y, double z, double radius, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersNear(level, excluded, x, y, z, radius, payload);
    }

    public static void sendToTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, payload);
    }

    public static void sendToAll(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }
}
