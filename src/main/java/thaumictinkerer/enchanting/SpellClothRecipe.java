package thaumictinkerer.enchanting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public final class SpellClothRecipe extends CustomRecipe {
    public static final SpellClothRecipe INSTANCE = new SpellClothRecipe();
    public static final RecipeSerializer<SpellClothRecipe> SERIALIZER = new RecipeSerializer<>() {
        public MapCodec<SpellClothRecipe> codec() { return MapCodec.unit(INSTANCE); }
        public StreamCodec<RegistryFriendlyByteBuf, SpellClothRecipe> streamCodec() { return StreamCodec.unit(INSTANCE); }
    };

    public SpellClothRecipe() {
        super(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean cloth = false;
        boolean enchanted = false;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.isEnchanted() && !EnchantmentManager.isNoRemoveEnchant(stack) && !enchanted) {
                enchanted = true;
            } else if (stack.is(EnchantingFeature.SPELL_CLOTH.get()) && !cloth) {
                cloth = true;
            } else {
                return false;
            }
        }
        return cloth && enchanted;
    }

    @Override
    public ItemStack assemble(CraftingInput input, net.minecraft.core.HolderLookup.Provider registries) {
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEnchanted()) {
                ItemStack result = stack.copyWithCount(1);
                result.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.is(EnchantingFeature.SPELL_CLOTH.get())) {
                ItemStack used = stack.copyWithCount(1);
                used.setDamageValue(used.getDamageValue() + 1);
                if (used.getDamageValue() < used.getMaxDamage()) {
                    result.set(slot, used);
                }
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<SpellClothRecipe> getSerializer() {
        return SERIALIZER;
    }
}
