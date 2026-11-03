package thaumictinkerer;

import thaumictinkerer.awakenedarmor.AwakenedArmorFeature;
import thaumcraft.aspect.AspectRegistry;
import thaumictinkerer.crops.CropsFeature;
import thaumictinkerer.attractor.AttractorFeature;
import thaumictinkerer.tablet.TabletFeature;
import thaumictinkerer.darkquartz.DarkQuartzFeature;
import thaumictinkerer.decor.DecorFeature;
import thaumictinkerer.funnel.FunnelFeature;
import thaumictinkerer.gear.GearFeature;
import thaumictinkerer.necromancy.NecromancyFeature;
import thaumictinkerer.gas.GasFeature;
import thaumictinkerer.mobilizer.MobilizerFeature;
import thaumictinkerer.repairer.RepairerFeature;
import thaumictinkerer.transvector.TransvectorFeature;
import thaumictinkerer.foci.FociFeature;
import thaumictinkerer.enchanting.EnchantingFeature;
import thaumictinkerer.elements.ElementsFeature;
import thaumictinkerer.kami.KamiFeature;
import thaumictinkerer.kami.tool.AwakenedToolsFeature;
import thaumictinkerer.kami.trinkets.TrinketsFeature;
import thaumictinkerer.kami.foci.KamiFociFeature;
import thaumictinkerer.kami.gateway.GatewayFeature;

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
        AwakenedToolsFeature.load();
        AwakenedArmorFeature.load();
        TrinketsFeature.load();
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
        AwakenedToolsFeature.research();
        AwakenedArmorFeature.research();
        TrinketsFeature.research();
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
        AwakenedToolsFeature.recipes();
        AwakenedArmorFeature.recipes();
        TrinketsFeature.recipes();
        GatewayFeature.recipes();
        KamiFociFeature.recipes();
    }

    public static void aspects(AspectRegistry registry) {
        NecromancyFeature.aspects(registry);
        KamiFeature.aspects(registry);
    }
}
