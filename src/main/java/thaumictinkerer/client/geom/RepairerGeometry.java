package thaumictinkerer.client.geom;

public final class RepairerGeometry {
    public interface Sink {
        void vertex(float x, float y, float z, float u, float v, int argb, int light);
    }

    private static final float TEXTURE_SIZE = 64.0F;
    private static final float PIXEL = 1.0F / 16.0F;
    private static final int[][] QUADS = {
        {5, 1, 2, 6},
        {0, 4, 7, 3},
        {5, 4, 0, 1},
        {2, 3, 7, 6},
        {1, 0, 3, 2},
        {4, 5, 6, 7}
    };
    private static final float[][] NORMALS = {
        {1.0F, 0.0F, 0.0F},
        {-1.0F, 0.0F, 0.0F},
        {0.0F, -1.0F, 0.0F},
        {0.0F, 1.0F, 0.0F},
        {0.0F, 0.0F, -1.0F},
        {0.0F, 0.0F, 1.0F}
    };

    private RepairerGeometry() {
    }

    public static void body(Sink sink, int argb, int light) {
        box(sink, -8.0F, 23.0F, -8.0F, 16, 1, 16, 0, 0, false, argb, light);
        box(sink, -8.0F, 9.0F, 6.0F, 2, 14, 2, 0, 17, false, argb, light);
        box(sink, 6.0F, 9.0F, 6.0F, 2, 14, 2, 0, 17, false, argb, light);
        box(sink, -8.0F, 9.0F, -8.0F, 2, 14, 2, 0, 17, false, argb, light);
        box(sink, 6.0F, 9.0F, -8.0F, 2, 14, 2, 0, 17, false, argb, light);
        box(sink, -8.0F, 8.0F, -8.0F, 16, 1, 2, 11, 19, false, argb, light);
        box(sink, -8.0F, 8.0F, 6.0F, 16, 1, 2, 11, 19, false, argb, light);
        box(sink, 6.0F, 8.0F, -6.0F, 2, 1, 12, 11, 23, false, argb, light);
        box(sink, -8.0F, 8.0F, -6.0F, 2, 1, 12, 11, 23, false, argb, light);
        box(sink, -2.5F, 14.0F, 7.0F, 5, 1, 1, 48, 30, false, argb, light);
        box(sink, -2.5F, 17.0F, 7.0F, 5, 1, 1, 48, 30, false, argb, light);
        box(sink, -2.0F, 14.5F, 7.0F, 1, 3, 1, 48, 24, false, argb, light);
        box(sink, 1.0F, 14.5F, 7.0F, 1, 3, 1, 48, 24, false, argb, light);
    }

    public static void glass(Sink sink, int argb, int light) {
        box(sink, -6.0F, 8.5F, -6.0F, 12, 0, 12, -11, 37, false, argb, light);
        box(sink, 7.5F, 9.0F, -6.0F, 0, 14, 12, 1, 38, false, argb, light);
        box(sink, -7.5F, 9.0F, 6.0F, 0, 14, 12, 1, 38, true, argb, light);
        box(sink, -6.0F, 9.0F, -7.5F, 12, 14, 0, 40, 34, false, argb, light);
        box(sink, 6.0F, 9.0F, 7.5F, 12, 14, 0, 33, 50, true, argb, light);
    }

    public static void overlay(Sink sink, float size, int argb, int light) {
        float high = size / 2.0F;
        float low = -high;
        sink.vertex(low, 0.0F, high, 0.0F, 1.0F, argb, light);
        sink.vertex(high, 0.0F, high, 1.0F, 1.0F, argb, light);
        sink.vertex(high, 0.0F, low, 1.0F, 0.0F, argb, light);
        sink.vertex(low, 0.0F, low, 0.0F, 0.0F, argb, light);
        sink.vertex(low, 0.0F, low, 0.0F, 0.0F, argb, light);
        sink.vertex(high, 0.0F, low, 1.0F, 0.0F, argb, light);
        sink.vertex(high, 0.0F, high, 1.0F, 1.0F, argb, light);
        sink.vertex(low, 0.0F, high, 0.0F, 1.0F, argb, light);
    }

    private static void box(
        Sink sink,
        float pivotX,
        float pivotY,
        float pivotZ,
        int width,
        int height,
        int depth,
        int textureU,
        int textureV,
        boolean turned,
        int argb,
        int light
    ) {
        float sign = turned ? -1.0F : 1.0F;
        float[][] local = {
            {0, 0, 0},
            {width, 0, 0},
            {width, height, 0},
            {0, height, 0},
            {0, 0, depth},
            {width, 0, depth},
            {width, height, depth},
            {0, height, depth}
        };
        float[][] corners = new float[8][3];
        for (int index = 0; index < 8; index++) {
            corners[index][0] = pivotX + sign * local[index][0];
            corners[index][1] = pivotY + local[index][1];
            corners[index][2] = pivotZ + sign * local[index][2];
        }
        int[][] regions = {
            {textureU + depth + width, textureV + depth, textureU + depth + width + depth, textureV + depth + height},
            {textureU, textureV + depth, textureU + depth, textureV + depth + height},
            {textureU + depth, textureV, textureU + depth + width, textureV + depth},
            {textureU + depth + width, textureV + depth, textureU + depth + width + width, textureV},
            {textureU + depth, textureV + depth, textureU + depth + width, textureV + depth + height},
            {textureU + depth + width + depth, textureV + depth, textureU + depth + width + depth + width, textureV + depth + height}
        };
        for (int quad = 0; quad < QUADS.length; quad++) {
            int[] indices = QUADS[quad];
            int[] region = regions[quad];
            float[][] uvs = {
                {region[2] / TEXTURE_SIZE, region[1] / TEXTURE_SIZE},
                {region[0] / TEXTURE_SIZE, region[1] / TEXTURE_SIZE},
                {region[0] / TEXTURE_SIZE, region[3] / TEXTURE_SIZE},
                {region[2] / TEXTURE_SIZE, region[3] / TEXTURE_SIZE}
            };
            float[] first = corners[indices[0]];
            float[] second = corners[indices[1]];
            float[] fourth = corners[indices[3]];
            float firstX = second[0] - first[0];
            float firstY = second[1] - first[1];
            float firstZ = second[2] - first[2];
            float secondX = fourth[0] - first[0];
            float secondY = fourth[1] - first[1];
            float secondZ = fourth[2] - first[2];
            float normalX = firstY * secondZ - firstZ * secondY;
            float normalY = firstZ * secondX - firstX * secondZ;
            float normalZ = firstX * secondY - firstY * secondX;
            float[] wanted = NORMALS[quad];
            boolean outward = normalX * wanted[0] * sign + normalY * wanted[1] + normalZ * wanted[2] * sign >= 0.0F;
            for (int step = 0; step < 4; step++) {
                int vertex = outward ? step : (4 - step) % 4;
                float[] corner = corners[indices[vertex]];
                sink.vertex(corner[0] * PIXEL, corner[1] * PIXEL, corner[2] * PIXEL, uvs[vertex][0], uvs[vertex][1], argb, light);
            }
        }
    }
}
