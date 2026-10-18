package thaumictinkerer.platform;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import javax.annotation.Nullable;

public final class TtDataKey<T> {
    static final Map<String, TtDataKey<?>> KEYS = new HashMap<>();

    private final String name;
    private final Supplier<AttachmentType<T>> type;
    private final @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> selfSync;
    private final @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> allSync;

    TtDataKey(String name, Supplier<AttachmentType<T>> type, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> selfSync, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> allSync) {
        this.name = name;
        this.type = type;
        this.selfSync = selfSync;
        this.allSync = allSync;
        KEYS.put(name, this);
    }

    String name() {
        return name;
    }

    boolean syncsToSelf() {
        return selfSync != null || allSync != null;
    }

    void syncToTracker(Entity holder, ServerPlayer player) {
        if (allSync != null && has(holder)) {
            TtAttachments.sendSync(this, holder, player);
        }
    }

    @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec() {
        return allSync != null ? allSync : selfSync;
    }

    void receive(Entity holder, T value) {
        holder.setData(type.get(), value);
    }

    T current(Entity holder) {
        return holder.getData(type.get());
    }

    public static <T> Builder<T> builder(String name, Supplier<T> defaultValue) {
        return new Builder<>(name, defaultValue);
    }

    public T get(Entity holder) {
        return read(holder);
    }

    public T get(LevelChunk holder) {
        return read(holder);
    }

    public T get(Level holder) {
        return read(holder);
    }

    public void set(Entity holder, T value) {
        write(holder, value);
    }

    public void set(LevelChunk holder, T value) {
        write(holder, value);
    }

    public void set(Level holder, T value) {
        write(holder, value);
    }

    public boolean has(Entity holder) {
        return exists(holder);
    }

    public boolean has(LevelChunk holder) {
        return exists(holder);
    }

    public boolean has(Level holder) {
        return exists(holder);
    }

    public void remove(Entity holder) {
        delete(holder);
    }

    public void remove(LevelChunk holder) {
        delete(holder);
    }

    public void remove(Level holder) {
        delete(holder);
    }

    public void sync(Entity holder) {
        if (holder.level().isClientSide()) {
            return;
        }
        if (allSync != null) {
            TtAttachments.sendSync(this, holder, true);
        } else if (selfSync != null && holder instanceof ServerPlayer) {
            TtAttachments.sendSync(this, holder, false);
        }
    }

    public void sync(LevelChunk holder) {
    }

    public void sync(Level holder) {
    }

    private T read(IAttachmentHolder holder) {
        return holder.getData(type.get());
    }

    private void write(IAttachmentHolder holder, T value) {
        holder.setData(type.get(), value);
        if (holder instanceof Entity entity) {
            sync(entity);
        }
    }

    private boolean exists(IAttachmentHolder holder) {
        return holder.hasData(type.get());
    }

    private void delete(IAttachmentHolder holder) {
        holder.removeData(type.get());
    }

    public static final class Builder<T> {
        private final String name;
        private final Supplier<T> defaultValue;
        private @Nullable Codec<T> codec;
        private boolean copyOnDeath;
        private @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> selfSync;
        private @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> allSync;

        private Builder(String name, Supplier<T> defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public Builder<T> codec(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<T> copyOnDeath() {
            this.copyOnDeath = true;
            return this;
        }

        public Builder<T> syncToSelf(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            this.selfSync = streamCodec;
            return this;
        }

        public Builder<T> syncToAll(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            this.allSync = streamCodec;
            return this;
        }

        public TtDataKey<T> build() {
            return new TtDataKey<>(name, TtAttachments.register(name, () -> {
                AttachmentType.Builder<T> builder = AttachmentType.builder(defaultValue);
                if (codec != null) {
                    builder.serialize(codec.fieldOf("value").codec());
                    if (copyOnDeath) {
                        builder.copyOnDeath();
                    }
                }
                return builder.build();
            }), selfSync, allSync);
        }
    }
}
