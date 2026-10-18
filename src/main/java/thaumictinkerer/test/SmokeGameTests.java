package thaumictinkerer.test;

import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import thaumcraft.research.ResearchCategories;
import thaumictinkerer.ThaumicTinkerer;

public final class SmokeGameTests {
    private SmokeGameTests() {
    }

    public static void register(IEventBus bus) {
        new TtGameTestSuite("smoke")
            .add("addon_and_base_loaded", SmokeGameTests::addonAndBaseLoaded)
            .add("base_research_category_exists", SmokeGameTests::baseResearchCategoryExists)
            .register(bus);
    }

    private static void addonAndBaseLoaded(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded(ThaumicTinkerer.MODID), "addon is not loaded");
        helper.assertTrue(ModList.get().isLoaded("thaumcraft"), "base is not loaded");
        helper.succeed();
    }

    private static void baseResearchCategoryExists(GameTestHelper helper) {
        helper.assertTrue(ResearchCategories.get("BASICS") != null, "base research category BASICS is missing");
        helper.succeed();
    }
}
