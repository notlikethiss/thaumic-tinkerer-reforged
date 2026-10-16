package thaumictinkerer.enchanting;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import thaumictinkerer.ThaumicTinkerer;

public final class TtEnchantments {
    public static final ResourceKey<Enchantment> ASCENT_BOOST = key("ascent_boost");
    public static final ResourceKey<Enchantment> SLOW_FALL = key("slow_fall");
    public static final ResourceKey<Enchantment> AUTO_SMELT = key("auto_smelt");
    public static final ResourceKey<Enchantment> DESINTEGRATE = key("desintegrate");
    public static final ResourceKey<Enchantment> QUICK_DRAW = key("quick_draw");
    public static final ResourceKey<Enchantment> VAMPIRISM = key("vampirism");
    public static final ResourceKey<Enchantment> DISPERSED_STRIKE = key("dispersed_strike");
    public static final ResourceKey<Enchantment> SHATTER = key("shatter");
    public static final ResourceKey<Enchantment> FINAL_STRIKE = key("final_strike");
    public static final ResourceKey<Enchantment> FOCUSED_STRIKE = key("focused_strike");
    public static final ResourceKey<Enchantment> POUNCE = key("pounce");
    public static final ResourceKey<Enchantment> SHOCKWAVE = key("shockwave");
    public static final ResourceKey<Enchantment> TUNNEL = key("tunnel");
    public static final ResourceKey<Enchantment> VALIANCE = key("valiance");

    public static final List<ResourceKey<Enchantment>> ALL = List.of(
        ASCENT_BOOST, SLOW_FALL, AUTO_SMELT, DESINTEGRATE, QUICK_DRAW, VAMPIRISM, DISPERSED_STRIKE,
        SHATTER, FINAL_STRIKE, FOCUSED_STRIKE, POUNCE, SHOCKWAVE, TUNNEL, VALIANCE
    );

    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private TtEnchantments() {
    }

    public static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ThaumicTinkerer.id(name));
    }

    public static Optional<Holder.Reference<Enchantment>> holder(HolderLookup.Provider access, ResourceKey<Enchantment> key) {
        return access.lookupOrThrow(Registries.ENCHANTMENT).get(key);
    }

    public static int level(RegistryAccess access, ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) {
            return 0;
        }
        return holder(access, key).map(stack::getEnchantmentLevel).orElse(0);
    }

    public static int maxArmorLevel(LivingEntity entity, ResourceKey<Enchantment> key) {
        int result = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            result = Math.max(result, level(entity.level().registryAccess(), entity.getItemBySlot(slot), key));
        }
        return result;
    }
}
