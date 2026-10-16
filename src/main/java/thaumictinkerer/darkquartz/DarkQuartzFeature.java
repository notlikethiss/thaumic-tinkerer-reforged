package thaumictinkerer.darkquartz;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.registry.ModBlocks;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.research.TtResearch;

public final class DarkQuartzFeature {
    public static final String RESEARCH = "DARK_QUARTZ";

    public static final DeferredItem<Item> DARK_QUARTZ = ModItems.ITEMS.registerSimpleItem("dark_quartz");
    public static final DeferredBlock<Block> DARK_QUARTZ_BLOCK = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "dark_quartz_block",
        Block::new,
        DarkQuartzFeature::quartz
    ));
    public static final DeferredBlock<Block> CHISELED_DARK_QUARTZ_BLOCK = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "chiseled_dark_quartz_block",
        Block::new,
        DarkQuartzFeature::quartz
    ));
    public static final DeferredBlock<RotatedPillarBlock> DARK_QUARTZ_PILLAR = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "dark_quartz_pillar",
        RotatedPillarBlock::new,
        DarkQuartzFeature::quartz
    ));
    public static final DeferredBlock<SlabBlock> DARK_QUARTZ_SLAB = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "dark_quartz_slab",
        SlabBlock::new,
        DarkQuartzFeature::quartz
    ));
    public static final DeferredBlock<StairBlock> DARK_QUARTZ_STAIRS = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "dark_quartz_stairs",
        properties -> new StairBlock(DarkQuartzFeature.DARK_QUARTZ_BLOCK.get().defaultBlockState(), properties),
        DarkQuartzFeature::quartz
    ));

    private DarkQuartzFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        TtRecipes.vanilla("DARK_QUARTZ0", RESEARCH, "dark_quartz_from_charcoal", TcStack.of("thaumictinkerer:dark_quartz", 4));
        TtRecipes.vanilla("DARK_QUARTZ0_COAL", RESEARCH, "dark_quartz_from_coal", TcStack.of("thaumictinkerer:dark_quartz", 4));
        TtRecipes.vanilla("DARK_QUARTZ1", RESEARCH, "dark_quartz_block", TcStack.of("thaumictinkerer:dark_quartz_block"));
        TtRecipes.vanilla("DARK_QUARTZ2", RESEARCH, "dark_quartz_slab", TcStack.of("thaumictinkerer:dark_quartz_slab", 6));
        TtRecipes.vanilla("DARK_QUARTZ3", RESEARCH, "dark_quartz_pillar", TcStack.of("thaumictinkerer:dark_quartz_pillar", 2));
        TtRecipes.vanilla("DARK_QUARTZ4", RESEARCH, "chiseled_dark_quartz_block", TcStack.of("thaumictinkerer:chiseled_dark_quartz_block"));
        TtRecipes.vanilla("DARK_QUARTZ5", RESEARCH, "dark_quartz_stairs", TcStack.of("thaumictinkerer:dark_quartz_stairs", 4));
    }

    public static void research() {
        TtResearch.item(RESEARCH, new AspectList(), -2, 2, 0, TcStack.of("thaumictinkerer:dark_quartz"))
            .setPages(
                TtResearch.text(0),
                TtResearch.recipe("DARK_QUARTZ0", "DARK_QUARTZ0_COAL"),
                TtResearch.recipe("DARK_QUARTZ1"),
                TtResearch.recipe("DARK_QUARTZ2"),
                TtResearch.recipe("DARK_QUARTZ3"),
                TtResearch.recipe("DARK_QUARTZ4"),
                TtResearch.recipe("DARK_QUARTZ5")
            )
            .setStub()
            .setAutoUnlock()
            .setRound()
            .registerResearchItem();
    }

    private static BlockBehaviour.Properties quartz(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.COLOR_BLACK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(0.8F, 10.0F)
            .sound(SoundType.STONE);
    }
}
