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
import javax.annotation.Nullable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;

public final class TtTransfer {
    private static final List<Proxy<?>> PROXIES = new ArrayList<>();

    private TtTransfer() {
    }

    private record Proxy<B extends BlockEntity>(Supplier<? extends BlockEntityType<B>> type, Function<B, BlockEntity> target) {
    }

    public static <B extends BlockEntity> void exposeProxy(Supplier<? extends BlockEntityType<B>> type, Function<B, BlockEntity> target) {
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
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, proxy.type().get(), (entity, side) -> new IItemHandler() {
            private IItemHandler target() {
                return resolve(Capabilities.ItemHandler.BLOCK, proxy, entity, side, net.neoforged.neoforge.items.wrapper.EmptyItemHandler.INSTANCE);
            }
            public int getSlots() { return target().getSlots(); }
            public ItemStack getStackInSlot(int slot) { return target().getStackInSlot(slot); }
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return target().insertItem(slot, stack, simulate); }
            public ItemStack extractItem(int slot, int amount, boolean simulate) { return target().extractItem(slot, amount, simulate); }
            public int getSlotLimit(int slot) { return target().getSlotLimit(slot); }
            public boolean isItemValid(int slot, ItemStack stack) { return target().isItemValid(slot, stack); }
        });
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, proxy.type().get(), (entity, side) -> new IFluidHandler() {
            private IFluidHandler target() {
                return resolve(Capabilities.FluidHandler.BLOCK, proxy, entity, side, net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler.INSTANCE);
            }
            public int getTanks() { return target().getTanks(); }
            public FluidStack getFluidInTank(int tank) { return target().getFluidInTank(tank); }
            public int getTankCapacity(int tank) { return target().getTankCapacity(tank); }
            public boolean isFluidValid(int tank, FluidStack stack) { return target().isFluidValid(tank, stack); }
            public int fill(FluidStack resource, FluidAction action) { return target().fill(resource, action); }
            public FluidStack drain(FluidStack resource, FluidAction action) { return target().drain(resource, action); }
            public FluidStack drain(int amount, FluidAction action) { return target().drain(amount, action); }
        });
    }

    private static <B extends BlockEntity, H> H resolve(BlockCapability<H, Direction> capability, Proxy<B> proxy, B entity, @Nullable Direction side, H empty) {
        BlockEntity target = proxy.target().apply(entity);
        if (target == null || target.isRemoved() || target.getLevel() == null || !target.getLevel().hasChunkAt(target.getBlockPos())) {
            return empty;
        }
        H handler = target.getLevel().getCapability(capability, target.getBlockPos(), target.getBlockState(), target, side);
        return handler == null ? empty : handler;
    }
}
