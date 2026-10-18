package thaumictinkerer.client.kami;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.client.platform.TtGuiBlit;
import thaumictinkerer.kami.tool.AwakenedToolItem;
import thaumictinkerer.kami.tool.SoulHeartHandler;
import thaumictinkerer.kami.tool.ToolHandler;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class AwakenedToolsClient {
    private static final ResourceLocation SOUL_HEARTS = ThaumicTinkerer.id("textures/gui/soulhearts.png");
    private static final int TOOLTIP_TICKS = 400;
    private static final int TEXTURE_SIZE = 16;
    private static final int HEART_SIZE = 9;
    private static final int HEART_SPACING = 8;
    private static final int HALF_HEART_WIDTH = 7;

    private static Component tooltip;
    private static int tooltipTicks;
    private static int elapsedTicks;
    private static Item lastItem;
    private static int lastMode;

    private AwakenedToolsClient() {
    }

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, ThaumicTinkerer.id("soul_hearts"), AwakenedToolsClient::renderSoulHearts);
        event.registerAbove(VanillaGuiLayers.HOTBAR, ThaumicTinkerer.id("tool_mode"), AwakenedToolsClient::renderToolMode);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        elapsedTicks++;
        if (tooltipTicks > 0) {
            tooltipTicks--;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            lastItem = null;
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof AwakenedToolItem tool)) {
            lastItem = null;
            return;
        }
        int mode = ToolHandler.getMode(held);
        if (lastItem == tool && lastMode != mode) {
            setTooltip(ToolHandler.modeName(tool.kind(), mode));
        }
        lastItem = tool;
        lastMode = mode;
    }

    private static void setTooltip(Component text) {
        tooltip = text;
        tooltipTicks = TOOLTIP_TICKS;
    }

    private static void renderToolMode(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (tooltipTicks <= 0 || tooltip == null) {
            return;
        }
        int opacity = Math.min((int) (tooltipTicks * 256.0F / 10.0F), 160);
        if (opacity <= 0) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int x = (graphics.guiWidth() - font.width(tooltip)) / 2;
        int y = graphics.guiHeight() - 72;
        float hue = Mth.frac((float) Math.cos(elapsedTicks / 250.0D));
        int color = FastColor.ARGB32.color(opacity, Mth.hsvToRgb(hue, 0.6F, 1.0F));
        graphics.drawString(font, tooltip, x, y, color, true);
    }

    private static void renderSoulHearts(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()) {
            return;
        }
        int hearts = SoulHeartHandler.getHP(player);
        if (hearts <= 0) {
            return;
        }
        int x = graphics.guiWidth() / 2 + 10;
        int y = graphics.guiHeight() - 29;
        int drawn = 0;
        for (int index = 0; index < hearts; index++) {
            boolean half = index == hearts - 1 && hearts % 2 != 0;
            if (half || index % 2 == 0) {
                int left = x + drawn * HEART_SPACING;
                if (half) {
                    TtGuiBlit.blit(graphics, SOUL_HEARTS, left, y, HEART_SIZE, 0, HALF_HEART_WIDTH, HEART_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
                } else {
                    TtGuiBlit.blit(graphics, SOUL_HEARTS, left, y, 0, 0, HEART_SIZE, HEART_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
                }
                drawn++;
            }
        }
    }
}
