package thaumictinkerer.client.camo;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import thaumictinkerer.ThaumicTinkerer;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class CamoModels {
    private CamoModels() {
    }

    @SubscribeEvent
    static void registerModels(RegisterBlockStateModels event) {
        event.registerModel(ThaumicTinkerer.id("camo"), CamoBlockModel.Unbaked.MAP_CODEC);
    }
}
