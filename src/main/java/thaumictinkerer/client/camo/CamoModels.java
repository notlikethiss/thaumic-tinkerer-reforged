package thaumictinkerer.client.camo;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.core.registries.BuiltInRegistries;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.camo.CamoBlock;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class CamoModels {
    @SubscribeEvent
    static void wrap(ModelEvent.ModifyBakingResult event) {
        for (var block : BuiltInRegistries.BLOCK) {
            if (block instanceof CamoBlock) {
                for (var state : block.getStateDefinition().getPossibleStates()) {
                    var key = BlockModelShaper.stateToModelLocation(state);
                    event.getModels().computeIfPresent(key, (ignored, model) -> new CamoBlockModel(model));
                }
            }
        }
    }
}
