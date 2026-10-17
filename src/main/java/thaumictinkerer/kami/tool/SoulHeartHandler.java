package thaumictinkerer.kami.tool;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.platform.TtDataKey;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID)
public final class SoulHeartHandler {
    public static final int MAX_HP = 20;

    public static final TtDataKey<Integer> SOUL_HEARTS = TtDataKey.builder("soul_hearts", () -> 0)
        .codec(Codec.INT)
        .syncToSelf(ByteBufCodecs.VAR_INT)
        .build();

    private SoulHeartHandler() {
    }

    public static int getHP(Player player) {
        return SOUL_HEARTS.get(player);
    }

    public static void setHP(Player player, int hp) {
        int clamped = Math.max(0, Math.min(MAX_HP, hp));
        if (clamped != getHP(player)) {
            SOUL_HEARTS.set(player, clamped);
        }
    }

    public static void addHearts(Player player) {
        addHP(player, 1);
    }

    public static boolean addHP(Player player, int hp) {
        int current = getHP(player);
        if (current >= MAX_HP) {
            return false;
        }
        setHP(player, current + hp);
        return true;
    }

    public static int removeHP(Player player, int hp) {
        int remaining = getHP(player) - hp;
        setHP(player, remaining);
        return Math.max(0, -remaining);
    }

    @SubscribeEvent
    static void onPlayerDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        float damage = event.getNewDamage();
        int absorbable = Math.min(getHP(player), (int) damage);
        if (damage <= 0.0F || absorbable <= 0) {
            return;
        }
        removeHP(player, absorbable);
        event.setNewDamage(damage - absorbable);
    }
}
