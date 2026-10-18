package thaumictinkerer.client.awakenedarmor;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.resources.PlayerSkin;
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
    private static final String CATEGORY = "key.categories.thaumictinkerer";
    public static final KeyMapping TOGGLE = new KeyMapping("ttinkerer.ttmisc.toggleArmor", InputConstants.KEY_U, CATEGORY);

    private AwakenedArmorClient() {
    }

    public static void handle(ArmorStatusPayload payload, Player player) {
        AwakenedArmorFeature.ARMOR_ENABLED.set(player, payload.enabled());
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null) {
            minecraft.gui.setOverlayMessage(Component.translatable(payload.enabled() ? "ttinkerer.ttmisc.enableAllArmor" : "ttinkerer.ttmisc.disableAllArmor"), false);
        }
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        while (TOGGLE.consumeClick()) {
            if (player != null && minecraft.screen == null && AwakenedArmor.wearsAny(player)) {
                TtClientNetwork.sendToServer(new ToggleArmorPayload(!AwakenedArmor.isEnabled(player)));
            }
        }
    }

    @SubscribeEvent
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new WingsLayer(renderer));
            }
        }
    }
}
