package thaumictinkerer.client.geom;

public final class TabletGeometry {
    private TabletGeometry() {
    }

    public static void overlay(ModelQuadSink sink, double size) {
        float high = (float) (size / 2.0);
        float low = -high;
        sink.vertex(low, 0.0F, high, 0.0F, 1.0F);
        sink.vertex(high, 0.0F, high, 1.0F, 1.0F);
        sink.vertex(high, 0.0F, low, 1.0F, 0.0F);
        sink.vertex(low, 0.0F, low, 0.0F, 0.0F);
    }
}
