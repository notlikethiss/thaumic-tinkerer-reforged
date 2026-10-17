package thaumictinkerer.crops;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModItems;
import thaumictinkerer.ThaumicTinkerer;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID)
public final class CropsEvents {
    public static final int HOE_DAMAGE = 25;

    private CropsEvents() {
    }

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = event.getItemStack();
        if (!(state.getBlock() instanceof InfusedGrainBlock crop) || !stack.is(ModItems.ELEMENTAL_HOE.get())) {
            return;
        }
        Player player = event.getEntity();
        if (level instanceof ServerLevel serverLevel) {
            if (!crop.isMaxAge(state)) {
                crop.growCrops(serverLevel, pos, state);
            }
            stack.hurtAndBreak(HOE_DAMAGE, player, event.getHand().asEquipmentSlot());
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        } else {
            Fx.get().blockSparkle(level, pos, 0, 2);
        }
    }
}
