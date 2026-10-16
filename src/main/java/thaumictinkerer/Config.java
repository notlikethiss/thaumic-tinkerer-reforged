package thaumictinkerer;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue KAMI_ENABLED = BUILDER
        .translation("thaumictinkerer.configuration.kami_enabled")
        .define("general.kami.forceenabled", true);
    public static final ModConfigSpec.BooleanValue IMBUED_FIRE = BUILDER
        .translation("thaumictinkerer.configuration.imbued_fire")
        .define("general.imbuedFire.enabled", true);
    public static final ModConfigSpec.BooleanValue IMBUED_FIRE_CAKE = BUILDER
        .translation("thaumictinkerer.configuration.imbued_fire_cake")
        .define("general.imbuedFire.cake.enabled", true);
    public static final ModConfigSpec.BooleanValue SHARE_TOME_SURVIVAL = BUILDER
        .translation("thaumictinkerer.configuration.share_tome_survival")
        .define("general.shareTome.survival.enabled", true);
    public static final ModConfigSpec.BooleanValue CROPS_ALLOW_BONEMEAL = BUILDER
        .translation("thaumictinkerer.configuration.crops_allow_bonemeal")
        .define("general.cropsAllowBonemeal.enabled", false);
    public static final ModConfigSpec.BooleanValue MOD_FLIGHT = BUILDER
        .translation("thaumictinkerer.configuration.mod_flight")
        .define("general.modFlight.enabled", true);
    public static final ModConfigSpec.BooleanValue PLACEMENT_MIRROR_BLOCKS = BUILDER
        .translation("thaumictinkerer.configuration.placement_mirror_blocks")
        .define("general.placementMirror.blocks.show", true);
    public static final ModConfigSpec.BooleanValue TOOLTIP_INDICATORS = BUILDER
        .translation("thaumictinkerer.configuration.tooltip_indicators")
        .define("general.tooltipIndicators.enabled", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    private static boolean flag(ModConfigSpec.BooleanValue value) {
        return SPEC.isLoaded() ? value.getAsBoolean() : value.getDefault();
    }

    public static boolean kamiEnabled() {
        return flag(KAMI_ENABLED);
    }

    public static boolean imbuedFire() {
        return flag(IMBUED_FIRE);
    }

    public static boolean imbuedFireCake() {
        return flag(IMBUED_FIRE_CAKE);
    }

    public static boolean shareTomeSurvival() {
        return flag(SHARE_TOME_SURVIVAL);
    }

    public static boolean cropsAllowBonemeal() {
        return flag(CROPS_ALLOW_BONEMEAL);
    }

    public static boolean modFlight() {
        return flag(MOD_FLIGHT);
    }

    public static boolean placementMirrorBlocks() {
        return flag(PLACEMENT_MIRROR_BLOCKS);
    }

    public static boolean tooltipIndicators() {
        return flag(TOOLTIP_INDICATORS);
    }
}
