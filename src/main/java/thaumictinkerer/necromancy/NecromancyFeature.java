package thaumictinkerer.necromancy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.gear.GearFeature;
import thaumictinkerer.platform.TtStackKey;
import thaumictinkerer.registry.ModBlockEntities;
import thaumictinkerer.registry.ModBlocks;
import thaumictinkerer.registry.ModCreativeTabs;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.research.TtResearch;

public final class NecromancyFeature {
    public static final String SUMMON = "SUMMON";
    public static final String SUMMON_TABLET = "SUMMON0";
    public static final String CONDENSE_PREFIX = "SUMMON1_";
    public static final String INFUSE_PREFIX = "SUMMON_INFUSED_";
    public static final int ASPECT_COUNT = 17;

    public static final List<Aspect> ASPECTS = List.of(
        Aspect.WATER, Aspect.MAN, Aspect.AIR, Aspect.FLIGHT, Aspect.FIRE, Aspect.MAGIC, Aspect.UNDEAD, Aspect.FLESH, Aspect.BEAST,
        Aspect.POISON, Aspect.EARTH, Aspect.ELDRITCH, Aspect.TRAVEL, Aspect.METAL, Aspect.SLIME, Aspect.CLOTH, Aspect.CROP
    );

    public static final TtStackKey<BlockPos> LAST_TABLET = TtStackKey.register("last_tablet", BlockPos.CODEC, BlockPos.STREAM_CODEC);

    public static final List<DeferredItem<SoulAspectItem>> BASE = new ArrayList<>();
    public static final List<DeferredItem<SoulAspectItem>> CONDENSED = new ArrayList<>();
    public static final List<DeferredItem<SoulAspectItem>> INFUSED = new ArrayList<>();

    static {
        for (Aspect aspect : ASPECTS) {
            BASE.add(soul(aspect, SoulAspectItem.Form.BASE, aspect.getTag() + "_soul_aspect"));
            CONDENSED.add(soul(aspect, SoulAspectItem.Form.CONDENSED, "condensed_" + aspect.getTag() + "_soul_aspect"));
            INFUSED.add(soul(aspect, SoulAspectItem.Form.INFUSED, "infused_" + aspect.getTag() + "_soul_aspect"));
        }
    }

    public static final DeferredBlock<NecromancyTabletBlock> TABLET = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "necromancy_tablet",
        NecromancyTabletBlock::new,
        properties -> properties
            .mapColor(MapColor.METAL)
            .strength(3.0F, 50.0F)
            .sound(SoundType.METAL)
    ));

    public static final Supplier<BlockEntityType<NecromancyTabletBlockEntity>> TABLET_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "necromancy_tablet",
        () -> new BlockEntityType<>(NecromancyTabletBlockEntity::new, TABLET.get())
    );

    private NecromancyFeature() {
    }

    private static DeferredItem<SoulAspectItem> soul(Aspect aspect, SoulAspectItem.Form form, String name) {
        return ModItems.ITEMS.registerItem(
            name,
            properties -> new SoulAspectItem(properties, aspect, form),
            properties -> properties.stacksTo(16)
        );
    }

    public static void load() {
        for (DeferredItem<SoulAspectItem> item : CONDENSED) {
            ModCreativeTabs.hide(item);
        }
    }

    public static ItemStack soulStack(Aspect aspect) {
        return new ItemStack(BASE.get(ASPECTS.indexOf(aspect)).get());
    }

    public static ItemStack infusedStack(Aspect aspect) {
        return new ItemStack(INFUSED.get(ASPECTS.indexOf(aspect)).get());
    }

    public static void aspects(AspectRegistry registry) {
        for (int index = 0; index < ASPECT_COUNT; index++) {
            registry.register(BASE.get(index).get(), new AspectList().add(ASPECTS.get(index), 8));
        }
    }

    public static void recipes() {
        TtRecipes.arcane(
            SUMMON_TABLET, SUMMON,
            TcStack.of("thaumictinkerer:necromancy_tablet"),
            new AspectList().add(Aspect.ORDER, 50).add(Aspect.ENTROPY, 50),
            new String[] {"WWW", "SSS"},
            'S', TcIngredient.item("minecraft:stone"),
            'W', TcIngredient.item("thaumcraft:obsidian_tile")
        );
        for (int index = 0; index < ASPECT_COUNT; index++) {
            Aspect aspect = ASPECTS.get(index);
            String tag = aspect.getTag();
            String condensed = "thaumictinkerer:condensed_" + tag + "_soul_aspect";
            TtRecipes.vanilla(CONDENSE_PREFIX + tag.toUpperCase(), SUMMON, "condensed_" + tag + "_soul_aspect", TcStack.of(condensed));
            TcIngredient input = TcIngredient.item(condensed);
            TtRecipes.infusion(
                INFUSE_PREFIX + tag.toUpperCase(),
                SUMMON,
                TcStack.of("thaumictinkerer:infused_" + tag + "_soul_aspect"),
                4,
                new AspectList().add(aspect, 10),
                input,
                input, input, input, input, input, input, input, input
            );
        }
    }

    public static void research() {
        String[] condenseKeys = new String[ASPECT_COUNT];
        String[] infuseKeys = new String[ASPECT_COUNT];
        for (int index = 0; index < ASPECT_COUNT; index++) {
            String tag = ASPECTS.get(index).getTag().toUpperCase();
            condenseKeys[index] = CONDENSE_PREFIX + tag;
            infuseKeys[index] = INFUSE_PREFIX + tag;
        }
        TtResearch.item(
            SUMMON,
            new AspectList().add(Aspect.WEAPON, 1).add(Aspect.BEAST, 3).add(Aspect.MAGIC, 3),
            -5,
            8,
            3,
            TcStack.of("thaumictinkerer:necromancy_tablet")
        )
            .setWarp(3)
            .setParents(GearFeature.BLOOD_SWORD_RESEARCH)
            .setPages(
                TtResearch.text(0),
                TtResearch.arcane(SUMMON_TABLET),
                TtResearch.recipe(condenseKeys),
                TtResearch.infusion(infuseKeys),
                TtResearch.text(1)
            )
            .registerResearchItem();
    }
}
