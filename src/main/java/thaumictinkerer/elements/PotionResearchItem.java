package thaumictinkerer.elements;

import java.util.List;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumictinkerer.Config;
import thaumictinkerer.research.TtResearchItem;

public class PotionResearchItem extends TtResearchItem {
    public PotionResearchItem(String key, AspectList tags, int column, int row, int complexity, TcStack icon) {
        super(key, tags, column, row, complexity, icon);
    }

    @Override
    public List<String> parents() {
        return Config.imbuedFire() ? super.parents() : List.of();
    }
}
