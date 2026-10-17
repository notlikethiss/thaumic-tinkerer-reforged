package thaumictinkerer.client.gateway;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumictinkerer.client.platform.TtClientNetwork;
import thaumictinkerer.client.platform.TtGuiTint;
import thaumictinkerer.kami.gateway.GatewayFeature;
import thaumictinkerer.kami.gateway.PearlLocation;
import thaumictinkerer.kami.gateway.SkyPearlItem;
import thaumictinkerer.kami.gateway.WarpGateBlockEntity;
import thaumictinkerer.network.WarpGateTeleportPayload;

public class WarpGateDestinationsScreen extends Screen {
    private static final Identifier PORTAL = Identifier.withDefaultNamespace("textures/entity/end_portal/end_portal.png");
    private static final int PORTAL_SIZE = 256;
    private static final int HOVER_RADIUS = 4;
    private static final int KEY_SPACE = 32;
    private static final int KEY_ZERO = 48;

    private record Destination(int x, int y, ItemStack stack, int index) {
    }

    private final WarpGateBlockEntity gate;
    private double originX;
    private double originY;
    private int ticks;

    public WarpGateDestinationsScreen(WarpGateBlockEntity gate) {
        super(Component.empty());
        this.gate = gate;
    }

    private void center() {
        BlockPos pos = gate.getBlockPos();
        originX = pos.getX() - width / 2;
        originY = pos.getZ() - height / 2;
    }

    @Override
    protected void init() {
        center();
    }

    @Override
    public void tick() {
        ticks++;
        if (gate.isRemoved()) {
            onClose();
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            originX -= dragX;
            originY -= dragY;
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == KEY_SPACE) {
            center();
            return true;
        }
        int number = event.key() - KEY_ZERO;
        if (number >= 0 && number <= 9) {
            int index = number == 0 ? 9 : number - 1;
            PearlLocation location = SkyPearlItem.location(gate.getItem(index));
            if (location != null && gate.getLevel() != null && location.dimension().equals(gate.getLevel().dimension())) {
                originX = location.pos().getX() - width / 2;
                originY = location.pos().getZ() - height / 2;
            }
            return true;
        }
        return super.keyPressed(event);
    }

    private List<Destination> destinations() {
        List<Destination> result = new ArrayList<>();
        if (gate.getLevel() == null) {
            return result;
        }
        for (int index = 0; index < WarpGateBlockEntity.SIZE; index++) {
            ItemStack stack = gate.getItem(index);
            PearlLocation location = SkyPearlItem.location(stack);
            if (location != null && location.dimension().equals(gate.getLevel().dimension())) {
                result.add(new Destination((int) (location.pos().getX() - originX), (int) (location.pos().getZ() - originY), stack, index));
            }
        }
        return result;
    }

    private static boolean hovering(Destination destination, double mouseX, double mouseY) {
        return mouseX >= destination.x() - HOVER_RADIUS && mouseX <= destination.x() + HOVER_RADIUS && mouseY >= destination.y() - HOVER_RADIUS && mouseY < destination.y() + HOVER_RADIUS;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && event.hasShiftDown()) {
            for (Destination destination : destinations()) {
                if (hovering(destination, event.x(), event.y())) {
                    TtClientNetwork.sendToServer(new WarpGateTeleportPayload(gate.getBlockPos(), destination.index()));
                    onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        float hue = (float) (Math.sin(ticks / 150.0) + 0.5);
        int color = 0xFF000000 | Mth.hsvToRgb(hue - (float) Math.floor(hue), 0.5F, 0.4F);
        for (int column = 0; column < width; column += PORTAL_SIZE) {
            for (int row = 0; row < height; row += PORTAL_SIZE) {
                TtGuiTint.blit(graphics, PORTAL, column, row, 0, 0, Math.min(PORTAL_SIZE, width - column), Math.min(PORTAL_SIZE, height - row), PORTAL_SIZE, PORTAL_SIZE, color);
            }
        }
    }

    private void line(GuiGraphicsExtractor graphics, int fromX, int fromY, int toX, int toY, int color) {
        int deltaX = Math.abs(toX - fromX);
        int deltaY = Math.abs(toY - fromY);
        int stepX = fromX < toX ? 1 : -1;
        int stepY = fromY < toY ? 1 : -1;
        int error = deltaX - deltaY;
        int x = fromX;
        int y = fromY;
        int guard = deltaX + deltaY + 2;
        while (guard-- > 0) {
            graphics.fill(x, y, x + 2, y + 2, color);
            if (x == toX && y == toY) {
                break;
            }
            int doubled = error * 2;
            if (doubled > -deltaY) {
                error -= deltaY;
                x += stepX;
            }
            if (doubled < deltaX) {
                error += deltaX;
                y += stepY;
            }
        }
    }

    private Component destinationName(@Nullable ItemStack stack) {
        if (stack != null && stack.has(DataComponents.CUSTOM_NAME)) {
            return stack.getHoverName();
        }
        return Component.translatable(stack == null ? "ttinkerer.ttmisc.entrancePoint" : "ttinkerer.ttmisc.destination");
    }

    private void drawPearlAt(GuiGraphicsExtractor graphics, int index, @Nullable ItemStack stack, int pearlX, int pearlY, int mouseX, int mouseY) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(pearlX, pearlY);
        graphics.pose().scale(0.5F, 0.5F);
        graphics.item(new ItemStack(GatewayFeature.SKY_PEARL.get()), -8, -8);
        graphics.pose().popMatrix();
        Component name = destinationName(stack);
        if (stack != null) {
            graphics.text(font, Component.literal((index + 1) + ": ").append(name), 5, 54 + index * 11, 0xFFFFFFFF, true);
        }
        if (mouseX >= pearlX - HOVER_RADIUS && mouseX <= pearlX + HOVER_RADIUS && mouseY >= pearlY - HOVER_RADIUS && mouseY < pearlY + HOVER_RADIUS) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(name.copy().withStyle(ChatFormatting.AQUA).append(Component.literal(" ").append(Component.translatable("ttinkerer.ttmisc.destinationInd", index + 1)).withStyle(ChatFormatting.ITALIC)));
            if (stack != null) {
                BlockPos pos = gate.getBlockPos();
                PearlInfo.add(stack, gate.getLevel().dimension(), new Vec3(pos.getX(), pos.getY(), pos.getZ()), tooltip, true);
                tooltip.add(Component.translatable("ttinkerer.ttmisc.clickToTeleport"));
            } else {
                tooltip.add(Component.literal("X: " + (int) (pearlX + originX)));
                tooltip.add(Component.literal("Z: " + (int) (pearlY + originY)));
            }
            graphics.setTooltipForNextFrame(font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        BlockPos gatePos = gate.getBlockPos();
        int gateX = (int) (gatePos.getX() - originX);
        int gateY = (int) (gatePos.getZ() - originY);
        List<Destination> destinations = destinations();
        int alpha = (int) (((Math.sin(ticks / 10.0) + 1.0) / 4.0 + 0.25) * 255.0);
        int lineColor = (alpha << 24) | 0xFFFFFF;
        for (Destination destination : destinations) {
            line(graphics, gateX, gateY, destination.x(), destination.y(), lineColor);
        }
        graphics.text(font, Component.translatable("ttinkerer.ttmisc.destinations").withStyle(ChatFormatting.UNDERLINE), 3, 40, 0xFFFFFFFF, true);
        drawPearlAt(graphics, 0, null, gateX, gateY, mouseX, mouseY);
        for (Destination destination : destinations) {
            drawPearlAt(graphics, destination.index(), destination.stack(), destination.x(), destination.y(), mouseX, mouseY);
        }
        graphics.centeredText(font, Component.translatable("ttinkerer.ttmisc.numberKeys"), width / 2, 5, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable("ttinkerer.ttmisc.spaceToReset"), width / 2, 16, 0xFFFFFFFF);
    }
}
