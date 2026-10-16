package thaumictinkerer;

import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.AspectRegistry;
import thaumictinkerer.darkquartz.DarkQuartzFeature;
import thaumictinkerer.decor.DecorFeature;
import thaumictinkerer.gas.GasFeature;
import thaumictinkerer.test.DarkQuartzGameTests;
import thaumictinkerer.test.DecorGameTests;
import thaumictinkerer.test.GasGameTests;
import thaumictinkerer.test.TransvectorGameTests;
import thaumictinkerer.transvector.TransvectorFeature;
import thaumictinkerer.foci.FociFeature;
import thaumictinkerer.enchanting.EnchantingFeature;
import thaumictinkerer.test.EnchantingGameTests;
import thaumictinkerer.test.DarkQuartzGameTests;
import thaumictinkerer.test.FociGameTests;

public final class Features {
    private Features() {
    }

    public static void load() {
        DarkQuartzFeature.load();
        DecorFeature.load();
        GasFeature.load();
        TransvectorFeature.load();
        FociFeature.load();
        EnchantingFeature.load();
    }

    public static void research() {
        DarkQuartzFeature.research();
        GasFeature.research();
        TransvectorFeature.research();
        FociFeature.research();
        EnchantingFeature.research();
    }

    public static void recipes() {
        DarkQuartzFeature.recipes();
        GasFeature.recipes();
        TransvectorFeature.recipes();
        FociFeature.recipes();
        EnchantingFeature.recipes();
    }

    public static void aspects(AspectRegistry registry) {
    }

    public static void tests(IEventBus modEventBus) {
        DarkQuartzGameTests.register(modEventBus);
        DecorGameTests.register(modEventBus);
        GasGameTests.register(modEventBus);
        TransvectorGameTests.register(modEventBus);
        FociGameTests.register(modEventBus);
        EnchantingGameTests.register(modEventBus);
    }
}
