package thaumictinkerer.crops;

import net.minecraft.util.StringRepresentable;
import thaumcraft.aspect.Aspect;

public enum CropKind implements StringRepresentable {
    AER("aer"),
    IGNIS("ignis"),
    AQUA("aqua"),
    TERRA("terra"),
    ORDO("ordo"),
    PERDITIO("perditio"),
    GENERIC("generic");

    private final String name;

    CropKind(String name) {
        this.name = name;
    }

    public static CropKind of(Aspect aspect) {
        return switch (aspect) {
            case AIR -> AER;
            case FIRE -> IGNIS;
            case WATER -> AQUA;
            case EARTH -> TERRA;
            case ORDER -> ORDO;
            case ENTROPY -> PERDITIO;
            default -> GENERIC;
        };
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
