package thaumictinkerer.foci;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusUpgradeType;

public class HealFocus extends TtFocus {
    private static final AspectList COST = new AspectList().add(Aspect.EARTH, 45).add(Aspect.WATER, 45);
    private static final Map<String, Integer> PROGRESS = new HashMap<>();

    public HealFocus(Properties properties) {
        super(properties, "focusheal", false, true);
    }

    private static String key(Player player) {
        return (player.level().isClientSide() ? "R" : "S") + player.getUUID();
    }

    public static int threshold(int potency) {
        return 30 - potency * 10 / 3;
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "HEAL";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0xFD00D6;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Override
    public FocusUpgradeType @Nullable [] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[]{FocusUpgradeType.TREASURE, FocusUpgradeType.POTENCY};
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        WandItem item = (WandItem) wand.getItem();
        if (!item.consumeAllVis(wand, player, COST.copy(), false, false) || !player.isHurt()) {
            return;
        }
        Level level = player.level();
        int potency = Wands.getFocusUpgrade(wand, FocusUpgradeType.POTENCY);
        String key = key(player);
        int progress = PROGRESS.getOrDefault(key, 0) + 1;
        PROGRESS.put(key, progress);
        if (level.isClientSide()) {
            Fx.get().sparkle(
                level,
                (float) player.getX() + level.getRandom().nextFloat() - 0.5F,
                (float) player.getY() + level.getRandom().nextFloat(),
                (float) player.getZ() + level.getRandom().nextFloat() - 0.5F,
                1.5F,
                0,
                0.0F
            );
        }
        if (progress >= threshold(potency)) {
            PROGRESS.put(key, 0);
            if (level instanceof ServerLevel serverLevel && item.consumeAllVis(wand, player, COST.copy(), true, false)) {
                player.heal(1.0F);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WAND.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }
    }

    @Override
    public void onPlayerStoppedUsingFocus(ItemStack wand, Level level, Player player, int count) {
        PROGRESS.put(key(player), 0);
    }
}
