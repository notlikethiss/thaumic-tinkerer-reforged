package thaumictinkerer.client.tablet;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.client.platform.TtClientNetwork;
import thaumictinkerer.client.platform.TtGuiBlit;
import thaumictinkerer.network.TabletButtonPayload;
import thaumictinkerer.tablet.DynamismTabletMenu;

public class DynamismTabletScreen extends AbstractContainerScreen<DynamismTabletMenu> {
    private static final Identifier TEXTURE = ThaumicTinkerer.id("textures/gui/animationtablet.png");
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
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    private int redstoneX() {
        return imageWidth / 2 - 7;
    }

    private boolean over(double mouseX, double mouseY, int x, int y) {
        double offsetX = mouseX - (leftPos + x);
        double offsetY = mouseY - (topPos + y);
        return offsetX >= 0 && offsetY >= 0 && offsetX < BUTTON_SIZE && offsetY < BUTTON_SIZE;
    }

    private void button(GuiGraphicsExtractor graphics, int x, int y, boolean enabled) {
        if (enabled) {
            TtGuiBlit.blit(graphics, TEXTURE, leftPos + x, topPos + y, 176, 13, BUTTON_SIZE, BUTTON_SIZE);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        TtGuiBlit.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        button(graphics, LEFT_X, MODE_Y, menu.isLeftClick());
        button(graphics, RIGHT_X, MODE_Y, !menu.isLeftClick());
        button(graphics, redstoneX(), REDSTONE_Y, menu.isRedstone());
        Component left = Component.translatable("ttinkerer.ttmisc.leftClick");
        Component redstone = Component.translatable("ttinkerer.ttmisc.redstoneControl");
        graphics.text(font, left, leftPos + 48 - font.width(left), topPos + 18, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("ttinkerer.ttmisc.rightClick"), leftPos + 128, topPos + 18, TEXT_COLOR, false);
        graphics.text(font, redstone, leftPos + imageWidth / 2 - font.width(redstone) / 2, topPos + 50, TEXT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            double x = event.x();
            double y = event.y();
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
        return super.mouseClicked(event, doubleClick);
    }
}
