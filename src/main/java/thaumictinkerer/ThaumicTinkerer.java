package thaumictinkerer;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import thaumictinkerer.network.ModPayloads;
import thaumictinkerer.platform.TtPlatform;
import thaumictinkerer.registry.ModBlockEntities;
import thaumictinkerer.registry.ModBlocks;
import thaumictinkerer.registry.ModCreativeTabs;
import thaumictinkerer.registry.ModEffects;
import thaumictinkerer.registry.ModEntities;
import thaumictinkerer.registry.ModItems;
import thaumictinkerer.registry.ModMenus;
import thaumictinkerer.registry.ModSounds;
import thaumictinkerer.test.SmokeGameTests;

@Mod(ThaumicTinkerer.MODID)
public class ThaumicTinkerer {
    public static final String MODID = "thaumictinkerer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ThaumicTinkerer(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SYNCED, Config.SPEC);
        TtPlatform.register(modEventBus);
        ModEffects.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModSounds.register(modEventBus);
        ModMenus.register(modEventBus);
        ModPayloads.register();
        SmokeGameTests.register(modEventBus);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
