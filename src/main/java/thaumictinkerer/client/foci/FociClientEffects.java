package thaumictinkerer.client.foci;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.client.fx.WandBeamFx;
import thaumcraft.client.fx.WispFx;
import thaumcraft.client.platform.WorldFxRenderer;
import thaumcraft.fx.Fx;
import thaumictinkerer.network.FociEffectPayload;

public final class FociClientEffects {
    private static final float[] BEAM_COLOR = {1.0F, 0.0F, 0.0F};
    private static final Map<Integer, WandBeamFx> BEAMS = new HashMap<>();

    private FociClientEffects() {
    }

    public static void handle(FociEffectPayload payload, Player player) {
        Level level = player.level();
        switch (payload.kind()) {
            case FociEffectPayload.PICK_BURST -> burst(level, payload, 0.0F);
            case FociEffectPayload.PLACE_BURST -> burst(level, payload, 0.65F);
            case FociEffectPayload.SMELT_BEAM -> beam(level, payload);
            case FociEffectPayload.SMELT_PROGRESS -> wisps(level, payload, 2);
            case FociEffectPayload.SMELT_DONE -> wisps(level, payload, 25);
            default -> {
            }
        }
    }

    private static void burst(Level level, FociEffectPayload payload, float lift) {
        RandomSource random = level.getRandom();
        for (int index = 0; index < 8; index++) {
            Fx.get().golemOrbBurst(level, payload.x() + random.nextDouble(), payload.y() + random.nextDouble() + lift, payload.z() + random.nextDouble(), 0.2F);
        }
    }

    private static void wisps(Level level, FociEffectPayload payload, int count) {
        RandomSource random = level.getRandom();
        for (int index = 0; index < count; index++) {
            WispFx fx = new WispFx(random, payload.x() + random.nextDouble(), payload.y() + random.nextDouble(), payload.z() + random.nextDouble(), random.nextFloat() / 2.0F, 4, null);
            fx.shrink(true);
            fx.gravity(-random.nextFloat() / 10.0F);
            WorldFxRenderer.add(fx);
        }
    }

    private static void beam(Level level, FociEffectPayload payload) {
        Entity source = level.getEntity(payload.entityId());
        if (source == null) {
            return;
        }
        double x = payload.x() + 0.5;
        double y = payload.y() + 0.5;
        double z = payload.z() + 0.5;
        WandBeamFx beam = BEAMS.get(source.getId());
        if (beam != null && beam.isFor(source)) {
            beam.retarget(x, y, z);
        } else {
            beam = new WandBeamFx(level, source, x, y, z, BEAM_COLOR[0], BEAM_COLOR[1], BEAM_COLOR[2], 5);
            beam.setType(2);
            beam.setReverse(false);
            WorldFxRenderer.add(beam);
            BEAMS.put(source.getId(), beam);
        }
        beam.setEndMod(2.0F);
        beam.setImpact(1);
        BEAMS.values().removeIf(WandBeamFx::isDead);
    }
}
