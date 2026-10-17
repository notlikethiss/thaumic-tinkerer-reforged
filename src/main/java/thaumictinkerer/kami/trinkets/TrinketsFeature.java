package thaumictinkerer.kami.trinkets;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.platform.Menus;
import thaumictinkerer.TtRarity;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.platform.TtStackKey;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.registry.ModMenus;
import thaumictinkerer.research.TtResearch;

public final class TrinketsFeature {
    public static final String BLOCK_TALISMAN_RESEARCH = "BLOCK_TALISMAN";
    public static final String PLACEMENT_MIRROR_RESEARCH = "PLACEMENT_MIRROR";
    public static final String PROTOCLAY_RESEARCH = "PROTOCLAY";
    public static final String ICHOR_POUCH_RESEARCH = "ICHOR_POUCH";
    public static final String CAT_AMULET_RESEARCH = "CAT_AMULET";

    public static final TtStackKey<Identifier> TALISMAN_BLOCK = TtStackKey.register("talisman_block", Identifier.CODEC, Identifier.STREAM_CODEC);
    public static final TtStackKey<Integer> TALISMAN_COUNT = TtStackKey.register("talisman_count", Codec.INT, ByteBufCodecs.VAR_INT);
    public static final TtStackKey<Boolean> RING_ACTIVE = TtStackKey.register("ring_active", Codec.BOOL, ByteBufCodecs.BOOL);
    public static final TtStackKey<Identifier> MIRROR_BLOCK = TtStackKey.register("mirror_block", Identifier.CODEC, Identifier.STREAM_CODEC);
    public static final TtStackKey<Integer> MIRROR_SIZE = TtStackKey.register("mirror_size", Codec.INT, ByteBufCodecs.VAR_INT);

    public static final DeferredItem<BlackHoleRingItem> BLOCK_TALISMAN = ModItems.ITEMS.registerItem(
        "block_talisman",
        BlackHoleRingItem::new,
        properties -> properties.stacksTo(1).rarity(TtRarity.kami())
    );
    public static final DeferredItem<WorldshaperGlassItem> PLACEMENT_MIRROR = ModItems.ITEMS.registerItem(
        "placement_mirror",
        WorldshaperGlassItem::new,
        properties -> properties.stacksTo(1).rarity(TtRarity.kami())
    );
    public static final DeferredItem<ProtoclayItem> PROTOCLAY = ModItems.ITEMS.registerItem(
        "protoclay",
        ProtoclayItem::new,
        properties -> properties.stacksTo(1).rarity(TtRarity.kami())
    );
    public static final DeferredItem<IchorPouchItem> ICHOR_POUCH = ModItems.ITEMS.registerItem(
        "ichor_pouch",
        IchorPouchItem::new,
        properties -> properties.stacksTo(1).rarity(TtRarity.kami())
    );
    public static final DeferredItem<CatAmuletItem> CAT_AMULET = ModItems.ITEMS.registerItem(
        "cat_amulet",
        CatAmuletItem::new,
        properties -> properties.stacksTo(1).rarity(TtRarity.kami())
    );

    public static final DeferredHolder<MenuType<?>, MenuType<IchorPouchMenu>> ICHOR_POUCH_MENU = ModMenus.MENUS.register(
        "ichor_pouch",
        () -> Menus.simple(IchorPouchMenu::new)
    );

    private TrinketsFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        TcIngredient ichor = TcIngredient.item("thaumictinkerer:ichor");
        TcIngredient cloth = TcIngredient.item("thaumictinkerer:ichor_cloth");
        TcIngredient diamond = TcIngredient.item("minecraft:diamond");
        TcIngredient portableHole = TcIngredient.item("thaumcraft:focus_portable_hole");
        TcIngredient voidJar = TcIngredient.item("thaumcraft:void_jar");

        TtRecipes.infusion(
            BLOCK_TALISMAN_RESEARCH,
            TcStack.of("thaumictinkerer:block_talisman"),
            9,
            new AspectList().add(Aspect.VOID, 65).add(Aspect.DARKNESS, 32).add(Aspect.MAGIC, 50).add(Aspect.ELDRITCH, 32),
            portableHole,
            ichor,
            TcIngredient.item("minecraft:ender_chest"),
            diamond,
            ichor,
            TcIngredient.item("thaumcraft:tainted_goo"),
            voidJar
        );
        TtRecipes.infusion(
            PLACEMENT_MIRROR_RESEARCH,
            TcStack.of("thaumictinkerer:placement_mirror"),
            12,
            new AspectList().add(Aspect.CRAFT, 65).add(Aspect.CRYSTAL, 32).add(Aspect.MAGIC, 50).add(Aspect.MIND, 32),
            TcIngredient.item("thaumictinkerer:block_talisman"),
            ichor,
            TcIngredient.item("minecraft:dropper"),
            diamond,
            TcIngredient.item("minecraft:glass"),
            TcIngredient.item("minecraft:blaze_powder"),
            ichor
        );
        TtRecipes.infusion(
            PROTOCLAY_RESEARCH,
            TcStack.of("thaumictinkerer:protoclay"),
            4,
            new AspectList().add(Aspect.MINE, 16).add(Aspect.TOOL, 16),
            TcIngredient.item("minecraft:clay_ball"),
            TcIngredient.item("minecraft:dirt"),
            TcIngredient.item("minecraft:stone"),
            TcIngredient.item("minecraft:oak_log"),
            TcIngredient.item("thaumictinkerer:ender_shard")
        );
        TtRecipes.infusion(
            ICHOR_POUCH_RESEARCH,
            TcStack.of("thaumictinkerer:ichor_pouch"),
            9,
            new AspectList().add(Aspect.VOID, 64).add(Aspect.MAN, 32).add(Aspect.CLOTH, 32).add(Aspect.ELDRITCH, 32).add(Aspect.AIR, 64),
            TcIngredient.item("thaumcraft:focus_pouch"),
            cloth,
            portableHole,
            diamond,
            cloth,
            TcIngredient.item("thaumcraft:hungry_chest"),
            voidJar
        );
        TtRecipes.infusion(
            CAT_AMULET_RESEARCH,
            TcStack.of("thaumictinkerer:cat_amulet"),
            8,
            new AspectList().add(Aspect.DARKNESS, 16).add(Aspect.ORDER, 32).add(Aspect.MIND, 16),
            TcIngredient.item("minecraft:quartz_block"),
            ichor,
            TcIngredient.item("minecraft:gold_ingot"),
            TcIngredient.item("minecraft:gold_ingot"),
            TcIngredient.item("minecraft:cocoa_beans"),
            TcIngredient.item("minecraft:jungle_leaves"),
            TcIngredient.item("minecraft:cod")
        );
    }

    public static void research() {
        TtResearch.kami(
            BLOCK_TALISMAN_RESEARCH,
            new AspectList().add(Aspect.VOID, 2).add(Aspect.DARKNESS, 1).add(Aspect.ELDRITCH, 1).add(Aspect.MAGIC, 1),
            14,
            17,
            5,
            TcStack.of("thaumictinkerer:block_talisman")
        )
            .setParents("ICHOR_PICK_GEM", "ICHOR_SHOVEL_GEM")
            .setPages(TtResearch.text(0), TtResearch.infusion(BLOCK_TALISMAN_RESEARCH))
            .registerResearchItem();

        TtResearch.kami(
            PLACEMENT_MIRROR_RESEARCH,
            new AspectList().add(Aspect.CRAFT, 2).add(Aspect.CRYSTAL, 1).add(Aspect.ELDRITCH, 1).add(Aspect.MIND, 1),
            17,
            16,
            5,
            TcStack.of("thaumictinkerer:placement_mirror")
        )
            .setParents(BLOCK_TALISMAN_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(PLACEMENT_MIRROR_RESEARCH))
            .registerResearchItem();

        TtResearch.kami(
            PROTOCLAY_RESEARCH,
            new AspectList().add(Aspect.TOOL, 2).add(Aspect.MINE, 1).add(Aspect.MAN, 1).add(Aspect.MECHANISM, 1),
            12,
            17,
            5,
            TcStack.of("thaumictinkerer:protoclay")
        )
            .setParents("ICHOR_PICK_GEM")
            .setParentsHidden("ICHOR_SHOVEL_GEM")
            .setPages(TtResearch.text(0), TtResearch.infusion(PROTOCLAY_RESEARCH))
            .registerResearchItem();

        TtResearch.kami(
            ICHOR_POUCH_RESEARCH,
            new AspectList().add(Aspect.VOID, 2).add(Aspect.CLOTH, 1).add(Aspect.ELDRITCH, 1).add(Aspect.MAN, 1),
            13,
            6,
            5,
            TcStack.of("thaumictinkerer:ichor_pouch")
        )
            .setParents("ICHOR_CLOTH")
            .setPages(TtResearch.text(0), TtResearch.infusion(ICHOR_POUCH_RESEARCH))
            .registerResearchItem();

        TtResearch.kami(
            CAT_AMULET_RESEARCH,
            new AspectList().add(Aspect.MIND, 2).add(Aspect.ORDER, 1).add(Aspect.DARKNESS, 1).add(Aspect.DEATH, 1),
            13,
            10,
            5,
            TcStack.of("thaumictinkerer:cat_amulet")
        )
            .setParents("ICHORIUM")
            .setPages(TtResearch.text(0), TtResearch.infusion(CAT_AMULET_RESEARCH))
            .registerResearchItem();
    }
}
