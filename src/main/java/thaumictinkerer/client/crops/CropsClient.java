package thaumictinkerer.client.crops;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import thaumcraft.fx.Fx;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.crops.CropsFeature;
import thaumictinkerer.network.CropFxPayload;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class CropsClient {
    private static final int TRAILS = 50;

    private CropsClient() {
    }

    @SubscribeEvent
    static void registerItemTints(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(ThaumicTinkerer.id("seed_aspect"), SeedAspectTint.MAP_CODEC);
    }

    @SubscribeEvent
    static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(new CropAspectTint()), CropsFeature.INFUSED_GRAIN.get());
    }

    public static void handle(CropFxPayload payload, Player player) {
        for (int index = 0; index < TRAILS; index++) {
            Fx.get().essentiaTrail(player.level(), payload.from(), payload.to(), TRAILS, payload.color(), 1.0F);
        }
    }
}
