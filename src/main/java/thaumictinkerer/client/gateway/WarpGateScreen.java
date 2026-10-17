package thaumictinkerer.client.gateway;

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
import thaumictinkerer.kami.gateway.WarpGateMenu;
import thaumictinkerer.network.WarpGateButtonPayload;

public class WarpGateScreen extends AbstractContainerScreen<WarpGateMenu> {
    private static final Identifier TEXTURE = ThaumicTinkerer.id("textures/gui/warpgate.png");
    private static final int TEXT_COLOR = 0xFF999999;
    private static final int BUTTON_SIZE = 13;
    private static final int BUTTON_X = 5;
    private static final int BUTTON_Y = 5;

    public WarpGateScreen(WarpGateMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    private boolean overButton(double mouseX, double mouseY) {
        double offsetX = mouseX - (leftPos + BUTTON_X);
        double offsetY = mouseY - (topPos + BUTTON_Y);
        return offsetX >= 0 && offsetY >= 0 && offsetX < BUTTON_SIZE && offsetY < BUTTON_SIZE;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        TtGuiBlit.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isLocked()) {
            TtGuiBlit.blit(graphics, TEXTURE, leftPos + BUTTON_X, topPos + BUTTON_Y, 176, 0, BUTTON_SIZE, BUTTON_SIZE);
        }
        graphics.text(font, Component.translatable("ttinkerer.ttmisc.lockedGate"), leftPos + 20, topPos + 7, TEXT_COLOR, true);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overButton(event.x(), event.y())) {
            TtClientNetwork.sendToServer(new WarpGateButtonPayload(!menu.isLocked()));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
