package thaumictinkerer.kami.gateway;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class SkyPearlItem extends Item {
    public SkyPearlItem(Properties properties) {
        super(properties);
    }

    public static boolean isAttuned(ItemStack stack) {
        return stack.getItem() instanceof SkyPearlItem && GatewayFeature.LOCATION.has(stack);
    }

    public static @Nullable PearlLocation location(ItemStack stack) {
        return stack.getItem() instanceof SkyPearlItem ? GatewayFeature.LOCATION.get(stack) : null;
    }

    public static void attune(ItemStack stack, PearlLocation location) {
        GatewayFeature.LOCATION.set(stack, location);
    }

    public static void clear(ItemStack stack) {
        GatewayFeature.LOCATION.remove(stack);
    }

    private static void chime(ServerLevel level, double x, double y, double z) {
        level.playSound(null, x, y, z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isAttuned(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        if (level instanceof ServerLevel serverLevel && !isAttuned(stack) && level.getBlockState(context.getClickedPos()).is(GatewayFeature.WARP_GATE.get())) {
            attune(stack, new PearlLocation(serverLevel.dimension(), context.getClickedPos()));
            Player player = context.getPlayer();
            if (player != null) {
                chime(serverLevel, player.getX(), player.getY(), player.getZ());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && isAttuned(stack)) {
            if (level instanceof ServerLevel serverLevel) {
                chime(serverLevel, player.getX(), player.getY(), player.getZ());
                clear(stack);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
