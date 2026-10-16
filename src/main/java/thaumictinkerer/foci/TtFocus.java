package thaumictinkerer.foci;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import thaumcraft.wand.focus.FocusItem;
import thaumictinkerer.ThaumicTinkerer;

public abstract class TtFocus extends FocusItem {
    private final String texture;
    private final boolean ornament;
    private final boolean depth;

    protected TtFocus(Properties properties, String texture, boolean ornament, boolean depth) {
        super(properties);
        this.texture = texture;
        this.ornament = ornament;
        this.depth = depth;
    }

    @Override
    public @Nullable Identifier getFocusTexture(ItemStack focus) {
        return ThaumicTinkerer.id("item/" + texture);
    }

    @Override
    public @Nullable Identifier getOrnamentTexture(ItemStack focus) {
        return ornament ? ThaumicTinkerer.id("item/" + texture + "orn") : null;
    }

    @Override
    public @Nullable Identifier getDepthLayerTexture(ItemStack focus) {
        return depth ? ThaumicTinkerer.id("item/" + texture + "depth") : null;
    }
}
