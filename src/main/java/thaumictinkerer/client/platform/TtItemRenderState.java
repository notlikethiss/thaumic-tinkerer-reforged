package thaumictinkerer.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;

public final class TtItemRenderState {
    private ItemStack stack = ItemStack.EMPTY;
    private ItemDisplayContext context = ItemDisplayContext.GROUND;
    private Level level;
    private int seed;

    public void clear() {
        stack = ItemStack.EMPTY;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public static void updateForTopItem(TtItemRenderState state, ItemStack stack, ItemDisplayContext context, Level level, LivingEntity entity, int seed) {
        state.stack = stack;
        state.context = context;
        state.level = level;
        state.seed = seed;
    }

    public void submit(PoseStack pose, MultiBufferSource buffers, int light, int overlay, int outline) {
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, context, light, overlay, pose, buffers, level, seed);
    }
}
