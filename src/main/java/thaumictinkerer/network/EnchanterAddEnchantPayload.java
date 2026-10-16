package thaumictinkerer.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.enchanting.EnchanterBlockEntity;
import thaumictinkerer.enchanting.EnchanterMenu;

public record EnchanterAddEnchantPayload(BlockPos pos, ResourceKey<Enchantment> enchantment, int level) implements CustomPacketPayload {
    public static final Type<EnchanterAddEnchantPayload> TYPE = new Type<>(ThaumicTinkerer.id("enchanter_add_enchant"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchanterAddEnchantPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, EnchanterAddEnchantPayload::pos,
        ResourceKey.streamCodec(Registries.ENCHANTMENT), EnchanterAddEnchantPayload::enchantment,
        ByteBufCodecs.VAR_INT, EnchanterAddEnchantPayload::level,
        EnchanterAddEnchantPayload::new
    );

    @Override
    public Type<EnchanterAddEnchantPayload> type() {
        return TYPE;
    }

    public static EnchanterBlockEntity openEnchanter(Player player, BlockPos pos) {
        if (player.containerMenu instanceof EnchanterMenu menu && menu.pos().equals(pos) && menu.stillValid(player)) {
            return menu.enchanter();
        }
        return null;
    }

    public static void handle(EnchanterAddEnchantPayload payload, Player player) {
        EnchanterBlockEntity enchanter = openEnchanter(player, payload.pos());
        if (enchanter != null) {
            enchanter.applyEdit(player, payload.enchantment(), payload.level());
        }
    }
}
