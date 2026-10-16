package thaumictinkerer.platform;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumictinkerer.ThaumicTinkerer;

public final class TtStackData {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ThaumicTinkerer.MODID);

    private TtStackData() {
    }

    public static <T> TtStackKey<T> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return new TtStackKey<>(COMPONENTS.registerComponentType(name, builder -> builder.persistent(codec).networkSynchronized(streamCodec)));
    }

    public static <T> TtStackKey<T> registerTransient(String name) {
        return new TtStackKey<>(COMPONENTS.<T>registerComponentType(name, builder -> builder));
    }

    static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
