package thaumictinkerer.gear;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;
import thaumcraft.registry.ModEffects;
import thaumcraft.registry.ModSounds;

public class CleansingTalismanItem extends Item implements Bauble {
    public static final int USES = 100;
    private static final int DEFAULT_DAMAGE = 1;
    private static final int WARP_DAMAGE = 10;

    public CleansingTalismanItem(Properties properties) {
        super(properties);
    }

    public static boolean isEnabled(ItemStack stack) {
        return GearFeature.ACTIVE.has(stack);
    }

    public static void flipEnabled(ItemStack stack) {
        GearFeature.ACTIVE.set(stack, isEnabled(stack) ? null : Boolean.TRUE);
    }

    public static boolean isWarpEffect(MobEffectInstance effect) {
        return List.of(
            ModEffects.BLURRED_VISION,
            ModEffects.DEATH_GAZE,
            ModEffects.INFECTIOUS_VIS_EXHAUST,
            ModEffects.SUN_SCORNED,
            ModEffects.UNNATURAL_HUNGER
        ).stream().anyMatch(holder -> effect.getEffect().is(holder.getId()));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            flipEnabled(player.getItemInHand(hand));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        builder.accept(Component.translatable(isEnabled(stack) ? "ttinkerer.ttmisc.active" : "ttinkerer.ttmisc.inactive"));
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.AMULET;
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity wearer) {
        if (!(wearer.level() instanceof ServerLevel level) || !(wearer instanceof Player) || !isEnabled(stack) || wearer.tickCount % 20 != 0) {
            return;
        }
        cleanse(level, stack, wearer);
    }

    public static boolean cleanse(ServerLevel level, ItemStack stack, LivingEntity wearer) {
        boolean removed = false;
        int damage = DEFAULT_DAMAGE;
        if (wearer.isOnFire()) {
            wearer.clearFire();
            removed = true;
        } else {
            for (MobEffectInstance effect : new ArrayList<>(wearer.getActiveEffects())) {
                if (effect.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) {
                    continue;
                }
                wearer.removeEffect(effect.getEffect());
                removed = true;
                if (isWarpEffect(effect)) {
                    damage = WARP_DAMAGE;
                }
                break;
            }
        }
        if (removed) {
            stack.hurtAndBreak(damage, level, wearer, broken -> {
            });
            level.playSound(null, wearer.getX(), wearer.getY(), wearer.getZ(), ModSounds.WAND.get(), SoundSource.PLAYERS, 0.3F, 0.1F);
        }
        return removed;
    }
}
