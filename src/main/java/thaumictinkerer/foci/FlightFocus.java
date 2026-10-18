package thaumictinkerer.foci;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusUpgradeType;
import thaumictinkerer.Config;

public class FlightFocus extends TtFocus {
    private static final AspectList COST = new AspectList().add(Aspect.AIR, 15);

    public FlightFocus(Properties properties) {
        super(properties, "focusflight", true, false);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "FLIGHT";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x9EF2FF;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[]{FocusUpgradeType.TREASURE, FocusUpgradeType.POTENCY};
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        if (!Config.modFlight()) {
            return InteractionResult.PASS;
        }
        WandItem item = (WandItem) wand.getItem();
        boolean affordable = item.consumeAllVis(wand, player, COST.copy(), level instanceof ServerLevel, false);
        if (affordable) {
            Vec3 look = player.getLookAngle();
            double force = 1 / 1.5 * (1 + Wands.getFocusUpgrade(wand, FocusUpgradeType.POTENCY) * 0.2);
            if (level instanceof ServerLevel serverLevel) {
                player.setDeltaMovement(look.x * force, look.y * force, look.z * force);
                player.hurtMarked = true;
                player.resetFallDistance();
                if (player instanceof ServerPlayer serverPlayer) {
                    thaumcraft.platform.FlightGuard.reset(serverPlayer);
                }
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WIND.get(), SoundSource.PLAYERS, 0.4F, 1.0F);
            } else {
                for (int index = 0; index < 5; index++) {
                    Fx.get().smokeSpiral(level, player.getX(), player.getEyeY() - look.y * force, player.getZ(), 2.0F, (int) (Math.random() * 360), (int) player.getEyeY(), 0x9E2FF);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
