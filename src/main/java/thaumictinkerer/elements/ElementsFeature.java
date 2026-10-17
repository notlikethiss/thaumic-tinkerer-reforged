package thaumictinkerer.elements;

import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumictinkerer.Config;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.platform.TtConsumables;
import thaumictinkerer.registry.ModBlocks;
import thaumictinkerer.registry.ModEffects;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.research.TtResearch;
import thaumictinkerer.research.TtResearchItem;

public final class ElementsFeature {
    public static final String FIRE_AER_RESEARCH = "FIRE_AER";
    public static final String FIRE_PERDITIO_RESEARCH = "FIRE_PERDITIO";
    public static final String FIRE_TERRA_RESEARCH = "FIRE_TERRA";
    public static final String FIRE_IGNIS_RESEARCH = "FIRE_IGNIS";
    public static final String FIRE_ORDO_RESEARCH = "FIRE_ORDO";
    public static final String FIRE_AQUA_RESEARCH = "FIRE_AQUA";
    public static final String POTIONS_RESEARCH = "INFUSED_POTIONS";
    public static final String BRIGHT_NITOR_RESEARCH = "BRIGHT_NITOR";
    public static final int POTION_DURATION = 3600;

    public static final OrderFireTransformation ORDER_TRANSFORMATION = new OrderFireTransformation();

    public static final DeferredHolder<MobEffect, ImbuedEffect> FIRE_IMBUED = ModEffects.EFFECTS.register("fire_imbued", ImbuedEffect::new);
    public static final DeferredHolder<MobEffect, ImbuedEffect> WATER_IMBUED = ModEffects.EFFECTS.register("water_imbued", ImbuedEffect::new);
    public static final DeferredHolder<MobEffect, ImbuedEffect> EARTH_IMBUED = ModEffects.EFFECTS.register("earth_imbued", ImbuedEffect::new);
    public static final DeferredHolder<MobEffect, ImbuedEffect> AIR_IMBUED = ModEffects.EFFECTS.register("air_imbued", ImbuedEffect::new);

    public static final DeferredBlock<ImbuedFireBlock> AER_FIRE = fire("aer_imbued_fire", 200, new MapFireTransformation(FireMaps::air));
    public static final DeferredBlock<ImbuedFireBlock> PERDITIO_FIRE = fire("perditio_imbued_fire", 1, new MapFireTransformation(FireMaps::chaos));
    public static final DeferredBlock<ImbuedFireBlock> TERRA_FIRE = fire("terra_imbued_fire", 200, new MapFireTransformation(FireMaps::earth));
    public static final DeferredBlock<ImbuedFireBlock> IGNIS_FIRE = fire("ignis_imbued_fire", 200, new MapFireTransformation(FireMaps::fire));
    public static final DeferredBlock<ImbuedFireBlock> ORDO_FIRE = fire("ordo_imbued_fire", 200, ORDER_TRANSFORMATION);
    public static final DeferredBlock<ImbuedFireBlock> AQUA_FIRE = fire("aqua_imbued_fire", 200, new MapFireTransformation(FireMaps::water));

    public static final DeferredItem<ImbuedFireItem> AER_FIRE_ITEM = fireItem(AER_FIRE);
    public static final DeferredItem<ImbuedFireItem> PERDITIO_FIRE_ITEM = fireItem(PERDITIO_FIRE);
    public static final DeferredItem<ImbuedFireItem> TERRA_FIRE_ITEM = fireItem(TERRA_FIRE);
    public static final DeferredItem<ImbuedFireItem> IGNIS_FIRE_ITEM = fireItem(IGNIS_FIRE);
    public static final DeferredItem<ImbuedFireItem> ORDO_FIRE_ITEM = fireItem(ORDO_FIRE);
    public static final DeferredItem<ImbuedFireItem> AQUA_FIRE_ITEM = fireItem(AQUA_FIRE);

    public static final DeferredBlock<ForcefieldBlock> FORCEFIELD = ModBlocks.BLOCKS.registerBlock(
        "forcefield",
        ForcefieldBlock::new,
        properties -> properties
            .mapColor(MapColor.NONE)
            .noOcclusion()
            .replaceable()
            .instabreak()
            .noLootTable()
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos, box) -> false)
    );

    public static final DeferredItem<InfusedPotionItem> AER_POTION = potion("aer_infused_potion", Aspect.AIR, AIR_IMBUED);
    public static final DeferredItem<InfusedPotionItem> IGNIS_POTION = potion("ignis_infused_potion", Aspect.FIRE, FIRE_IMBUED);
    public static final DeferredItem<InfusedPotionItem> TERRA_POTION = potion("terra_infused_potion", Aspect.EARTH, EARTH_IMBUED);
    public static final DeferredItem<InfusedPotionItem> AQUA_POTION = potion("aqua_infused_potion", Aspect.WATER, WATER_IMBUED);

    private ElementsFeature() {
    }

    private static UnaryOperator<BlockBehaviour.Properties> fireProperties() {
        return properties -> properties
            .mapColor(MapColor.FIRE)
            .replaceable()
            .noCollision()
            .instabreak()
            .lightLevel(state -> 15)
            .sound(SoundType.WOOL)
            .pushReaction(PushReaction.POPPED)
            .noLootTable();
    }

    private static DeferredBlock<ImbuedFireBlock> fire(String name, int tickDelay, FireTransformation transformation) {
        return ModBlocks.BLOCKS.registerBlock(name, properties -> new ImbuedFireBlock(properties, tickDelay, transformation), fireProperties());
    }

    private static DeferredItem<ImbuedFireItem> fireItem(DeferredBlock<ImbuedFireBlock> block) {
        return ModItems.ITEMS.registerItem(block.getId().getPath(), properties -> new ImbuedFireItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    private static DeferredItem<InfusedPotionItem> potion(String name, Aspect aspect, DeferredHolder<MobEffect, ImbuedEffect> effect) {
        return ModItems.ITEMS.registerItem(
            name,
            properties -> new InfusedPotionItem(aspect, properties),
            properties -> TtConsumables.drinkable(properties, aspect.getColor(), List.of(new MobEffectInstance(effect, POTION_DURATION)))
        );
    }

    public static void load() {
    }

    public static void recipes() {
        fireRecipe(FIRE_AER_RESEARCH, AER_FIRE_ITEM, "air_shard", new AspectList().add(Aspect.FIRE, 5).add(Aspect.MAGIC, 5).add(Aspect.AIR, 5));
        fireRecipe(FIRE_PERDITIO_RESEARCH, PERDITIO_FIRE_ITEM, "entropy_shard", new AspectList().add(Aspect.FIRE, 5).add(Aspect.MAGIC, 5).add(Aspect.ENTROPY, 5));
        fireRecipe(FIRE_TERRA_RESEARCH, TERRA_FIRE_ITEM, "earth_shard", new AspectList().add(Aspect.FIRE, 5).add(Aspect.MAGIC, 5).add(Aspect.EARTH, 5));
        fireRecipe(FIRE_IGNIS_RESEARCH, IGNIS_FIRE_ITEM, "fire_shard", new AspectList().add(Aspect.FIRE, 10).add(Aspect.AIR, 5));
        fireRecipe(FIRE_ORDO_RESEARCH, ORDO_FIRE_ITEM, "order_shard", new AspectList().add(Aspect.FIRE, 5).add(Aspect.MAGIC, 5).add(Aspect.ORDER, 5));
        fireRecipe(FIRE_AQUA_RESEARCH, AQUA_FIRE_ITEM, "water_shard", new AspectList().add(Aspect.FIRE, 5).add(Aspect.MAGIC, 5).add(Aspect.WATER, 5));
        potionRecipe(0, "aer", Aspect.AIR);
        potionRecipe(1, "ignis", Aspect.FIRE);
        potionRecipe(2, "terra", Aspect.EARTH);
        potionRecipe(3, "aqua", Aspect.WATER);
    }

    private static void fireRecipe(String key, DeferredItem<ImbuedFireItem> result, String shard, AspectList aspects) {
        TtRecipes.crucible(key, TcStack.of("thaumictinkerer:" + result.getId().getPath()), TcIngredient.item("thaumcraft:" + shard), aspects);
    }

    private static void potionRecipe(int index, String element, Aspect aspect) {
        TtRecipes.crucible(
            POTIONS_RESEARCH + "POT" + index,
            TcStack.of("thaumictinkerer:" + element + "_infused_potion"),
            TcIngredient.item("thaumictinkerer:" + element + "_infused_grain"),
            new AspectList().add(Aspect.AURA, 5).add(aspect, 5)
        );
    }

    public static void research() {
        fireResearch(FIRE_AER_RESEARCH, AER_FIRE_ITEM, new AspectList().add(Aspect.FIRE, 5).add(Aspect.AIR, 5), 3, -7, 2);
        fireResearch(FIRE_PERDITIO_RESEARCH, PERDITIO_FIRE_ITEM, new AspectList().add(Aspect.FIRE, 5).add(Aspect.ENTROPY, 5), 2, -8, 3);
        fireResearch(FIRE_TERRA_RESEARCH, TERRA_FIRE_ITEM, new AspectList().add(Aspect.FIRE, 5).add(Aspect.EARTH, 5), 4, -6, 2);
        fireResearch(FIRE_IGNIS_RESEARCH, IGNIS_FIRE_ITEM, new AspectList().add(Aspect.FIRE, 10), 4, -4, 2);
        fireResearch(FIRE_ORDO_RESEARCH, ORDO_FIRE_ITEM, new AspectList().add(Aspect.FIRE, 5).add(Aspect.ORDER, 5), 3, -3, 2);
        fireResearch(FIRE_AQUA_RESEARCH, AQUA_FIRE_ITEM, new AspectList().add(Aspect.FIRE, 5).add(Aspect.WATER, 5), 2, -2, 2);
        new PotionResearchItem(
            POTIONS_RESEARCH,
            new AspectList().add(Aspect.WATER, 5).add(Aspect.ENTROPY, 5),
            7,
            -5,
            2,
            TcStack.of("thaumictinkerer:infused_seeds")
        )
            .setParents(FIRE_PERDITIO_RESEARCH, FIRE_ORDO_RESEARCH, FIRE_IGNIS_RESEARCH, FIRE_TERRA_RESEARCH, FIRE_AER_RESEARCH, FIRE_AQUA_RESEARCH)
            .setParentsHidden(TtResearchItem.INFUSION)
            .setConcealed()
            .setPages(
                TtResearch.text(0),
                TtResearch.text(1),
                TtResearch.text(2),
                TtResearch.text(3),
                TtResearch.text(4),
                TtResearch.text(5),
                TtResearch.infusion(POTIONS_RESEARCH, 4),
                TtResearch.crucible(POTIONS_RESEARCH + "POT0"),
                TtResearch.crucible(POTIONS_RESEARCH + "POT1"),
                TtResearch.crucible(POTIONS_RESEARCH + "POT2"),
                TtResearch.crucible(POTIONS_RESEARCH + "POT3")
            )
            .registerResearchItem();
    }

    private static void fireResearch(String key, DeferredItem<ImbuedFireItem> icon, AspectList aspects, int column, int row, int complexity) {
        TtResearch.item(key, aspects, column, row, complexity, TcStack.of("thaumictinkerer:" + icon.getId().getPath()))
            .when(Config::imbuedFire)
            .setParents(BRIGHT_NITOR_RESEARCH)
            .setConcealed()
            .setPages(TtResearch.text(0), TtResearch.crucible(key))
            .setSecondary()
            .registerResearchItem();
    }
}
