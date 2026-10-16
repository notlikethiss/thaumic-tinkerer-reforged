package thaumictinkerer.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.enchanting.EnchanterBlockEntity;

public record EnchanterStartWorkingPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<EnchanterStartWorkingPayload> TYPE = new Type<>(ThaumicTinkerer.id("enchanter_start_working"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchanterStartWorkingPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, EnchanterStartWorkingPayload::pos,
        EnchanterStartWorkingPayload::new
    );

    @Override
    public Type<EnchanterStartWorkingPayload> type() {
        return TYPE;
    }

    public static void handle(EnchanterStartWorkingPayload payload, Player player) {
        EnchanterBlockEntity enchanter = EnchanterAddEnchantPayload.openEnchanter(player, payload.pos());
        if (enchanter != null) {
            enchanter.startWorking();
        }
    }
}
