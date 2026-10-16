package thaumictinkerer.enchanting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.registry.ModSounds;
import thaumictinkerer.ThaumicTinkerer;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID)
public final class TtEnchantmentEvents {
    private static final double SLOW_FALL_MIN_MOTION = -0.0784000015258789;
    private static final double SLOW_FALL_MIN_DISTANCE = 2.9;

    private TtEnchantmentEvents() {
    }

    public record StrikeState(int lastTarget) {
        public static final StrikeState NONE = new StrikeState(0);
    }

    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            event.setNewDamage(modifyDamage(attacker, event.getEntity(), event.getNewDamage()));
        }
    }

    public static float modifyDamage(LivingEntity attacker, LivingEntity target, float damage) {
        ItemStack held = attacker.getMainHandItem();
        if (held.isEmpty()) {
            return damage;
        }
        var access = attacker.level().registryAccess();
        float amount = damage;
        if (attacker instanceof Player player) {
            int pounce = TtEnchantments.level(access, player.getItemBySlot(EquipmentSlot.LEGS), TtEnchantments.POUNCE);
            if (pounce > 0) {
                BlockPos below = BlockPos.containing(player.getX(), Mth.floor(player.getEyeY()) - 1, player.getZ());
                if (player.level().getBlockState(below).isAir()) {
                    amount *= 1 + 0.25 * pounce;
                }
            }
        }
        int finalStrike = TtEnchantments.level(access, held, TtEnchantments.FINAL_STRIKE);
        if (finalStrike > 0 && attacker.getRandom().nextInt(20 - finalStrike) == 0) {
            amount *= 3;
        }
        int valiance = TtEnchantments.level(access, held, TtEnchantments.VALIANCE);
        if (valiance > 0 && attacker.getHealth() / attacker.getMaxHealth() < 0.5F) {
            amount *= 1 + 0.1 * valiance;
        }
        int focused = TtEnchantments.level(access, held, TtEnchantments.FOCUSED_STRIKE);
        int dispersed = TtEnchantments.level(access, held, TtEnchantments.DISPERSED_STRIKE);
        if (focused > 0 || dispersed > 0) {
            int successive = EnchantingFeature.STRIKES.get(attacker).lastTarget() == target.getId() ? 1 : 0;
            if (focused > 0) {
                amount /= 2;
                amount += (float) (0.5 * successive * amount * focused);
            }
            if (dispersed > 0) {
                amount *= 1 + (successive / 5);
                amount /= 1 + successive * 2;
            }
            EnchantingFeature.STRIKES.set(attacker, new StrikeState(target.getId()));
        }
        int vampirism = TtEnchantments.level(access, held, TtEnchantments.VAMPIRISM);
        if (vampirism > 0) {
            attacker.heal(vampirism);
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.ZAP.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
        }
        return amount;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onPlayerTick(PlayerTickEvent.Pre event) {
        applySlowFall(event.getEntity());
    }

    public static void applySlowFall(Player player) {
        int slowFall = TtEnchantments.maxArmorLevel(player, TtEnchantments.SLOW_FALL);
        if (slowFall > 0 && !player.isShiftKeyDown() && player.getKnownMovement().y < SLOW_FALL_MIN_MOTION && player.fallDistance >= SLOW_FALL_MIN_DISTANCE) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, motion.y / (1 + slowFall * 0.33F), motion.z);
            player.fallDistance = Math.max(SLOW_FALL_MIN_DISTANCE, player.fallDistance - slowFall / 3.0F);
            if (player.level().isClientSide()) {
                player.level().addParticle(ParticleTypes.CLOUD, player.getX() + 0.25, player.getY() - 1, player.getZ() + 0.25, -motion.x, motion.y, -motion.z);
            }
        }
    }

    @SubscribeEvent
    static void onUseTick(LivingEntityUseItemEvent.Tick event) {
        event.setDuration(quickDrawDuration(event.getEntity(), event.getItem(), event.getDuration()));
    }

    public static int quickDrawDuration(LivingEntity entity, ItemStack using, int remaining) {
        if (!(entity instanceof Player player) || using.getUseAnimation() != ItemUseAnimation.BOW) {
            return remaining;
        }
        int quickDraw = TtEnchantments.level(player.level().registryAccess(), player.getMainHandItem(), TtEnchantments.QUICK_DRAW);
        if (quickDraw > 0 && (using.getUseDuration(player) - remaining) % (6 - quickDraw) == 0) {
            return remaining - 1;
        }
        return remaining;
    }

    @SubscribeEvent
    static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyAscentBoost(player);
        }
    }

    public static void applyAscentBoost(Player player) {
        int boost = TtEnchantments.maxArmorLevel(player, TtEnchantments.ASCENT_BOOST);
        if (boost >= 1 && !player.isShiftKeyDown()) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, motion.y * (boost + 2) / 2.0, motion.z);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyShockwave(player, event.getDistance());
        }
    }

    public static void applyShockwave(Player player, double distance) {
        int shockwave = TtEnchantments.level(player.level().registryAccess(), player.getItemBySlot(EquipmentSlot.FEET), TtEnchantments.SHOCKWAVE);
        if (shockwave > 0 && distance > 3 && player.level() instanceof ServerLevel level) {
            AABB area = new AABB(player.getX() - 10, player.getY() - 10, player.getZ() - 10, player.getX() + 10, player.getY() + 10, player.getZ() + 10);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
                if (target != player) {
                    target.hurtServer(level, level.damageSources().fall(), (float) (0.1F * shockwave * distance));
                }
            }
        }
    }

    @SubscribeEvent
    static void onBreak(BreakBlockEvent event) {
        Player player = event.getPlayer();
        if (TtEnchantments.level(player.level().registryAccess(), player.getMainHandItem(), TtEnchantments.TUNNEL) > 0) {
            EnchantingFeature.TUNNEL_DIRECTION.set(player, player.getYRot());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return;
        }
        BlockPos pos = event.getPosition().orElse(player.blockPosition());
        float speed = event.getNewSpeed();
        Float result = breakSpeed(player, held, event.getState(), pos, speed);
        if (result == null) {
            event.setCanceled(true);
        } else {
            event.setNewSpeed(result);
        }
    }

    public static Float breakSpeed(Player player, ItemStack held, BlockState state, BlockPos pos, float speed) {
        var access = player.level().registryAccess();
        float newSpeed = speed;
        float hardness = state.getDestroySpeed(player.level(), pos);
        int shatter = TtEnchantments.level(access, held, TtEnchantments.SHATTER);
        if (shatter > 0) {
            if (hardness > 20.0F) {
                newSpeed *= 3 * shatter;
            } else {
                newSpeed *= 0.8F;
            }
        }
        int tunnel = TtEnchantments.level(access, held, TtEnchantments.TUNNEL);
        if (tunnel > 0) {
            float oldDirection = EnchantingFeature.TUNNEL_DIRECTION.get(player);
            if (!Float.isNaN(oldDirection)) {
                if (Math.abs(oldDirection - player.getYRot()) < 50) {
                    newSpeed *= (float) (1 + 0.2 * tunnel);
                } else {
                    newSpeed *= 0.3F;
                }
            }
        }
        int desintegrate = TtEnchantments.level(access, held, TtEnchantments.DESINTEGRATE);
        int autoSmelt = TtEnchantments.level(access, held, TtEnchantments.AUTO_SMELT);
        boolean desintegrateApplies = desintegrate > 0 && hardness <= 1.5F;
        boolean autoSmeltApplies = autoSmelt > 0 && state.is(EnchantingFeature.FLAMING_TOUCH_BLOCKS);
        if (desintegrateApplies || autoSmeltApplies) {
            if (!player.level().isClientSide()) {
                held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
            return Float.MAX_VALUE;
        }
        if (desintegrate > 0 || autoSmelt > 0) {
            return null;
        }
        return newSpeed;
    }

}
