package thaumictinkerer.kami.gateway;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;
import thaumcraft.blockentity.TcBlockEntity;

public class WarpGateBlockEntity extends TcBlockEntity implements Container, MenuProvider {
    public static final int SIZE = 10;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private boolean locked;
    private long teleportedAt = Long.MIN_VALUE;

    public WarpGateBlockEntity(BlockPos pos, BlockState state) {
        super(GatewayFeature.WARP_GATE_ENTITY.get(), pos, state);
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if (this.locked != locked) {
            this.locked = locked;
            sync();
        }
    }

    public AABB standingZone() {
        return new AABB(worldPosition.getX(), worldPosition.getY() + 1.0, worldPosition.getZ(), worldPosition.getX() + 1.0, worldPosition.getY() + 1.5, worldPosition.getZ() + 1.0);
    }

    public boolean isStandingOn(Player player) {
        return player.getBoundingBox().intersects(standingZone().inflate(0.5));
    }

    public void teleportPlayer(ServerPlayer player, int index) {
        if (level == null || teleportedAt == level.getGameTime() || index < 0 || index >= SIZE || !isStandingOn(player)) {
            return;
        }
        PearlLocation location = SkyPearlItem.location(items.get(index));
        if (location != null && GatewayTeleport.teleport(player, location)) {
            teleportedAt = level.getGameTime();
        }
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            sync();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        sync();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof SkyPearlItem;
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
            && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, this);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumictinkerer.warp_gate");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new WarpGateMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        locked = input.getBooleanOr("locked", false);
        items.clear();
        ContainerHelper.loadAllItems(input.tag(), items, input.lookup());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("locked", locked);
        ContainerHelper.saveAllItems(output.tag(), items, true, output.lookup());
    }
}
