package thaumictinkerer.transvector;

import java.util.function.Supplier;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumictinkerer.camo.CamoBlockEntity;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.platform.TtStackKey;
import thaumictinkerer.platform.TtTransfer;
import thaumictinkerer.registry.ModBlockEntities;
import thaumictinkerer.registry.ModBlocks;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.research.TtResearch;

public final class TransvectorFeature {
    public static final String INTERFACE = "INTERFACE";
    public static final String INTERFACE_BINDER = "INTERFACE1";
    public static final String DISLOCATOR = "DISLOCATOR";
    public static final String PLATFORM = "PLATFORM";

    public static final TtStackKey<GlobalPos> BINDER_ORIGIN = TtStackKey.register("binder_origin", GlobalPos.CODEC, GlobalPos.STREAM_CODEC);

    public static final DeferredBlock<TransvectorInterfaceBlock> TRANSVECTOR_INTERFACE = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "transvector_interface",
        TransvectorInterfaceBlock::new,
        TransvectorFeature::machine
    ));
    public static final DeferredBlock<TransvectorDislocatorBlock> TRANSVECTOR_DISLOCATOR = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "transvector_dislocator",
        TransvectorDislocatorBlock::new,
        TransvectorFeature::machine
    ));
    public static final DeferredBlock<EtherealPlatformBlock> ETHEREAL_PLATFORM = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "ethereal_platform",
        EtherealPlatformBlock::new,
        properties -> properties
            .mapColor(MapColor.WOOD)
            .strength(2.0F, 5.0F)
            .sound(SoundType.WOOD)
    ));
    public static final DeferredItem<TransvectorBinderItem> TRANSVECTOR_BINDER = ModItems.ITEMS.registerItem(
        "transvector_binder",
        properties -> new TransvectorBinderItem(properties.stacksTo(1))
    );

    public static final Supplier<BlockEntityType<TransvectorInterfaceBlockEntity>> INTERFACE_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "transvector_interface",
        () -> BlockEntityType.Builder.of(TransvectorInterfaceBlockEntity::new,  TRANSVECTOR_INTERFACE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<TransvectorDislocatorBlockEntity>> DISLOCATOR_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "transvector_dislocator",
        () -> BlockEntityType.Builder.of(TransvectorDislocatorBlockEntity::new,  TRANSVECTOR_DISLOCATOR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<CamoBlockEntity>> PLATFORM_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "ethereal_platform",
        () -> BlockEntityType.Builder.of((pos, state) -> new CamoBlockEntity(TransvectorFeature.PLATFORM_ENTITY.get(), pos, state),  ETHEREAL_PLATFORM.get()).build(null)
    );

    private TransvectorFeature() {
    }

    public static void load() {
        TtTransfer.exposeProxy(INTERFACE_ENTITY, TransvectorInterfaceBlockEntity::proxyTarget);
    }

    public static void recipes() {
        TtRecipes.arcane(
            INTERFACE, INTERFACE,
            TcStack.of("thaumictinkerer:transvector_interface"),
            new AspectList().add(Aspect.ORDER, 12).add(Aspect.ENTROPY, 16),
            new String[]{"BRB", "LEL", "BRB"},
            'B', TcIngredient.item("thaumcraft:arcane_stone"),
            'E', TcIngredient.item("minecraft:ender_pearl"),
            'L', TcIngredient.item("minecraft:lapis_lazuli"),
            'R', TcIngredient.item("minecraft:redstone")
        );
        TtRecipes.arcane(
            INTERFACE_BINDER, INTERFACE,
            TcStack.of("thaumictinkerer:transvector_binder"),
            new AspectList().add(Aspect.ORDER, 2),
            new String[]{" I ", " WI", "S  "},
            'I', TcIngredient.item("minecraft:iron_ingot"),
            'W', TcIngredient.item("minecraft:stick"),
            'S', TcIngredient.item("thaumcraft:order_shard")
        );
        TtRecipes.arcane(
            DISLOCATOR, DISLOCATOR,
            TcStack.of("thaumictinkerer:transvector_dislocator"),
            new AspectList().add(Aspect.EARTH, 5).add(Aspect.ENTROPY, 5),
            new String[]{" M ", " I ", " C "},
            'M', TcIngredient.item("thaumcraft:mirrored_glass"),
            'I', TcIngredient.item("thaumictinkerer:transvector_interface"),
            'C', TcIngredient.item("minecraft:comparator")
        );
        TtRecipes.arcane(
            PLATFORM, PLATFORM,
            TcStack.of("thaumictinkerer:ethereal_platform", 2),
            new AspectList().add(Aspect.AIR, 2).add(Aspect.ENTROPY, 4),
            new String[]{" S ", "G G"},
            'G', TcIngredient.item("thaumcraft:greatwood_planks"),
            'S', TcIngredient.item("thaumcraft:silverwood_planks")
        );
    }

    public static void research() {
        TtResearch.item(INTERFACE, new AspectList().add(Aspect.ENTROPY, 4).add(Aspect.ORDER, 4), -4, 2, 1, TcStack.of("thaumictinkerer:transvector_interface"))
            .setParents("DARK_QUARTZ")
            .setPages(
                TtResearch.text(0),
                TtResearch.arcane(INTERFACE),
                TtResearch.text(1),
                TtResearch.arcane(INTERFACE_BINDER),
                TtResearch.text(2)
            )
            .registerResearchItem();
        TtResearch.item(DISLOCATOR, new AspectList().add(Aspect.TRAVEL, 2).add(Aspect.MECHANISM, 1).add(Aspect.ELDRITCH, 1), -6, 1, 3, TcStack.of("thaumictinkerer:transvector_dislocator"))
            .when(() -> thaumictinkerer.platform.TtBaseConfig.allowMirrors())
            .setConcealed()
            .setParents(INTERFACE)
            .setParentsHidden("MIRROR")
            .setPages(
                TtResearch.text(0),
                TtResearch.arcane(DISLOCATOR)
            )
            .setSecondary()
            .registerResearchItem();
        TtResearch.item(PLATFORM, new AspectList().add(Aspect.SENSES, 2).add(Aspect.TREE, 1).add(Aspect.MOTION, 1), -2, 6, 3, TcStack.of("thaumictinkerer:ethereal_platform"))
            .setConcealed()
            .setParents("CLEANSING_TALISMAN")
            .setPages(
                TtResearch.text(0),
                TtResearch.arcane(PLATFORM)
            )
            .setSecondary()
            .registerResearchItem();
    }

    private static BlockBehaviour.Properties machine(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.METAL)
            .strength(3.0F, 10.0F)
            .sound(SoundType.STONE);
    }
}
