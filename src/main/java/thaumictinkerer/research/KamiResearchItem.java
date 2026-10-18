package thaumictinkerer.research;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumictinkerer.Config;

public class KamiResearchItem extends TtResearchItem {
    public static final List<String> BLACKLIST = List.of("MINILITH");
    private static final List<String> SWEPT_CATEGORIES = List.of(TtResearch.TT_CATEGORY, "BASICS", "GOLEMANCY", "ARTIFICE", "ALCHEMY", "THAUMATURGY");

    public KamiResearchItem(String key, AspectList tags, int column, int row, int complexity, TcStack icon) {
        super(key, tags, column, row, complexity, icon);
        setConcealed();
    }

    public KamiResearchItem(String key, AspectList tags, int column, int row, int complexity, ResourceLocation icon) {
        super(key, tags, column, row, complexity, icon);
        setConcealed();
    }

    @Override
    public boolean isAvailable() {
        return Config.kamiEnabled() && super.isAvailable();
    }

    @Override
    public List<String> parentsHidden() {
        List<String> own = super.parentsHidden();
        if (isAutoUnlock()) {
            return own;
        }
        List<String> requirements = new ArrayList<>(own);
        for (ResearchItem research : ResearchCategories.allResearch()) {
            if (isSweepable(research) && !requirements.contains(research.key())) {
                requirements.add(research.key());
            }
        }
        return requirements;
    }

    @Override
    protected boolean checkInfusion() {
        return false;
    }

    @Override
    protected String textSuffix() {
        return "_kami";
    }

    private static boolean isSweepable(ResearchItem research) {
        if (research instanceof KamiResearchItem || research.isLost() || research.isVirtual()) {
            return false;
        }
        if (!SWEPT_CATEGORIES.contains(research.category())) {
            return false;
        }
        if (research.parents().isEmpty() && research.parentsHidden().isEmpty()) {
            return false;
        }
        if (!research.aspectTriggers().isEmpty() || !research.entityTriggers().isEmpty() || !research.itemTriggers().isEmpty()) {
            return false;
        }
        String key = research.key();
        return !key.endsWith("KAMI") && BLACKLIST.stream().noneMatch(key::startsWith);
    }
}
