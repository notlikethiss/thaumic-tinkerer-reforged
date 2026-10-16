package thaumictinkerer.test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumictinkerer.ThaumicTinkerer;

public final class TtGameTestSuite {
    private final String name;
    private final DeferredRegister<Consumer<GameTestHelper>> functions;
    private final List<String> tests = new ArrayList<>();

    public TtGameTestSuite(String name) {
        this.name = name;
        this.functions = DeferredRegister.create(Registries.TEST_FUNCTION, ThaumicTinkerer.MODID);
    }

    public TtGameTestSuite add(String test, Consumer<GameTestHelper> body) {
        String testName = name + "_" + test;
        tests.add(testName);
        functions.register(testName, () -> body);
        return this;
    }

    public void register(IEventBus modEventBus) {
        functions.register(modEventBus);
        modEventBus.addListener(this::registerTests);
    }

    private void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(ThaumicTinkerer.id(name + "_default"), new TestEnvironmentDefinition.AllOf());
        for (String testName : tests) {
            ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(Registries.TEST_FUNCTION, ThaumicTinkerer.id(testName));
            event.registerTest(
                ThaumicTinkerer.id(testName),
                new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.fromNamespaceAndPath("thaumcraft", "test/platform"), 100, 0, true))
            );
        }
    }
}
