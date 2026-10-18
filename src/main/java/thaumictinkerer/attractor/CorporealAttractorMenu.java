package thaumictinkerer.attractor;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

public class CorporealAttractorMenu extends AbstractContainerMenu {
    public static final int FILTER_SLOT = 0;
    public static final int PLAYER_SLOTS_END = 37;

    private final Container container;
    private final ContainerData data;
    private final @Nullable CorporealAttractorBlockEntity attractor;

    public CorporealAttractorMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(1), new SimpleContainerData(1), null);
    }

    public CorporealAttractorMenu(int containerId, Inventory inventory, CorporealAttractorBlockEntity attractor) {
        this(containerId, inventory, attractor, new ContainerData() {
            @Override
            public int get(int index) {
                return attractor.isAdult() ? 1 : 0;
            }

            @Override
            public void set(int index, int value) {
                attractor.setAdult(value != 0);
            }

            @Override
            public int getCount() {
                return 1;
            }
        }, attractor);
    }

    private CorporealAttractorMenu(int containerId, Inventory inventory, Container container, ContainerData data, @Nullable CorporealAttractorBlockEntity attractor) {
        super(AttractorFeature.CORPOREAL_MENU.get(), containerId);
        this.container = container;
        this.data = data;
        this.attractor = attractor;
        checkContainerSize(container, 1);
        addSlot(new FilterSlot(container, FILTER_SLOT, 53, 37));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    public boolean isAdult() {
        return data.get(0) != 0;
    }

    public ItemStack filter() {
        return container.getItem(FILTER_SLOT);
    }

    public void setAdult(boolean adult) {
        if (attractor != null) {
            attractor.setAdult(adult);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        if (!(stack.getItem() instanceof SoulMouldItem)) {
            return ItemStack.EMPTY;
        }
        ItemStack original = stack.copy();
        if (index < 1) {
            if (!moveItemStackTo(stack, 1, PLAYER_SLOTS_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    private static final class FilterSlot extends Slot {
        private FilterSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof SoulMouldItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
