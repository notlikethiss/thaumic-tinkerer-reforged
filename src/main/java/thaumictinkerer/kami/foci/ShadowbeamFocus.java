package thaumictinkerer.kami.foci;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusUpgradeType;
import thaumictinkerer.TtRarity;
import thaumictinkerer.foci.TtFocus;
import thaumictinkerer.network.ShadowbeamPayload;
import thaumictinkerer.platform.TtNetwork;

public class ShadowbeamFocus extends TtFocus {
    private static final AspectList COST = new AspectList().add(Aspect.ORDER, 25).add(Aspect.ENTROPY, 25).add(Aspect.AIR, 15);

    public ShadowbeamFocus(Properties properties) {
        super(properties.rarity(TtRarity.kami()), "focusshadowbeam", true, false);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "SHADOWBEAM";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x4B0053;
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
        return new FocusUpgradeType[] {FocusUpgradeType.FRUGAL};
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        WandItem item = (WandItem) wand.getItem();
        if (item.consumeAllVis(wand, player, COST.copy(), true, false)) {
            if (level.getRandom().nextInt(10) == 0) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.BRAIN.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
            }
            Beam beam = Beam.create(level, player, 0);
            List<Vec3> path = beam.travel();
            TtNetwork.sendToNear(level, null, player.getX(), player.getY(), player.getZ(), Beam.LENGTH * Beam.STEP + 16.0, new ShadowbeamPayload(path.size() > ShadowbeamPayload.MAX_POINTS ? path.subList(0, ShadowbeamPayload.MAX_POINTS) : path));
        }
    }

    public static class Beam extends ThrowableProjectile {
        public static final int INITIAL_OFFSET = 2;
        public static final int LENGTH = 298;
        public static final int MAX_TICKS = INITIAL_OFFSET + LENGTH;
        public static final double STEP = 0.15;
        public static final int BASE_DAMAGE = 8;

        private int potency;
        private Vec3 movement = Vec3.ZERO;
        private final List<Vec3> path = new ArrayList<>();

        public Beam(EntityType<? extends Beam> type, Level level) {
            super(type, level);
        }

        public static Beam create(ServerLevel level, LivingEntity shooter, int potency) {
            Beam beam = new Beam(KamiFociFeature.BEAM.get(), level);
            beam.potency = potency;
            beam.setOwner(shooter);
            float yaw = shooter.getYRot();
            float pitch = shooter.getXRot();
            double x = shooter.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F;
            double y = shooter.getEyeY() - 0.1F;
            double z = shooter.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F;
            beam.snapTo(x, y, z, yaw, pitch);
            Vec3 direction = new Vec3(
                -Mth.sin(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI),
                -Mth.sin(pitch / 180.0F * (float) Math.PI),
                Mth.cos(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI)
            ).normalize();
            beam.movement = direction.scale(STEP);
            beam.setDeltaMovement(beam.movement);
            return beam;
        }

        public Vec3 movement() {
            return movement;
        }

        public int potency() {
            return potency;
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {
        }

        @Override
        protected double getDefaultGravity() {
            return 0.0;
        }

        public List<Vec3> travel() {
            path.clear();
            path.add(position());
            for (int tick = 0; tick < MAX_TICKS; tick++) {
                step();
            }
            path.add(position());
            discard();
            return List.copyOf(path);
        }

        private boolean canHit(Entity target) {
            return target != getOwner() && target.canBeHitByProjectile();
        }

        private void step() {
            if (!(level() instanceof ServerLevel serverLevel)) {
                return;
            }
            Vec3 from = position();
            Vec3 to = from.add(movement);
            BlockHitResult blockHit = serverLevel.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            Vec3 limit = blockHit.getType() == HitResult.Type.MISS ? to : blockHit.getLocation();
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(serverLevel, this, from, limit, getBoundingBox().expandTowards(movement).inflate(1.0), this::canHit, 0.3F);
            if (entityHit != null) {
                damage(serverLevel, entityHit.getEntity());
            }
            if (blockHit.getType() != HitResult.Type.MISS) {
                path.add(from);
                Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i());
                movement = normal.scale(-2.0 * movement.dot(normal)).add(movement);
                setDeltaMovement(movement);
            }
            setPos(from.add(movement));
        }

        private void damage(ServerLevel level, Entity target) {
            if (getOwner() instanceof Player player && (level.isPvpAllowed() || !(target instanceof Player))) {
                target.hurtServer(level, level.damageSources().playerAttack(player), BASE_DAMAGE + potency);
            }
        }
    }
}
