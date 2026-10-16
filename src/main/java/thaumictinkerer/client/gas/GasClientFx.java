package thaumictinkerer.client.gas;

import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import thaumcraft.client.fx.WispFx;
import thaumcraft.client.platform.WorldFxRenderer;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.gas.GasFx;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class GasClientFx implements GasFx.Handler {
    private GasClientFx() {
    }

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        GasFx.set(new GasClientFx());
    }

    @Override
    public void shadowWisp(Level level, double x, double y, double z) {
        WispFx fx = new WispFx(level.getRandom(), x, y, z, 0.125F, 5, null);
        fx.shrink(true);
        fx.gravity(-0.02F);
        WorldFxRenderer.add(fx);
    }
}
