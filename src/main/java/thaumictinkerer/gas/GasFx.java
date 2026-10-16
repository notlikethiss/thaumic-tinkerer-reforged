package thaumictinkerer.gas;

import net.minecraft.world.level.Level;

public final class GasFx {
    public interface Handler {
        default void shadowWisp(Level level, double x, double y, double z) {
        }
    }

    private static Handler handler = new Handler() {
    };

    private GasFx() {
    }

    public static Handler get() {
        return handler;
    }

    public static void set(Handler value) {
        handler = value;
    }
}
