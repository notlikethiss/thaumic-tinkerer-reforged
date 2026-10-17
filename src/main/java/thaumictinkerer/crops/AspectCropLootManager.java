package thaumictinkerer.crops;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;

public final class AspectCropLootManager {
    private static final String[] GOLEM_CORES = {"fill", "empty", "gather", "harvest", "guard", "decanting", "alchemy", "chop", "use", "butcher", "sorting", "fishing"};
    private static final String[] SAPLINGS = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"};

    private AspectCropLootManager() {
    }

    public record Entry(ItemStack stack, int weight) {
    }

    public static ItemStack getLootForAspect(Aspect aspect, HolderLookup.Provider registries, RandomSource random) {
        List<Entry> entries = entries(aspect, registries);
        int sum = 0;
        for (Entry entry : entries) {
            sum += entry.weight();
        }
        if (sum <= 0) {
            return ItemStack.EMPTY;
        }
        int roll = random.nextInt(sum);
        for (Entry entry : entries) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry.stack().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    public static List<Entry> entries(Aspect aspect, HolderLookup.Provider registries) {
        List<Entry> entries = new ArrayList<>();
        switch (aspect) {
            case AIR -> add(entries, "thaumictinkerer:aer_infused_grain", 1, 1);
            case FIRE -> add(entries, "thaumictinkerer:ignis_infused_grain", 1, 1);
            case EARTH -> add(entries, "thaumictinkerer:terra_infused_grain", 1, 1);
            case WATER -> add(entries, "thaumictinkerer:aqua_infused_grain", 1, 1);
            case ORDER -> add(entries, "minecraft:glass", 64, 1);
            case ENTROPY -> add(entries, "minecraft:sand", 64, 1);
            case ELDRITCH -> {
                add(entries, "minecraft:ender_pearl", 4, 10);
                add(entries, "minecraft:ender_eye", 4, 5);
            }
            case TREE -> {
                addTag(entries, ItemTags.LOGS);
                addTag(entries, ItemTags.PLANKS);
                addTag(entries, ItemTags.WOODEN_SLABS);
                addTag(entries, ItemTags.WOODEN_STAIRS);
                add(entries, "minecraft:stick", 1, 1);
                add(entries, "minecraft:oak_log", 1, 1);
            }
            case AURA -> {
                Item essence = item("thaumcraft:wisp_essence");
                if (essence != null) {
                    for (Aspect tag : Aspect.values()) {
                        ItemStack stack = new ItemStack(essence);
                        AspectKeys.ASPECTS.set(stack, new AspectList().add(tag, 2));
                        entries.add(new Entry(stack, 1));
                    }
                }
            }
            case BEAST -> {
                for (Item item : BuiltInRegistries.ITEM) {
                    if (item instanceof SpawnEggItem) {
                        entries.add(new Entry(new ItemStack(item), 1));
                    }
                }
            }
            case MIND -> {
                add(entries, "minecraft:paper", 64, 15);
                add(entries, "minecraft:book", 32, 10);
                add(entries, "minecraft:bookshelf", 16, 5);
                for (Holder.Reference<Enchantment> enchantment : registries.lookupOrThrow(Registries.ENCHANTMENT).listElements().toList()) {
                    ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                    ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                    enchantments.set(enchantment, 1);
                    book.set(DataComponents.STORED_ENCHANTMENTS, enchantments.toImmutable());
                    entries.add(new Entry(book, 1));
                }
            }
            case FLESH -> {
                add(entries, "thaumcraft:zombie_brain", 16, 4);
                add(entries, "thaumcraft:zombie_brain", 16, 1);
            }
            case UNDEAD -> add(entries, "minecraft:rotten_flesh", 32, 1);
            case CRAFT -> {
                add(entries, "thaumictinkerer:dark_quartz_block", 32, 1);
                add(entries, "thaumcraft:alchemical_furnace", 16, 1);
            }
            case HUNGER -> add(entries, "minecraft:nether_wart", 16, 1);
            case COLD -> add(entries, "minecraft:snowball", 16, 1);
            case PLANT -> {
                addTag(entries, ItemTags.SAPLINGS);
                for (String sapling : SAPLINGS) {
                    add(entries, "minecraft:" + sapling + "_sapling", 1, 1);
                }
            }
            case MAN -> {
                for (String core : GOLEM_CORES) {
                    add(entries, "thaumcraft:golem_core_" + core, 1, 1);
                }
            }
            case ARMOR -> {
                add(entries, "minecraft:diamond_boots", 1, 1);
                add(entries, "minecraft:diamond_leggings", 1, 1);
                add(entries, "minecraft:diamond_chestplate", 1, 1);
                add(entries, "minecraft:diamond_helmet", 1, 1);
            }
            case MINE -> add(entries, "thaumcraft:thaumium_pickaxe", 1, 1);
            case HARVEST -> add(entries, "thaumcraft:thaumium_hoe", 1, 1);
            case WEAPON -> add(entries, "thaumcraft:thaumium_sword", 1, 1);
            case TOOL -> {
                add(entries, "thaumcraft:thaumium_shovel", 1, 1);
                add(entries, "thaumcraft:thaumium_axe", 1, 1);
            }
            case TRAVEL -> add(entries, "thaumcraft:arcane_stone_bricks", 8, 1);
            case SLIME -> {
                add(entries, "minecraft:slime_ball", 16, 1);
                add(entries, "minecraft:slime_ball", 1, 1);
            }
            case GREED -> add(entries, "minecraft:gold_ingot", 4, 1);
            case LIGHT -> {
                add(entries, "minecraft:glowstone_dust", 16, 5);
                add(entries, "thaumcraft:nitor", 4, 1);
                add(entries, "thaumictinkerer:bright_nitor", 1, 1);
            }
            case MECHANISM -> add(entries, "minecraft:piston", 8, 1);
            case CROP -> add(entries, "minecraft:wheat", 32, 1);
            case METAL -> {
                add(entries, "minecraft:iron_ingot", 4, 100);
                addTag(entries, commonTag("ingots/iron"));
                addTag(entries, commonTag("nuggets/iron"));
                addTag(entries, commonTag("ores/iron"));
                addTag(entries, commonTag("storage_blocks/iron"));
            }
            case DEATH -> add(entries, "minecraft:bone", 32, 1);
            case MOTION -> {
                add(entries, "minecraft:rail", 1, 10);
                add(entries, "minecraft:activator_rail", 1, 1);
            }
            case CLOTH -> {
                add(entries, "minecraft:white_wool", 16, 30);
                add(entries, "minecraft:string", 15, 10);
                for (DyeColor color : DyeColor.values()) {
                    add(entries, "minecraft:" + color.getName() + "_wool", 4, 1);
                }
            }
            case EXCHANGE -> {
                addTag(entries, commonTag("ingots/copper"), 4);
                add(entries, "thaumcraft:cinnabar_ore", 4, 1);
            }
            case ENERGY -> add(entries, "thaumcraft:alumentum", 12, 1);
            case MAGIC -> {
                for (String shard : List.of("air", "fire", "water", "earth", "order", "entropy")) {
                    add(entries, "thaumcraft:" + shard + "_shard", 1, 1);
                }
            }
            case HEAL -> {
                add(entries, "minecraft:golden_apple", 1, 1);
                add(entries, "minecraft:cake", 1, 1);
            }
            case SENSES -> add(entries, "minecraft:lapis_lazuli", 20, 1);
            case SOUL -> {
                add(entries, "minecraft:soul_sand", 64, 2);
                add(entries, "minecraft:netherrack", 64, 2);
                add(entries, "minecraft:nether_bricks", 1, 1);
            }
            case DARKNESS -> add(entries, "minecraft:obsidian", 10, 1);
            case VOID -> {
                add(entries, "minecraft:bucket", 1, 1);
                add(entries, "minecraft:water_bucket", 1, 1);
                add(entries, "minecraft:lava_bucket", 1, 1);
                add(entries, "minecraft:milk_bucket", 1, 1);
            }
            case POISON -> add(entries, "thaumcraft:quicksilver", 16, 1);
            case LIFE -> add(entries, "minecraft:egg", 8, 1);
            case TRAP -> add(entries, "minecraft:cobweb", 4, 1);
            case TAINT -> add(entries, "thaumcraft:tainted_goo", 4, 1);
            case CRYSTAL -> add(entries, "minecraft:diamond", 1, 1);
            default -> {
            }
        }
        return entries;
    }

    private static Item item(String id) {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
        return item == Items.AIR ? null : item;
    }

    private static void add(List<Entry> entries, String id, int count, int weight) {
        Item item = item(id);
        if (item != null) {
            entries.add(new Entry(new ItemStack(item, count), weight));
        }
    }

    private static TagKey<Item> commonTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }

    private static void addTag(List<Entry> entries, TagKey<Item> tag) {
        addTag(entries, tag, 1);
    }

    private static void addTag(List<Entry> entries, TagKey<Item> tag, int count) {
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            entries.add(new Entry(new ItemStack(holder.value(), count), 1));
        }
    }
}
