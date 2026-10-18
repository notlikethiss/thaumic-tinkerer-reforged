package thaumictinkerer.enchanting;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.platform.Menus;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.platform.TtDataKey;
import thaumictinkerer.registry.ModBlockEntities;
import thaumictinkerer.registry.ModBlocks;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.registry.ModMenus;
import thaumictinkerer.registry.ModRecipes;
import thaumictinkerer.research.TtResearch;

public final class EnchantingFeature {
    public static final String SPELL_CLOTH_RESEARCH = "SPELL_CLOTH";
    public static final String ENCHANTER_RESEARCH = "ENCHANTER";
    public static final String ENCHANT_ASCENT_BOOST = "TTENCH_ASCENT_BOOST";
    public static final String ENCHANT_SLOW_FALL = "TTENCH_SLOW_FALL";
    public static final String ENCHANT_AUTO_SMELT = "TTENCH_AUTO_SMELT";
    public static final String ENCHANT_DESINTEGRATE = "TTENCH_DESINTEGRATE";
    public static final String ENCHANT_QUICK_DRAW = "TTENCH_QUICK_DRAW";
    public static final String ENCHANT_VAMPIRISM = "TTENCH_VAMPIRISM";
    public static final String ENCHANT_DISPERSED_STRIKE = "TTENCH_DISPERSED";
    public static final String ENCHANT_FINAL_STRIKE = "TTENCH_FINAL";
    public static final String ENCHANT_FOCUSED_STRIKE = "TTENCH_FOCUSED";
    public static final String ENCHANT_POUNCE = "TTENCH_POUNCE";
    public static final String ENCHANT_TUNNEL = "TTENCH_TUNNEL";
    public static final String ENCHANT_SHOCKWAVE = "TTENCH_SHOCKWAVE";
    public static final String ENCHANT_SHATTER = "TTENCH_SHATTER";
    public static final String ENCHANT_VALIANCE = "TTENCH_VALIANCE";

    public static final TagKey<Block> FLAMING_TOUCH_BLOCKS = TagKey.create(Registries.BLOCK, ThaumicTinkerer.id("flaming_touch_breakable"));

    public static final TtDataKey<Float> TUNNEL_DIRECTION = TtDataKey.builder("tunnel_direction", () -> Float.NaN)
        .codec(Codec.FLOAT)
        .syncToSelf(ByteBufCodecs.FLOAT)
        .build();
    public static final TtDataKey<TtEnchantmentEvents.StrikeState> STRIKES = TtDataKey.builder("strike_state", () -> TtEnchantmentEvents.StrikeState.NONE).build();

    public static final DeferredItem<SpellClothItem> SPELL_CLOTH = ModItems.ITEMS.registerItem(
        "spellbinding_cloth",
        SpellClothItem::new,
        properties -> properties.durability(SpellClothItem.USES).rarity(Rarity.UNCOMMON)
    );
    public static final DeferredBlock<EnchanterBlock> ENCHANTER = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "osmotic_enchanter",
        EnchanterBlock::new,
        properties -> properties
            .mapColor(MapColor.STONE)
            .strength(5.0F, 2000.0F)
            .requiresCorrectToolForDrops()
            .sound(SoundType.STONE)
            .noOcclusion()
    ));
    public static final Supplier<BlockEntityType<EnchanterBlockEntity>> ENCHANTER_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "osmotic_enchanter",
        () -> BlockEntityType.Builder.of(EnchanterBlockEntity::new,  ENCHANTER.get()).build(null)
    );
    public static final DeferredHolder<MenuType<?>, MenuType<EnchanterMenu>> ENCHANTER_MENU = ModMenus.MENUS.register(
        "osmotic_enchanter",
        () -> Menus.withPos(EnchanterMenu::new)
    );
    public static final Supplier<net.minecraft.world.item.crafting.RecipeSerializer<SpellClothRecipe>> SPELL_CLOTH_SERIALIZER = ModRecipes.SERIALIZERS.register(
        "spell_cloth",
        () -> SpellClothRecipe.SERIALIZER
    );

    private EnchantingFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        TtRecipes.infusion(
            ENCHANTER_RESEARCH, TcStack.of("thaumictinkerer:osmotic_enchanter"), 15,
            new AspectList().add(Aspect.MAGIC, 50).add(Aspect.ENERGY, 20).add(Aspect.ELDRITCH, 20).add(Aspect.VOID, 20).add(Aspect.MIND, 10),
            TcIngredient.item("minecraft:enchanting_table"),
            TcIngredient.item("thaumcraft:obsidian_tile"), TcIngredient.item("thaumcraft:obsidian_tile"), TcIngredient.item("thaumcraft:obsidian_tile"),
            TcIngredient.item("thaumcraft:obsidian_tile"), TcIngredient.item("thaumcraft:obsidian_tile"),
            TcIngredient.item("thaumcraft:thaumium_ingot"), TcIngredient.item("thaumcraft:thaumium_ingot"),
            TcIngredient.item("thaumictinkerer:spellbinding_cloth")
        );
        TtRecipes.crucible(
            SPELL_CLOTH_RESEARCH, TcStack.of("thaumictinkerer:spellbinding_cloth"), TcIngredient.item("thaumcraft:enchanted_fabric"),
            new AspectList().add(Aspect.MAGIC, 10).add(Aspect.ENTROPY, 6).add(Aspect.EXCHANGE, 4)
        );
    }

    private static void enchant(String key, String texture, AspectList aspects, int column, int row, String... parents) {
        TtResearch.item(key, aspects, column, row, 2, ThaumicTinkerer.id("textures/enchants/" + texture + ".png"))
            .setParents(parents)
            .setPages(TtResearch.text(0))
            .setSecondary()
            .registerResearchItem();
    }

    public static void research() {
        TtResearch.item(SPELL_CLOTH_RESEARCH, new AspectList().add(Aspect.MAGIC, 2).add(Aspect.CLOTH, 1), 3, 2, 2, TcStack.of("thaumictinkerer:spellbinding_cloth"))
            .setParentsHidden("ENCHFABRIC")
            .setPages(TtResearch.text(0), TtResearch.crucible(SPELL_CLOTH_RESEARCH))
            .registerResearchItem();
        TtResearch.item(ENCHANTER_RESEARCH, new AspectList().add(Aspect.MAGIC, 2).add(Aspect.AURA, 1).add(Aspect.ELDRITCH, 1).add(Aspect.DARKNESS, 1).add(Aspect.MIND, 1), 5, 4, 5, TcStack.of("thaumictinkerer:osmotic_enchanter"))
            .setParents(SPELL_CLOTH_RESEARCH)
            .setPages(TtResearch.text(0), TtResearch.text(1), TtResearch.text(2), TtResearch.infusion(ENCHANTER_RESEARCH))
            .registerResearchItem();
        enchant(ENCHANT_ASCENT_BOOST, "ascentboost", new AspectList().add(Aspect.AIR, 1).add(Aspect.MOTION, 1).add(Aspect.MAGIC, 2), 6, 2, ENCHANTER_RESEARCH);
        enchant(ENCHANT_SLOW_FALL, "slowfall", new AspectList().add(Aspect.AIR, 1).add(Aspect.MOTION, 1).add(Aspect.MAGIC, 2), 7, 3, ENCHANTER_RESEARCH);
        enchant(ENCHANT_AUTO_SMELT, "autosmelt", new AspectList().add(Aspect.FIRE, 1).add(Aspect.ENTROPY, 1).add(Aspect.MAGIC, 2), 7, 5, ENCHANTER_RESEARCH);
        enchant(ENCHANT_DESINTEGRATE, "desintegrate", new AspectList().add(Aspect.ENTROPY, 1).add(Aspect.VOID, 1).add(Aspect.MAGIC, 2), 6, 6, ENCHANTER_RESEARCH);
        enchant(ENCHANT_QUICK_DRAW, "quickdraw", new AspectList().add(Aspect.SENSES, 1).add(Aspect.WEAPON, 1).add(Aspect.MAGIC, 2), 4, 6, ENCHANTER_RESEARCH);
        enchant(ENCHANT_VAMPIRISM, "vamprisim", new AspectList().add(Aspect.HUNGER, 1).add(Aspect.WEAPON, 1).add(Aspect.MAGIC, 2), 3, 5, ENCHANTER_RESEARCH);
        enchant(ENCHANT_FOCUSED_STRIKE, "focusedstrikes", new AspectList().add(Aspect.ORDER, 1).add(Aspect.WEAPON, 1).add(Aspect.MAGIC, 2), 2, 7, ENCHANT_VAMPIRISM);
        enchant(ENCHANT_DISPERSED_STRIKE, "dispersedstrikes", new AspectList().add(Aspect.ENTROPY, 1).add(Aspect.WEAPON, 1).add(Aspect.MAGIC, 2), 1, 6, ENCHANT_VAMPIRISM);
        enchant(ENCHANT_FINAL_STRIKE, "finalstrike", new AspectList().add(Aspect.ENTROPY, 3).add(Aspect.ORDER, 3).add(Aspect.WEAPON, 3).add(Aspect.MAGIC, 2), 0, 8, ENCHANT_FOCUSED_STRIKE, ENCHANT_DISPERSED_STRIKE);
        enchant(ENCHANT_POUNCE, "pounce", new AspectList().add(Aspect.AIR, 3).add(Aspect.ORDER, 3).add(Aspect.ARMOR, 3).add(Aspect.MAGIC, 2), 7, 0, ENCHANT_ASCENT_BOOST);
        enchant(ENCHANT_SHATTER, "shatter", new AspectList().add(Aspect.EARTH, 3).add(Aspect.ENTROPY, 3).add(Aspect.TOOL, 1).add(Aspect.MAGIC, 2), 5, 8, ENCHANT_DESINTEGRATE);
        enchant(ENCHANT_SHOCKWAVE, "shockwave", new AspectList().add(Aspect.AIR, 3).add(Aspect.ENTROPY, 3).add(Aspect.ARMOR, 1).add(Aspect.MAGIC, 2), 9, 2, ENCHANT_SLOW_FALL);
        enchant(ENCHANT_TUNNEL, "tunnel", new AspectList().add(Aspect.EARTH, 3).add(Aspect.ORDER, 3).add(Aspect.TOOL, 1).add(Aspect.MAGIC, 2), 9, 6, ENCHANT_AUTO_SMELT);
        enchant(ENCHANT_VALIANCE, "valiance", new AspectList().add(Aspect.WEAPON, 3).add(Aspect.HEAL, 3).add(Aspect.MAGIC, 2), 1, 4, ENCHANT_VAMPIRISM);
    }
}
