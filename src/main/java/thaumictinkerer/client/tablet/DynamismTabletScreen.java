package thaumictinkerer.client.tablet;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.client.platform.TtClientNetwork;
import thaumictinkerer.client.platform.TtGuiBlit;
import thaumictinkerer.network.TabletButtonPayload;
import thaumictinkerer.tablet.DynamismTabletMenu;

public class DynamismTabletScreen extends AbstractContainerScreen<DynamismTabletMenu> {
    private static final ResourceLocation TEXTURE = ThaumicTinkerer.id("textures/gui/animationtablet.png");
    private static final int TEXT_COLOR = 0xFF999999;
    private static final int BUTTON_SIZE = 13;
    private static final int LEFT_X = 52;
    private static final int RIGHT_X = 111;
    private static final int MODE_Y = 15;
    private static final int REDSTONE_Y = 60;

    public DynamismTabletScreen(DynamismTabletMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private int redstoneX() {
        return imageWidth / 2 - 7;
    }

    private boolean over(double mouseX, double mouseY, int x, int y) {
        double offsetX = mouseX - (leftPos + x);
        double offsetY = mouseY - (topPos + y);
        return offsetX >= 0 && offsetY >= 0 && offsetX < BUTTON_SIZE && offsetY < BUTTON_SIZE;
    }

    private void button(GuiGraphics graphics, int x, int y, boolean enabled) {
        if (enabled) {
            TtGuiBlit.blit(graphics, TEXTURE, leftPos + x, topPos + y, 176, 13, BUTTON_SIZE, BUTTON_SIZE);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        TtGuiBlit.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        button(graphics, LEFT_X, MODE_Y, menu.isLeftClick());
        button(graphics, RIGHT_X, MODE_Y, !menu.isLeftClick());
        button(graphics, redstoneX(), REDSTONE_Y, menu.isRedstone());
        Component left = Component.translatable("ttinkerer.ttmisc.leftClick");
        Component redstone = Component.translatable("ttinkerer.ttmisc.redstoneControl");
        graphics.drawString(font, left, leftPos + 48 - font.width(left), topPos + 18, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("ttinkerer.ttmisc.rightClick"), leftPos + 128, topPos + 18, TEXT_COLOR, false);
        graphics.drawString(font, redstone, leftPos + imageWidth / 2 - font.width(redstone) / 2, topPos + 50, TEXT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            double x = mouseX;
            double y = mouseY;
            if (over(x, y, LEFT_X, MODE_Y)) {
                TtClientNetwork.sendToServer(new TabletButtonPayload(true, menu.isRedstone()));
                return true;
            }
            if (over(x, y, RIGHT_X, MODE_Y)) {
                TtClientNetwork.sendToServer(new TabletButtonPayload(false, menu.isRedstone()));
                return true;
            }
            if (over(x, y, redstoneX(), REDSTONE_Y)) {
                TtClientNetwork.sendToServer(new TabletButtonPayload(menu.isLeftClick(), !menu.isRedstone()));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
