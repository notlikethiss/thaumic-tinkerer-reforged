package thaumictinkerer.kami.tool;

import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.platform.Registration;
import thaumictinkerer.TtRarity;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.kami.KamiFeature;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.research.TtResearch;

public final class AwakenedToolsFeature {
    public static final String ICHOR_PICK_GEM_RESEARCH = "ICHOR_PICK_GEM";
    public static final String ICHOR_SHOVEL_GEM_RESEARCH = "ICHOR_SHOVEL_GEM";
    public static final String ICHOR_AXE_GEM_RESEARCH = "ICHOR_AXE_GEM";
    public static final String ICHOR_SWORD_GEM_RESEARCH = "ICHOR_SWORD_GEM";

    public static final DeferredItem<AwakenedToolItem> AWAKENED_PICKAXE = tool(
        "awakened_ichorium_pickaxe", ToolKind.PICKAXE, properties -> Registration.pickaxe(properties, KamiFeature.ICHOR_TOOL, 2.0F, -2.8F)
    );
    public static final DeferredItem<AwakenedToolItem> AWAKENED_SHOVEL = tool(
        "awakened_ichorium_shovel", ToolKind.SHOVEL, properties -> Registration.shovel(properties, KamiFeature.ICHOR_TOOL, 1.0F, -3.0F)
    );
    public static final DeferredItem<AwakenedToolItem> AWAKENED_AXE = tool(
        "awakened_ichorium_axe", ToolKind.AXE, properties -> Registration.axe(properties, KamiFeature.ICHOR_TOOL, 3.0F, -3.0F)
    );
    public static final DeferredItem<AwakenedToolItem> AWAKENED_SWORD = tool(
        "awakened_ichorium_sword", ToolKind.SWORD, properties -> Registration.sword(properties, KamiFeature.ICHOR_TOOL, 4.0F, -2.4F)
    );

    private AwakenedToolsFeature() {
    }

    private static DeferredItem<AwakenedToolItem> tool(String name, ToolKind kind, UnaryOperator<Item.Properties> type) {
        return ModItems.ITEMS.registerItem(
            name,
            properties -> new AwakenedToolItem(properties, kind),
            properties -> type.apply(properties)
                .rarity(TtRarity.kami())
                .component(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
                .component(DataComponents.CUSTOM_MODEL_DATA, ToolHandler.DEFAULT_MODE)
        );
    }

    public static void load() {
    }

    public static void recipes() {
        TcIngredient ingot = TcIngredient.item("thaumictinkerer:ichorium_ingot");
        TcIngredient ichor = TcIngredient.item("thaumictinkerer:ichor");
        TcIngredient cloth = TcIngredient.item("thaumictinkerer:ichor_cloth");
        TcIngredient excavation = TcIngredient.item("thaumcraft:focus_excavation");
        TcIngredient frost = TcIngredient.item("thaumcraft:focus_frost");
        TcIngredient tnt = TcIngredient.item("minecraft:tnt");
        TcIngredient cinnabar = TcIngredient.item("thaumcraft:native_cinnabar_cluster");
        TcIngredient iron = TcIngredient.item("thaumcraft:native_iron_cluster");
        TcIngredient gold = TcIngredient.item("thaumcraft:native_gold_cluster");
        TcIngredient diamond = TcIngredient.item("minecraft:diamond");

        TcIngredient elementalPickaxe = TcIngredient.item("thaumcraft:elemental_pickaxe");
        TtRecipes.infusion(
            ICHOR_PICK_GEM_RESEARCH,
            TcStack.of("thaumictinkerer:awakened_ichorium_pickaxe"),
            15,
            new AspectList().add(Aspect.FIRE, 50).add(Aspect.MINE, 64).add(Aspect.METAL, 32).add(Aspect.EARTH, 32).add(Aspect.HARVEST, 32).add(Aspect.GREED, 16).add(Aspect.SENSES, 16),
            TcIngredient.item("thaumictinkerer:ichorium_pickaxe"),
            ingot, ichor, elementalPickaxe, excavation, tnt, cinnabar, iron, gold, diamond, excavation, elementalPickaxe, cloth
        );
        TcIngredient elementalShovel = TcIngredient.item("thaumcraft:elemental_shovel");
        TtRecipes.infusion(
            ICHOR_SHOVEL_GEM_RESEARCH,
            TcStack.of("thaumictinkerer:awakened_ichorium_shovel"),
            15,
            new AspectList().add(Aspect.EARTH, 50).add(Aspect.MINE, 64).add(Aspect.TOOL, 32).add(Aspect.EARTH, 32).add(Aspect.HARVEST, 32).add(Aspect.TRAP, 16).add(Aspect.SENSES, 16),
            TcIngredient.item("thaumictinkerer:ichorium_shovel"),
            ingot, ichor, elementalShovel, excavation, tnt, cinnabar, iron, gold, diamond, excavation, elementalShovel, cloth
        );
        TcIngredient elementalAxe = TcIngredient.item("thaumcraft:elemental_axe");
        TtRecipes.infusion(
            ICHOR_AXE_GEM_RESEARCH,
            TcStack.of("thaumictinkerer:awakened_ichorium_axe"),
            15,
            new AspectList().add(Aspect.WATER, 50).add(Aspect.MINE, 64).add(Aspect.TOOL, 32).add(Aspect.TREE, 32).add(Aspect.HARVEST, 32).add(Aspect.CROP, 16).add(Aspect.SENSES, 16),
            TcIngredient.item("thaumictinkerer:ichorium_axe"),
            ingot, ichor, elementalAxe, excavation, tnt, cinnabar, iron, gold, diamond, excavation, elementalAxe, cloth
        );
        TcIngredient elementalSword = TcIngredient.item("thaumcraft:elemental_sword");
        TtRecipes.infusion(
            ICHOR_SWORD_GEM_RESEARCH,
            TcStack.of("thaumictinkerer:awakened_ichorium_sword"),
            15,
            new AspectList().add(Aspect.AIR, 50).add(Aspect.HUNGER, 64).add(Aspect.SOUL, 32).add(Aspect.WEAPON, 32).add(Aspect.ENERGY, 32).add(Aspect.ORDER, 16).add(Aspect.CRYSTAL, 16),
            TcIngredient.item("thaumictinkerer:ichorium_sword"),
            ingot, ichor, elementalSword, frost, TcIngredient.item("minecraft:cactus"), cinnabar, iron, gold, diamond, frost, elementalSword, cloth
        );
    }

    public static void research() {
        TtResearch.kami(
            ICHOR_PICK_GEM_RESEARCH,
            new AspectList().add(Aspect.FIRE, 2).add(Aspect.TOOL, 1).add(Aspect.MINE, 1).add(Aspect.EARTH, 1),
            13,
            15,
            5,
            TcStack.of("thaumictinkerer:awakened_ichorium_pickaxe")
        )
            .setParents(KamiFeature.ICHOR_TOOLS_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(ICHOR_PICK_GEM_RESEARCH), TtResearch.text(1))
            .registerResearchItem();

        TtResearch.kami(
            ICHOR_SHOVEL_GEM_RESEARCH,
            new AspectList().add(Aspect.EARTH, 2).add(Aspect.TOOL, 1).add(Aspect.MINE, 1).add(Aspect.EARTH, 1),
            15,
            15,
            5,
            TcStack.of("thaumictinkerer:awakened_ichorium_shovel")
        )
            .setParents(KamiFeature.ICHOR_TOOLS_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(ICHOR_SHOVEL_GEM_RESEARCH))
            .registerResearchItem();

        TtResearch.kami(
            ICHOR_AXE_GEM_RESEARCH,
            new AspectList().add(Aspect.WATER, 2).add(Aspect.TOOL, 1).add(Aspect.TREE, 1).add(Aspect.CROP, 1),
            16,
            14,
            5,
            TcStack.of("thaumictinkerer:awakened_ichorium_axe")
        )
            .setParents(KamiFeature.ICHOR_TOOLS_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(ICHOR_AXE_GEM_RESEARCH))
            .registerResearchItem();

        TtResearch.kami(
            ICHOR_SWORD_GEM_RESEARCH,
            new AspectList().add(Aspect.AIR, 2).add(Aspect.WEAPON, 1).add(Aspect.SOUL, 1).add(Aspect.HUNGER, 1),
            16,
            12,
            5,
            TcStack.of("thaumictinkerer:awakened_ichorium_sword")
        )
            .setParents(KamiFeature.ICHOR_TOOLS_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(ICHOR_SWORD_GEM_RESEARCH), TtResearch.text(1))
            .registerResearchItem();
    }
}
