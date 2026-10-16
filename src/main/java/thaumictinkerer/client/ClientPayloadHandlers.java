package thaumictinkerer.client;

import thaumictinkerer.client.foci.FociClientEffects;
import thaumictinkerer.client.platform.TtClientNetwork;
import thaumictinkerer.network.FociEffectPayload;

public final class ClientPayloadHandlers {
    private ClientPayloadHandlers() {
    }

    public static void register() {
        TtClientNetwork.handle(FociEffectPayload.TYPE, FociClientEffects::handle);
    }
}
