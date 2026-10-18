package thaumictinkerer.test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumictinkerer.ThaumicTinkerer;

@GameTestHolder(ThaumicTinkerer.MODID)
@PrefixGameTestTemplate(false)
public final class TtGameTestSuite {
    private static final List<TtGameTestSuite> SUITES = new ArrayList<>();
    private final String name;
    private final List<TestCase> tests = new ArrayList<>();
    private boolean isolated;

    private record TestCase(String name, Consumer<GameTestHelper> body) {
    }

    public TtGameTestSuite(String name) {
        this.name = name;
    }

    public TtGameTestSuite add(String test, Consumer<GameTestHelper> body) {
        tests.add(new TestCase(name + "_" + test, body));
        return this;
    }

    public TtGameTestSuite isolated() {
        isolated = true;
        return this;
    }

    public void register(IEventBus bus) {
        SUITES.add(this);
    }

    @GameTestGenerator
    public static List<TestFunction> generate() {
        List<TestFunction> result = new ArrayList<>();
        for (TtGameTestSuite suite : SUITES) {
            for (TestCase test : suite.tests) {
                result.add(new TestFunction("thaumictinkerer_" + (suite.isolated ? test.name() : suite.name), "thaumictinkerer." + test.name(), "thaumictinkerer:test/platform", 100, 0, true, helper -> test.body().accept(new TtGameTestHelper(helper))));
            }
        }
        return result;
    }
}
