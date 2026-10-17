package thaumictinkerer.kami.trinkets;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.platform.Baubles;

public final class BlockStorage {
    private BlockStorage() {
    }

    public static Item resolve(Identifier id) {
        if (id == null) {
            return Items.AIR;
        }
        return BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
    }

    public static Identifier idOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static boolean matches(ItemStack stack, Item item) {
        return !stack.isEmpty() && stack.is(item) && ItemStack.isSameItemSameComponents(stack, new ItemStack(item));
    }

    private static List<ItemStack> rings(Player player) {
        List<ItemStack> rings = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof BlackHoleRingItem) {
                rings.add(stack);
            }
        }
        for (ItemStack stack : Baubles.stacks(player)) {
            if (stack.getItem() instanceof BlackHoleRingItem) {
                rings.add(stack);
            }
        }
        return rings;
    }

    public static long count(Player player, Item item) {
        long total = 0;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (matches(stack, item)) {
                total += stack.getCount();
            }
        }
        for (ItemStack ring : rings(player)) {
            if (BlackHoleRingItem.getItem(ring) == item) {
                total += BlackHoleRingItem.getCount(ring);
            }
        }
        return total;
    }

    public static boolean consume(Player player, Item item, int amount) {
        if (count(player, item) < amount) {
            return false;
        }
        int remaining = amount;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (matches(stack, item)) {
                int taken = Math.min(remaining, stack.getCount());
                stack.shrink(taken);
                remaining -= taken;
            }
        }
        for (ItemStack ring : rings(player)) {
            if (remaining <= 0) {
                break;
            }
            if (BlackHoleRingItem.getItem(ring) == item) {
                remaining -= BlackHoleRingItem.remove(ring, remaining);
            }
        }
        inventory.setChanged();
        return remaining == 0;
    }
}
