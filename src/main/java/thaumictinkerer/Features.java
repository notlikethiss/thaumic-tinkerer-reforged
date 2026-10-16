package thaumictinkerer;

import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.AspectRegistry;
import thaumictinkerer.darkquartz.DarkQuartzFeature;
import thaumictinkerer.decor.DecorFeature;
import thaumictinkerer.gas.GasFeature;
import thaumictinkerer.test.DarkQuartzGameTests;
import thaumictinkerer.test.DecorGameTests;
import thaumictinkerer.test.GasGameTests;

public final class Features {
    private Features() {
    }

    public static void load() {
        DarkQuartzFeature.load();
        DecorFeature.load();
        GasFeature.load();
    }

    public static void research() {
        DarkQuartzFeature.research();
        GasFeature.research();
    }

    public static void recipes() {
        DarkQuartzFeature.recipes();
        GasFeature.recipes();
    }

    public static void aspects(AspectRegistry registry) {
    }

    public static void tests(IEventBus modEventBus) {
        DarkQuartzGameTests.register(modEventBus);
        DecorGameTests.register(modEventBus);
        GasGameTests.register(modEventBus);
    }
}
