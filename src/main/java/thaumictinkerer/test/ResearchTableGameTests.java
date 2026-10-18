package thaumictinkerer.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.CraftingRecipeRef;
import thaumcraft.crafting.TcRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumictinkerer.research.KamiResearchItem;
import thaumictinkerer.research.TtResearch;

public final class ResearchTableGameTests {
    private static final String TABLE = "tools/tt_research.json";
    private static final String PAGE_PREFIX = "tc.research_page.";

    private static final TtGameTestSuite SUITE = new TtGameTestSuite("research_table")
        .add("research_matches_tt_table", ResearchTableGameTests::researchMatchesTable)
        .add("research_pages_resolve", ResearchTableGameTests::researchPagesResolve);

    private ResearchTableGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void researchMatchesTable(GameTestHelper helper) {
        JsonArray table = load();
        List<String> problems = new ArrayList<>();
        Set<String> expectedKeys = new HashSet<>();
        for (JsonElement element : table) {
            JsonObject expected = element.getAsJsonObject();
            String key = expected.get("key").getAsString();
            expectedKeys.add(key);
            ResearchItem actual = ResearchCategories.getResearch(key);
            if (actual == null) {
                problems.add(key + ": not registered");
                continue;
            }
            compare(expected, actual, problems);
        }
        for (ResearchItem research : ResearchCategories.allResearch()) {
            if (TtResearch.TT_CATEGORY.equals(research.category()) && !expectedKeys.contains(research.key())) {
                problems.add(research.key() + ": registered but absent from the TT table");
            }
        }
        helper.assertTrue(problems.isEmpty(), problems.size() + " research mismatches: " + String.join("; ", problems));
        helper.succeed();
    }

    private static void researchPagesResolve(GameTestHelper helper) {
        List<String> problems = new ArrayList<>();
        for (ResearchItem research : ResearchCategories.allResearch()) {
            if (!TtResearch.TT_CATEGORY.equals(research.category())) {
                continue;
            }
            for (ResearchPage page : research.pages()) {
                List<String> keys = switch (page) {
                    case ResearchPage.Crafting crafting -> crafting.recipes();
                    case ResearchPage.Arcane arcane -> arcane.recipes();
                    case ResearchPage.Crucible crucible -> crucible.recipes();
                    case ResearchPage.Infusion infusion -> infusion.recipes();
                    case ResearchPage.InfusionEnchantment enchantment -> List.of(enchantment.recipe());
                    default -> List.of();
                };
                for (String key : keys) {
                    TcRecipe recipe = TcRecipes.get(key);
                    if (recipe == null) {
                        problems.add(research.key() + ": unknown recipe " + key);
                        continue;
                    }
                    if (recipe instanceof CraftingRecipeRef reference) {
                        boolean present = helper.getLevel().getServer().getRecipeManager().byKey(reference.recipe()).isPresent();
                        if (!present) {
                            problems.add(research.key() + ": crafting recipe missing for " + key);
                        }
                        if (!reference.result().exists()) {
                            problems.add(research.key() + ": result item missing for " + key);
                        }
                    }
                }
            }
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    private static void compare(JsonObject expected, ResearchItem actual, List<String> problems) {
        String key = actual.key();
        boolean kami = expected.get("kami").getAsBoolean();
        check(problems, key, "category", expected.get("category").getAsString(), actual.category());
        check(problems, key, "kami", kami, actual instanceof KamiResearchItem);
        check(problems, key, "column", expected.get("column").getAsInt(), actual.column());
        check(problems, key, "row", expected.get("row").getAsInt(), actual.row());
        check(problems, key, "complexity", expected.get("complexityClamped").getAsInt(), actual.complexity());
        check(problems, key, "warp", expected.get("warp").getAsInt(), actual.warp());
        check(problems, key, "special", expected.get("special").getAsBoolean(), actual.isSpecial());
        check(problems, key, "secondary", expected.get("secondary").getAsBoolean(), actual.isSecondary());
        check(problems, key, "round", expected.get("round").getAsBoolean(), actual.isRound());
        check(problems, key, "stub", expected.get("stub").getAsBoolean(), actual.isStub());
        check(problems, key, "virtual", expected.get("virtual").getAsBoolean(), actual.isVirtual());
        check(problems, key, "concealed", expected.get("concealed").getAsBoolean(), actual.isConcealed());
        check(problems, key, "hidden", expected.get("hidden").getAsBoolean(), actual.isHidden());
        check(problems, key, "lost", expected.get("lost").getAsBoolean(), actual.isLost());
        check(problems, key, "autoUnlock", expected.get("autoUnlock").getAsBoolean(), actual.isAutoUnlock());
        check(problems, key, "parents", strings(expected.getAsJsonArray("parents")), actual.parents());
        check(problems, key, "siblings", strings(expected.getAsJsonArray("siblings")), actual.siblings());
        List<String> hidden = strings(expected.getAsJsonArray("parentsHidden"));
        if (kami) {
            List<String> actualHidden = actual.parentsHidden();
            boolean prefix = actualHidden.size() >= hidden.size() && actualHidden.subList(0, hidden.size()).equals(hidden);
            if (!prefix) {
                problems.add(key + ": parentsHidden expected to start with " + hidden + " but was " + actualHidden);
            }
        } else {
            check(problems, key, "parentsHidden", hidden, actual.parentsHidden());
        }
        Map<String, Integer> aspects = new HashMap<>();
        for (JsonElement entry : expected.getAsJsonArray("aspects")) {
            JsonArray pair = entry.getAsJsonArray();
            aspects.merge(pair.get(0).getAsString(), pair.get(1).getAsInt(), Integer::sum);
        }
        Map<String, Integer> actualAspects = new HashMap<>();
        for (Aspect aspect : actual.tags().getAspects()) {
            actualAspects.put(aspect.name(), actual.tags().getAmount(aspect));
        }
        check(problems, key, "aspects", aspects, actualAspects);
        comparePages(key, expected.getAsJsonArray("pages"), actual.pages(), problems);
    }

    private static void comparePages(String key, JsonArray expected, List<ResearchPage> actual, List<String> problems) {
        if (expected.size() != actual.size()) {
            problems.add(key + ": expected " + expected.size() + " pages but was " + actual.size());
            return;
        }
        for (int index = 0; index < expected.size(); index++) {
            JsonObject page = expected.get(index).getAsJsonObject();
            String type = page.get("type").getAsString();
            ResearchPage found = actual.get(index);
            String label = "page " + index;
            switch (type) {
                case "text" -> {
                    String text = page.get("text").getAsString();
                    String wanted = text.chars().allMatch(Character::isDigit) ? PAGE_PREFIX + key + "." + (Integer.parseInt(text) + 1) : null;
                    if (!(found instanceof ResearchPage.Text actualText)) {
                        problems.add(key + ": " + label + " expected text but was " + found);
                    } else if (wanted != null) {
                        check(problems, key, label, wanted, actualText.text());
                    }
                }
                case "crafting" -> recipes(problems, key, label, page, found instanceof ResearchPage.Crafting crafting ? crafting.recipes() : null);
                case "arcane" -> recipes(problems, key, label, page, found instanceof ResearchPage.Arcane arcane ? arcane.recipes() : null);
                case "crucible" -> recipes(problems, key, label, page, found instanceof ResearchPage.Crucible crucible ? crucible.recipes() : null);
                case "infusion" -> recipes(problems, key, label, page, found instanceof ResearchPage.Infusion infusion ? infusion.recipes() : null);
                case "enchant" -> recipes(problems, key, label, page, found instanceof ResearchPage.InfusionEnchantment enchantment ? List.of(enchantment.recipe()) : null);
                default -> problems.add(key + ": unknown page type " + type);
            }
        }
    }

    private static void recipes(List<String> problems, String key, String label, JsonObject page, List<String> actual) {
        if (actual == null) {
            problems.add(key + ": " + label + " has a wrong type, expected " + page.get("type").getAsString());
            return;
        }
        List<String> wanted = strings(page.getAsJsonArray("recipes"));
        List<String> mapped = new ArrayList<>();
        for (String recipe : actual) {
            String owner = null;
            for (String candidate : wanted) {
                if (recipe.equals(candidate) || recipe.startsWith(candidate + "_")) {
                    owner = candidate;
                    break;
                }
            }
            if (owner == null) {
                problems.add(key + ": " + label + " has unexpected recipe " + recipe);
            } else if (!mapped.contains(owner)) {
                mapped.add(owner);
            }
        }
        check(problems, key, label + " recipes", wanted, mapped);
    }

    private static void check(List<String> problems, String key, String field, Object expected, Object actual) {
        if (!expected.equals(actual)) {
            problems.add(key + ": " + field + " expected " + expected + " but was " + actual);
        }
    }

    private static List<String> strings(JsonArray array) {
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            values.add(element.getAsString());
        }
        return values;
    }

    private static JsonArray load() {
        Path directory = Path.of("").toAbsolutePath();
        while (directory != null) {
            Path candidate = directory.resolve(TABLE);
            if (Files.exists(candidate)) {
                try {
                    return JsonParser.parseString(Files.readString(candidate)).getAsJsonArray();
                } catch (IOException exception) {
                    throw new IllegalStateException(exception);
                }
            }
            directory = directory.getParent();
        }
        throw new IllegalStateException(TABLE + " not found");
    }
}
