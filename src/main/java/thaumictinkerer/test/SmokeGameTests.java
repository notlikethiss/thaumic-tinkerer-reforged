package thaumictinkerer.test;

import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.research.ResearchCategories;
import thaumictinkerer.ThaumicTinkerer;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID)
public final class SmokeGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, ThaumicTinkerer.MODID);
    private static final String[] TESTS = {
        "addon_and_base_loaded",
        "base_research_category_exists",
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> SmokeGameTests::addonAndBaseLoaded);
        FUNCTIONS.register(TESTS[1], () -> SmokeGameTests::baseResearchCategoryExists);
    }

    private SmokeGameTests() {
    }

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
    }

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(ThaumicTinkerer.id("smoke_default"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(Registries.TEST_FUNCTION, ThaumicTinkerer.id(name));
            event.registerTest(
                ThaumicTinkerer.id(name),
                new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.fromNamespaceAndPath("thaumcraft", "test/platform"), 100, 0, true))
            );
        }
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
