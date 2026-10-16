package thaumictinkerer;

import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.AspectRegistry;
import thaumictinkerer.darkquartz.DarkQuartzFeature;
import thaumictinkerer.test.DarkQuartzGameTests;

public final class Features {
    private Features() {
    }

    public static void load() {
        DarkQuartzFeature.load();
    }

    public static void research() {
        DarkQuartzFeature.research();
    }

    public static void recipes() {
        DarkQuartzFeature.recipes();
    }

    public static void aspects(AspectRegistry registry) {
    }

    public static void tests(IEventBus modEventBus) {
        DarkQuartzGameTests.register(modEventBus);
    }
}
