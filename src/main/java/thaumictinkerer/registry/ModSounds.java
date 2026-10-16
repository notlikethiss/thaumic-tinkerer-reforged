package thaumictinkerer.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumictinkerer.ThaumicTinkerer;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, ThaumicTinkerer.MODID);

    private ModSounds() {
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
