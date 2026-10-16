package thaumictinkerer.repairer;

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

public final class RepairerFeature {
    public static final String REPAIRER_RESEARCH = "REPAIRER";

    public static final DeferredBlock<RepairerBlock> REPAIRER = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "thaumic_restorer",
        RepairerBlock::new,
        properties -> properties
            .mapColor(MapColor.METAL)
            .strength(5.0F, 10.0F)
            .sound(SoundType.STONE)
    ));

    public static final Supplier<BlockEntityType<RepairerBlockEntity>> REPAIRER_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "thaumic_restorer",
        () -> new BlockEntityType<>(RepairerBlockEntity::new, REPAIRER.get())
    );

    private RepairerFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        TtRecipes.infusion(
            REPAIRER_RESEARCH,
            TcStack.of("thaumictinkerer:thaumic_restorer"),
            8,
            new AspectList().add(Aspect.TOOL, 15).add(Aspect.CRAFT, 20).add(Aspect.ORDER, 10).add(Aspect.MAGIC, 15),
            TcIngredient.item("thaumcraft:thaumium_block"),
            TcIngredient.item("minecraft:iron_ingot"),
            TcIngredient.item("minecraft:gold_ingot"),
            TcIngredient.item("minecraft:diamond"),
            TcIngredient.item("minecraft:cobblestone"),
            TcIngredient.item("minecraft:oak_planks"),
            TcIngredient.item("minecraft:leather"),
            TcIngredient.item("thaumcraft:enchanted_fabric"),
            TcIngredient.item("thaumcraft:thaumium_ingot")
        );
    }

    public static void research() {
        TtResearch.item(
            REPAIRER_RESEARCH,
            new AspectList().add(Aspect.TOOL, 2).add(Aspect.CRAFT, 1).add(Aspect.ORDER, 1).add(Aspect.MAGIC, 1),
            -1,
            -9,
            3,
            TcStack.of("thaumictinkerer:thaumic_restorer")
        )
            .setConcealed()
            .setParents("FUNNEL")
            .setParentsHidden("THAUMIUM", "ENCHFABRIC")
            .setPages(
                TtResearch.text(0),
                TtResearch.infusion(REPAIRER_RESEARCH)
            )
            .registerResearchItem();
    }
}
