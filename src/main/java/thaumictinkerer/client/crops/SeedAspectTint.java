package thaumictinkerer.client.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumictinkerer.crops.InfusedSeedsItem;

public record SeedAspectTint()  {
    public static final MapCodec<SeedAspectTint> MAP_CODEC = MapCodec.unit(new SeedAspectTint());

    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        return FastColor.ARGB32.opaque(InfusedSeedsItem.getAspect(stack).getColor());
    }

}
