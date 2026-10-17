package thaumictinkerer.kami.foci;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.focus.FocusUpgradeType;
import thaumictinkerer.foci.TtFocus;
import thaumictinkerer.kami.gateway.GatewayTeleport;
import thaumictinkerer.kami.gateway.PearlLocation;
import thaumictinkerer.kami.gateway.SkyPearlItem;

public class RecallFocus extends TtFocus {
    public static final int CHANNEL_TICKS = 60;
    private static final AspectList COST = new AspectList().add(Aspect.AIR, 4000).add(Aspect.EARTH, 4000).add(Aspect.ORDER, 4000);

    public RecallFocus(Properties properties) {
        super(properties, "focusrecall", false, true);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "RECALL";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x9CF8FF;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Override
    public FocusUpgradeType @Nullable [] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[] {FocusUpgradeType.FRUGAL};
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        return InteractionResult.CONSUME;
    }

    public static @Nullable PearlLocation boundPearl(Player player) {
        for (int slot = 0; slot < 9; slot++) {
            PearlLocation location = SkyPearlItem.location(player.getInventory().getItem(slot));
            if (location != null) {
                return location;
            }
        }
        return null;
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        WandItem item = (WandItem) wand.getItem();
        if (item.getUseDuration(wand, player) - count <= CHANNEL_TICKS) {
            return;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            PearlLocation location = boundPearl(serverPlayer);
            if (location != null && location.dimension().equals(serverPlayer.level().dimension())
                && item.consumeAllVis(wand, serverPlayer, COST.copy(), false, false)
                && GatewayTeleport.teleport(serverPlayer, location)) {
                item.consumeAllVis(wand, serverPlayer, COST.copy(), true, false);
            }
        }
        player.stopUsingItem();
    }
}
