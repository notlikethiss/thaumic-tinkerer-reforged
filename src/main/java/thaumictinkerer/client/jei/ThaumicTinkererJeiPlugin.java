package thaumictinkerer.client.jei;

import java.util.List;
import java.util.function.Consumer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import javax.annotation.Nullable;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.wand.Wands;
import thaumictinkerer.Config;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.crops.CropsFeature;
import thaumictinkerer.crops.InfusedSeedsItem;
import thaumictinkerer.kami.WandVariant;

@JeiPlugin
public final class ThaumicTinkererJeiPlugin implements IModPlugin {
    private final Consumer<ClientTickEvent.Post> tickListener = event -> updateKami();
    private @Nullable IJeiRuntime runtime;
    private @Nullable Boolean kamiEnabled;

    @Override
    public ResourceLocation getPluginUid() {
        return ThaumicTinkerer.id("jei");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(CropsFeature.INFUSED_SEEDS.get(), new ISubtypeInterpreter<ItemStack>() {
            @Override
            public Object getSubtypeData(ItemStack stack, UidContext context) {
                return InfusedSeedsItem.getAspect(stack).getTag();
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                return InfusedSeedsItem.getAspect(stack).getTag();
            }
        });
        registration.registerSubtypeInterpreter(ModItems.WAND.get(), new ISubtypeInterpreter<ItemStack>() {
            @Override
            public Object getSubtypeData(ItemStack stack, UidContext context) {
                return Wands.getParts(stack);
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                return Wands.getParts(stack).toString();
            }
        });
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        registration.addExtraItemStacks(List.of(WandVariant.SILVERWOOD.creativeStack(), WandVariant.ICHORCLOTH.creativeStack()));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new WandCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(WandCategory.TYPE, List.of(WandVariant.values()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(WandCategory.TYPE, ModBlocks.ARCANE_WORKTABLE.get());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        this.runtime = runtime;
        kamiEnabled = null;
        updateKami();
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, tickListener);
    }

    @Override
    public void onRuntimeUnavailable() {
        NeoForge.EVENT_BUS.unregister(tickListener);
        runtime = null;
        kamiEnabled = null;
    }

    private void updateKami() {
        boolean enabled = Config.kamiEnabled();
        if (runtime == null || Boolean.valueOf(enabled).equals(kamiEnabled)) {
            return;
        }
        var recipes = List.of(WandVariant.ICHORCLOTH);
        var stacks = List.of(WandVariant.ICHORCLOTH.creativeStack());
        if (enabled) {
            runtime.getRecipeManager().unhideRecipes(WandCategory.TYPE, recipes);
            if (Boolean.FALSE.equals(kamiEnabled)) {
                runtime.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, stacks);
            }
        } else {
            runtime.getRecipeManager().hideRecipes(WandCategory.TYPE, recipes);
            runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, stacks);
        }
        kamiEnabled = enabled;
    }
}
