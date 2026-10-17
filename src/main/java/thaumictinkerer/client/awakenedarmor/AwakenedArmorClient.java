package thaumictinkerer.client.awakenedarmor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.awakenedarmor.AwakenedArmor;
import thaumictinkerer.awakenedarmor.AwakenedArmorFeature;
import thaumictinkerer.client.platform.TtClientNetwork;
import thaumictinkerer.network.ArmorStatusPayload;
import thaumictinkerer.network.ToggleArmorPayload;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class AwakenedArmorClient {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(ThaumicTinkerer.id("main"));
    public static final KeyMapping TOGGLE = new KeyMapping("ttinkerer.ttmisc.toggleArmor", InputConstants.KEY_U, CATEGORY);

    private AwakenedArmorClient() {
    }

    public static void handle(ArmorStatusPayload payload, Player player) {
        AwakenedArmorFeature.ARMOR_ENABLED.set(player, payload.enabled());
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() == null) {
            minecraft.gui.hud.setOverlayMessage(Component.translatable(payload.enabled() ? "ttinkerer.ttmisc.enableAllArmor" : "ttinkerer.ttmisc.disableAllArmor"), false);
        }
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(TOGGLE);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        while (TOGGLE.consumeClick()) {
            if (player != null && minecraft.gui.screen() == null && AwakenedArmor.wearsAny(player)) {
                TtClientNetwork.sendToServer(new ToggleArmorPayload(!AwakenedArmor.isEnabled(player)));
            }
        }
    }

    @SubscribeEvent
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<AbstractClientPlayer> renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                renderer.addLayer(new WingsLayer(renderer));
            }
        }
    }
}
