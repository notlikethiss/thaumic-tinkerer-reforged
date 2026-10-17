package thaumictinkerer.awakenedarmor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AwakenedArmor {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private AwakenedArmor() {
    }

    public static boolean isEnabled(Player player) {
        return AwakenedArmorFeature.ARMOR_ENABLED.get(player);
    }

    public static boolean isPieceDisabled(ItemStack stack) {
        return AwakenedArmorFeature.PIECE_DISABLED.getOrDefault(stack, false);
    }

    public static boolean isActive(Player player, EquipmentSlot slot, Item item) {
        ItemStack stack = player.getItemBySlot(slot);
        return stack.is(item) && !isPieceDisabled(stack) && isEnabled(player);
    }

    public static boolean wearsAny(Player player) {
        for (EquipmentSlot slot : SLOTS) {
            if (player.getItemBySlot(slot).getItem() instanceof AwakenedArmorItem) {
                return true;
            }
        }
        return false;
    }

    public static boolean carriesLight(Player player) {
        return isActive(player, EquipmentSlot.LEGS, AwakenedArmorFeature.LEGGINGS.get());
    }
}
