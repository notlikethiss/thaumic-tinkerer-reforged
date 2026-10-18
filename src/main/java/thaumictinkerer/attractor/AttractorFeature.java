package thaumictinkerer.attractor;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.platform.Menus;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.platform.TtStackKey;
import thaumictinkerer.registry.ModBlockEntities;
import thaumictinkerer.registry.ModBlocks;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.registry.ModMenus;
import thaumictinkerer.research.TtResearch;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID)
public final class AttractorFeature {
    public static final String MAGNETS = "MAGNETS";
    public static final String MAGNET = "MAGNET";
    public static final String MOB_MAGNET = "MOB_MAGNET";
    public static final String FOCUS_TELEKINESIS = "FOCUS_TELEKINESIS";

    public static final TtStackKey<String> SOUL_PATTERN = TtStackKey.register("soul_pattern", Codec.STRING, ByteBufCodecs.STRING_UTF8);

    public static final DeferredBlock<AttractorBlock> KINETIC_ATTRACTOR = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "kinetic_attractor",
        properties -> new AttractorBlock(properties, false),
        AttractorFeature::attractor
    ));
    public static final DeferredBlock<AttractorBlock> CORPOREAL_ATTRACTOR = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "corporeal_attractor",
        properties -> new AttractorBlock(properties, true),
        AttractorFeature::attractor
    ));
    public static final DeferredItem<SoulMouldItem> SOUL_MOULD = ModItems.ITEMS.registerItem(
        "soul_mould",
        properties -> new SoulMouldItem(properties.stacksTo(1))
    );

    public static final Supplier<BlockEntityType<AttractorBlockEntity>> KINETIC_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "kinetic_attractor",
        () -> BlockEntityType.Builder.of(AttractorBlockEntity::new,  KINETIC_ATTRACTOR.get()).build(null)
    );
    public static final Supplier<BlockEntityType<CorporealAttractorBlockEntity>> CORPOREAL_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "corporeal_attractor",
        () -> BlockEntityType.Builder.of(CorporealAttractorBlockEntity::new,  CORPOREAL_ATTRACTOR.get()).build(null)
    );
    public static final Supplier<MenuType<CorporealAttractorMenu>> CORPOREAL_MENU = ModMenus.MENUS.register(
        "corporeal_attractor",
        () -> Menus.simple(CorporealAttractorMenu::new)
    );

    private AttractorFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        TtRecipes.arcane(
            MAGNET, MAGNETS,
            TcStack.of("thaumictinkerer:kinetic_attractor"),
            new AspectList().add(Aspect.AIR, 20).add(Aspect.ORDER, 5).add(Aspect.EARTH, 15).add(Aspect.ENTROPY, 5),
            new String[]{" I ", "SIs", "WFW"},
            'I', TcIngredient.item("minecraft:iron_ingot"),
            's', TcIngredient.item("thaumcraft:earth_shard"),
            'S', TcIngredient.item("thaumcraft:air_shard"),
            'W', TcIngredient.item("thaumcraft:greatwood_log"),
            'F', TcIngredient.item("thaumictinkerer:focus_telekinesis")
        );
        TtRecipes.arcane(
            MOB_MAGNET, MAGNETS,
            TcStack.of("thaumictinkerer:corporeal_attractor"),
            new AspectList().add(Aspect.AIR, 20).add(Aspect.ORDER, 5).add(Aspect.EARTH, 15).add(Aspect.ENTROPY, 5),
            new String[]{" G ", "SGs", "WFW"},
            'G', TcIngredient.tag("c:ingots/copper"),
            's', TcIngredient.item("thaumcraft:earth_shard"),
            'S', TcIngredient.item("thaumcraft:air_shard"),
            'W', TcIngredient.item("thaumcraft:greatwood_log"),
            'F', TcIngredient.item("thaumictinkerer:focus_telekinesis")
        );
        TtRecipes.crucible(
            MAGNETS,
            TcStack.of("thaumictinkerer:soul_mould"),
            TcIngredient.item("minecraft:ender_pearl"),
            new AspectList().add(Aspect.BEAST, 4).add(Aspect.MIND, 8).add(Aspect.SENSES, 8)
        );
    }

    public static void research() {
        TtResearch.item(MAGNETS, new AspectList().add(Aspect.MECHANISM, 2).add(Aspect.MOTION, 1).add(Aspect.SENSES, 1), -6, 3, 3, TcStack.of("thaumictinkerer:kinetic_attractor"))
            .setParents("INTERFACE")
            .setParentsHidden(FOCUS_TELEKINESIS)
            .setConcealed()
            .setPages(
                TtResearch.text(0),
                TtResearch.text(1),
                TtResearch.arcane(MAGNET),
                TtResearch.arcane(MOB_MAGNET),
                TtResearch.crucible(MAGNETS)
            )
            .registerResearchItem();
    }

    @SubscribeEvent
    static void soulMouldPriority(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof SoulMouldItem mould && event.getTarget() instanceof LivingEntity living) {
            event.setCancellationResult(mould.interactLivingEntity(stack, event.getEntity(), living, event.getHand()));
            event.setCanceled(true);
        }
    }

    private static BlockBehaviour.Properties attractor(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.METAL)
            .strength(1.7F, 1.0F)
            .sound(SoundType.WOOD);
    }
}
