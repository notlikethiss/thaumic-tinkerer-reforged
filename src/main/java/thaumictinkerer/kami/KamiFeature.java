package thaumictinkerer.kami;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.platform.ArmorSlot;
import thaumcraft.platform.Registration;
import thaumcraft.platform.TcArmorMaterial;
import thaumcraft.platform.TcToolMaterial;
import thaumcraft.research.ResearchItem;
import thaumcraft.wand.WandCap;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandRod;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.TtRarity;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.platform.TtToolItem;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.research.TtResearch;

public final class KamiFeature {
    public static final String DIMENSION_SHARDS_RESEARCH = "DIMENSION_SHARDS";
    public static final String ICHOR_RESEARCH = "ICHOR";
    public static final String ICHOR_CLOTH_RESEARCH = "ICHOR_CLOTH";
    public static final String ICHORIUM_RESEARCH = "ICHORIUM";
    public static final String ICHOR_CAP_RESEARCH = "CAP_ICHOR";
    public static final String ICHORCLOTH_ROD_RESEARCH = "ROD_ICHORCLOTH";
    public static final String ICHORCLOTH_ARMOR_RESEARCH = "ICHORCLOTH_ARMOR";
    public static final String ICHORCLOTH_HELM_RECIPE = "ICHORCLOTH_HELM";
    public static final String ICHORCLOTH_CHEST_RECIPE = "ICHORCLOTH_CHEST";
    public static final String ICHORCLOTH_LEGS_RECIPE = "ICHORCLOTH_LEGS";
    public static final String ICHORCLOTH_BOOTS_RECIPE = "ICHORCLOTH_BOOTS";
    public static final String ICHOR_TOOLS_RESEARCH = "ICHOR_TOOLS";
    public static final String ICHOR_PICK_RECIPE = "ICHOR_PICK";
    public static final String ICHOR_SHOVEL_RECIPE = "ICHOR_SHOVEL";
    public static final String ICHOR_AXE_RECIPE = "ICHOR_AXE";
    public static final String ICHOR_SWORD_RECIPE = "ICHOR_SWORD";

    public static final String CAP_TAG = "ICHOR";
    public static final String ROD_TAG = "ICHORCLOTH";
    public static final float CAP_DISCOUNT = 0.7F;
    public static final int CAP_CRAFT_COST = 10;
    public static final int ROD_CAPACITY = 1000;
    public static final int ROD_CRAFT_COST = 10;
    public static final int HELMET_CHESTPLATE_LEGGINGS_DISCOUNT = 4;
    public static final int BOOTS_DISCOUNT = 3;

    public static final TcToolMaterial ICHOR_TOOL = new TcToolMaterial(1, 10.0F, 5.0F, 25, TcMaterials.REPAIRS_NONE, BlockTags.INCORRECT_FOR_NETHERITE_TOOL);
    public static final TcArmorMaterial ICHORCLOTH_ARMOR = new TcArmorMaterial(
        1,
        3,
        8,
        6,
        3,
        20,
        TcMaterials.THAUMIUM_ARMOR.equipSound(),
        0.0F,
        0.0F,
        TcMaterials.REPAIRS_NONE,
        ThaumicTinkerer.id("ichorcloth")
    );

    public static final DeferredItem<Item> ICHOR = resource("ichor", true);
    public static final DeferredItem<Item> ICHOR_CLOTH = resource("ichor_cloth", true);
    public static final DeferredItem<Item> ICHORIUM_INGOT = resource("ichorium_ingot", true);
    public static final DeferredItem<Item> ICHORIUM_NUGGET = resource("ichorium_nugget", true);
    public static final DeferredItem<Item> ICHORIUM_CAP = resource("ichorium_cap", true);
    public static final DeferredItem<Item> ICHORCLOTH_ROD = resource("ichorcloth_rod", true);
    public static final DeferredItem<Item> NETHER_SHARD = resource("nether_shard", false);
    public static final DeferredItem<Item> ENDER_SHARD = resource("ender_shard", false);

    public static final DeferredItem<Item> ICHORIUM_PICKAXE = ModItems.ITEMS.registerItem(
        "ichorium_pickaxe",
        properties -> new TtToolItem(properties, ICHOR_TOOL),
        properties -> unbreakable(Registration.pickaxe(properties, ICHOR_TOOL, 2.0F, -2.8F))
    );
    public static final DeferredItem<Item> ICHORIUM_SHOVEL = ModItems.ITEMS.registerItem(
        "ichorium_shovel",
        properties -> new TtToolItem(properties, ICHOR_TOOL),
        properties -> unbreakable(Registration.shovel(properties, ICHOR_TOOL, 1.0F, -3.0F))
    );
    public static final DeferredItem<Item> ICHORIUM_AXE = ModItems.ITEMS.registerItem(
        "ichorium_axe",
        properties -> new TtToolItem(properties, ICHOR_TOOL),
        properties -> unbreakable(Registration.axe(properties, ICHOR_TOOL, 3.0F, -3.0F))
    );
    public static final DeferredItem<Item> ICHORIUM_SWORD = ModItems.ITEMS.registerItem(
        "ichorium_sword",
        properties -> new TtToolItem(properties, ICHOR_TOOL),
        properties -> unbreakable(Registration.sword(properties, ICHOR_TOOL, 4.0F, -2.4F))
    );

    public static final DeferredItem<IchorclothArmorItem> ICHORCLOTH_COWL = armor("ichorcloth_cowl", ArmorSlot.HELMET, HELMET_CHESTPLATE_LEGGINGS_DISCOUNT);
    public static final DeferredItem<IchorclothArmorItem> ICHORCLOTH_ROBE = armor("ichorcloth_robe", ArmorSlot.CHESTPLATE, HELMET_CHESTPLATE_LEGGINGS_DISCOUNT);
    public static final DeferredItem<IchorclothArmorItem> ICHORCLOTH_LEGGINGS = armor("ichorcloth_leggings", ArmorSlot.LEGGINGS, HELMET_CHESTPLATE_LEGGINGS_DISCOUNT);
    public static final DeferredItem<IchorclothArmorItem> ICHORCLOTH_BOOTS = armor("ichorcloth_boots", ArmorSlot.BOOTS, BOOTS_DISCOUNT);

    public static WandCap ichorCap;
    public static WandRod ichorclothRod;

    private KamiFeature() {
    }

    private static DeferredItem<Item> resource(String name, boolean kami) {
        return ModItems.ITEMS.registerItem(name, Item::new, properties -> kami ? properties.rarity(TtRarity.kami()) : properties);
    }

    private static Item.Properties unbreakable(Item.Properties properties) {
        return properties.rarity(TtRarity.kami()).component(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true));
    }

    private static DeferredItem<IchorclothArmorItem> armor(String name, ArmorSlot slot, int discount) {
        return ModItems.ITEMS.registerItem(
            name,
            properties -> new IchorclothArmorItem(properties, discount, ICHORCLOTH_ARMOR, slot),
            properties -> unbreakable(Registration.armor(properties, ICHORCLOTH_ARMOR, slot))
        );
    }

    public static void load() {
        ichorCap = WandRegistry.registerCap(
            CAP_TAG,
            CAP_DISCOUNT,
            List.of(),
            0.0F,
            ThaumicTinkerer.id("ichorium_cap"),
            CAP_CRAFT_COST,
            null,
            ThaumicTinkerer.id("textures/model/capichor.png")
        );
        ichorclothRod = WandRegistry.registerRod(
            ROD_TAG,
            ROD_CAPACITY,
            ThaumicTinkerer.id("ichorcloth_rod"),
            ROD_CRAFT_COST,
            null,
            true,
            false,
            false,
            ThaumicTinkerer.id("textures/model/rodichorcloth.png")
        );
    }

    public static void aspects(AspectRegistry registry) {
        registry.register(NETHER_SHARD.get(), new AspectList().add(Aspect.CRYSTAL, 1).add(Aspect.MAGIC, 1).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2));
        registry.register(ENDER_SHARD.get(), new AspectList().add(Aspect.CRYSTAL, 1).add(Aspect.MAGIC, 1).add(Aspect.ELDRITCH, 2).add(Aspect.TRAVEL, 2));
    }

    public static void recipes() {
        AspectList primals = new AspectList().add(Aspect.FIRE, 125).add(Aspect.EARTH, 125).add(Aspect.WATER, 125).add(Aspect.AIR, 125).add(Aspect.ORDER, 125).add(Aspect.ENTROPY, 125);
        AspectList primalsHundred = new AspectList().add(Aspect.FIRE, 100).add(Aspect.EARTH, 100).add(Aspect.WATER, 100).add(Aspect.AIR, 100).add(Aspect.ORDER, 100).add(Aspect.ENTROPY, 100);
        TcIngredient ichor = TcIngredient.item("thaumictinkerer:ichor");
        TcIngredient ingot = TcIngredient.item("thaumictinkerer:ichorium_ingot");
        TcIngredient cloth = TcIngredient.item("thaumictinkerer:ichor_cloth");
        TcIngredient diamond = TcIngredient.item("minecraft:diamond");
        TcIngredient silverwoodRod = TcIngredient.item("thaumcraft:silverwood_rod");

        TtRecipes.arcane(
            ICHOR_CLOTH_RESEARCH,
            ICHOR_CLOTH_RESEARCH,
            TcStack.of("thaumictinkerer:ichor_cloth", 3),
            primals,
            new String[] {"CCC", "III", "DDD"},
            'C', TcIngredient.item("thaumcraft:enchanted_fabric"),
            'I', ichor,
            'D', diamond
        );
        TtRecipes.arcane(
            ICHORIUM_RESEARCH,
            ICHORIUM_RESEARCH,
            TcStack.of("thaumictinkerer:ichorium_ingot"),
            primalsHundred,
            new String[] {" T ", "IDI", " I "},
            'T', TcIngredient.item("thaumcraft:thaumium_ingot"),
            'I', ichor,
            'D', diamond
        );
        TtRecipes.arcane(
            ICHOR_CAP_RESEARCH,
            ICHOR_CAP_RESEARCH,
            TcStack.of("thaumictinkerer:ichorium_cap", 2),
            primalsHundred,
            new String[] {"ICI", " M ", "ICI"},
            'M', ingot,
            'I', ichor,
            'C', TcIngredient.item("thaumcraft:thaumium_wand_cap")
        );
        TtRecipes.infusion(
            ICHOR_RESEARCH,
            TcStack.of("thaumictinkerer:ichor", 8),
            7,
            new AspectList().add(Aspect.MAN, 32).add(Aspect.LIGHT, 32).add(Aspect.SOUL, 64),
            TcIngredient.item("minecraft:nether_star"),
            diamond,
            TcIngredient.item("thaumictinkerer:ender_shard"),
            TcIngredient.item("minecraft:ender_eye"),
            TcIngredient.item("thaumictinkerer:nether_shard")
        );
        TtRecipes.infusion(
            ICHORCLOTH_ROD_RESEARCH,
            TcStack.of("thaumictinkerer:ichorcloth_rod"),
            9,
            new AspectList().add(Aspect.MAGIC, 100).add(Aspect.LIGHT, 32).add(Aspect.TOOL, 32),
            silverwoodRod,
            ichor,
            cloth,
            TcIngredient.item("thaumcraft:salis_mundus"),
            TcIngredient.item("minecraft:ghast_tear"),
            TcIngredient.item("thaumcraft:salis_mundus"),
            cloth
        );
        TtRecipes.arcane(
            ICHORCLOTH_HELM_RECIPE,
            ICHORCLOTH_ARMOR_RESEARCH,
            TcStack.of("thaumictinkerer:ichorcloth_cowl"),
            new AspectList().add(Aspect.WATER, 75),
            new String[] {"CCC", "C C"},
            'C', cloth
        );
        TtRecipes.arcane(
            ICHORCLOTH_CHEST_RECIPE,
            ICHORCLOTH_ARMOR_RESEARCH,
            TcStack.of("thaumictinkerer:ichorcloth_robe"),
            new AspectList().add(Aspect.AIR, 75),
            new String[] {"C C", "CCC", "CCC"},
            'C', cloth
        );
        TtRecipes.arcane(
            ICHORCLOTH_LEGS_RECIPE,
            ICHORCLOTH_ARMOR_RESEARCH,
            TcStack.of("thaumictinkerer:ichorcloth_leggings"),
            new AspectList().add(Aspect.FIRE, 75),
            new String[] {"CCC", "C C", "C C"},
            'C', cloth
        );
        TtRecipes.arcane(
            ICHORCLOTH_BOOTS_RECIPE,
            ICHORCLOTH_ARMOR_RESEARCH,
            TcStack.of("thaumictinkerer:ichorcloth_boots"),
            new AspectList().add(Aspect.EARTH, 75),
            new String[] {"C C", "C C"},
            'C', cloth
        );
        TtRecipes.arcane(
            ICHOR_PICK_RECIPE,
            ICHOR_TOOLS_RESEARCH,
            TcStack.of("thaumictinkerer:ichorium_pickaxe"),
            new AspectList().add(Aspect.FIRE, 75),
            new String[] {"III", " R ", " R "},
            'R', silverwoodRod,
            'I', ingot
        );
        TtRecipes.arcane(
            ICHOR_SHOVEL_RECIPE,
            ICHOR_TOOLS_RESEARCH,
            TcStack.of("thaumictinkerer:ichorium_shovel"),
            new AspectList().add(Aspect.EARTH, 75),
            new String[] {" I ", " R ", " R "},
            'R', silverwoodRod,
            'I', ingot
        );
        TtRecipes.arcane(
            ICHOR_AXE_RECIPE,
            ICHOR_TOOLS_RESEARCH,
            TcStack.of("thaumictinkerer:ichorium_axe"),
            new AspectList().add(Aspect.WATER, 75),
            new String[] {"II ", "IR ", " R "},
            'R', silverwoodRod,
            'I', ingot
        );
        TtRecipes.arcane(
            ICHOR_SWORD_RECIPE,
            ICHOR_TOOLS_RESEARCH,
            TcStack.of("thaumictinkerer:ichorium_sword"),
            new AspectList().add(Aspect.AIR, 75),
            new String[] {" I ", " I ", " R "},
            'R', silverwoodRod,
            'I', ingot
        );
    }

    public static void research() {
        TtResearch.kami(DIMENSION_SHARDS_RESEARCH, new AspectList(), 7, 8, 0, TcStack.of("thaumictinkerer:ender_shard"))
            .setStub()
            .setAutoUnlock()
            .setRound()
            .setPages(TtResearch.text(0))
            .registerResearchItem();

        ResearchItem ichor = TtResearch.kami(
            ICHOR_RESEARCH,
            new AspectList().add(Aspect.MAN, 1).add(Aspect.LIGHT, 2).add(Aspect.SOUL, 1).add(Aspect.TAINT, 1),
            9,
            8,
            5,
            TcStack.of("thaumictinkerer:ichor")
        )
            .setWarp(5)
            .setPages(TtResearch.text(0), TtResearch.infusion(ICHOR_RESEARCH))
            .registerResearchItem();
        TtResearch.setKamiResearch(ichor);

        TtResearch.kami(
            ICHOR_CLOTH_RESEARCH,
            new AspectList().add(Aspect.CLOTH, 2).add(Aspect.LIGHT, 1).add(Aspect.CRAFT, 1).add(Aspect.SENSES, 1),
            11,
            7,
            5,
            TcStack.of("thaumictinkerer:ichor_cloth")
        )
            .setParents(ICHOR_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.arcane(ICHOR_CLOTH_RESEARCH))
            .registerResearchItem();

        TtResearch.kami(
            ICHORIUM_RESEARCH,
            new AspectList().add(Aspect.METAL, 2).add(Aspect.LIGHT, 1).add(Aspect.CRAFT, 1).add(Aspect.TOOL, 1),
            11,
            9,
            5,
            TcStack.of("thaumictinkerer:ichorium_ingot")
        )
            .setParents(ICHOR_RESEARCH)
            .setParentsHidden(ICHOR_CLOTH_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.arcane(ICHORIUM_RESEARCH))
            .registerResearchItem();

        TtResearch.kami(
            ICHOR_CAP_RESEARCH,
            new AspectList().add(Aspect.TOOL, 2).add(Aspect.METAL, 1).add(Aspect.LIGHT, 1).add(Aspect.MAGIC, 1),
            11,
            11,
            5,
            TcStack.of("thaumictinkerer:ichorium_cap")
        )
            .setWarp(2)
            .setParents(ICHORIUM_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.arcane(ICHOR_CAP_RESEARCH))
            .registerResearchItem();

        TtResearch.kami(
            ICHORCLOTH_ROD_RESEARCH,
            new AspectList().add(Aspect.TOOL, 2).add(Aspect.CLOTH, 1).add(Aspect.LIGHT, 1).add(Aspect.MAGIC, 1),
            14,
            2,
            5,
            TcStack.of("thaumictinkerer:ichorcloth_rod")
        )
            .setWarp(1)
            .setParents(ICHOR_CLOTH_RESEARCH)
            .setParentsHidden(ICHOR_CAP_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(ICHORCLOTH_ROD_RESEARCH))
            .registerResearchItem();

        TtResearch.kami(
            ICHORCLOTH_ARMOR_RESEARCH,
            new AspectList().add(Aspect.ARMOR, 2).add(Aspect.CLOTH, 1).add(Aspect.LIGHT, 1).add(Aspect.CRAFT, 1),
            17,
            5,
            5,
            TcStack.of("thaumictinkerer:ichorcloth_cowl")
        )
            .setWarp(4)
            .setParents(ICHOR_CLOTH_RESEARCH)
            .setPages(
                TtResearch.text(0),
                TtResearch.arcane(ICHORCLOTH_HELM_RECIPE),
                TtResearch.arcane(ICHORCLOTH_CHEST_RECIPE),
                TtResearch.arcane(ICHORCLOTH_LEGS_RECIPE),
                TtResearch.arcane(ICHORCLOTH_BOOTS_RECIPE)
            )
            .registerResearchItem();

        TtResearch.kami(
            ICHOR_TOOLS_RESEARCH,
            new AspectList().add(Aspect.TOOL, 2).add(Aspect.WEAPON, 1).add(Aspect.METAL, 1).add(Aspect.CRAFT, 1),
            13,
            12,
            5,
            TcStack.of("thaumictinkerer:ichorium_pickaxe")
        )
            .setWarp(2)
            .setParents(ICHORIUM_RESEARCH)
            .setParentsHidden(ICHORCLOTH_ROD_RESEARCH)
            .setPages(
                TtResearch.text(0),
                TtResearch.arcane(ICHOR_PICK_RECIPE),
                TtResearch.arcane(ICHOR_SHOVEL_RECIPE),
                TtResearch.arcane(ICHOR_AXE_RECIPE),
                TtResearch.arcane(ICHOR_SWORD_RECIPE)
            )
            .registerResearchItem();
    }
}
