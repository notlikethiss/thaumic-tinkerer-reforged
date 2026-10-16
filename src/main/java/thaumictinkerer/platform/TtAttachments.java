package thaumictinkerer.platform;

import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import thaumictinkerer.ThaumicTinkerer;

public final class TtAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ThaumicTinkerer.MODID);

    private TtAttachments() {
    }

    static <T> Supplier<AttachmentType<T>> register(String name, Supplier<AttachmentType<T>> factory) {
        return ATTACHMENTS.register(name, factory);
    }

    static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}
