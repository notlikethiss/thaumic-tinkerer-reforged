package thaumictinkerer.kami.foci;

import java.util.List;
import java.util.function.IntSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusUpgradeType;
import thaumictinkerer.TtRarity;
import thaumictinkerer.foci.TtFocus;

public class XpDrainFocus extends TtFocus {
    public static final int XP_USE = 15;
    public static final int VIS_PER_TICK = 500;

    private static IntSupplier ticks = () -> 0;
    private static boolean clientTicks;

    private int lastGiven;

    public XpDrainFocus(Properties properties) {
        super(properties.rarity(TtRarity.kami()), "focusxpdrain", false, false);
    }

    public static void useClientTicks(IntSupplier source) {
        ticks = source;
        clientTicks = true;
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "XPDRAIN";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return clientTicks ? Mth.hsvToRgb(ticks.getAsInt() * 2 % 360 / 360.0F, 1.0F, 1.0F) : 0xFFFFFF;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return new AspectList();
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focus) {
        return true;
    }

    @Override
    public void appendCostText(ItemStack stack, java.util.List<Component> builder) {
        builder.add(Component.translatable("item.Focus.cost2"));
        builder.add(Component.literal(" §a").append(Component.translatable("ttinkerer.ttmisc.experience")).append("§r x " + XP_USE));
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[] {FocusUpgradeType.FRUGAL};
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        if (player.level().isClientSide()) {
            return;
        }
        List<Aspect> primals = Aspect.getPrimalAspects();
        int max = Wands.getMaxVis(wand);
        Aspect aspectToAdd = null;
        int takes = 0;
        while (aspectToAdd == null && takes < 7) {
            lastGiven = lastGiven == primals.size() - 1 ? 0 : lastGiven + 1;
            Aspect aspect = primals.get(lastGiven);
            if (Wands.getVis(wand, aspect) < max) {
                aspectToAdd = aspect;
            }
            takes++;
        }
        if (aspectToAdd != null && player.totalExperience >= XP_USE) {
            player.giveExperiencePoints(-XP_USE);
            Wands.storeVis(wand, aspectToAdd, Math.min(max, Wands.getVis(wand, aspectToAdd) + VIS_PER_TICK));
        }
    }
}
