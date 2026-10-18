package thaumictinkerer.client.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.crops.CropsFeature;
import thaumictinkerer.crops.InfusedSeedsItem;

@JeiPlugin
public final class ThaumicTinkererJeiPlugin implements IModPlugin {
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
    }
}
