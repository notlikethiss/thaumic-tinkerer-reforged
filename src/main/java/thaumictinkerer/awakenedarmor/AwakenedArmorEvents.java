package thaumictinkerer.awakenedarmor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumictinkerer.Config;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.foci.DeflectFocus;
import thaumictinkerer.gas.BrightNitorItem;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID)
public final class AwakenedArmorEvents {
    public static final Identifier STEP_MODIFIER = ThaumicTinkerer.id("awakened_boots_step");
    public static final double STEP_BONUS = 0.4;
    public static final int FIRE_RESISTANCE_AMPLIFIER = 10;
    public static final int HASTE_AMPLIFIER = 1;
    public static final int HASTE_DURATION = 2;
    public static final float JUMP_BOOST = 0.3F;
    public static final int LIGHT_STEPS = 5;

    private AwakenedArmorEvents() {
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Pre event) {
        tick(event.getEntity());
    }

    public static void tick(Player player) {
        updateStepHeight(player);
        if (player instanceof ServerPlayer serverPlayer) {
            updateFlight(serverPlayer);
        }
        if (AwakenedArmor.isActive(player, EquipmentSlot.CHEST, AwakenedArmorFeature.ROBES.get())) {
            DeflectFocus.protect(player);
        }
        if (player.level().isClientSide()) {
            tickBootsMovement(player);
            return;
        }
        tickCowl(player);
        tickLeggings(player);
        tickBoots(player);
    }

    private static void refresh(Player player, Holder<MobEffect> effect, int duration, int amplifier) {
        MobEffectInstance existing = player.getEffect(effect);
        if (existing == null || existing.getDuration() <= duration && existing.getAmplifier() <= amplifier) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false));
        }
    }

    private static void tickCowl(Player player) {
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(AwakenedArmorFeature.COWL.get())) {
            return;
        }
        if (AwakenedArmor.isActive(player, EquipmentSlot.HEAD, AwakenedArmorFeature.COWL.get())) {
            if (player.isEyeInFluid(FluidTags.WATER)) {
                player.setAirSupply(300);
                refresh(player, MobEffects.NIGHT_VISION, AwakenedArmorFeature.EFFECT_DURATION, 0);
            }
            if (player.isEyeInFluid(FluidTags.LAVA)) {
                player.setAirSupply(300);
                refresh(player, MobEffects.BLINDNESS, AwakenedArmorFeature.EFFECT_DURATION, 0);
            }
        }
        int food = player.getFoodData().getFoodLevel();
        if (food > 0 && food < 18 && player.isHurt() && player.tickCount % 80 == 0) {
            player.heal(1.0F);
        }
    }

    private static void tickLeggings(Player player) {
        if (!AwakenedArmor.isActive(player, EquipmentSlot.LEGS, AwakenedArmorFeature.LEGGINGS.get())) {
            return;
        }
        refresh(player, MobEffects.FIRE_RESISTANCE, AwakenedArmorFeature.EFFECT_DURATION, FIRE_RESISTANCE_AMPLIFIER);
        Level level = player.level();
        int x = Mth.floor(player.getX());
        int y = (int) player.getY() + 1;
        int z = Mth.floor(player.getZ());
        BrightNitorItem.placeNitorGas(level, new BlockPos(x, y, z), true);
        float yaw = Mth.wrapDegrees(player.getYRot() + 90.0F) * (float) Math.PI / 180.0F;
        double lookX = Math.cos(yaw);
        double lookZ = Math.sin(yaw);
        double length = Math.sqrt(lookX * lookX + lookZ * lookZ);
        lookX /= length;
        lookZ /= length;
        double stepX = lookX;
        double stepZ = lookZ;
        for (int index = 0; index < LIGHT_STEPS; index++) {
            stepX += lookX;
            stepZ += lookZ;
            BrightNitorItem.placeNitorGas(level, new BlockPos(x + (int) stepX, y, z + (int) stepZ), false);
        }
    }

    private static void tickBoots(Player player) {
        if (!AwakenedArmor.isActive(player, EquipmentSlot.FEET, AwakenedArmorFeature.BOOTS.get())) {
            return;
        }
        MobEffectInstance existing = player.getEffect(MobEffects.HASTE);
        if (existing == null || existing.getDuration() <= 1 && existing.getAmplifier() <= HASTE_AMPLIFIER) {
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, HASTE_DURATION, HASTE_AMPLIFIER, true, false));
        }
        player.resetFallDistance();
        BlockPos below = new BlockPos(Mth.floor(player.getX()), Mth.floor(player.getY()) - 1, Mth.floor(player.getZ()));
        if (player.level().getBlockState(below).is(Blocks.DIRT)) {
            player.level().setBlock(below, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
        }
    }

    private static void tickBootsMovement(Player player) {
        if (!player.isLocalPlayer() || !AwakenedArmor.isActive(player, EquipmentSlot.FEET, AwakenedArmorFeature.BOOTS.get())) {
            return;
        }
        boolean flying = player.getAbilities().flying;
        if ((player.onGround() || flying) && player.zza > 0.0F) {
            player.moveRelative(flying ? 0.075F : 0.15F, new Vec3(0.0, 0.0, 1.0));
        }
        if (!player.onGround() && !flying) {
            float airControl = (player.isSprinting() ? 0.05F : 0.04F) - 0.02F;
            player.moveRelative(airControl, new Vec3(player.xxa, 0.0, player.zza));
        }
        player.resetFallDistance();
    }

    public static void updateStepHeight(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.STEP_HEIGHT);
        if (attribute == null) {
            return;
        }
        boolean wanted = !player.isShiftKeyDown() && AwakenedArmor.isActive(player, EquipmentSlot.FEET, AwakenedArmorFeature.BOOTS.get());
        boolean present = attribute.getModifier(STEP_MODIFIER) != null;
        if (wanted && !present) {
            attribute.addTransientModifier(new AttributeModifier(STEP_MODIFIER, STEP_BONUS, AttributeModifier.Operation.ADD_VALUE));
        } else if (!wanted && present) {
            attribute.removeModifier(STEP_MODIFIER);
        }
    }

    public static void updateFlight(ServerPlayer player) {
        boolean granted = AwakenedArmorFeature.FLIGHT_GRANTED.has(player) && AwakenedArmorFeature.FLIGHT_GRANTED.get(player);
        boolean wanted = Config.modFlight() && AwakenedArmor.isActive(player, EquipmentSlot.CHEST, AwakenedArmorFeature.ROBES.get());
        if (wanted) {
            if (!player.getAbilities().mayfly || !granted) {
                boolean changed = !player.getAbilities().mayfly;
                player.getAbilities().mayfly = true;
                AwakenedArmorFeature.FLIGHT_GRANTED.set(player, true);
                if (changed) {
                    player.onUpdateAbilities();
                }
            }
        } else if (granted) {
            revokeFlight(player);
        }
    }

    public static void revokeFlight(ServerPlayer player) {
        AwakenedArmorFeature.FLIGHT_GRANTED.remove(player);
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }

    public static void release(Player player, EquipmentSlot slot) {
        switch (slot) {
            case HEAD -> {
                removeOwned(player, MobEffects.NIGHT_VISION, 0);
                removeOwned(player, MobEffects.BLINDNESS, 0);
            }
            case CHEST -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    updateFlight(serverPlayer);
                }
            }
            case LEGS -> removeOwned(player, MobEffects.FIRE_RESISTANCE, FIRE_RESISTANCE_AMPLIFIER);
            case FEET -> {
                removeOwned(player, MobEffects.HASTE, HASTE_AMPLIFIER);
                updateStepHeight(player);
            }
            default -> {
            }
        }
    }

    private static void removeOwned(Player player, Holder<MobEffect> effect, int amplifier) {
        MobEffectInstance existing = player.getEffect(effect);
        if (existing != null && existing.isAmbient() && existing.getAmplifier() == amplifier && existing.getDuration() <= AwakenedArmorFeature.EFFECT_DURATION) {
            player.removeEffect(effect);
        }
    }

    @SubscribeEvent
    static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        if (event.getFrom().getItem() instanceof AwakenedArmorItem && !event.getTo().is(event.getFrom().getItem())) {
            release(player, event.getSlot());
        }
    }

    @SubscribeEvent
    static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player && AwakenedArmor.isActive(player, EquipmentSlot.FEET, AwakenedArmorFeature.BOOTS.get())) {
            player.setDeltaMovement(player.getDeltaMovement().add(0.0, JUMP_BOOST, 0.0));
        }
    }

    @SubscribeEvent
    static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && player.getItemBySlot(EquipmentSlot.FEET).is(AwakenedArmorFeature.BOOTS.get())) {
            event.setDistance(0.0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player
            && !player.level().isClientSide()
            && event.getSource().is(DamageTypeTags.IS_FIRE)
            && AwakenedArmor.isActive(player, EquipmentSlot.LEGS, AwakenedArmorFeature.LEGGINGS.get())) {
            event.setCanceled(true);
            player.heal(event.getAmount());
        }
    }
}
