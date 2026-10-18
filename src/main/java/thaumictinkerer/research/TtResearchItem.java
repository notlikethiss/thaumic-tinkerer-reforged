package thaumictinkerer.research;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumictinkerer.Config;

public class TtResearchItem extends ResearchItem {
    public static final String INFUSION = "INFUSION";
    private static final String PAGE_PREFIX = "tc.research_page.";

    private BooleanSupplier condition = () -> true;

    public TtResearchItem(String key, AspectList tags, int column, int row, int complexity, TcStack icon) {
        super(key, TtResearch.TT_CATEGORY, tags, column, row, complexity, icon);
    }

    public TtResearchItem(String key, AspectList tags, int column, int row, int complexity, ResourceLocation icon) {
        super(key, TtResearch.TT_CATEGORY, tags, column, row, complexity, icon);
    }

    public TtResearchItem when(BooleanSupplier condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public boolean isAvailable() {
        return condition.getAsBoolean() && super.isAvailable();
    }

    @Override
    public ResearchItem setPages(ResearchPage... pages) {
        ResearchPage[] converted = new ResearchPage[pages.length];
        boolean infusion = false;
        for (int index = 0; index < pages.length; index++) {
            ResearchPage page = pages[index];
            if (page instanceof ResearchPage.Text text && isIndex(text.text())) {
                page = ResearchPage.text(PAGE_PREFIX + key() + "." + (Integer.parseInt(text.text()) + 1));
            }
            infusion |= page instanceof ResearchPage.Infusion;
            converted[index] = page;
        }
        if (infusion && checkInfusion() && !parentsHidden().contains(INFUSION)) {
            List<String> parents = new ArrayList<>();
            parents.add(INFUSION);
            parents.addAll(parentsHidden());
            setParentsHidden(parents.toArray(String[]::new));
        }
        return super.setPages(converted);
    }

    @Override
    public String textKey() {
        if (!Config.tooltipIndicators()) {
            return super.textKey();
        }
        return "ttinkerer.research_text" + textSuffix() + "." + key();
    }

    protected boolean checkInfusion() {
        return true;
    }

    protected String textSuffix() {
        return "";
    }

    private static boolean isIndex(String text) {
        return !text.isEmpty() && text.chars().allMatch(Character::isDigit);
    }
}
