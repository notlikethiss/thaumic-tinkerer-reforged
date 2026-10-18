package thaumictinkerer.client.enchanting;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

public record SpellClothTint()  {
    public static final MapCodec<SpellClothTint> MAP_CODEC = MapCodec.unit(new SpellClothTint());

    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        int max = Math.max(1, stack.getMaxDamage());
        float saturation = (float) (max - stack.getDamageValue()) / max * 0.5F;
        return FastColor.ARGB32.opaque(Mth.hsvToRgb(0.75F, saturation, 1.0F));
    }

}
