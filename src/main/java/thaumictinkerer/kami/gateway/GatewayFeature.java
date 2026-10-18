package thaumictinkerer.kami.gateway;

import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.platform.Menus;
import thaumictinkerer.TtRarity;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.platform.TtStackKey;
import thaumictinkerer.registry.ModBlockEntities;
import thaumictinkerer.registry.ModBlocks;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.registry.ModMenus;
import thaumictinkerer.research.TtResearch;

public final class GatewayFeature {
    public static final String WARP_GATE_RESEARCH = "WARP_GATE";
    public static final String SKY_PEARL_RECIPE = "SKY_PEARL";
    public static final String CHEST_GEM_RESEARCH = "ICHORCLOTH_CHEST_GEM";
    public static final String BOOTS_GEM_RESEARCH = "ICHORCLOTH_BOOTS_GEM";

    public static final TtStackKey<PearlLocation> LOCATION = TtStackKey.register("pearl_location", PearlLocation.CODEC, PearlLocation.STREAM_CODEC);

    public static final DeferredBlock<WarpGateBlock> WARP_GATE = ModBlocks.withItem(
        ModBlocks.BLOCKS.registerBlock(
            "warp_gate",
            WarpGateBlock::new,
            properties -> properties
                .mapColor(MapColor.STONE)
                .strength(5.0F, 2000.0F)
                .sound(SoundType.STONE)
        ),
        properties -> properties.rarity(TtRarity.kami())
    );

    public static final DeferredItem<SkyPearlItem> SKY_PEARL = ModItems.ITEMS.registerItem(
        "sky_pearl",
        SkyPearlItem::new,
        properties -> properties.stacksTo(1).rarity(TtRarity.kami())
    );

    public static final Supplier<BlockEntityType<WarpGateBlockEntity>> WARP_GATE_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "warp_gate",
        () -> BlockEntityType.Builder.of(WarpGateBlockEntity::new,  WARP_GATE.get()).build(null)
    );

    public static final Supplier<MenuType<WarpGateMenu>> WARP_GATE_MENU = ModMenus.MENUS.register(
        "warp_gate",
        () -> Menus.simple(WarpGateMenu::new)
    );

    private GatewayFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        TtRecipes.infusion(
            SKY_PEARL_RECIPE,
            WARP_GATE_RESEARCH,
            TcStack.of("thaumictinkerer:sky_pearl", 2),
            6,
            new AspectList().add(Aspect.TRAVEL, 32).add(Aspect.ELDRITCH, 32).add(Aspect.FLIGHT, 32).add(Aspect.AIR, 16),
            TcIngredient.item("minecraft:ender_pearl"),
            TcIngredient.item("thaumictinkerer:ichor"),
            TcIngredient.item("thaumictinkerer:ender_shard"),
            TcIngredient.item("minecraft:lapis_block"),
            TcIngredient.item("minecraft:diamond")
        );
        TtRecipes.infusion(
            WARP_GATE_RESEARCH,
            TcStack.of("thaumictinkerer:warp_gate"),
            8,
            new AspectList().add(Aspect.TRAVEL, 64).add(Aspect.ELDRITCH, 50).add(Aspect.FLIGHT, 50),
            TcIngredient.item("thaumcraft:travel_paving_stone"),
            TcIngredient.item("thaumictinkerer:ichor"),
            TcIngredient.item("thaumictinkerer:ender_shard"),
            TcIngredient.item("thaumictinkerer:transvector_dislocator"),
            TcIngredient.item("thaumictinkerer:nether_shard"),
            TcIngredient.item("minecraft:diamond"),
            TcIngredient.item("minecraft:feather")
        );
    }

    public static void research() {
        TtResearch.kami(
            WARP_GATE_RESEARCH,
            new AspectList().add(Aspect.TRAVEL, 2).add(Aspect.ELDRITCH, 1).add(Aspect.FLIGHT, 1).add(Aspect.MECHANISM, 1),
            19,
            6,
            5,
            TcStack.of("thaumictinkerer:warp_gate")
        )
            .when(thaumictinkerer.platform.TtBaseConfig::allowMirrors)
            .setParents(CHEST_GEM_RESEARCH)
            .setParentsHidden(BOOTS_GEM_RESEARCH)
            .setPages(
                TtResearch.text(0),
                TtResearch.infusion(WARP_GATE_RESEARCH),
                TtResearch.text(1),
                TtResearch.infusion(SKY_PEARL_RECIPE)
            )
            .registerResearchItem();
    }
}
