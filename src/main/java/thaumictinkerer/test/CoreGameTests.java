package thaumictinkerer.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchCategory;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.TtRarity;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.research.KamiResearchItem;
import thaumictinkerer.research.TtResearch;
import thaumictinkerer.research.TtResearchItem;

public final class CoreGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("core")
        .add("category_registered", CoreGameTests::categoryRegistered)
        .add("research_lang_keys", CoreGameTests::researchLangKeys)
        .add("infusion_hidden_parent", CoreGameTests::infusionHiddenParent)
        .add("kami_hidden_parents", CoreGameTests::kamiHiddenParents)
        .add("recipe_key_collision", CoreGameTests::recipeKeyCollision)
        .add("kami_rarity", CoreGameTests::kamiRarity);

    private CoreGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void categoryRegistered(GameTestHelper helper) {
        ResearchCategory category = ResearchCategories.get(TtResearch.TT_CATEGORY);
        helper.assertTrue(category != null, "TT_CATEGORY is missing");
        helper.assertTrue(category.icon().equals(ThaumicTinkerer.id("textures/misc/r_enchanting.png")), "wrong icon " + category.icon());
        helper.assertTrue(category.background().toString().equals("thaumcraft:textures/gui/gui_researchback.png"), "wrong background");
        helper.assertTrue(ThaumicTinkerer.class.getResource("/assets/thaumictinkerer/textures/misc/r_enchanting.png") != null, "icon texture is missing");
        helper.succeed();
    }

    private static void researchLangKeys(GameTestHelper helper) {
        for (String language : new String[]{"en_us", "ru_ru"}) {
            JsonObject lang = lang(language);
            helper.assertTrue(lang.has("tc.research_category." + TtResearch.TT_CATEGORY), language + " lacks the category name");
            for (ResearchItem research : ResearchCategories.get(TtResearch.TT_CATEGORY).research()) {
                helper.assertTrue(lang.has(research.nameKey()), language + " lacks " + research.nameKey());
                helper.assertTrue(lang.has(research.textKey()), language + " lacks " + research.textKey());
                helper.assertTrue(lang.has("tc.research_text." + research.key()), language + " lacks the plain text of " + research.key());
                for (ResearchPage page : research.pages()) {
                    if (page instanceof ResearchPage.Text text) {
                        helper.assertTrue(lang.has(text.text()), language + " lacks " + text.text());
                    }
                }
            }
        }
        helper.succeed();
    }

    private static void infusionHiddenParent(GameTestHelper helper) {
        TtResearchItem plain = TtResearch.item("CORE_TEST_PLAIN", new AspectList(), 0, 0, 1, TcStack.of("minecraft:stone"));
        plain.setPages(TtResearch.text(0), TtResearch.recipe("A"));
        helper.assertTrue(!plain.parentsHidden().contains("INFUSION"), "plain research got INFUSION");
        TtResearchItem infused = TtResearch.item("CORE_TEST_INFUSED", new AspectList(), 0, 0, 1, TcStack.of("minecraft:stone"));
        infused.setParentsHidden("OTHER").setPages(TtResearch.text(0), TtResearch.infusion("B"), TtResearch.infusion("B"));
        helper.assertTrue(infused.parentsHidden().equals(java.util.List.of("INFUSION", "OTHER")), "got " + infused.parentsHidden());
        helper.assertTrue(infused.pages().getFirst() instanceof ResearchPage.Text text && text.text().equals("tc.research_page.CORE_TEST_INFUSED.1"), "page key is wrong");
        helper.succeed();
    }

    private static void kamiHiddenParents(GameTestHelper helper) {
        KamiResearchItem kami = TtResearch.kami("CORE_TEST_KAMI", new AspectList(), 0, 0, 5, TcStack.of("minecraft:stone"));
        helper.assertTrue(kami.isConcealed() && kami.complexity() == 3, "kami flags are wrong");
        helper.assertTrue(kami.parentsHidden().contains("FOCUSEXCAVATION"), "regular research is not required");
        helper.assertTrue(!kami.parentsHidden().contains("FOCUSHELLBAT"), "trigger research is required");
        helper.assertTrue(!kami.parentsHidden().contains("ASPECTS"), "parentless research is required");
        helper.assertTrue(!kami.parentsHidden().contains("ELDRITCH"), "other category is required");
        KamiResearchItem own = TtResearch.kami("CORE_TEST_KAMI_OWN", new AspectList(), 0, 0, 5, TcStack.of("minecraft:stone"));
        own.setParentsHidden("FIRST");
        helper.assertTrue(own.parentsHidden().getFirst().equals("FIRST"), "own hidden parents are not first");
        helper.assertTrue(own.textKey().equals("ttinkerer.research_text_kami.CORE_TEST_KAMI_OWN"), "text key is wrong");
        helper.succeed();
    }

    private static void recipeKeyCollision(GameTestHelper helper) {
        boolean thrown = false;
        try {
            TtRecipes.arcaneShapeless("Thaumometer", "X", TcStack.of("minecraft:stone"), new AspectList(), TcIngredient.item("minecraft:stone"));
        } catch (IllegalStateException exception) {
            thrown = true;
        }
        helper.assertTrue(thrown, "duplicate recipe key was accepted");
        helper.succeed();
    }

    private static void kamiRarity(GameTestHelper helper) {
        Rarity rarity = TtRarity.kami();
        helper.assertTrue(rarity != null, "KAMI rarity is not created");
        helper.assertTrue(rarity.getSerializedName().equals("thaumictinkerer:kami"), "got " + rarity.getSerializedName());
        helper.assertTrue(rarity.ordinal() > Rarity.EPIC.ordinal(), "KAMI is not appended");
        helper.succeed();
    }

    private static JsonObject lang(String language) {
        try (InputStream stream = ThaumicTinkerer.class.getResourceAsStream("/assets/thaumictinkerer/lang/" + language + ".json")) {
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
