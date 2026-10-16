package thaumictinkerer.gas;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.registry.ModBlocks;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.research.TtResearch;

public final class GasFeature {
    public static final String GASEOUS_LIGHT_RESEARCH = "GASEOUS_LIGHT";
    public static final String GASEOUS_SHADOW_RESEARCH = "GASEOUS_SHADOW";
    public static final String GAS_REMOVER_RESEARCH = "GAS_REMOVER";
    public static final String BRIGHT_NITOR_RESEARCH = "BRIGHT_NITOR";

    private static final List<Predicate<Player>> LIGHT_CARRIERS = new ArrayList<>();

    public static final DeferredBlock<GaseousLightBlock> GASEOUS_LIGHT = ModBlocks.BLOCKS.registerBlock(
        "gaseous_light",
        GaseousLightBlock::new,
        properties -> GasBlock.gas(properties).lightLevel(state -> GaseousLightBlock.LIGHT)
    );
    public static final DeferredBlock<GaseousShadowBlock> GASEOUS_SHADOW = ModBlocks.BLOCKS.registerBlock(
        "gaseous_shadow",
        GaseousShadowBlock::new,
        GasBlock::gas
    );
    public static final DeferredBlock<NitorGasBlock> NITOR_GAS = ModBlocks.BLOCKS.registerBlock(
        "nitor_gas",
        NitorGasBlock::new,
        properties -> GasBlock.gas(properties).lightLevel(NitorGasBlock::lightLevel)
    );

    public static final DeferredItem<GasBottleItem> GASEOUS_ILLUMINAE = ModItems.ITEMS.registerItem(
        "gaseous_illuminae",
        properties -> new GasBottleItem(GASEOUS_LIGHT, properties)
    );
    public static final DeferredItem<GasBottleItem> GASEOUS_TENEBRAE = ModItems.ITEMS.registerItem(
        "gaseous_tenebrae",
        properties -> new GasBottleItem(GASEOUS_SHADOW, properties)
    );
    public static final DeferredItem<FumeDissipatorItem> FUME_DISSIPATOR = ModItems.ITEMS.registerItem(
        "fume_dissipator",
        FumeDissipatorItem::new,
        properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON)
    );
    public static final DeferredItem<BrightNitorItem> BRIGHT_NITOR = ModItems.ITEMS.registerItem(
        "bright_nitor",
        BrightNitorItem::new,
        properties -> properties.stacksTo(1)
    );

    private GasFeature() {
    }

    public static void load() {
    }

    public static void registerLightCarrier(Predicate<Player> carrier) {
        LIGHT_CARRIERS.add(carrier);
    }

    public static boolean carriesLight(Player player) {
        Item brightNitor = BRIGHT_NITOR.get();
        if (player.getInventory().contains(stack -> !stack.isEmpty() && stack.is(brightNitor))) {
            return true;
        }
        for (Predicate<Player> carrier : LIGHT_CARRIERS) {
            if (carrier.test(player)) {
                return true;
            }
        }
        return false;
    }

    public static void recipes() {
        TtRecipes.crucible(
            GASEOUS_LIGHT_RESEARCH,
            TcStack.of("thaumictinkerer:gaseous_illuminae"),
            TcIngredient.item("thaumcraft:glass_phial"),
            new AspectList().add(Aspect.LIGHT, 16).add(Aspect.AIR, 10).add(Aspect.MOTION, 8)
        );
        TtRecipes.crucible(
            GASEOUS_SHADOW_RESEARCH,
            TcStack.of("thaumictinkerer:gaseous_tenebrae"),
            TcIngredient.item("thaumcraft:glass_phial"),
            new AspectList().add(Aspect.DARKNESS, 16).add(Aspect.AIR, 10).add(Aspect.MOTION, 8)
        );
        TtRecipes.crucible(
            BRIGHT_NITOR_RESEARCH,
            TcStack.of("thaumictinkerer:bright_nitor"),
            TcIngredient.item("thaumcraft:nitor"),
            new AspectList().add(Aspect.ENERGY, 25).add(Aspect.LIGHT, 25).add(Aspect.AIR, 10).add(Aspect.FIRE, 10)
        );
        TtRecipes.arcane(
            GAS_REMOVER_RESEARCH,
            GAS_REMOVER_RESEARCH,
            TcStack.of("thaumictinkerer:fume_dissipator"),
            new AspectList().add(Aspect.AIR, 2).add(Aspect.ORDER, 2),
            new String[]{"DDD", "T G", "QQQ"},
            'D', TcIngredient.item("thaumictinkerer:dark_quartz"),
            'T', TcIngredient.item("thaumictinkerer:gaseous_tenebrae"),
            'G', TcIngredient.item("thaumictinkerer:gaseous_illuminae"),
            'Q', TcIngredient.item("minecraft:quartz")
        );
    }

    public static void research() {
        TtResearch.item(
            GASEOUS_LIGHT_RESEARCH,
            new AspectList().add(Aspect.LIGHT, 2).add(Aspect.AIR, 1),
            0,
            -3,
            1,
            TcStack.of("thaumictinkerer:gaseous_illuminae")
        )
            .setParents("NITOR")
            .setPages(TtResearch.text(0), TtResearch.crucible(GASEOUS_LIGHT_RESEARCH))
            .registerResearchItem();
        TtResearch.item(
            GASEOUS_SHADOW_RESEARCH,
            new AspectList().add(Aspect.DARKNESS, 2).add(Aspect.AIR, 1).add(Aspect.MOTION, 4),
            -1,
            -5,
            2,
            TcStack.of("thaumictinkerer:gaseous_tenebrae")
        )
            .setSecondary()
            .setParents(GASEOUS_LIGHT_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.crucible(GASEOUS_SHADOW_RESEARCH))
            .registerResearchItem();
        TtResearch.item(
            GAS_REMOVER_RESEARCH,
            new AspectList().add(Aspect.DARKNESS, 2).add(Aspect.LIGHT, 2),
            -2,
            -7,
            0,
            TcStack.of("thaumictinkerer:fume_dissipator")
        )
            .setRound()
            .setPages(TtResearch.text(0), TtResearch.arcane(GAS_REMOVER_RESEARCH))
            .setParents(GASEOUS_SHADOW_RESEARCH)
            .registerResearchItem();
        TtResearch.item(
            BRIGHT_NITOR_RESEARCH,
            new AspectList().add(Aspect.LIGHT, 2).add(Aspect.FIRE, 1).add(Aspect.ENERGY, 1).add(Aspect.AIR, 1),
            1,
            -5,
            2,
            TcStack.of("thaumictinkerer:bright_nitor")
        )
            .setParents(GASEOUS_LIGHT_RESEARCH)
            .setConcealed()
            .setPages(TtResearch.text(0), TtResearch.crucible(BRIGHT_NITOR_RESEARCH))
            .setSecondary()
            .registerResearchItem();
    }
}
