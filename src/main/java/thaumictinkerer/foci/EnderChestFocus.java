package thaumictinkerer.foci;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.WandItem;

public class EnderChestFocus extends TtFocus {
    public static final AspectList COST = new AspectList().add(Aspect.ENTROPY, 100).add(Aspect.ORDER, 100);

    public EnderChestFocus(Properties properties) {
        super(properties, "focusenderchest", false, true);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "ENDERCHEST";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x132223;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        WandItem item = (WandItem) wand.getItem();
        if (item.consumeAllVis(wand, player, COST.copy(), true, false)) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> ChestMenu.threeRows(containerId, inventory, menuPlayer.getEnderChestInventory()),
                Component.translatable("container.enderchest")
            ));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }
}
