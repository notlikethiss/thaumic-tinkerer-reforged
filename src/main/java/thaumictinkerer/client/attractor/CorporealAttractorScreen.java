package thaumictinkerer.client.attractor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.attractor.CorporealAttractorMenu;
import thaumictinkerer.attractor.SoulMouldItem;
import thaumictinkerer.client.platform.TtClientNetwork;
import thaumictinkerer.client.platform.TtGuiBlit;
import thaumictinkerer.network.MobMagnetButtonPayload;

public class CorporealAttractorScreen extends AbstractContainerScreen<CorporealAttractorMenu> {
    private static final Identifier TEXTURE = ThaumicTinkerer.id("textures/gui/mobmagnet.png");
    private static final int TEXT_COLOR = 0xFF999999;
    private static final int BUTTON_SIZE = 13;
    private static final int BUTTON_X = 100;
    private static final int ADULT_Y = 28;
    private static final int CHILD_Y = 48;

    public CorporealAttractorScreen(CorporealAttractorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    private boolean over(double mouseX, double mouseY, int y) {
        double offsetX = mouseX - (leftPos + BUTTON_X);
        double offsetY = mouseY - (topPos + y);
        return offsetX >= 0 && offsetY >= 0 && offsetX < BUTTON_SIZE && offsetY < BUTTON_SIZE;
    }

    private Component filterName() {
        ItemStack stack = menu.filter();
        if (stack.isEmpty()) {
            return Component.translatable("ttinkerer.ttmisc.all");
        }
        String pattern = SoulMouldItem.getPatternName(stack);
        if (pattern.isEmpty()) {
            return Component.translatable("ttinkerer.ttmisc.none");
        }
        return SoulMouldItem.patternName(pattern);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        TtGuiBlit.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int enabledY = menu.isAdult() ? ADULT_Y : CHILD_Y;
        TtGuiBlit.blit(graphics, TEXTURE, leftPos + BUTTON_X, topPos + enabledY, 176, 13, BUTTON_SIZE, BUTTON_SIZE);
        Component filter = filterName();
        graphics.text(font, filter, leftPos + imageWidth / 2 - font.width(filter) / 2 - 26, topPos + 16, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("ttinkerer.ttmisc.adult"), leftPos + 120, topPos + 30, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("ttinkerer.ttmisc.child"), leftPos + 120, topPos + 50, TEXT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            if (over(event.x(), event.y(), ADULT_Y)) {
                TtClientNetwork.sendToServer(new MobMagnetButtonPayload(true));
                return true;
            }
            if (over(event.x(), event.y(), CHILD_Y)) {
                TtClientNetwork.sendToServer(new MobMagnetButtonPayload(false));
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
