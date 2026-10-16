package thaumictinkerer.network;

import thaumictinkerer.platform.TtNetwork;

public final class ModPayloads {
    private ModPayloads() {
    }

    public static void register() {
        TtNetwork.toServer(MobMagnetButtonPayload.TYPE, MobMagnetButtonPayload.STREAM_CODEC, MobMagnetButtonPayload::handle);
        TtNetwork.toServer(TabletButtonPayload.TYPE, TabletButtonPayload.STREAM_CODEC, TabletButtonPayload::handle);
    }
}
