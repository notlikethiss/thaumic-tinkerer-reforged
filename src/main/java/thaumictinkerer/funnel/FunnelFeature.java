package thaumictinkerer.funnel;

import java.util.function.Supplier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.registry.ModBlockEntities;
import thaumictinkerer.registry.ModBlocks;
import thaumictinkerer.research.TtResearch;

public final class FunnelFeature {
    public static final String FUNNEL_RESEARCH = "FUNNEL";

    public static final DeferredBlock<FunnelBlock> FUNNEL = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "essentia_funnel",
        FunnelBlock::new,
        properties -> properties
            .mapColor(MapColor.STONE)
            .strength(3.0F, 8.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
    ));

    public static final Supplier<BlockEntityType<FunnelBlockEntity>> FUNNEL_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "essentia_funnel",
        () -> new BlockEntityType<>(FunnelBlockEntity::new, FUNNEL.get())
    );

    private FunnelFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        TtRecipes.arcane(
            FUNNEL_RESEARCH, FUNNEL_RESEARCH,
            TcStack.of("thaumictinkerer:essentia_funnel"),
            new AspectList().add(Aspect.ORDER, 1).add(Aspect.ENTROPY, 1),
            new String[]{"STS"},
            'S', TcIngredient.item("minecraft:stone"),
            'T', TcIngredient.item("thaumcraft:thaumium_ingot")
        );
    }

    public static void research() {
        TtResearch.item(FUNNEL_RESEARCH, new AspectList().add(Aspect.TOOL, 1).add(Aspect.TRAVEL, 2), 0, -7, 1, TcStack.of("thaumictinkerer:essentia_funnel"))
            .setParentsHidden("DISTILESSENTIA")
            .setParents("BRIGHT_NITOR")
            .setConcealed()
            .setPages(
                TtResearch.text(0),
                TtResearch.arcane(FUNNEL_RESEARCH)
            )
            .setSecondary()
            .registerResearchItem();
    }
}
