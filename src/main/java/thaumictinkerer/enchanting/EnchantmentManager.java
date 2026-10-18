package thaumictinkerer.enchanting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.registry.ModEnchantments;
import thaumcraft.research.ResearchManager;
import thaumictinkerer.ThaumicTinkerer;

public final class EnchantmentManager {
    public static final List<Aspect> PRIMAL_ASPECTS = List.of(Aspect.AIR, Aspect.EARTH, Aspect.FIRE, Aspect.WATER, Aspect.ORDER, Aspect.ENTROPY);

    public record Data(ResourceKey<Enchantment> enchantment, ResourceLocation texture, boolean vanilla, AspectList level1Aspects, String research) {
        public AspectList aspects(int level) {
            double multiplier = level * (1.0 + level * 0.2);
            AspectList result = new AspectList();
            for (Aspect aspect : level1Aspects.getAspects()) {
                result.add(aspect, (int) (level1Aspects.getAmount(aspect) * multiplier));
            }
            return result;
        }
    }

    private static final Map<ResourceKey<Enchantment>, Data> DATA = new LinkedHashMap<>();

    static {
        vanilla(Enchantments.PROTECTION, "protection", new AspectList().add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 7));
        vanilla(Enchantments.FIRE_PROTECTION, "fireprotection", new AspectList().add(Aspect.FIRE, 10).add(Aspect.ENTROPY, 3).add(Aspect.WATER, 4));
        vanilla(Enchantments.FEATHER_FALLING, "featherfalling", new AspectList().add(Aspect.AIR, 16).add(Aspect.ORDER, 5));
        vanilla(Enchantments.BLAST_PROTECTION, "blastprotection", new AspectList().add(Aspect.EARTH, 5).add(Aspect.FIRE, 5).add(Aspect.ENTROPY, 8));
        vanilla(Enchantments.PROJECTILE_PROTECTION, "projectileprotection", new AspectList().add(Aspect.AIR, 10).add(Aspect.ENTROPY, 7));
        vanilla(Enchantments.RESPIRATION, "respiration", new AspectList().add(Aspect.WATER, 10).add(Aspect.AIR, 8).add(Aspect.ORDER, 5));
        vanilla(Enchantments.AQUA_AFFINITY, "aquaaffinity", new AspectList().add(Aspect.WATER, 25).add(Aspect.ORDER, 20).add(Aspect.EARTH, 5));
        vanilla(Enchantments.THORNS, "thorns", new AspectList().add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 12));
        vanilla(Enchantments.SHARPNESS, "sharpness", new AspectList().add(Aspect.ORDER, 10));
        vanilla(Enchantments.SMITE, "smite", new AspectList().add(Aspect.ORDER, 5).add(Aspect.AIR, 5));
        vanilla(Enchantments.BANE_OF_ARTHROPODS, "baneofawfulturds", new AspectList().add(Aspect.ORDER, 5).add(Aspect.FIRE, 5));
        vanilla(Enchantments.KNOCKBACK, "knockback", new AspectList().add(Aspect.ENTROPY, 5).add(Aspect.AIR, 10));
        vanilla(Enchantments.FIRE_ASPECT, "fireaspect", new AspectList().add(Aspect.FIRE, 15).add(Aspect.EARTH, 4));
        vanilla(Enchantments.LOOTING, "looting", new AspectList().add(Aspect.AIR, 10).add(Aspect.FIRE, 10).add(Aspect.WATER, 10).add(Aspect.EARTH, 10).add(Aspect.ORDER, 15).add(Aspect.ENTROPY, 15));
        vanilla(Enchantments.EFFICIENCY, "efficiency", new AspectList().add(Aspect.ENTROPY, 12).add(Aspect.EARTH, 4));
        vanilla(Enchantments.SILK_TOUCH, "silktouch", new AspectList().add(Aspect.ORDER, 50).add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 10));
        vanilla(Enchantments.UNBREAKING, "unbreaking", new AspectList().add(Aspect.ORDER, 15).add(Aspect.WATER, 8).add(Aspect.EARTH, 8));
        vanilla(Enchantments.FORTUNE, "fortune", new AspectList().add(Aspect.AIR, 10).add(Aspect.FIRE, 10).add(Aspect.WATER, 10).add(Aspect.EARTH, 10).add(Aspect.ORDER, 15).add(Aspect.ENTROPY, 15));
        vanilla(Enchantments.POWER, "power", new AspectList().add(Aspect.EARTH, 5).add(Aspect.ORDER, 10));
        vanilla(Enchantments.PUNCH, "punch", new AspectList().add(Aspect.AIR, 4).add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 5));
        vanilla(Enchantments.FLAME, "flame", new AspectList().add(Aspect.ENTROPY, 5).add(Aspect.FIRE, 20).add(Aspect.EARTH, 5));
        vanilla(Enchantments.INFINITY, "infinity", new AspectList().add(Aspect.ENTROPY, 40).add(Aspect.ORDER, 40).add(Aspect.EARTH, 10));
        vanilla(Enchantments.LURE, "lure", new AspectList().add(Aspect.WATER, 20).add(Aspect.BEAST, 20));
        vanilla(Enchantments.LUCK_OF_THE_SEA, "luckofthesea", new AspectList().add(Aspect.ENTROPY, 20).add(Aspect.WATER, 20));
        vanilla(ModEnchantments.HASTE, "haste", new AspectList().add(Aspect.AIR, 10).add(Aspect.ENTROPY, 5).add(Aspect.EARTH, 5));
        vanilla(ModEnchantments.REPAIR, "repair", new AspectList().add(Aspect.WATER, 20).add(Aspect.FIRE, 20).add(Aspect.EARTH, 20).add(Aspect.AIR, 20).add(Aspect.ORDER, 20).add(Aspect.ENTROPY, 5));
        custom(TtEnchantments.ASCENT_BOOST, "ascentboost", new AspectList().add(Aspect.ENTROPY, 8).add(Aspect.AIR, 10), EnchantingFeature.ENCHANT_ASCENT_BOOST);
        custom(TtEnchantments.SLOW_FALL, "slowfall", new AspectList().add(Aspect.ORDER, 8).add(Aspect.AIR, 10), EnchantingFeature.ENCHANT_SLOW_FALL);
        custom(TtEnchantments.AUTO_SMELT, "autosmelt", new AspectList().add(Aspect.ENTROPY, 20).add(Aspect.FIRE, 30), EnchantingFeature.ENCHANT_AUTO_SMELT);
        custom(TtEnchantments.DESINTEGRATE, "desintegrate", new AspectList().add(Aspect.ENTROPY, 25).add(Aspect.AIR, 10).add(Aspect.EARTH, 10), EnchantingFeature.ENCHANT_DESINTEGRATE);
        custom(TtEnchantments.QUICK_DRAW, "quickdraw", new AspectList().add(Aspect.ORDER, 10).add(Aspect.AIR, 10).add(Aspect.WATER, 5), EnchantingFeature.ENCHANT_QUICK_DRAW);
        custom(TtEnchantments.VAMPIRISM, "vamprisim", new AspectList().add(Aspect.ENTROPY, 8).add(Aspect.FIRE, 10).add(Aspect.WATER, 10), EnchantingFeature.ENCHANT_VAMPIRISM);
        custom(TtEnchantments.FOCUSED_STRIKE, "focusedstrikes", new AspectList().add(Aspect.ORDER, 12).add(Aspect.AIR, 10).add(Aspect.WATER, 10), EnchantingFeature.ENCHANT_VAMPIRISM);
        custom(TtEnchantments.DISPERSED_STRIKE, "dispersedstrikes", new AspectList().add(Aspect.ENTROPY, 12).add(Aspect.FIRE, 10).add(Aspect.EARTH, 10), EnchantingFeature.ENCHANT_VAMPIRISM);
        custom(TtEnchantments.VALIANCE, "valiance", new AspectList().add(Aspect.ORDER, 12).add(Aspect.FIRE, 10).add(Aspect.EARTH, 10), EnchantingFeature.ENCHANT_VAMPIRISM);
        custom(TtEnchantments.FINAL_STRIKE, "finalstrike", new AspectList().add(Aspect.ENTROPY, 16).add(Aspect.FIRE, 16), EnchantingFeature.ENCHANT_VAMPIRISM);
        custom(TtEnchantments.TUNNEL, "tunnel", new AspectList().add(Aspect.EARTH, 16).add(Aspect.ORDER, 16), EnchantingFeature.ENCHANT_VAMPIRISM);
        custom(TtEnchantments.SHATTER, "shatter", new AspectList().add(Aspect.EARTH, 16).add(Aspect.ENTROPY, 16), EnchantingFeature.ENCHANT_VAMPIRISM);
        custom(TtEnchantments.SHOCKWAVE, "shockwave", new AspectList().add(Aspect.EARTH, 16).add(Aspect.AIR, 16), EnchantingFeature.ENCHANT_VAMPIRISM);
        custom(TtEnchantments.POUNCE, "pounce", new AspectList().add(Aspect.EARTH, 16).add(Aspect.AIR, 16), EnchantingFeature.ENCHANT_VAMPIRISM);
    }

    private EnchantmentManager() {
    }

    private static void vanilla(ResourceKey<Enchantment> key, String texture, AspectList level1) {
        DATA.put(key, new Data(key, ThaumicTinkerer.id("textures/enchants/" + texture + ".png"), true, level1, ""));
    }

    private static void custom(ResourceKey<Enchantment> key, String texture, AspectList level1, String research) {
        DATA.put(key, new Data(key, ThaumicTinkerer.id("textures/enchants/" + texture + ".png"), false, level1, research));
    }

    public static Map<ResourceKey<Enchantment>, Data> data() {
        return DATA;
    }

    public static Data data(ResourceKey<Enchantment> key) {
        return DATA.get(key);
    }

    public static boolean isTool(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(Items.BOOK) && stack.getMaxStackSize() == 1 && stack.isDamageableItem();
    }

    public static boolean canApply(HolderLookup.Provider access, ItemStack stack, ResourceKey<Enchantment> key, List<ResourceKey<Enchantment>> current) {
        Optional<Holder.Reference<Enchantment>> holder = TtEnchantments.holder(access, key);
        if (holder.isEmpty() || !DATA.containsKey(key) || current.contains(key) || !stack.supportsEnchantment(holder.get())) {
            return false;
        }
        if (stack.is(TagKey.create(Registries.ITEM, ThaumicTinkerer.id("enchanter_excluded/" + key.location().getPath())))) {
            return false;
        }
        for (ResourceKey<Enchantment> other : current) {
            Optional<Holder.Reference<Enchantment>> otherHolder = TtEnchantments.holder(access, other);
            if (otherHolder.isPresent() && !Enchantment.areCompatible(holder.get(), otherHolder.get())) {
                return false;
            }
        }
        return true;
    }

    public static boolean canEnchantStack(ItemStack stack) {
        return isTool(stack) && stack.getEnchantmentValue() > 0 && !stack.isEnchanted();
    }

    public static boolean canEnchantmentBeUsed(Player player, ResourceKey<Enchantment> key) {
        Data data = DATA.get(key);
        return data != null && (data.research().isEmpty() || ResearchManager.isComplete(player, data.research()));
    }

    public static boolean isNoRemoveEnchant(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof NoRemoveEnchant || stack.is(TagKey.create(Registries.ITEM, ThaumicTinkerer.id("no_remove_enchant")));
    }
}
