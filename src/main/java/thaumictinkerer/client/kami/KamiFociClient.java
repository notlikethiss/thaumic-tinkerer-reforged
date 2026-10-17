package thaumictinkerer.client.kami;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumcraft.client.platform.WorldFxRenderer;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.kami.foci.KamiFociFeature;
import thaumictinkerer.kami.foci.ShadowbeamFocus;
import thaumictinkerer.kami.foci.XpDrainFocus;
import thaumictinkerer.network.ShadowbeamPayload;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class KamiFociClient {
    private static final float PARTICLE_SIZE = 4.0F;

    private KamiFociClient() {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        XpDrainFocus.useClientTicks(() -> {
            Player player = Minecraft.getInstance().player;
            return player == null ? 0 : player.tickCount;
        });
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(KamiFociFeature.BEAM.get(), NoopRenderer::new);
    }

    public static void handleShadowbeam(ShadowbeamPayload payload, Player player) {
        List<Vec3> path = payload.path();
        RandomSource random = player.level().getRandom();
        double first = (ShadowbeamFocus.Beam.INITIAL_OFFSET + 1) * ShadowbeamFocus.Beam.STEP;
        double last = (ShadowbeamFocus.Beam.MAX_TICKS) * ShadowbeamFocus.Beam.STEP;
        double distance = 0.0;
        double next = first;
        for (int index = 0; index + 1 < path.size() && next <= last; index++) {
            Vec3 from = path.get(index);
            Vec3 to = path.get(index + 1);
            double length = from.distanceTo(to);
            if (length <= 0.0) {
                continue;
            }
            while (next <= distance + length && next <= last) {
                Vec3 point = from.lerp(to, (next - distance) / length);
                WorldFxRenderer.add(new ShadowFx(random, point.x, point.y, point.z, PARTICLE_SIZE));
                next += ShadowbeamFocus.Beam.STEP;
            }
            distance += length;
        }
    }
}
