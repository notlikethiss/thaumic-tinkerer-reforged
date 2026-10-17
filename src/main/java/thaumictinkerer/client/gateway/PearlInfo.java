package thaumictinkerer.client.gateway;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumictinkerer.kami.gateway.PearlLocation;
import thaumictinkerer.kami.gateway.SkyPearlItem;

public final class PearlInfo {
    private PearlInfo() {
    }

    public static void add(ItemStack stack, ResourceKey<Level> dimension, Vec3 position, List<Component> lines, boolean simple) {
        PearlLocation location = SkyPearlItem.location(stack);
        if (location == null) {
            return;
        }
        BlockPos pos = location.pos();
        lines.add(Component.literal("X: " + pos.getX()));
        if (!simple) {
            lines.add(Component.literal("Y: " + pos.getY()));
        }
        lines.add(Component.literal("Z: " + pos.getZ()));
        if (!location.dimension().equals(dimension)) {
            if (!simple) {
                lines.add(Component.translatable("ttinkerer.ttmisc.differentDim").withStyle(ChatFormatting.RED));
            }
            return;
        }
        double deltaX = pos.getX() - position.x;
        double deltaY = simple ? 0.0 : pos.getY() - position.y;
        double deltaZ = pos.getZ() - position.z;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        String text = new BigDecimal(distance).setScale(2, RoundingMode.UP).toString();
        lines.add(Component.translatable("ttinkerer.ttmisc.distance").append(": " + text + "m").withStyle(ChatFormatting.BLUE));
    }
}
