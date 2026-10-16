package thaumictinkerer.network;

import thaumictinkerer.platform.TtNetwork;

public final class ModPayloads {
    private ModPayloads() {
    }

    public static void register() {
        TtNetwork.toClient(FociEffectPayload.TYPE, FociEffectPayload.STREAM_CODEC);
    }
}
