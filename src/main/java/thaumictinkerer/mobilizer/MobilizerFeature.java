package thaumictinkerer.mobilizer;

import java.util.function.Supplier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

public final class MobilizerFeature {
    public static final String MOBILIZER_RESEARCH = "LEVITATOR_LOCOMOTIVE";
    public static final String RELAY_RECIPE = "LEVITATOR_RELAY";

    public static final DeferredBlock<MobilizerBlock> MOBILIZER = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "levitational_locomotive",
        MobilizerBlock::new,
        MobilizerFeature::properties
    ));
    public static final DeferredBlock<RelayBlock> RELAY = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "levitational_locomotive_relay",
        RelayBlock::new,
        MobilizerFeature::properties
    ));

    public static final Supplier<BlockEntityType<MobilizerBlockEntity>> MOBILIZER_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "levitational_locomotive",
        () -> BlockEntityType.Builder.of(MobilizerBlockEntity::new,  MOBILIZER.get()).build(null)
    );
    public static final Supplier<BlockEntityType<RelayBlockEntity>> RELAY_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "levitational_locomotive_relay",
        () -> BlockEntityType.Builder.of(RelayBlockEntity::new,  RELAY.get()).build(null)
    );

    private MobilizerFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        TtRecipes.infusion(
            MOBILIZER_RESEARCH,
            TcStack.of("thaumictinkerer:levitational_locomotive"),
            4,
            new AspectList().add(Aspect.MOTION, 15).add(Aspect.ORDER, 20).add(Aspect.MAGIC, 15),
            TcIngredient.item("thaumcraft:arcane_levitator"),
            TcIngredient.item("minecraft:iron_ingot"),
            TcIngredient.item("minecraft:feather"),
            TcIngredient.item("minecraft:iron_ingot"),
            TcIngredient.item("thaumcraft:obsidian_tile")
        );
        TtRecipes.arcane(
            RELAY_RECIPE, MOBILIZER_RESEARCH,
            TcStack.of("thaumictinkerer:levitational_locomotive_relay"),
            new AspectList().add(Aspect.AIR, 20).add(Aspect.ORDER, 5).add(Aspect.EARTH, 15),
            new String[]{"WFW", "SIs", "WFW"},
            'I', TcIngredient.item("minecraft:iron_ingot"),
            's', TcIngredient.item("thaumcraft:earth_shard"),
            'S', TcIngredient.item("thaumcraft:air_shard"),
            'W', TcIngredient.item("thaumcraft:greatwood_log"),
            'F', TcIngredient.item("minecraft:glass")
        );
    }

    public static void research() {
        TtResearch.item(MOBILIZER_RESEARCH, new AspectList().add(Aspect.MOTION, 2).add(Aspect.ORDER, 2), -7, 5, 3, TcStack.of("thaumictinkerer:levitational_locomotive"))
            .setParents("MAGNETS")
            .setPages(
                TtResearch.text(0),
                TtResearch.infusion(MOBILIZER_RESEARCH),
                TtResearch.arcane(RELAY_RECIPE)
            )
            .setSecondary()
            .registerResearchItem();
    }

    private static BlockBehaviour.Properties properties(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.METAL)
            .strength(0.0F)
            .sound(SoundType.STONE);
    }
}
