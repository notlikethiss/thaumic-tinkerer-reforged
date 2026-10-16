package thaumictinkerer;

import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.AspectRegistry;
import thaumictinkerer.darkquartz.DarkQuartzFeature;
import thaumictinkerer.test.DarkQuartzGameTests;
import thaumictinkerer.test.TransvectorGameTests;
import thaumictinkerer.transvector.TransvectorFeature;

public final class Features {
    private Features() {
    }

    public static void load() {
        DarkQuartzFeature.load();
        TransvectorFeature.load();
    }

    public static void research() {
        DarkQuartzFeature.research();
        TransvectorFeature.research();
    }

    public static void recipes() {
        DarkQuartzFeature.recipes();
        TransvectorFeature.recipes();
    }

    public static void aspects(AspectRegistry registry) {
    }

    public static void tests(IEventBus modEventBus) {
        DarkQuartzGameTests.register(modEventBus);
        TransvectorGameTests.register(modEventBus);
    }
}
