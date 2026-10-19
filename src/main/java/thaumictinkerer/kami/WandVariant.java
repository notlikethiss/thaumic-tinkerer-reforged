package thaumictinkerer.kami;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.ArcaneWandRecipe;
import thaumcraft.wand.WandCap;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;
import thaumictinkerer.Config;

public enum WandVariant {
    SILVERWOOD,
    ICHORCLOTH;

    public boolean available() {
        return this != ICHORCLOTH || Config.kamiEnabled();
    }

    public WandCap cap() {
        return this == SILVERWOOD ? WandRegistry.CAP_THAUMIUM : KamiFeature.ichorCap;
    }

    public WandRod rod() {
        return this == SILVERWOOD ? WandRegistry.ROD_SILVERWOOD : KamiFeature.ichorclothRod;
    }

    public List<ItemStack> grid() {
        List<ItemStack> grid = new ArrayList<>();
        for (int slot = 0; slot < 9; slot++) {
            grid.add(ItemStack.EMPTY);
        }
        grid.set(6, new ItemStack(BuiltInRegistries.ITEM.getValue(cap().item())));
        grid.set(2, new ItemStack(BuiltInRegistries.ITEM.getValue(cap().item())));
        grid.set(4, new ItemStack(BuiltInRegistries.ITEM.getValue(rod().item())));
        return grid;
    }

    public ItemStack result() {
        return new ArcaneWandRecipe().result(grid());
    }

    public AspectList aspects() {
        return new ArcaneWandRecipe().aspects(grid());
    }

    public ItemStack creativeStack() {
        ItemStack stack = result();
        Wands.fill(stack);
        return stack;
    }
}
