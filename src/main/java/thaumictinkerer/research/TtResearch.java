package thaumictinkerer.research;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumictinkerer.ThaumicTinkerer;

public final class TtResearch {
    public static final String TT_CATEGORY = "TT_CATEGORY";

    private static @Nullable ResearchItem kamiResearch;

    private TtResearch() {
    }

    public static void init() {
        ResearchCategories.registerCategory(
            TT_CATEGORY,
            ThaumicTinkerer.id("textures/misc/r_enchanting.png"),
            Identifier.fromNamespaceAndPath("thaumcraft", "textures/gui/gui_researchback.png")
        );
    }

    public static TtResearchItem item(String key, AspectList tags, int column, int row, int complexity, TcStack icon) {
        return new TtResearchItem(key, tags, column, row, complexity, icon);
    }

    public static TtResearchItem item(String key, AspectList tags, int column, int row, int complexity, Identifier icon) {
        return new TtResearchItem(key, tags, column, row, complexity, icon);
    }

    public static KamiResearchItem kami(String key, AspectList tags, int column, int row, int complexity, TcStack icon) {
        return new KamiResearchItem(key, tags, column, row, complexity, icon);
    }

    public static KamiResearchItem kami(String key, AspectList tags, int column, int row, int complexity, Identifier icon) {
        return new KamiResearchItem(key, tags, column, row, complexity, icon);
    }

    public static @Nullable ResearchItem kamiResearch() {
        return kamiResearch;
    }

    public static void setKamiResearch(ResearchItem research) {
        kamiResearch = research;
    }

    public static ResearchPage text(int index) {
        return ResearchPage.text(Integer.toString(index));
    }

    public static ResearchPage recipe(String... recipes) {
        return ResearchPage.crafting(recipes);
    }

    public static ResearchPage arcane(String... recipes) {
        return ResearchPage.arcane(recipes);
    }

    public static ResearchPage infusion(String... recipes) {
        return ResearchPage.infusion(recipes);
    }

    public static ResearchPage infusion(String name, int count) {
        List<String> recipes = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            recipes.add(name + index);
        }
        return ResearchPage.infusion(recipes.toArray(String[]::new));
    }

    public static ResearchPage enchant(String recipe) {
        return ResearchPage.infusionEnchantment(recipe);
    }

    public static ResearchPage crucible(String... recipes) {
        return ResearchPage.crucible(recipes);
    }
}
