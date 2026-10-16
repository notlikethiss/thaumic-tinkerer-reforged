package thaumictinkerer.foci;

import java.util.List;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
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

public class DeflectFocus extends TtFocus {
    private static final AspectList COST = new AspectList().add(Aspect.ORDER, 8).add(Aspect.AIR, 4);

    public DeflectFocus(Properties properties) {
        super(properties, "focusdeflect", false, false);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "DEFLECT";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0xFFFFFF;
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focus) {
        return true;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        float modifier = getUpgradeLevel(focus, FocusUpgradeType.FRUGAL) * 0.10F;
        AspectList cost = COST.copy();
        for (Aspect aspect : COST.getAspects()) {
            cost.remove(aspect, Math.round(cost.getAmount(aspect) * modifier));
        }
        return cost;
    }

    @Override
    public FocusUpgradeType @Nullable [] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[]{FocusUpgradeType.FRUGAL};
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        return InteractionResult.CONSUME;
    }

    public static boolean isDeflectable(Projectile projectile) {
        if (projectile instanceof ThrownExperienceBottle) {
            return false;
        }
        return projectile instanceof AbstractArrow || projectile instanceof ThrowableProjectile;
    }

    public static List<Projectile> findProjectiles(Player player) {
        double x = player.getX();
        double y = player.getEyeY();
        double z = player.getZ();
        AABB area = new AABB(x - 4, y - 4, z - 4, x + 3, y + 3, z + 3);
        return player.level().getEntitiesOfClass(
            Projectile.class,
            area,
            projectile -> isDeflectable(projectile) && projectile.getOwner() != player
        );
    }

    public static void protect(Player player) {
        Level level = player.level();
        Vec3 origin = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        for (Projectile projectile : findProjectiles(player)) {
            if (level.isClientSide()) {
                for (int index = 0; index < 6; index++) {
                    Fx.get().sparkle(level, (float) projectile.getX(), (float) projectile.getY(), (float) projectile.getZ(), 1.5F, 6, 0.0F);
                }
                continue;
            }
            Vec3 motion = projectile.getDeltaMovement();
            if (motion.lengthSqr() < 1.0E-7) {
                continue;
            }
            Vec3 shift = motion.normalize().scale(projectile.position().distanceTo(origin) * 2);
            projectile.setPos(projectile.position().add(shift));
        }
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        WandItem item = (WandItem) wand.getItem();
        if (item.consumeAllVis(wand, player, COST.copy(), true, false)) {
            protect(player);
        }
    }
}
