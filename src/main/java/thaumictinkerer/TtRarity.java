package thaumictinkerer;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public final class TtRarity {
    public static final EnumProxy<Rarity> KAMI_PROXY = new EnumProxy<>(Rarity.class, -1, ThaumicTinkerer.MODID + ":kami", ChatFormatting.LIGHT_PURPLE);

    private TtRarity() {
    }

    public static Rarity kami() {
        return KAMI_PROXY.getValue();
    }
}
