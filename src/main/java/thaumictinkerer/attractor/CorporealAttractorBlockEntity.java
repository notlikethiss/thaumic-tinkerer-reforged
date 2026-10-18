package thaumictinkerer.attractor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;

public class CorporealAttractorBlockEntity extends AttractorBlockEntity implements Container, MenuProvider {
    private boolean adult = true;
    private ItemStack filter = ItemStack.EMPTY;

    public CorporealAttractorBlockEntity(BlockPos pos, BlockState state) {
        super(AttractorFeature.CORPOREAL_ENTITY.get(), pos, state);
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        if (this.adult != adult) {
            this.adult = adult;
            sync();
        }
    }

    public ItemStack filter() {
        return filter;
    }

    @Override
    public boolean applies(Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity instanceof Player) {
            return false;
        }
        boolean can = true;
        if (entity instanceof AgeableMob ageable) {
            can = adult != ageable.isBaby();
        }
        if (can && !filter.isEmpty()) {
            return SoulMouldItem.getPatternName(filter).equals(SoulMouldItem.patternOf(living));
        }
        return can;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return filter.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? filter : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || filter.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = filter.split(amount);
        sync();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = filter;
        filter = ItemStack.EMPTY;
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            filter = stack;
            stack.limitSize(getMaxStackSize(stack));
            sync();
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof SoulMouldItem;
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
            && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        filter = ItemStack.EMPTY;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide() && !filter.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), filter);
            filter = ItemStack.EMPTY;
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumictinkerer.corporeal_attractor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CorporealAttractorMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        adult = input.getBooleanOr("adultCheck", true);
        NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input.tag(), items, input.lookup());
        filter = items.get(0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("adultCheck", adult);
        ContainerHelper.saveAllItems(output.tag(), NonNullList.of(ItemStack.EMPTY, filter), true, output.lookup());
    }
}
