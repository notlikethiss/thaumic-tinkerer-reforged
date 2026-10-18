package thaumictinkerer.client.platform;

import java.util.List;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.gear.GearFeature;
import thaumictinkerer.kami.trinkets.TrinketsFeature;
import thaumictinkerer.crops.CropsFeature;
import thaumictinkerer.crops.InfusedSeedsItem;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class TtItemProperties {
    @SubscribeEvent
    static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (var item : List.of(GearFeature.CLEANSING_TALISMAN, GearFeature.XP_TALISMAN, GearFeature.BLOOD_SWORD)) {
                ItemProperties.register(item.get(), ThaumicTinkerer.id("active"), (stack, level, entity, seed) -> GearFeature.ACTIVE.has(stack) ? 1.0F : 0.0F);
            }
            ItemProperties.register(TrinketsFeature.BLOCK_TALISMAN.get(), ThaumicTinkerer.id("active"), (stack, level, entity, seed) -> TrinketsFeature.RING_ACTIVE.has(stack) ? 1.0F : 0.0F);
            ItemProperties.register(CropsFeature.INFUSED_SEEDS.get(), ThaumicTinkerer.id("seed_aspect"), (stack, level, entity, seed) -> switch (InfusedSeedsItem.getAspect(stack).getTag()) {
                case "aer" -> 1.0F;
                case "ignis" -> 2.0F;
                case "aqua" -> 3.0F;
                case "terra" -> 4.0F;
                case "ordo" -> 5.0F;
                case "perditio" -> 6.0F;
                default -> 0.0F;
            });
        });
    }
}
