package thaumictinkerer.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import thaumictinkerer.client.geom.ModelQuadSink;

public final class ModelVertexSink implements ModelQuadSink {
    private final PoseStack.Pose pose;
    private final VertexConsumer buffer;
    private final int color;
    private final int light;
    private final float[] positions = new float[12];
    private final float[] uvs = new float[8];
    private int count;

    public ModelVertexSink(PoseStack.Pose pose, VertexConsumer buffer, int color, int light) {
        this.pose = pose;
        this.buffer = buffer;
        this.color = color;
        this.light = light;
    }

    @Override
    public void vertex(float x, float y, float z, float u, float v) {
        positions[count * 3] = x;
        positions[count * 3 + 1] = y;
        positions[count * 3 + 2] = z;
        uvs[count * 2] = u;
        uvs[count * 2 + 1] = v;
        count++;
        if (count == 4) {
            flush();
            count = 0;
        }
    }

    private void flush() {
        float firstX = positions[3] - positions[0];
        float firstY = positions[4] - positions[1];
        float firstZ = positions[5] - positions[2];
        float secondX = positions[9] - positions[0];
        float secondY = positions[10] - positions[1];
        float secondZ = positions[11] - positions[2];
        float normalX = firstY * secondZ - firstZ * secondY;
        float normalY = firstZ * secondX - firstX * secondZ;
        float normalZ = firstX * secondY - firstY * secondX;
        float length = (float) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        if (length > 0.0F) {
            normalX /= length;
            normalY /= length;
            normalZ /= length;
        }
        for (int index = 0; index < 4; index++) {
            buffer.addVertex(pose, positions[index * 3], positions[index * 3 + 1], positions[index * 3 + 2])
                .setColor(color)
                .setUv(uvs[index * 2], uvs[index * 2 + 1])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, normalX, normalY, normalZ);
        }
    }
}
