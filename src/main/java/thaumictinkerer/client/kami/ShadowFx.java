package thaumictinkerer.client.kami;

import net.minecraft.util.RandomSource;
import thaumcraft.client.fx.SparkleFx;

public class ShadowFx extends SparkleFx {
    private static final int DARK = 5;
    private static final int MAX_AGE = 5;
    private static final float GRAVITY = -0.7F;

    public ShadowFx(RandomSource random, double x, double y, double z, float size) {
        super(random, x, y, z, size, DARK, 1);
        this.maxAge = MAX_AGE;
        noShrink();
        gravity(GRAVITY);
    }
}
