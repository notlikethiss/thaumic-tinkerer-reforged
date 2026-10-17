package thaumictinkerer.client.attractor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class AttractorItemRenderer implements NoDataSpecialModelRenderer {
    private final boolean corporeal;

    public AttractorItemRenderer(boolean corporeal) {
        this.corporeal = corporeal;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        long ticks = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
        AttractorRendering.submit(collector, poseStack, corporeal, true, 15, lightCoords, ticks, System.currentTimeMillis());
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0.0625F, 0.0F, 0.0625F));
        output.accept(new Vector3f(0.9375F, 0.9F, 0.9375F));
    }

    public record Unbaked(boolean corporeal) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("corporeal", false).forGetter(Unbaked::corporeal)
        ).apply(instance, Unbaked::new));

        @Override
        public NoDataSpecialModelRenderer bake(BakingContext context) {
            return new AttractorItemRenderer(corporeal);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
