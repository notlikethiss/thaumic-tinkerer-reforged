package thaumictinkerer.foci;

import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.wand.focus.FocusItem;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.platform.TtStackKey;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.research.TtResearch;

public final class FociFeature {
    public static final String SMELT = "FOCUS_SMELT";
    public static final String FLIGHT = "FOCUS_FLIGHT";
    public static final String DISLOCATION = "FOCUS_DISLOCATION";
    public static final String TELEKINESIS = "FOCUS_TELEKINESIS";
    public static final String DEFLECT = "FOCUS_DEFLECT";
    public static final String ENDER_CHEST = "FOCUS_ENDER_CHEST";
    public static final String HEAL = "FOCUS_HEAL";

    public static final TtStackKey<DislocatedBlock> DISLOCATED = TtStackKey.register("dislocated_block", DislocatedBlock.CODEC, DislocatedBlock.STREAM_CODEC);

    public static final DeferredItem<FlightFocus> FOCUS_FLIGHT = ModItems.ITEMS.registerItem("focus_flight", FlightFocus::new, FocusItem::properties);
    public static final DeferredItem<DislocationFocus> FOCUS_DISLOCATION = ModItems.ITEMS.registerItem("focus_dislocation", DislocationFocus::new, FocusItem::properties);
    public static final DeferredItem<TelekinesisFocus> FOCUS_TELEKINESIS = ModItems.ITEMS.registerItem("focus_telekinesis", TelekinesisFocus::new, FocusItem::properties);
    public static final DeferredItem<SmeltFocus> FOCUS_SMELT = ModItems.ITEMS.registerItem("focus_smelt", SmeltFocus::new, FocusItem::properties);
    public static final DeferredItem<HealFocus> FOCUS_HEAL = ModItems.ITEMS.registerItem("focus_heal", HealFocus::new, FocusItem::properties);
    public static final DeferredItem<EnderChestFocus> FOCUS_ENDER_CHEST = ModItems.ITEMS.registerItem("focus_ender_chest", EnderChestFocus::new, FocusItem::properties);
    public static final DeferredItem<DeflectFocus> FOCUS_DEFLECT = ModItems.ITEMS.registerItem("focus_deflect", DeflectFocus::new, FocusItem::properties);

    private FociFeature() {
    }

    public static void load() {
    }

    private static TcIngredient item(String id) {
        return TcIngredient.item(id);
    }

    public static void recipes() {
        TtRecipes.arcane(
            SMELT, SMELT, TcStack.of("thaumictinkerer:focus_smelt"),
            new AspectList().add(Aspect.FIRE, 10).add(Aspect.ORDER, 5).add(Aspect.ENTROPY, 6),
            new String[]{"FNE"},
            'F', item("thaumcraft:focus_fire"),
            'E', item("thaumcraft:focus_excavation"),
            'N', item("thaumcraft:nitor")
        );
        TtRecipes.arcane(
            ENDER_CHEST, ENDER_CHEST, TcStack.of("thaumictinkerer:focus_ender_chest"),
            new AspectList().add(Aspect.ORDER, 10).add(Aspect.ENTROPY, 10),
            new String[]{"M", "E", "P"},
            'M', item("thaumcraft:magic_mirror"),
            'E', item("minecraft:ender_eye"),
            'P', item("thaumcraft:focus_portable_hole")
        );
        TtRecipes.infusion(
            FLIGHT, TcStack.of("thaumictinkerer:focus_flight"), 3,
            new AspectList().add(Aspect.AIR, 15).add(Aspect.MOTION, 20).add(Aspect.TRAVEL, 10),
            item("minecraft:ender_pearl"),
            item("minecraft:quartz"), item("minecraft:quartz"), item("minecraft:quartz"), item("minecraft:quartz"),
            item("minecraft:feather"), item("minecraft:feather"),
            item("thaumcraft:air_shard")
        );
        TtRecipes.infusion(
            DISLOCATION, TcStack.of("thaumictinkerer:focus_dislocation"), 8,
            new AspectList().add(Aspect.ELDRITCH, 20).add(Aspect.DARKNESS, 10).add(Aspect.VOID, 25).add(Aspect.MAGIC, 20).add(Aspect.TAINT, 5),
            item("minecraft:ender_pearl"),
            item("minecraft:quartz"), item("minecraft:quartz"), item("minecraft:quartz"), item("minecraft:quartz"),
            item("thaumcraft:amber"), item("thaumcraft:amber"), item("thaumcraft:amber"),
            item("minecraft:diamond")
        );
        TtRecipes.infusion(
            TELEKINESIS, TcStack.of("thaumictinkerer:focus_telekinesis"), 5,
            new AspectList().add(Aspect.MOTION, 10).add(Aspect.AIR, 20).add(Aspect.ENTROPY, 20).add(Aspect.MIND, 10),
            item("minecraft:ender_pearl"),
            item("minecraft:quartz"), item("minecraft:quartz"), item("minecraft:quartz"), item("minecraft:quartz"),
            item("minecraft:iron_ingot"), item("minecraft:gold_ingot"),
            item("thaumcraft:air_shard")
        );
        TtRecipes.infusion(
            HEAL, TcStack.of("thaumictinkerer:focus_heal"), 4,
            new AspectList().add(Aspect.HEAL, 10).add(Aspect.SOUL, 10).add(Aspect.LIFE, 15),
            item("thaumcraft:focus_pech"),
            item("minecraft:golden_carrot"), item("minecraft:gold_nugget"), item("minecraft:gold_nugget"), item("minecraft:gold_nugget")
        );
        TtRecipes.infusion(
            DEFLECT, TcStack.of("thaumictinkerer:focus_deflect"), 5,
            new AspectList().add(Aspect.AIR, 15).add(Aspect.ARMOR, 5).add(Aspect.ORDER, 20),
            item("thaumictinkerer:focus_flight"),
            item("thaumcraft:mirrored_glass"), item("thaumcraft:mirrored_glass"),
            item("thaumcraft:warding_paving_stone"),
            item("thaumcraft:order_shard")
        );
    }

    public static void research() {
        TtResearch.item(SMELT, new AspectList().add(Aspect.FIRE, 2).add(Aspect.ENERGY, 1).add(Aspect.MAGIC, 1), -2, -2, 2, TcStack.of("thaumictinkerer:focus_smelt"))
            .setParents("FOCUSEXCAVATION")
            .setConcealed()
            .setPages(TtResearch.text(0), TtResearch.arcane(SMELT))
            .registerResearchItem();
        TtResearch.item(FLIGHT, new AspectList().add(Aspect.MOTION, 1).add(Aspect.MAGIC, 1).add(Aspect.AIR, 2), -3, -4, 2, TcStack.of("thaumictinkerer:focus_flight"))
            .setParents(SMELT)
            .setConcealed()
            .setPages(TtResearch.text(0), TtResearch.infusion(FLIGHT))
            .registerResearchItem();
        TtResearch.item(DISLOCATION, new AspectList().add(Aspect.ELDRITCH, 2).add(Aspect.MAGIC, 1).add(Aspect.EXCHANGE, 1), -5, -5, 2, TcStack.of("thaumictinkerer:focus_dislocation"))
            .when(thaumcraft.Config::allowMirrors)
            .setSecondary()
            .setParents(FLIGHT)
            .setConcealed()
            .setPages(TtResearch.text(0), TtResearch.text(1), TtResearch.infusion(DISLOCATION))
            .registerResearchItem();
        TtResearch.item(TELEKINESIS, new AspectList().add(Aspect.ELDRITCH, 2).add(Aspect.MAGIC, 1).add(Aspect.MOTION, 1), -4, -6, 2, TcStack.of("thaumictinkerer:focus_telekinesis"))
            .setParents(FLIGHT)
            .setConcealed()
            .setPages(TtResearch.text(0), TtResearch.infusion(TELEKINESIS))
            .setSecondary()
            .registerResearchItem();
        TtResearch.item(DEFLECT, new AspectList().add(Aspect.MOTION, 2).add(Aspect.AIR, 1).add(Aspect.ORDER, 1).add(Aspect.DEATH, 1), -4, -3, 3, TcStack.of("thaumictinkerer:focus_deflect"))
            .when(thaumcraft.Config::allowMirrors)
            .setConcealed()
            .setParents(SMELT)
            .setPages(TtResearch.text(0), TtResearch.infusion(DEFLECT))
            .setSecondary()
            .registerResearchItem();
        TtResearch.item(HEAL, new AspectList().add(Aspect.HEAL, 2).add(Aspect.SOUL, 1).add(Aspect.MAGIC, 1), -6, -4, 2, TcStack.of("thaumictinkerer:focus_heal"))
            .when(thaumcraft.Config::allowMirrors)
            .setParents(DEFLECT)
            .setConcealed()
            .setPages(TtResearch.text(0), TtResearch.infusion(HEAL))
            .setSecondary()
            .registerResearchItem();
        TtResearch.item(ENDER_CHEST, new AspectList().add(Aspect.ELDRITCH, 2).add(Aspect.VOID, 1).add(Aspect.MAGIC, 1), -6, -2, 2, TcStack.of("thaumictinkerer:focus_ender_chest"))
            .when(thaumcraft.Config::allowMirrors)
            .setWarp(1)
            .setParents(DEFLECT)
            .setConcealed()
            .setPages(TtResearch.text(0), TtResearch.arcane(ENDER_CHEST))
            .registerResearchItem();
    }
}
