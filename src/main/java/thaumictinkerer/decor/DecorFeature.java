package thaumictinkerer.decor;

import java.util.function.Supplier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import thaumictinkerer.registry.ModBlockEntities;
import thaumictinkerer.registry.ModBlocks;

public final class DecorFeature {
    public static final DeferredBlock<TravelSlabBlock> TRAVEL_SLAB = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "travel_slab",
        TravelSlabBlock::new,
        properties -> stone(properties, 0.8F, 10.0F)
    ));
    public static final DeferredBlock<TravelStairsBlock> TRAVEL_STAIRS = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "travel_stairs",
        properties -> new TravelStairsBlock(thaumcraft.registry.ModBlocks.TRAVEL_PAVING_STONE.get().defaultBlockState(), properties),
        properties -> stone(properties, 2.0F, 10.0F)
    ));
    public static final DeferredBlock<WardSlabBlock> WARD_SLAB = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "ward_slab",
        WardSlabBlock::new,
        properties -> stone(properties, 0.8F, 10.0F)
    ));

    public static final Supplier<BlockEntityType<WardedSlabBlockEntity>> WARDED_SLAB = ModBlockEntities.BLOCK_ENTITIES.register(
        "warded_slab",
        () -> new BlockEntityType<>(WardedSlabBlockEntity::new, WARD_SLAB.get())
    );

    private DecorFeature() {
    }

    public static void load() {
    }

    private static BlockBehaviour.Properties stone(BlockBehaviour.Properties properties, float hardness, float resistance) {
        return properties
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(hardness, resistance)
            .sound(SoundType.STONE);
    }
}
