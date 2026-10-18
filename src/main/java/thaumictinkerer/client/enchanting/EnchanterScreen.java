package thaumictinkerer.client.enchanting;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.client.platform.GuiBlit;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.client.platform.TtClientNetwork;
import thaumictinkerer.enchanting.EnchanterBlockEntity;
import thaumictinkerer.enchanting.EnchanterMenu;
import thaumictinkerer.enchanting.EnchantmentManager;
import thaumictinkerer.enchanting.TtEnchantments;
import thaumictinkerer.network.EnchanterAddEnchantPayload;
import thaumictinkerer.network.EnchanterStartWorkingPayload;

public class EnchanterScreen extends AbstractContainerScreen<EnchanterMenu> {
    private static final ResourceLocation TEXTURE = ThaumicTinkerer.id("textures/gui/enchanter.png");
    private static final int MAX_BUTTONS = 16;

    private enum Kind {
        START, PICK, FRAMED, MINUS, PLUS
    }

    private record Hit(Kind kind, int x, int y, int size, ResourceKey<Enchantment> key, int index) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
        }
    }

    public EnchanterScreen(EnchanterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private void sheet(GuiGraphics graphics, int x, int y, int width, int height, int u, int v, int color) {
        if (width > 0 && height > 0) {
            GuiBlit.blit(graphics, TEXTURE, x, y, width, height, u, v, width, height, 256, 256, color);
        }
    }

    private List<ResourceKey<Enchantment>> available(EnchanterBlockEntity enchanter) {
        List<ResourceKey<Enchantment>> result = new ArrayList<>();
        ItemStack tool = enchanter.getTool();
        if (!EnchantmentManager.canEnchantStack(tool) || minecraft == null || minecraft.player == null) {
            return result;
        }
        for (ResourceKey<Enchantment> key : EnchantmentManager.data().keySet()) {
            if (EnchantmentManager.canApply(minecraft.player.level().registryAccess(), tool, key, enchanter.getEnchantments())
                && EnchantmentManager.canEnchantmentBeUsed(minecraft.player, key)) {
                result.add(key);
                if (result.size() >= MAX_BUTTONS) {
                    break;
                }
            }
        }
        return result;
    }

    private List<Hit> layout(EnchanterBlockEntity enchanter, List<ResourceKey<Enchantment>> available) {
        List<Hit> hits = new ArrayList<>();
        ResourceKey<Enchantment> none = TtEnchantments.ASCENT_BOOST;
        if (!enchanter.getEnchantments().isEmpty()) {
            hits.add(new Hit(Kind.START, leftPos + 151, topPos + 33, 15, none, 0));
        }
        boolean twoRows = available.size() > 8;
        for (int index = 0; index < available.size(); index++) {
            int offset = index <= 7 && twoRows ? -24 : 0;
            hits.add(new Hit(Kind.PICK, leftPos + 34 + (index % 8) * 16, topPos + 54 + offset, 16, available.get(index), index));
        }
        for (int index = 0; index < enchanter.getEnchantments().size(); index++) {
            ResourceKey<Enchantment> key = enchanter.getEnchantments().get(index);
            int baseX = leftPos + imageWidth + 4;
            int baseY = topPos + index * 26;
            hits.add(new Hit(Kind.FRAMED, baseX, baseY, 16, key, index));
            hits.add(new Hit(Kind.MINUS, leftPos + imageWidth + 24, baseY - 4, 7, key, index));
            hits.add(new Hit(Kind.PLUS, leftPos + imageWidth + 31, baseY - 4, 7, key, index));
        }
        return hits;
    }

    private Component enchantName(ResourceKey<Enchantment> key) {
        return TtEnchantments.holder(minecraft.player.level().registryAccess(), key)
            .map(holder -> holder.value().description())
            .orElse(Component.literal(key.location().toString()));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        sheet(graphics, leftPos, topPos, imageWidth, imageHeight, 0, 0, 0xFFFFFFFF);
        EnchanterBlockEntity enchanter = menu.enchanter();
        if (enchanter == null) {
            return;
        }
        List<ResourceKey<Enchantment>> available = available(enchanter);
        ItemStack tool = enchanter.getTool();
        boolean enchantable = !tool.isEmpty() && !tool.isEnchanted();
        boolean twoRows = available.size() > 8;
        if (twoRows && enchantable) {
            sheet(graphics, leftPos + 30, topPos + 26, 147, 24, 0, imageHeight, 0xFFFFFFFF);
        }
        if (enchantable) {
            sheet(graphics, leftPos + 30, topPos + 50, 147, 24, 0, imageHeight, 0xFFFFFFFF);
        }
        if (!enchanter.getEnchantments().isEmpty()) {
            int row = twoRows ? 26 : 50;
            int offset = 15;
            for (Aspect aspect : EnchantmentManager.PRIMAL_ASPECTS) {
                drawAspectBar(graphics, aspect, leftPos + 40 + offset, topPos + row);
                offset += 15;
            }
        }
        for (Hit hit : layout(enchanter, available)) {
            switch (hit.kind()) {
                case START -> sheet(graphics, hit.x(), hit.y(), 15, 15, 176, menu.isWorking() ? 39 : 24, 0xFFFFFFFF);
                case PICK -> GuiBlit.blit(graphics, EnchantmentManager.data(hit.key()).texture(), hit.x(), hit.y(), 0, 0, 16, 16, 16, 16);
                case FRAMED -> {
                    sheet(graphics, hit.x() - 4, hit.y() - 4, 24, 24, 176, 0, 0xFFFFFFFF);
                    GuiBlit.blit(graphics, EnchantmentManager.data(hit.key()).texture(), hit.x(), hit.y(), 0, 0, 16, 16, 16, 16);
                    int level = enchanter.getLevels().get(hit.index());
                    graphics.drawString(font, Component.translatable("enchantment.level." + level), hit.x() + 26, hit.y() + 8, 0xFFFFFFFF, true);
                }
                case MINUS -> sheet(graphics, hit.x(), hit.y(), 7, 7, 218, 0, 0xFFFFFFFF);
                case PLUS -> sheet(graphics, hit.x(), hit.y(), 7, 7, 225, 0, 0xFFFFFFFF);
            }
        }
    }

    private void drawAspectBar(GuiGraphics graphics, Aspect aspect, int x, int y) {
        int total = menu.total(aspect);
        int current = menu.current(aspect);
        int size = total == 0 ? 11 : 59;
        if (total == 0) {
            sheet(graphics, x, y - size, 10, 4, 200, 0, 0xFFFFFFFF);
            sheet(graphics, x, y - size + 4, 10, 10, 200, 52, 0xFFFFFFFF);
        } else {
            int pixels = (int) (48.0 * ((double) current / (double) total));
            sheet(graphics, x + 1, y - size + 4 + 48 - pixels, 8, pixels, 210, 48 - pixels, FastColor.ARGB32.opaque(aspect.getColor()));
            sheet(graphics, x, y - size, 10, size, 200, 0, 0xFFFFFFFF);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        EnchanterBlockEntity enchanter = menu.enchanter();
        if (enchanter == null) {
            return;
        }
        List<ResourceKey<Enchantment>> available = available(enchanter);
        for (Hit hit : layout(enchanter, available)) {
            if (!hit.contains(mouseX, mouseY)) {
                continue;
            }
            if (hit.kind() == Kind.START && !menu.isWorking()) {
                graphics.renderTooltip(font, Component.translatable("ttinkerer.ttmisc.startEnchant").withStyle(ChatFormatting.AQUA), mouseX, mouseY);
            } else if (hit.kind() == Kind.PICK || hit.kind() == Kind.FRAMED) {
                List<Component> lines = new ArrayList<>();
                lines.add(enchantName(hit.key()).copy().withStyle(ChatFormatting.AQUA));
                AspectList aspects = EnchantmentManager.data(hit.key()).aspects(1);
                for (Aspect aspect : aspects.getAspectsSorted()) {
                    lines.add(Component.literal(" ").append(aspect.getDescription().copy().withColor(aspect.getColor()))
                        .append(Component.literal(" x " + aspects.getAmount(aspect) + " "))
                        .append(Component.translatable("ttinkerer.ttmisc.baseCost")));
                }
                if (hit.kind() == Kind.FRAMED && !menu.isWorking()) {
                    lines.add(Component.literal(" ").append(Component.translatable("ttinkerer.ttmisc.clickToRemove")).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }
                graphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
            }
        }
        if (!enchanter.getEnchantments().isEmpty()) {
            int row = available.size() > 8 ? 26 : 50;
            int offset = 15;
            for (Aspect aspect : EnchantmentManager.PRIMAL_ASPECTS) {
                int x = leftPos + 40 + offset;
                int y = topPos + row;
                int size = menu.total(aspect) == 0 ? 11 : 59;
                if (mouseX > x && mouseX <= x + 10 && mouseY > y - size && mouseY <= y) {
                    List<Component> lines = List.of(
                        aspect.getDescription().copy().withColor(aspect.getColor()),
                        Component.literal(menu.current(aspect) + "/" + menu.total(aspect))
                    );
                    graphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
                }
                offset += 15;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        EnchanterBlockEntity enchanter = menu.enchanter();
        if (button != InputConstants.MOUSE_BUTTON_LEFT || enchanter == null) {
            return handled;
        }
        for (Hit hit : layout(enchanter, available(enchanter))) {
            if (!hit.contains(mouseX, mouseY)) {
                continue;
            }
            switch (hit.kind()) {
                case START -> TtClientNetwork.sendToServer(new EnchanterStartWorkingPayload(menu.pos()));
                case PICK -> TtClientNetwork.sendToServer(new EnchanterAddEnchantPayload(menu.pos(), hit.key(), 0));
                case FRAMED -> TtClientNetwork.sendToServer(new EnchanterAddEnchantPayload(menu.pos(), hit.key(), -1));
                case MINUS -> {
                    int level = enchanter.getLevels().get(hit.index());
                    TtClientNetwork.sendToServer(new EnchanterAddEnchantPayload(menu.pos(), hit.key(), level == 1 ? -1 : level - 1));
                }
                case PLUS -> TtClientNetwork.sendToServer(new EnchanterAddEnchantPayload(menu.pos(), hit.key(), enchanter.getLevels().get(hit.index()) + 1));
            }
            return true;
        }
        return handled;
    }
}
