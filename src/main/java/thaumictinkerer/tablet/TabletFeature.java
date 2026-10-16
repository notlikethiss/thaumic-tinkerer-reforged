package thaumictinkerer.tablet;

import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.platform.Menus;
import thaumcraft.platform.Transfer;
import thaumictinkerer.attractor.AttractorFeature;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.registry.ModBlockEntities;
import thaumictinkerer.registry.ModBlocks;
import thaumictinkerer.registry.ModMenus;
import thaumictinkerer.research.TtResearch;

public final class TabletFeature {
    public static final String ANIMATION_TABLET = "ANIMATION_TABLET";

    public static final DeferredBlock<DynamismTabletBlock> DYNAMISM_TABLET = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "dynamism_tablet",
        DynamismTabletBlock::new,
        properties -> properties
            .mapColor(MapColor.METAL)
            .strength(3.0F, 50.0F)
            .sound(SoundType.METAL)
    ));

    public static final Supplier<BlockEntityType<DynamismTabletBlockEntity>> TABLET_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "dynamism_tablet",
        () -> new BlockEntityType<>(DynamismTabletBlockEntity::new, DYNAMISM_TABLET.get())
    );
    public static final Supplier<MenuType<DynamismTabletMenu>> TABLET_MENU = ModMenus.MENUS.register(
        "dynamism_tablet",
        () -> Menus.simple(DynamismTabletMenu::new)
    );

    private TabletFeature() {
    }

    public static void load() {
        Transfer.exposeItems(TABLET_ENTITY, (entity, side) -> entity);
    }

    public static void recipes() {
        TtRecipes.arcane(
            ANIMATION_TABLET, ANIMATION_TABLET,
            TcStack.of("thaumictinkerer:dynamism_tablet"),
            new AspectList().add(Aspect.AIR, 25).add(Aspect.ORDER, 15).add(Aspect.FIRE, 10),
            new String[]{"GIG", "ICI"},
            'G', TcIngredient.item("minecraft:gold_ingot"),
            'I', TcIngredient.item("minecraft:iron_ingot"),
            'C', TcIngredient.item("thaumcraft:golem_core_blank")
        );
    }

    public static void research() {
        TtResearch.item(ANIMATION_TABLET, new AspectList().add(Aspect.MECHANISM, 2).add(Aspect.METAL, 1).add(Aspect.MOTION, 1).add(Aspect.ENERGY, 1), -8, 2, 4, TcStack.of("thaumictinkerer:dynamism_tablet"))
            .setWarp(1)
            .setParents(AttractorFeature.MAGNETS)
            .setPages(
                TtResearch.text(0),
                TtResearch.arcane(ANIMATION_TABLET)
            )
            .registerResearchItem();
    }
}
