package thaumictinkerer.gear;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import thaumictinkerer.Config;

public class ShareTomeRecipe extends ShapedRecipe {
    public static final MapCodec<ShareTomeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
        CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
        ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
        ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.resultTemplate)
    ).apply(instance, ShareTomeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShareTomeRecipe> STREAM_CODEC = StreamCodec.composite(
        Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
        CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
        ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
        ItemStackTemplate.STREAM_CODEC, recipe -> recipe.resultTemplate,
        ShareTomeRecipe::new
    );

    public static final RecipeSerializer<ShareTomeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final ItemStackTemplate resultTemplate;

    public ShareTomeRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
        this.resultTemplate = result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) SERIALIZER;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return Config.shareTomeSurvival() && super.matches(input, level);
    }
}
