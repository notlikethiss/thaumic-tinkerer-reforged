package thaumictinkerer.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.gear.ShareTomeRecipe;

public final class ModRecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ThaumicTinkerer.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShareTomeRecipe>> SHARE_TOME = SERIALIZERS.register("share_tome", () -> ShareTomeRecipe.SERIALIZER);

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
