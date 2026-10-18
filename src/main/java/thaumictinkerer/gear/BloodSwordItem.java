package thaumictinkerer.gear;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import thaumcraft.item.gear.Repairable;

public class BloodSwordItem extends Item implements Repairable {
    public BloodSwordItem(Properties properties) {
        super(properties);
    }

    public static boolean isActivated(ItemStack stack) {
        return GearFeature.ACTIVE.has(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && !level.isClientSide()) {
            boolean activated = isActivated(stack);
            GearFeature.ACTIVE.set(stack, activated ? null : Boolean.TRUE);
            player.sendSystemMessage(Component.translatable(activated ? "ttinkerer.ttmisc.bloodSword.deactivateEssentiaHarvest" : "ttinkerer.ttmisc.bloodSword.activateEssentiaHarvest"));
        }
        player.startUsingItem(hand);
        return net.minecraft.world.InteractionResultHolder.consume(player.getItemInHand(hand));
    }
}
