package thaumictinkerer.client.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.aspect.AspectRenderer;
import thaumcraft.registry.ModBlocks;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.kami.WandVariant;

final class WandCategory implements IRecipeCategory<WandVariant> {
    static final RecipeType<WandVariant> TYPE = new RecipeType<>(ThaumicTinkerer.id("wand_assembly"), WandVariant.class);
    private static final int WIDTH = 150;
    private static final int COST_X = 21;
    private static final int COST_Y = 62;
    private final IGuiHelper helper;
    private final IDrawable icon;

    WandCategory(IGuiHelper helper) {
        this.helper = helper;
        icon = helper.createDrawableItemLike(ModBlocks.ARCANE_WORKTABLE.get());
    }

    @Override
    public RecipeType<WandVariant> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.thaumictinkerer.wand_assembly");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return 112;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WandVariant recipe, IFocusGroup focuses) {
        List<ItemStack> grid = recipe.grid();
        for (int slot = 0; slot < grid.size(); slot++) {
            var input = builder.addInputSlot(slot % 3 * 18, slot / 3 * 18).setStandardSlotBackground();
            if (!grid.get(slot).isEmpty()) {
                input.addItemStack(grid.get(slot));
            }
        }
        builder.addOutputSlot(122, 18).setOutputSlotBackground().addItemStack(recipe.result());
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemLike(ModBlocks.ARCANE_WORKTABLE.get());
    }

    @Override
    public void draw(WandVariant recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        helper.getRecipeArrow().draw(graphics, 79, 19);
        var aspects = recipe.aspects();
        List<Aspect> sorted = aspects.getAspectsSorted();
        for (int index = 0; index < sorted.size(); index++) {
            Aspect aspect = sorted.get(index);
            AspectRenderer.drawTag(graphics, COST_X + index * 18, COST_Y, aspect, aspects.getAmount(aspect));
        }
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("jei.thaumictinkerer.base_vis_cost"), 0, 84, 0xFF404040, false);
        graphics.drawString(font, Component.translatable("jei.thaumictinkerer.discounts_apply"), 0, 98, 0xFF404040, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, WandVariant recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        List<Aspect> sorted = recipe.aspects().getAspectsSorted();
        for (int index = 0; index < sorted.size(); index++) {
            int x = COST_X + index * 18;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= COST_Y && mouseY < COST_Y + 16) {
                tooltip.add(Component.literal(sorted.get(index).getName()));
                tooltip.add(sorted.get(index).getDescription());
                return;
            }
        }
    }
}
