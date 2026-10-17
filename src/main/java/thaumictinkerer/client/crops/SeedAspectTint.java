package thaumictinkerer.client.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import thaumictinkerer.crops.InfusedSeedsItem;

public record SeedAspectTint() implements ItemTintSource {
    public static final MapCodec<SeedAspectTint> MAP_CODEC = MapCodec.unit(new SeedAspectTint());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        return ARGB.opaque(InfusedSeedsItem.getAspect(stack).getColor());
    }

    @Override
    public MapCodec<SeedAspectTint> type() {
        return MAP_CODEC;
    }
}
