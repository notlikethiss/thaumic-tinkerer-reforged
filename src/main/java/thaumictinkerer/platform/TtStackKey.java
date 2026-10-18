package thaumictinkerer.platform;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

public final class TtStackKey<T> {
    private final Supplier<DataComponentType<T>> type;

    TtStackKey(Supplier<DataComponentType<T>> type) {
        this.type = type;
    }

    public static <T> TtStackKey<T> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return TtStackData.register(name, codec, streamCodec);
    }

    public @Nullable T get(ItemStack stack) {
        return stack.get(type.get());
    }

    public T getOrDefault(ItemStack stack, T fallback) {
        return stack.getOrDefault(type.get(), fallback);
    }

    public boolean has(ItemStack stack) {
        return stack.has(type.get());
    }

    public void set(ItemStack stack, @Nullable T value) {
        stack.set(type.get(), value);
    }

    public void remove(ItemStack stack) {
        stack.remove(type.get());
    }

    public void update(ItemStack stack, T fallback, UnaryOperator<T> operator) {
        stack.update(type.get(), fallback, operator);
    }

    public Item.Properties apply(Item.Properties properties, T value) {
        return properties.component(type.get(), value);
    }
}
