package thaumictinkerer.client.elements;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.client.fx.WispFx;
import thaumcraft.client.platform.WorldFxRenderer;
import thaumcraft.fx.Fx;
import thaumictinkerer.network.ElementsFxPayload;

public final class ElementsClientEffects {
    private static final int WISP_COUNT = 30;
    private static final double WISP_RADIUS = 2.5;

    private ElementsClientEffects() {
    }

    public static void handle(ElementsFxPayload payload, Player player) {
        Level level = player.level();
        switch (payload.kind()) {
            case ElementsFxPayload.BURST -> Fx.get().golemOrbBurst(level, payload.x(), payload.y(), payload.z(), payload.size());
            case ElementsFxPayload.SPARKLE -> Fx.get().blockSparkle(level, BlockPos.containing(payload.x(), payload.y(), payload.z()), 100, 100);
            case ElementsFxPayload.WISPS -> wisps(level, payload);
            default -> {
            }
        }
    }

    private static void wisps(Level level, ElementsFxPayload payload) {
        RandomSource random = level.getRandom();
        for (int index = 0; index < WISP_COUNT; index++) {
            double theta = random.nextFloat() * 2 * Math.PI;
            double phi = random.nextFloat() * 2 * Math.PI;
            double offsetX = WISP_RADIUS * Math.sin(theta) * Math.cos(phi);
            double offsetY = WISP_RADIUS * Math.sin(theta) * Math.sin(phi);
            double offsetZ = WISP_RADIUS * Math.cos(theta);
            WispFx fx = new WispFx(random, payload.x() + offsetX, payload.y() + offsetY + 1, payload.z() + offsetZ, 0.1F, 4, null);
            fx.shrink(true);
            fx.gravity(1.0F);
            WorldFxRenderer.add(fx);
        }
    }
}
