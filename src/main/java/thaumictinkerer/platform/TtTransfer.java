package thaumictinkerer.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

public final class TtTransfer {
    private static final List<Proxy<?>> PROXIES = new ArrayList<>();

    private TtTransfer() {
    }

    private record Proxy<B extends BlockEntity>(Supplier<? extends BlockEntityType<B>> type, Function<B, @Nullable BlockEntity> target) {
    }

    public static <B extends BlockEntity> void exposeProxy(Supplier<? extends BlockEntityType<B>> type, Function<B, @Nullable BlockEntity> target) {
        PROXIES.add(new Proxy<>(type, target));
    }

    static void register(IEventBus bus) {
        bus.addListener(TtTransfer::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Proxy<?> proxy : PROXIES) {
            registerProxy(event, proxy);
        }
    }

    private static <B extends BlockEntity> void registerProxy(RegisterCapabilitiesEvent event, Proxy<B> proxy) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, proxy.type().get(), (entity, side) -> delegating(Capabilities.Item.BLOCK, proxy, entity, side));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, proxy.type().get(), (entity, side) -> delegating(Capabilities.Fluid.BLOCK, proxy, entity, side));
    }

    private static <B extends BlockEntity, R extends Resource> ResourceHandler<R> delegating(
        BlockCapability<ResourceHandler<R>, @Nullable Direction> capability,
        Proxy<B> proxy,
        B entity,
        @Nullable Direction side
    ) {
        return new DelegatingResourceHandler<>(() -> resolve(capability, proxy, entity, side));
    }

    private static <B extends BlockEntity, R extends Resource> ResourceHandler<R> resolve(
        BlockCapability<ResourceHandler<R>, @Nullable Direction> capability,
        Proxy<B> proxy,
        B entity,
        @Nullable Direction side
    ) {
        BlockEntity target = proxy.target().apply(entity);
        if (target == null || target.getLevel() == null) {
            return EmptyResourceHandler.instance();
        }
        ResourceHandler<R> handler = target.getLevel().getCapability(capability, target.getBlockPos(), target.getBlockState(), target, side);
        return handler == null ? EmptyResourceHandler.instance() : handler;
    }
}
