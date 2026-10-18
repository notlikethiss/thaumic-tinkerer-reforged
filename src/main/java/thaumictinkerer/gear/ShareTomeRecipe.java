package thaumictinkerer.gear;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import thaumictinkerer.Config;

public class ShareTomeRecipe extends ShapedRecipe {
    public static final RecipeSerializer<ShareTomeRecipe> SERIALIZER = new RecipeSerializer<>() {
        private final ShapedRecipe.Serializer delegate = new ShapedRecipe.Serializer();

        public MapCodec<ShareTomeRecipe> codec() {
            return delegate.codec().xmap(ShareTomeRecipe::copy, recipe -> recipe);
        }

        public StreamCodec<RegistryFriendlyByteBuf, ShareTomeRecipe> streamCodec() {
            return delegate.streamCodec().map(ShareTomeRecipe::copy, recipe -> recipe);
        }
    };

    public ShareTomeRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean notification) {
        super(group, category, pattern, result, notification);
    }

    private static ShareTomeRecipe copy(ShapedRecipe recipe) {
        return new ShareTomeRecipe(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.getResultItem(null), recipe.showNotification());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return Config.shareTomeSurvival() && super.matches(input, level);
    }
}
