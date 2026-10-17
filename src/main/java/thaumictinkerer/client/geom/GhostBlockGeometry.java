package thaumictinkerer.client.geom;

public final class GhostBlockGeometry {
    private GhostBlockGeometry() {
    }

    public record UvRect(float u0, float v0, float u1, float v1) {
        public float u(float fraction) {
            return u0 + (u1 - u0) * fraction;
        }

        public float v(float fraction) {
            return v0 + (v1 - v0) * fraction;
        }
    }

    public static void cube(ModelQuadSink sink, float x, float y, float z, UvRect uv) {
        face(sink, uv, x, y, z, new float[][] {{0, 1, 0}, {0, 1, 1}, {1, 1, 1}, {1, 1, 0}}, 0, 2, false);
        face(sink, uv, x, y, z, new float[][] {{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}}, 0, 2, false);
        face(sink, uv, x, y, z, new float[][] {{0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}}, 0, 1, true);
        face(sink, uv, x, y, z, new float[][] {{0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}}, 0, 1, true);
        face(sink, uv, x, y, z, new float[][] {{0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}}, 2, 1, true);
        face(sink, uv, x, y, z, new float[][] {{1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}}, 2, 1, true);
    }

    private static void face(ModelQuadSink sink, UvRect uv, float x, float y, float z, float[][] corners, int uAxis, int vAxis, boolean flipV) {
        for (float[] corner : corners) {
            float v = flipV ? 1.0F - corner[vAxis] : corner[vAxis];
            sink.vertex(x + corner[0], y + corner[1], z + corner[2], uv.u(corner[uAxis]), uv.v(v));
        }
    }
}
