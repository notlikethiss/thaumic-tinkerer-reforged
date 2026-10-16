package thaumictinkerer.client.enchanting;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record SpellClothTint() implements ItemTintSource {
    public static final MapCodec<SpellClothTint> MAP_CODEC = MapCodec.unit(new SpellClothTint());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        int max = Math.max(1, stack.getMaxDamage());
        float saturation = (float) (max - stack.getDamageValue()) / max * 0.5F;
        return ARGB.opaque(Mth.hsvToRgb(0.75F, saturation, 1.0F));
    }

    @Override
    public MapCodec<SpellClothTint> type() {
        return MAP_CODEC;
    }
}
