package thaumictinkerer.kami.foci;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.wand.focus.FocusItem;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.kami.KamiFeature;
import thaumictinkerer.registry.ModEntities;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.research.TtResearch;

public final class KamiFociFeature {
    public static final String SHADOWBEAM = "FOCUS_SHADOWBEAM";
    public static final String XP_DRAIN = "FOCUS_XP_DRAIN";
    public static final String RECALL = "FOCUS_RECALL";
    public static final String WARP_GATE = "WARP_GATE";

    public static final DeferredItem<ShadowbeamFocus> FOCUS_SHADOWBEAM = ModItems.ITEMS.registerItem("focus_shadowbeam", ShadowbeamFocus::new, FocusItem::properties);
    public static final DeferredItem<XpDrainFocus> FOCUS_XP_DRAIN = ModItems.ITEMS.registerItem("focus_xp_drain", XpDrainFocus::new, FocusItem::properties);
    public static final DeferredItem<RecallFocus> FOCUS_RECALL = ModItems.ITEMS.registerItem("focus_recall", RecallFocus::new, FocusItem::properties);

    public static final DeferredHolder<EntityType<?>, EntityType<ShadowbeamFocus.Beam>> BEAM = ModEntities.ENTITIES.register(
        "shadowbeam_staff_beam",
        () -> EntityType.Builder.<ShadowbeamFocus.Beam>of(ShadowbeamFocus.Beam::new, MobCategory.MISC).sized(0.25F, 0.25F).noSave().noSummon().clientTrackingRange(4).updateInterval(20).build("thaumictinkerer:shadowbeam_staff_beam")
    );

    private KamiFociFeature() {
    }

    public static void load() {
    }

    private static TcIngredient item(String id) {
        return TcIngredient.item(id);
    }

    public static void recipes() {
        TtRecipes.infusion(
            SHADOWBEAM,
            TcStack.of("thaumictinkerer:focus_shadowbeam"),
            12,
            new AspectList().add(Aspect.DARKNESS, 65).add(Aspect.ELDRITCH, 32).add(Aspect.MAGIC, 50).add(Aspect.WEAPON, 32),
            item("thaumcraft:focus_shock"),
            item("thaumictinkerer:ichor"),
            item("minecraft:arrow"),
            item("minecraft:diamond"),
            item("thaumcraft:focus_excavation"),
            item("thaumictinkerer:focus_deflect"),
            item("thaumictinkerer:ichor")
        );
        TtRecipes.infusion(
            XP_DRAIN,
            TcStack.of("thaumictinkerer:focus_xp_drain"),
            12,
            new AspectList().add(Aspect.MIND, 65).add(Aspect.TAINT, 16).add(Aspect.MAGIC, 50).add(Aspect.AURA, 32),
            item("minecraft:ender_pearl"),
            item("thaumictinkerer:ichor"),
            item("minecraft:experience_bottle"),
            item("minecraft:diamond"),
            item("thaumictinkerer:xp_talisman"),
            item("minecraft:enchanting_table"),
            item("thaumictinkerer:ichor")
        );
        TtRecipes.infusion(
            RECALL,
            TcStack.of("thaumictinkerer:focus_recall"),
            10,
            new AspectList().add(Aspect.TRAVEL, 100).add(Aspect.ELDRITCH, 64).add(Aspect.MAGIC, 50),
            item("thaumictinkerer:sky_pearl"),
            item("thaumictinkerer:ichor"),
            item("thaumictinkerer:ichor"),
            item("minecraft:ender_pearl"),
            item("minecraft:diamond"),
            item("thaumcraft:magic_mirror"),
            item("thaumcraft:focus_portable_hole")
        );
    }

    public static void research() {
        TtResearch.kami(
            SHADOWBEAM,
            new AspectList().add(Aspect.DARKNESS, 2).add(Aspect.MAGIC, 1).add(Aspect.ELDRITCH, 1).add(Aspect.TAINT, 1),
            14,
            4,
            5,
            TcStack.of("thaumictinkerer:focus_shadowbeam")
        )
            .setParents(KamiFeature.ICHORCLOTH_ROD_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(SHADOWBEAM))
            .registerResearchItem();

        TtResearch.kami(
            XP_DRAIN,
            new AspectList().add(Aspect.MIND, 2).add(Aspect.MAGIC, 1).add(Aspect.AURA, 1).add(Aspect.MAN, 1),
            12,
            3,
            5,
            TcStack.of("thaumictinkerer:focus_xp_drain")
        )
            .setParents(KamiFeature.ICHORCLOTH_ROD_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(XP_DRAIN))
            .registerResearchItem();

        TtResearch.kami(
            RECALL,
            new AspectList().add(Aspect.TRAVEL, 2).add(Aspect.ELDRITCH, 1).add(Aspect.FLIGHT, 1).add(Aspect.MAGIC, 1),
            20,
            8,
            5,
            TcStack.of("thaumictinkerer:focus_recall")
        )
            .when(thaumictinkerer.platform.TtBaseConfig::allowMirrors)
            .setParents(WARP_GATE)
            .setParentsHidden(KamiFeature.ICHORCLOTH_ROD_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(RECALL))
            .registerResearchItem();
    }
}
