package thaumictinkerer.platform;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.Nullable;

public final class TtDataKey<T> {
    private final Supplier<AttachmentType<T>> type;

    TtDataKey(Supplier<AttachmentType<T>> type) {
        this.type = type;
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
        push(holder);
    }

    public void sync(LevelChunk holder) {
        push(holder);
    }

    public void sync(Level holder) {
        push(holder);
    }

    private T read(IAttachmentHolder holder) {
        return holder.getData(type.get());
    }

    private void write(IAttachmentHolder holder, T value) {
        holder.setData(type.get(), value);
    }

    private boolean exists(IAttachmentHolder holder) {
        return holder.hasData(type.get());
    }

    private void delete(IAttachmentHolder holder) {
        holder.removeData(type.get());
    }

    private void push(IAttachmentHolder holder) {
        holder.syncData(type.get());
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
            return new TtDataKey<>(TtAttachments.register(name, () -> {
                AttachmentType.Builder<T> builder = AttachmentType.builder(defaultValue);
                if (codec != null) {
                    builder.serialize(codec.fieldOf("value"));
                    if (copyOnDeath) {
                        builder.copyOnDeath();
                    }
                }
                if (allSync != null) {
                    builder.sync(allSync);
                } else if (selfSync != null) {
                    builder.sync((holder, player) -> holder == player, selfSync);
                }
                return builder.build();
            }));
        }
    }
}
