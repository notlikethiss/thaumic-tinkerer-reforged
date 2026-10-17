package thaumictinkerer.awakenedarmor;

import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.platform.ArmorSlot;
import thaumcraft.platform.Registration;
import thaumcraft.platform.TcArmorMaterial;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.TtRarity;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.gas.GasFeature;
import thaumictinkerer.kami.KamiFeature;
import thaumictinkerer.platform.TtDataKey;
import thaumictinkerer.platform.TtStackKey;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.research.TtResearch;

public final class AwakenedArmorFeature {
    public static final String HELM_RESEARCH = "ICHORCLOTH_HELM_GEM";
    public static final String CHEST_RESEARCH = "ICHORCLOTH_CHEST_GEM";
    public static final String LEGS_RESEARCH = "ICHORCLOTH_LEGS_GEM";
    public static final String BOOTS_RESEARCH = "ICHORCLOTH_BOOTS_GEM";

    public static final String POTION_DATA = "tt_potion";
    public static final int EFFECT_DURATION = 202;

    public static final TcArmorMaterial AWAKENED_ARMOR = new TcArmorMaterial(
        1,
        3,
        8,
        6,
        3,
        20,
        TcMaterials.THAUMIUM_ARMOR.equipSound(),
        0.0F,
        0.0F,
        TcMaterials.REPAIRS_NONE,
        ThaumicTinkerer.id("ichorcloth_gem")
    );

    public static final TtStackKey<Boolean> PIECE_DISABLED = TtStackKey.register("awakened_armor_disabled", Codec.BOOL, ByteBufCodecs.BOOL);
    public static final TtDataKey<Boolean> ARMOR_ENABLED = TtDataKey.builder("awakened_armor_enabled", () -> true)
        .codec(Codec.BOOL)
        .copyOnDeath()
        .syncToSelf(ByteBufCodecs.BOOL)
        .build();
    public static final TtDataKey<Boolean> FLIGHT_GRANTED = TtDataKey.builder("awakened_flight_granted", () -> false)
        .codec(Codec.BOOL)
        .build();

    public static final DeferredItem<AwakenedCowlItem> COWL = ModItems.ITEMS.registerItem(
        "cowl_of_the_abyssal_depths",
        properties -> new AwakenedCowlItem(properties, KamiFeature.HELMET_CHESTPLATE_LEGGINGS_DISCOUNT),
        properties -> unbreakable(Registration.armor(properties, AWAKENED_ARMOR, ArmorSlot.HELMET))
    );
    public static final DeferredItem<AwakenedArmorItem> ROBES = ModItems.ITEMS.registerItem(
        "robes_of_the_stratosphere",
        properties -> new AwakenedArmorItem(properties, KamiFeature.HELMET_CHESTPLATE_LEGGINGS_DISCOUNT),
        properties -> unbreakable(Registration.armor(properties, AWAKENED_ARMOR, ArmorSlot.CHESTPLATE))
    );
    public static final DeferredItem<AwakenedArmorItem> LEGGINGS = ModItems.ITEMS.registerItem(
        "leggings_of_the_burning_mantle",
        properties -> new AwakenedArmorItem(properties, KamiFeature.HELMET_CHESTPLATE_LEGGINGS_DISCOUNT),
        properties -> unbreakable(Registration.armor(properties, AWAKENED_ARMOR, ArmorSlot.LEGGINGS))
    );
    public static final DeferredItem<AwakenedArmorItem> BOOTS = ModItems.ITEMS.registerItem(
        "boots_of_the_horizontal_shield",
        properties -> new AwakenedArmorItem(properties, KamiFeature.BOOTS_DISCOUNT),
        properties -> unbreakable(Registration.armor(properties, AWAKENED_ARMOR, ArmorSlot.BOOTS))
    );

    private AwakenedArmorFeature() {
    }

    private static Item.Properties unbreakable(Item.Properties properties) {
        return properties.rarity(TtRarity.kami()).component(DataComponents.UNBREAKABLE, Unit.INSTANCE);
    }

    public static void load() {
        BiConsumer<ItemStack, String> potion = (stack, value) -> BuiltInRegistries.POTION
            .get(Identifier.parse(value))
            .ifPresent(holder -> stack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder)));
        TcStack.registerDataHandler(POTION_DATA, potion);
        GasFeature.registerLightCarrier(AwakenedArmor::carriesLight);
    }

    private static TcIngredient potion(String id) {
        return TcIngredient.of(TcStack.withData("minecraft:potion", 1, Map.of(POTION_DATA, id)));
    }

    public static void recipes() {
        TcIngredient diamond = TcIngredient.item("minecraft:diamond");
        TcIngredient ichor = TcIngredient.item("thaumictinkerer:ichor");
        TcIngredient thaumonomicon = TcIngredient.item("thaumcraft:thaumonomicon");
        TcIngredient primalFocus = TcIngredient.item("thaumcraft:focus_primal");
        TcIngredient ghastTear = TcIngredient.item("minecraft:ghast_tear");

        TtRecipes.infusion(
            HELM_RESEARCH,
            TcStack.of("thaumictinkerer:cowl_of_the_abyssal_depths"),
            13,
            new AspectList().add(Aspect.WATER, 50).add(Aspect.ARMOR, 32).add(Aspect.HUNGER, 32).add(Aspect.AURA, 32).add(Aspect.LIGHT, 64).add(Aspect.FLESH, 16).add(Aspect.MIND, 16),
            TcIngredient.item("thaumictinkerer:ichorcloth_cowl"),
            diamond,
            ichor,
            ichor,
            thaumonomicon,
            primalFocus,
            TcIngredient.item("minecraft:golden_helmet"),
            potion("minecraft:night_vision"),
            TcIngredient.item("thaumcraft:goggles_of_revealing"),
            ghastTear,
            TcIngredient.item("minecraft:cod"),
            TcIngredient.item("minecraft:cake"),
            TcIngredient.item("minecraft:ender_eye")
        );
        TtRecipes.infusion(
            CHEST_RESEARCH,
            TcStack.of("thaumictinkerer:robes_of_the_stratosphere"),
            13,
            new AspectList().add(Aspect.AIR, 50).add(Aspect.ARMOR, 32).add(Aspect.FLIGHT, 32).add(Aspect.ORDER, 32).add(Aspect.LIGHT, 64).add(Aspect.ELDRITCH, 16).add(Aspect.SENSES, 16),
            TcIngredient.item("thaumictinkerer:ichorcloth_robe"),
            diamond,
            ichor,
            ichor,
            primalFocus,
            thaumonomicon,
            TcIngredient.item("minecraft:golden_chestplate"),
            TcIngredient.item("thaumictinkerer:focus_flight"),
            TcIngredient.item("thaumcraft:hover_harness"),
            TcIngredient.item("thaumictinkerer:focus_deflect"),
            TcIngredient.item("minecraft:feather"),
            ghastTear,
            TcIngredient.item("minecraft:arrow")
        );
        TtRecipes.infusion(
            LEGS_RESEARCH,
            TcStack.of("thaumictinkerer:leggings_of_the_burning_mantle"),
            13,
            new AspectList().add(Aspect.FIRE, 50).add(Aspect.ARMOR, 32).add(Aspect.HEAL, 32).add(Aspect.ENERGY, 32).add(Aspect.LIGHT, 64).add(Aspect.GREED, 16).add(Aspect.ELDRITCH, 16),
            TcIngredient.item("thaumictinkerer:ichorcloth_leggings"),
            diamond,
            ichor,
            ichor,
            primalFocus,
            thaumonomicon,
            TcIngredient.item("minecraft:golden_chestplate"),
            potion("minecraft:fire_resistance"),
            TcIngredient.item("thaumictinkerer:focus_smelt"),
            TcIngredient.item("thaumictinkerer:bright_nitor"),
            TcIngredient.item("minecraft:lava_bucket"),
            TcIngredient.item("minecraft:fire_charge"),
            TcIngredient.item("minecraft:blaze_rod")
        );
        TtRecipes.infusion(
            BOOTS_RESEARCH,
            TcStack.of("thaumictinkerer:boots_of_the_horizontal_shield"),
            13,
            new AspectList().add(Aspect.EARTH, 50).add(Aspect.ARMOR, 32).add(Aspect.MINE, 32).add(Aspect.MOTION, 32).add(Aspect.LIGHT, 64).add(Aspect.PLANT, 16).add(Aspect.TRAVEL, 16),
            TcIngredient.item("thaumictinkerer:ichorcloth_boots"),
            diamond,
            ichor,
            ichor,
            thaumonomicon,
            primalFocus,
            TcIngredient.item("minecraft:golden_boots"),
            TcIngredient.item("minecraft:grass_block"),
            TcIngredient.item("thaumcraft:arcane_bore"),
            TcIngredient.item("thaumcraft:lamp_of_growth"),
            TcIngredient.item("minecraft:wheat_seeds"),
            TcIngredient.item("minecraft:white_wool"),
            TcIngredient.item("minecraft:lead")
        );
    }

    public static void research() {
        TtResearch.kami(
            HELM_RESEARCH,
            new AspectList().add(Aspect.WATER, 2).add(Aspect.HEAL, 1).add(Aspect.HUNGER, 1).add(Aspect.AURA, 1),
            18,
            3,
            5,
            TcStack.of("thaumictinkerer:cowl_of_the_abyssal_depths")
        )
            .setParents(KamiFeature.ICHORCLOTH_ARMOR_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(HELM_RESEARCH))
            .registerResearchItem();

        TtResearch.kami(
            CHEST_RESEARCH,
            new AspectList().add(Aspect.AIR, 2).add(Aspect.MOTION, 1).add(Aspect.FLIGHT, 1).add(Aspect.ELDRITCH, 1),
            17,
            7,
            5,
            TcStack.of("thaumictinkerer:robes_of_the_stratosphere")
        )
            .setParents(KamiFeature.ICHORCLOTH_ARMOR_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(CHEST_RESEARCH))
            .registerResearchItem();

        TtResearch.kami(
            LEGS_RESEARCH,
            new AspectList().add(Aspect.FIRE, 2).add(Aspect.HEAL, 1).add(Aspect.GREED, 1).add(Aspect.ENERGY, 1),
            17,
            9,
            5,
            TcStack.of("thaumictinkerer:leggings_of_the_burning_mantle")
        )
            .setParents(KamiFeature.ICHORCLOTH_ARMOR_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(LEGS_RESEARCH), TtResearch.text(1))
            .registerResearchItem();

        TtResearch.kami(
            BOOTS_RESEARCH,
            new AspectList().add(Aspect.EARTH, 2).add(Aspect.TRAVEL, 1).add(Aspect.MINE, 1).add(Aspect.PLANT, 1),
            15,
            10,
            5,
            TcStack.of("thaumictinkerer:boots_of_the_horizontal_shield")
        )
            .setParents(KamiFeature.ICHORCLOTH_ARMOR_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.infusion(BOOTS_RESEARCH))
            .registerResearchItem();
    }
}
