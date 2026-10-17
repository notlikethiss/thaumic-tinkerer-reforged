package thaumictinkerer.gear;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.platform.ArmorSlot;
import thaumcraft.platform.Registration;
import thaumcraft.platform.TcArmorMaterial;
import thaumcraft.platform.TcToolMaterial;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.platform.TtStackKey;
import thaumictinkerer.registry.ModCreativeTabs;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.registry.ModRecipes;
import thaumictinkerer.research.TtResearch;

public final class GearFeature {
    public static final String CLEANSING_TALISMAN_RESEARCH = "CLEANSING_TALISMAN";
    public static final String XP_TALISMAN_RESEARCH = "XP_TALISMAN";
    public static final String BLOOD_SWORD_RESEARCH = "BLOOD_SWORD";
    public static final String REVEALING_HELM_RESEARCH = "REVEALING_HELM";
    public static final String SHARE_TOME_RESEARCH = "SHARE_TOME";

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShareTomeRecipe>> SHARE_TOME_SERIALIZER = ModRecipes.SERIALIZERS.register("share_tome", () -> ShareTomeRecipe.SERIALIZER);
    public static final String INFUSED_INKWELL_RESEARCH = "INFUSED_INKWELL";

    public static final TcToolMaterial BLOOD_TOOL = new TcToolMaterial(950, 0.0F, 0.0F, 22, TcMaterials.REPAIRS_NONE, BlockTags.INCORRECT_FOR_WOODEN_TOOL);
    public static final TcArmorMaterial REVEALING_ARMOR = new TcArmorMaterial(
        TcMaterials.THAUMIUM_ARMOR.durabilityMultiplier(),
        TcMaterials.THAUMIUM_ARMOR.helmetDefense(),
        TcMaterials.THAUMIUM_ARMOR.chestplateDefense(),
        TcMaterials.THAUMIUM_ARMOR.leggingsDefense(),
        TcMaterials.THAUMIUM_ARMOR.bootsDefense(),
        TcMaterials.THAUMIUM_ARMOR.enchantability(),
        TcMaterials.THAUMIUM_ARMOR.equipSound(),
        TcMaterials.THAUMIUM_ARMOR.toughness(),
        TcMaterials.THAUMIUM_ARMOR.knockbackResistance(),
        TcMaterials.THAUMIUM_ARMOR.repairItems(),
        ThaumicTinkerer.id("revealing_helm")
    );

    public static final TtStackKey<Boolean> ACTIVE = TtStackKey.register("gear_active", Codec.BOOL, ByteBufCodecs.BOOL);
    public static final TtStackKey<Integer> XP = TtStackKey.register("talisman_xp", Codec.INT, ByteBufCodecs.VAR_INT);
    public static final TtStackKey<ShareData> SHARE = TtStackKey.register("share_data", ShareData.CODEC, ShareData.STREAM_CODEC);

    public static final DeferredItem<CleansingTalismanItem> CLEANSING_TALISMAN = ModItems.ITEMS.registerItem(
        "cleansing_talisman",
        CleansingTalismanItem::new,
        properties -> properties.durability(CleansingTalismanItem.USES).rarity(Rarity.UNCOMMON)
    );
    public static final DeferredItem<XpTalismanItem> XP_TALISMAN = ModItems.ITEMS.registerItem(
        "xp_talisman",
        XpTalismanItem::new,
        properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON)
    );
    public static final DeferredItem<BloodSwordItem> BLOOD_SWORD = ModItems.ITEMS.registerItem(
        "blood_sword",
        BloodSwordItem::new,
        GearFeature::bloodSword
    );
    public static final DeferredItem<RevealingHelmItem> REVEALING_HELM = ModItems.ITEMS.registerItem(
        "revealing_helm",
        RevealingHelmItem::new,
        properties -> Registration.armor(properties.rarity(Rarity.UNCOMMON), REVEALING_ARMOR, ArmorSlot.HELMET).durability(500)
    );
    public static final DeferredItem<ShareTomeItem> SHARE_TOME = ModItems.ITEMS.registerItem(
        "share_tome",
        ShareTomeItem::new,
        properties -> properties.stacksTo(1).rarity(Rarity.EPIC)
    );
    public static final DeferredItem<InfusedInkwellItem> INFUSED_INKWELL = ModItems.ITEMS.registerItem(
        "infused_inkwell",
        InfusedInkwellItem::new,
        properties -> properties.durability(InfusedInkwellItem.USES)
    );

    private GearFeature() {
    }

    public static void load() {
        ModCreativeTabs.hide(INFUSED_INKWELL);
    }

    private static Item.Properties bloodSword(Item.Properties properties) {
        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 10.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.MOVEMENT_SPEED, new AttributeModifier(ThaumicTinkerer.id("blood_sword_speed"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
            .build();
        return Registration.sword(properties, BLOOD_TOOL, 10.0F, -2.4F).attributes(attributes);
    }

    public static void recipes() {
        TtRecipes.infusion(
            CLEANSING_TALISMAN_RESEARCH,
            TcStack.of("thaumictinkerer:cleansing_talisman"),
            5,
            new AspectList().add(Aspect.HEAL, 10).add(Aspect.TOOL, 10).add(Aspect.MAN, 20).add(Aspect.LIFE, 10),
            TcIngredient.item("minecraft:ender_pearl"),
            TcIngredient.item("thaumictinkerer:dark_quartz"),
            TcIngredient.item("thaumictinkerer:dark_quartz"),
            TcIngredient.item("thaumictinkerer:dark_quartz"),
            TcIngredient.item("thaumictinkerer:dark_quartz"),
            TcIngredient.item("minecraft:ghast_tear"),
            TcIngredient.item("thaumcraft:nitor")
        );
        TtRecipes.infusion(
            XP_TALISMAN_RESEARCH,
            TcStack.of("thaumictinkerer:xp_talisman"),
            6,
            new AspectList().add(Aspect.GREED, 20).add(Aspect.EXCHANGE, 10).add(Aspect.BEAST, 10).add(Aspect.MECHANISM, 5),
            TcIngredient.item("minecraft:gold_ingot"),
            TcIngredient.item("minecraft:quartz"),
            TcIngredient.item("minecraft:quartz"),
            TcIngredient.item("thaumcraft:zombie_brain"),
            TcIngredient.item("minecraft:diamond")
        );
        TtRecipes.infusion(
            BLOOD_SWORD_RESEARCH,
            TcStack.of("thaumictinkerer:blood_sword"),
            6,
            new AspectList().add(Aspect.HUNGER, 20).add(Aspect.DARKNESS, 5).add(Aspect.SOUL, 10).add(Aspect.MAN, 6),
            TcIngredient.item("thaumcraft:thaumium_sword"),
            TcIngredient.item("minecraft:rotten_flesh"),
            TcIngredient.item("minecraft:porkchop"),
            TcIngredient.item("minecraft:beef"),
            TcIngredient.item("minecraft:bone"),
            TcIngredient.item("minecraft:diamond"),
            TcIngredient.item("minecraft:ghast_tear")
        );
        TtRecipes.arcane(
            REVEALING_HELM_RESEARCH,
            REVEALING_HELM_RESEARCH,
            TcStack.of("thaumictinkerer:revealing_helm"),
            new AspectList().add(Aspect.EARTH, 5).add(Aspect.FIRE, 5).add(Aspect.WATER, 5).add(Aspect.AIR, 5).add(Aspect.ORDER, 5).add(Aspect.ENTROPY, 5),
            new String[] {"GH"},
            'G', TcIngredient.item("thaumcraft:goggles_of_revealing"),
            'H', TcIngredient.item("thaumcraft:thaumium_helmet")
        );
        TtRecipes.vanilla(SHARE_TOME_RESEARCH, SHARE_TOME_RESEARCH, "share_tome", TcStack.of("thaumictinkerer:share_tome"));
        TtRecipes.vanilla(INFUSED_INKWELL_RESEARCH + "0", INFUSED_INKWELL_RESEARCH, "infused_inkwell_refill", TcStack.of("thaumictinkerer:infused_inkwell"));
        TtRecipes.infusion(
            INFUSED_INKWELL_RESEARCH,
            TcStack.of("thaumictinkerer:infused_inkwell"),
            2,
            new AspectList().add(Aspect.VOID, 8).add(Aspect.DARKNESS, 8),
            TcIngredient.item("thaumcraft:scribing_tools"),
            TcIngredient.item("thaumcraft:air_shard"),
            TcIngredient.item("thaumcraft:warded_jar"),
            TcIngredient.item("thaumcraft:quicksilver")
        );
    }

    public static void research() {
        TtResearch.item(
            CLEANSING_TALISMAN_RESEARCH,
            new AspectList().add(Aspect.HEAL, 2).add(Aspect.ORDER, 1).add(Aspect.POISON, 1),
            -3,
            4,
            3,
            TcStack.of("thaumictinkerer:cleansing_talisman")
        )
            .setSecondary()
            .setParents("DARK_QUARTZ")
            .setPages(
                TtResearch.text(0),
                TtResearch.infusion(CLEANSING_TALISMAN_RESEARCH)
            )
            .registerResearchItem();

        TtResearch.item(
            XP_TALISMAN_RESEARCH,
            new AspectList().add(Aspect.GREED, 1).add(Aspect.MAGIC, 1).add(Aspect.MAN, 1),
            4,
            -1,
            2,
            TcStack.of("thaumictinkerer:xp_talisman")
        )
            .setParents("JARBRAIN", "SPELL_CLOTH")
            .setConcealed()
            .setPages(
                TtResearch.text(0),
                TtResearch.infusion(XP_TALISMAN_RESEARCH)
            )
            .setSecondary()
            .registerResearchItem();

        TtResearch.item(
            BLOOD_SWORD_RESEARCH,
            new AspectList().add(Aspect.HUNGER, 2).add(Aspect.WEAPON, 1).add(Aspect.FLESH, 1).add(Aspect.SOUL, 1),
            -4,
            6,
            3,
            TcStack.of("thaumictinkerer:blood_sword")
        )
            .setWarp(1)
            .setParents("CLEANSING_TALISMAN")
            .setPages(
                TtResearch.text(0),
                TtResearch.infusion(BLOOD_SWORD_RESEARCH),
                TtResearch.text(1)
            )
            .setSecondary()
            .registerResearchItem();

        TtResearch.item(
            REVEALING_HELM_RESEARCH,
            new AspectList().add(Aspect.AURA, 2).add(Aspect.ARMOR, 1),
            0,
            0,
            1,
            TcStack.of("thaumictinkerer:revealing_helm")
        )
            .setParents("GOGGLES")
            .setParentsHidden("THAUMIUM")
            .setPages(
                TtResearch.text(0),
                TtResearch.arcane(REVEALING_HELM_RESEARCH)
            )
            .registerResearchItem();

        TtResearch.item(
            SHARE_TOME_RESEARCH,
            new AspectList(),
            0,
            -1,
            0,
            TcStack.of("thaumictinkerer:share_tome")
        )
            .setStub()
            .setAutoUnlock()
            .setRound()
            .setPages(
                TtResearch.text(0),
                TtResearch.recipe(SHARE_TOME_RESEARCH)
            )
            .registerResearchItem();
    }
}
