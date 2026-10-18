package thaumictinkerer.platform;

import io.netty.buffer.Unpooled;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import thaumictinkerer.ThaumicTinkerer;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID)
public final class TtAttachments {
    public record SyncPayload(String key, int entityId, byte[] data) implements CustomPacketPayload {
        public static final Type<SyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ThaumicTinkerer.MODID, "attachment_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncPayload::key,
            ByteBufCodecs.VAR_INT, SyncPayload::entityId,
            ByteBufCodecs.BYTE_ARRAY, SyncPayload::data,
            SyncPayload::new
        );

        @Override
        public Type<SyncPayload> type() {
            return TYPE;
        }
    }

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ThaumicTinkerer.MODID);

    private TtAttachments() {
    }

    static <T> Supplier<AttachmentType<T>> register(String name, Supplier<AttachmentType<T>> factory) {
        return ATTACHMENTS.register(name, factory);
    }

    static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
        TtNetwork.toClient(SyncPayload.TYPE, SyncPayload.STREAM_CODEC);
    }

    static <T> void sendSync(TtDataKey<T> key, Entity holder, boolean toTracking) {
        SyncPayload payload = encodeSync(key, holder);
        if (payload == null) {
            return;
        }
        if (toTracking) {
            TtNetwork.sendToTrackingAndSelf(holder, payload);
        } else if (holder instanceof ServerPlayer player) {
            TtNetwork.sendToPlayer(player, payload);
        }
    }

    static <T> void sendSync(TtDataKey<T> key, Entity holder, ServerPlayer player) {
        SyncPayload payload = encodeSync(key, holder);
        if (payload != null) {
            TtNetwork.sendToPlayer(player, payload);
        }
    }

    private static <T> SyncPayload encodeSync(TtDataKey<T> key, Entity holder) {
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec = key.syncCodec();
        if (codec == null) {
            return null;
        }
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), holder.registryAccess());
        try {
            codec.encode(buffer, key.current(holder));
            byte[] data = new byte[buffer.readableBytes()];
            buffer.readBytes(data);
            return new SyncPayload(key.name(), holder.getId(), data);
        } finally {
            buffer.release();
        }
    }

    public static <T> void receive(SyncPayload payload, Player player) {
        @SuppressWarnings("unchecked")
        TtDataKey<T> key = (TtDataKey<T>) TtDataKey.KEYS.get(payload.key());
        Entity holder = player.level().getEntity(payload.entityId());
        if (key == null || holder == null) {
            return;
        }
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec = key.syncCodec();
        if (codec == null) {
            return;
        }
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload.data()), player.registryAccess());
        try {
            key.receive(holder, codec.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    private static void syncAll(Entity holder) {
        for (TtDataKey<?> key : TtDataKey.KEYS.values()) {
            key.sync(holder);
        }
    }

    @SubscribeEvent
    static void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        if (event.getEntity() instanceof ServerPlayer player) {
            for (TtDataKey<?> key : TtDataKey.KEYS.values()) {
                key.syncToTracker(target, player);
            }
        }
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncAll(event.getEntity());
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncAll(event.getEntity());
    }

    @SubscribeEvent
    static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncAll(event.getEntity());
    }
}
