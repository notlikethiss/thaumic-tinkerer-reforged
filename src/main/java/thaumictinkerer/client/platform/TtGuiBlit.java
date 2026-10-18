package thaumictinkerer.client.platform;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class TtGuiBlit {
    private TtGuiBlit() {
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        graphics.blit(texture, x, y, (float) u, (float) v, width, height, 256, 256);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(texture, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight);
    }
}
