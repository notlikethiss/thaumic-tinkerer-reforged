package thaumictinkerer.gear;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;
import thaumcraft.registry.ModSounds;

public class XpTalismanItem extends Item implements Bauble {
    public static final int RANGE = 3;
    public static final int MAX_XP = 1500;
    public static final int BOTTLE_COST = 10;

    public XpTalismanItem(Properties properties) {
        super(properties);
    }

    public static int getXp(ItemStack stack) {
        return GearFeature.XP.getOrDefault(stack, 0);
    }

    public static void setXp(ItemStack stack, int xp) {
        GearFeature.XP.set(stack, xp);
    }

    public static boolean isAbsorbing(ItemStack stack) {
        return GearFeature.ACTIVE.has(stack);
    }

    public static void setAbsorbing(ItemStack stack, boolean absorbing) {
        GearFeature.ACTIVE.set(stack, absorbing ? Boolean.TRUE : null);
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (getXp(stack) >= MAX_XP) {
                return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand));
            }
            if (!level.isClientSide()) {
                setAbsorbing(stack, !isAbsorbing(stack));
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
            }
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        if (getXp(stack) < BOTTLE_COST) {
            return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        if (level.isClientSide()) {
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!candidate.is(Items.GLASS_BOTTLE)) {
                continue;
            }
            candidate.shrink(1);
            ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
            if (!inventory.add(bottle) && !bottle.isEmpty()) {
                player.spawnAtLocation(bottle);
            }
            setXp(stack, getXp(stack) - BOTTLE_COST);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, 0.1F + level.getRandom().nextFloat() / 2.0F);
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, builder, flag);
        builder.add(Component.literal("XP: " + getXp(stack)));
        if (getXp(stack) >= MAX_XP) {
            builder.add(Component.translatable("ttinkerer.ttmisc.full"));
        } else {
            builder.add(Component.translatable(isAbsorbing(stack) ? "ttinkerer.ttmisc.absorbing" : "ttinkerer.ttmisc.notAbsorbing"));
        }
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.AMULET;
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity wearer) {
        if (!(wearer.level() instanceof ServerLevel level) || !isAbsorbing(stack)) {
            return;
        }
        absorb(level, stack, wearer);
    }

    public static int absorb(ServerLevel level, ItemStack stack, LivingEntity wearer) {
        int currentXp = getXp(stack);
        int remaining = MAX_XP - currentXp;
        if (remaining <= 0) {
            setAbsorbing(stack, false);
            return 0;
        }
        int added = 0;
        AABB box = new AABB(wearer.getX() - RANGE, wearer.getY() - RANGE, wearer.getZ() - RANGE, wearer.getX() + RANGE, wearer.getY() + RANGE, wearer.getZ() + RANGE);
        List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, box);
        for (ExperienceOrb orb : orbs) {
            if (orb.isRemoved()) {
                continue;
            }
            int value = orb.getValue() * orb.count;
            if (added + value <= remaining) {
                added += value;
                orb.discard();
                level.playSound(null, orb.getX(), orb.getY(), orb.getZ(), ModSounds.ZAP.get(), SoundSource.PLAYERS, value / 10.0F, 1.0F);
            }
            remaining -= added;
            if (remaining <= 0) {
                break;
            }
        }
        setXp(stack, Math.min(MAX_XP, currentXp + added));
        return added;
    }
}
