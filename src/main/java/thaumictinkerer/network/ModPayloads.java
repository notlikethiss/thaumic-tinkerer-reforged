package thaumictinkerer.network;

import thaumictinkerer.platform.TtNetwork;

public final class ModPayloads {
    private ModPayloads() {
    }

    public static void register() {
        TtNetwork.toClient(FociEffectPayload.TYPE, FociEffectPayload.STREAM_CODEC);
        TtNetwork.toServer(EnchanterAddEnchantPayload.TYPE, EnchanterAddEnchantPayload.STREAM_CODEC, EnchanterAddEnchantPayload::handle);
        TtNetwork.toServer(EnchanterStartWorkingPayload.TYPE, EnchanterStartWorkingPayload.STREAM_CODEC, EnchanterStartWorkingPayload::handle);
    }
}
