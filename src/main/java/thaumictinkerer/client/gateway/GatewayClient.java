package thaumictinkerer.client.gateway;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import thaumcraft.fx.Fx;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.kami.gateway.GatewayFeature;
import thaumictinkerer.kami.gateway.SkyPearlItem;
import thaumictinkerer.kami.gateway.WarpGateBlockEntity;
import thaumictinkerer.network.GatewayFxPayload;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class GatewayClient {
    private static final int SPARKLES = 20;
    private static final float SPARKLE_SIZE = 1.5F;
    private static final int SPARKLE_COLOR = 6;

    private GatewayClient() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(GatewayFeature.WARP_GATE_MENU.get(), WarpGateScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(GatewayFeature.WARP_GATE_ENTITY.get(), WarpGateRenderer::new);
    }

    public static void handleFx(GatewayFxPayload payload, Player player) {
        Level level = player.level();
        RandomSource random = level.getRandom();
        for (int index = 0; index < SPARKLES; index++) {
            Fx.get().sparkle(
                level,
                (float) payload.x() + random.nextFloat() - 0.5F,
                (float) payload.y() + random.nextFloat(),
                (float) payload.z() + random.nextFloat() - 0.5F,
                SPARKLE_SIZE,
                SPARKLE_COLOR,
                0.0F
            );
        }
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.screen != null || !player.isShiftKeyDown()) {
            return;
        }
        AABB box = player.getBoundingBox();
        for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
            for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
                for (int y = Mth.floor(box.minY) - 2; y <= Mth.floor(box.minY); y++) {
                    if (minecraft.level.getBlockEntity(new BlockPos(x, y, z)) instanceof WarpGateBlockEntity gate && gate.standingZone().intersects(box)) {
                        minecraft.setScreen(new WarpGateDestinationsScreen(gate));
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player != null && SkyPearlItem.isAttuned(event.getItemStack())) {
            PearlInfo.add(event.getItemStack(), player.level().dimension(), player.position(), event.getToolTip(), false);
        }
    }
}
