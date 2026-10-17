package thaumictinkerer;

import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.AspectRegistry;
import thaumictinkerer.darkquartz.DarkQuartzFeature;
import thaumictinkerer.decor.DecorFeature;
import thaumictinkerer.funnel.FunnelFeature;
import thaumictinkerer.gas.GasFeature;
import thaumictinkerer.mobilizer.MobilizerFeature;
import thaumictinkerer.repairer.RepairerFeature;
import thaumictinkerer.test.DarkQuartzGameTests;
import thaumictinkerer.test.DecorGameTests;
import thaumictinkerer.test.FunnelGameTests;
import thaumictinkerer.test.GasGameTests;
import thaumictinkerer.test.MobilizerGameTests;
import thaumictinkerer.test.RepairerGameTests;
import thaumictinkerer.test.TransvectorGameTests;
import thaumictinkerer.transvector.TransvectorFeature;
import thaumictinkerer.foci.FociFeature;
import thaumictinkerer.enchanting.EnchantingFeature;
import thaumictinkerer.test.EnchantingGameTests;
import thaumictinkerer.test.DarkQuartzGameTests;
import thaumictinkerer.test.FociGameTests;
import thaumictinkerer.elements.ElementsFeature;
import thaumictinkerer.test.ElementsGameTests;

public final class Features {
    private Features() {
    }

    public static void load() {
        DarkQuartzFeature.load();
        DecorFeature.load();
        FunnelFeature.load();
        GasFeature.load();
        MobilizerFeature.load();
        RepairerFeature.load();
        TransvectorFeature.load();
        FociFeature.load();
        EnchantingFeature.load();
        ElementsFeature.load();
    }

    public static void research() {
        DarkQuartzFeature.research();
        FunnelFeature.research();
        GasFeature.research();
        MobilizerFeature.research();
        RepairerFeature.research();
        TransvectorFeature.research();
        FociFeature.research();
        EnchantingFeature.research();
        ElementsFeature.research();
    }

    public static void recipes() {
        DarkQuartzFeature.recipes();
        FunnelFeature.recipes();
        GasFeature.recipes();
        MobilizerFeature.recipes();
        RepairerFeature.recipes();
        TransvectorFeature.recipes();
        FociFeature.recipes();
        EnchantingFeature.recipes();
        ElementsFeature.recipes();
    }

    public static void aspects(AspectRegistry registry) {
    }

    public static void tests(IEventBus modEventBus) {
        DarkQuartzGameTests.register(modEventBus);
        DecorGameTests.register(modEventBus);
        FunnelGameTests.register(modEventBus);
        GasGameTests.register(modEventBus);
        MobilizerGameTests.register(modEventBus);
        RepairerGameTests.register(modEventBus);
        TransvectorGameTests.register(modEventBus);
        FociGameTests.register(modEventBus);
        EnchantingGameTests.register(modEventBus);
        ElementsGameTests.register(modEventBus);
    }
}
