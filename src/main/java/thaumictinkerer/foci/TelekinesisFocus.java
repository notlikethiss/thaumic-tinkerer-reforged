package thaumictinkerer.foci;

import java.util.List;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.fx.Fx;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.focus.FocusUpgradeType;

public class TelekinesisFocus extends TtFocus {
    private static final AspectList COST = new AspectList().add(Aspect.AIR, 5).add(Aspect.ENTROPY, 5);
    private static final int RANGE = 6;
    private static final double DISTANCE = RANGE - 1;

    public TelekinesisFocus(Properties properties) {
        super(properties, "focustelekinesis", true, false);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "TELEKINESIS";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x9C00BE;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focus) {
        return true;
    }

    @Override
    public FocusUpgradeType @Nullable [] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[0];
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        return InteractionResult.CONSUME;
    }

    public static Vec3 target(Player player) {
        Vec3 target = player.position().add(0.0, player.getBbHeight() / 2.0, 0.0);
        if (!player.isShiftKeyDown()) {
            target = target.add(player.getLookAngle().scale(DISTANCE));
        }
        return target.add(0.0, 0.5, 0.0);
    }

    public static void pull(ItemEntity entity, Vec3 target, float modifier) {
        Vec3 center = entity.position().add(0.0, entity.getBbHeight() / 2.0, 0.0);
        Vec3 difference = target.subtract(center);
        if (difference.length() > 1.0) {
            difference = difference.normalize();
        }
        entity.setDeltaMovement(difference.scale(modifier));
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        WandItem item = (WandItem) wand.getItem();
        Level level = player.level();
        Vec3 target = target(player);
        AABB area = new AABB(target.x - RANGE, target.y - RANGE, target.z - RANGE, target.x + RANGE, target.y + RANGE, target.z + RANGE);
        List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, area);
        if (entities.isEmpty() || !item.consumeAllVis(wand, player, COST.copy(), true, false)) {
            return;
        }
        for (ItemEntity entity : entities) {
            pull(entity, target, 0.3333F);
            if (level.isClientSide()) {
                Fx.get().sparkle(level, (float) entity.getX(), (float) entity.getY(), (float) entity.getZ(), 1.5F, 0, 0.0F);
            } else {
                entity.syncVelocity = true;
            }
        }
    }
}
