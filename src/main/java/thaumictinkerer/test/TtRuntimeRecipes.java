package thaumictinkerer.test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.gametest.framework.GameTestHelper;
import thaumcraft.crafting.*;

public final class TtRuntimeRecipes {
    private TtRuntimeRecipes() {
    }

    public static void dump(GameTestHelper helper) {
        JsonArray recipes = new JsonArray();
        for (TcRecipe recipe : TcRecipes.all()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("key", recipe.key());
            entry.addProperty("research", recipe.research());
            entry.addProperty("type", recipe.getClass().getSimpleName());
            switch (recipe) {
                case ShapedArcaneRecipe shaped -> {
                    entry.addProperty("result", stack(shaped.result()));
                    entry.addProperty("aspects", shaped.aspects().toString());
                    entry.addProperty("width", shaped.width());
                    entry.addProperty("height", shaped.height());
                    entry.add("grid", ingredients(shaped.grid()));
                }
                case ShapelessArcaneRecipe shapeless -> {
                    entry.addProperty("result", stack(shapeless.result()));
                    entry.addProperty("aspects", shapeless.aspects().toString());
                    entry.addProperty("width", shapeless.ingredients().size());
                    entry.addProperty("height", 1);
                    entry.add("grid", ingredients(shapeless.ingredients()));
                }
                case InfusionRecipe infusion -> {
                    entry.addProperty("result", stack(infusion.result()));
                    entry.addProperty("aspects", infusion.aspects().toString());
                    entry.addProperty("instability", infusion.instability());
                    entry.addProperty("central", ingredient(infusion.central()));
                    entry.add("components", ingredients(infusion.components()));
                }
                case CrucibleRecipe crucible -> {
                    entry.addProperty("result", stack(crucible.result()));
                    entry.addProperty("aspects", crucible.aspects().toString());
                    entry.addProperty("catalyst", ingredient(crucible.catalyst()));
                }
                case CraftingRecipeRef bench -> {
                    entry.addProperty("result", stack(bench.result()));
                    entry.addProperty("recipe", bench.recipe().toString());
                }
                default -> {
                }
            }
            recipes.add(entry);
        }
        try {
            Path directory = Path.of(System.getProperty("thaumictinkerer.validationDir", "../build/reports/backport"));
            Files.createDirectories(directory);
            JsonArray enchanting = new JsonArray();
            thaumictinkerer.enchanting.EnchantmentManager.data().forEach((key, data) -> {
                JsonObject entry = new JsonObject();
                entry.addProperty("id", key.location().toString());
                entry.addProperty("texture", data.texture().toString());
                entry.addProperty("vanilla", data.vanilla());
                entry.addProperty("research", data.research());
                entry.addProperty("aspects", data.level1Aspects().toString());
                JsonObject levels = new JsonObject();
                thaumictinkerer.enchanting.TtEnchantments.holder(helper.getLevel().registryAccess(), key).ifPresent(holder -> {
                    for (int level = holder.value().getMinLevel(); level <= holder.value().getMaxLevel(); level++) {
                        levels.addProperty(Integer.toString(level), data.aspects(level).toString());
                    }
                });
                entry.add("levels", levels);
                enchanting.add(entry);
            });
            Files.writeString(directory.resolve("runtime-enchanter.json"), new GsonBuilder().setPrettyPrinting().create().toJson(enchanting) + "\n");
            Files.writeString(directory.resolve("runtime-recipes.json"), new GsonBuilder().setPrettyPrinting().create().toJson(recipes) + "\n");
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Cannot export runtime recipes", exception);
        }
        helper.assertTrue(!recipes.isEmpty(), "recipe export is empty");
        helper.succeed();
    }

    private static String stack(TcStack stack) {
        return stack == TcStack.NONE || stack.count() == 0 ? "-" : stack.item() + " x" + stack.count() + (stack.data().isEmpty() ? "" : " " + stack.data());
    }

    private static String ingredient(TcIngredient value) {
        return switch (value) {
            case TcIngredient.Empty empty -> "-";
            case TcIngredient.Stack stack -> stack(stack.stack());
            case TcIngredient.Tag tag -> "#" + tag.tag().location();
            case TcIngredient.AnyOf any -> "any([" + String.join(", ", any.options().stream().map(TtRuntimeRecipes::ingredient).toList()) + "])";
        };
    }

    private static JsonArray ingredients(java.util.List<TcIngredient> values) {
        JsonArray result = new JsonArray();
        values.forEach(value -> result.add(ingredient(value)));
        return result;
    }
}
