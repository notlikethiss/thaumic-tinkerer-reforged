package thaumictinkerer.client;

import thaumictinkerer.client.elements.ElementsClientEffects;
import thaumictinkerer.client.foci.FociClientEffects;
import thaumictinkerer.client.platform.TtClientNetwork;
import thaumictinkerer.network.ElementsFxPayload;
import thaumictinkerer.network.FociEffectPayload;

public final class ClientPayloadHandlers {
    private ClientPayloadHandlers() {
    }

    public static void register() {
        TtClientNetwork.handle(FociEffectPayload.TYPE, FociClientEffects::handle);
        TtClientNetwork.handle(ElementsFxPayload.TYPE, ElementsClientEffects::handle);
    }
}
