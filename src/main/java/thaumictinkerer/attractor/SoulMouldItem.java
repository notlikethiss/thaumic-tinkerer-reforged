package thaumictinkerer.attractor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class SoulMouldItem extends Item {
    public SoulMouldItem(Properties properties) {
        super(properties);
    }

    public static String getPatternName(ItemStack stack) {
        return AttractorFeature.SOUL_PATTERN.getOrDefault(stack, "");
    }

    public static String patternOf(LivingEntity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    public static Component patternName(String pattern) {
        ResourceLocation id = ResourceLocation.tryParse(pattern);
        if (id != null) {
            return BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(EntityType::getDescription).orElse(Component.literal(pattern));
        }
        return Component.literal(pattern);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof Player) {
            return InteractionResult.SUCCESS;
        }
        if (!player.level().isClientSide()) {
            AttractorFeature.SOUL_PATTERN.set(stack, patternOf(target));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && AttractorFeature.SOUL_PATTERN.has(stack)) {
            if (!level.isClientSide()) {
                AttractorFeature.SOUL_PATTERN.remove(stack);
            }
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<Component> builder, TooltipFlag flag) {
        String pattern = getPatternName(stack);
        if (pattern.isEmpty()) {
            builder.add(Component.translatable("ttinkerer.ttmisc.soulMould.nonAssigned"));
        } else {
            builder.add(Component.translatable("ttinkerer.ttmisc.soulMould.pattern", patternName(pattern)));
        }
    }
}
