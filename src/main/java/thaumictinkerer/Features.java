package thaumictinkerer;

import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.AspectRegistry;
import thaumictinkerer.crops.CropsFeature;
import thaumictinkerer.test.CropsGameTests;
import thaumictinkerer.attractor.AttractorFeature;
import thaumictinkerer.tablet.TabletFeature;
import thaumictinkerer.test.AttractorGameTests;
import thaumictinkerer.test.TabletGameTests;
import thaumictinkerer.darkquartz.DarkQuartzFeature;
import thaumictinkerer.decor.DecorFeature;
import thaumictinkerer.funnel.FunnelFeature;
import thaumictinkerer.gear.GearFeature;
import thaumictinkerer.necromancy.NecromancyFeature;
import thaumictinkerer.test.NecromancyGameTests;
import thaumictinkerer.gas.GasFeature;
import thaumictinkerer.mobilizer.MobilizerFeature;
import thaumictinkerer.repairer.RepairerFeature;
import thaumictinkerer.test.DarkQuartzGameTests;
import thaumictinkerer.test.DecorGameTests;
import thaumictinkerer.test.FunnelGameTests;
import thaumictinkerer.test.GearGameTests;
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
import thaumictinkerer.kami.KamiFeature;
import thaumictinkerer.test.KamiGameTests;
import thaumictinkerer.test.ElementsGameTests;
import thaumictinkerer.kami.foci.KamiFociFeature;
import thaumictinkerer.kami.gateway.GatewayFeature;
import thaumictinkerer.test.GatewayGameTests;
import thaumictinkerer.test.KamiFociGameTests;

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
        CropsFeature.load();
        ElementsFeature.load();
        AttractorFeature.load();
        TabletFeature.load();
        GearFeature.load();
        NecromancyFeature.load();
        KamiFeature.load();
        GatewayFeature.load();
        KamiFociFeature.load();
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
        AttractorFeature.research();
        TabletFeature.research();
        GearFeature.research();
        NecromancyFeature.research();
        KamiFeature.research();
        GatewayFeature.research();
        KamiFociFeature.research();
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
        CropsFeature.recipes();
        ElementsFeature.recipes();
        AttractorFeature.recipes();
        TabletFeature.recipes();
        GearFeature.recipes();
        NecromancyFeature.recipes();
        KamiFeature.recipes();
        GatewayFeature.recipes();
        KamiFociFeature.recipes();
    }

    public static void aspects(AspectRegistry registry) {
        NecromancyFeature.aspects(registry);
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
        CropsGameTests.register(modEventBus);
        ElementsGameTests.register(modEventBus);
        AttractorGameTests.register(modEventBus);
        TabletGameTests.register(modEventBus);
        GearGameTests.register(modEventBus);
        NecromancyGameTests.register(modEventBus);
        KamiGameTests.register(modEventBus);
        GatewayGameTests.register(modEventBus);
        KamiFociGameTests.register(modEventBus);
    }
}
