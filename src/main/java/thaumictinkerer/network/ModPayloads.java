package thaumictinkerer.network;

import thaumictinkerer.platform.TtNetwork;

public final class ModPayloads {
    private ModPayloads() {
    }

    public static void register() {
        TtNetwork.toServer(MobMagnetButtonPayload.TYPE, MobMagnetButtonPayload.STREAM_CODEC, MobMagnetButtonPayload::handle);
        TtNetwork.toServer(TabletButtonPayload.TYPE, TabletButtonPayload.STREAM_CODEC, TabletButtonPayload::handle);
        TtNetwork.toClient(FociEffectPayload.TYPE, FociEffectPayload.STREAM_CODEC);
        TtNetwork.toClient(CropFxPayload.TYPE, CropFxPayload.STREAM_CODEC);
        TtNetwork.toClient(ElementsFxPayload.TYPE, ElementsFxPayload.STREAM_CODEC);
        TtNetwork.toServer(EnchanterAddEnchantPayload.TYPE, EnchanterAddEnchantPayload.STREAM_CODEC, EnchanterAddEnchantPayload::handle);
        TtNetwork.toServer(EnchanterStartWorkingPayload.TYPE, EnchanterStartWorkingPayload.STREAM_CODEC, EnchanterStartWorkingPayload::handle);
        TtNetwork.toServer(ToggleArmorPayload.TYPE, ToggleArmorPayload.STREAM_CODEC, ToggleArmorPayload::handle);
        TtNetwork.toClient(ArmorStatusPayload.TYPE, ArmorStatusPayload.STREAM_CODEC);
        TtNetwork.toServer(WarpGateButtonPayload.TYPE, WarpGateButtonPayload.STREAM_CODEC, WarpGateButtonPayload::handle);
        TtNetwork.toServer(WarpGateTeleportPayload.TYPE, WarpGateTeleportPayload.STREAM_CODEC, WarpGateTeleportPayload::handle);
        TtNetwork.toClient(GatewayFxPayload.TYPE, GatewayFxPayload.STREAM_CODEC);
        TtNetwork.toClient(ShadowbeamPayload.TYPE, ShadowbeamPayload.STREAM_CODEC);
    }
}
