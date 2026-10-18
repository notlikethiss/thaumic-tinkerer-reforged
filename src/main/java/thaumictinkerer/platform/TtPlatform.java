package thaumictinkerer.platform;

import net.neoforged.bus.api.IEventBus;

public final class TtPlatform {
    private TtPlatform() {
    }

    public static void register(IEventBus bus) {
        thaumcraft.platform.Registration.armorMaterial(thaumictinkerer.gear.GearFeature.REVEALING_ARMOR);
        thaumcraft.platform.Registration.armorMaterial(thaumictinkerer.kami.KamiFeature.ICHORCLOTH_ARMOR);
        thaumcraft.platform.Registration.armorMaterial(thaumictinkerer.awakenedarmor.AwakenedArmorFeature.AWAKENED_ARMOR);
        TtStackData.register(bus);
        TtAttachments.register(bus);
        TtTransfer.register(bus);
    }
}
