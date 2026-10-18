package thaumictinkerer.crops;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumictinkerer.crafting.TtRecipes;
import thaumictinkerer.platform.TtStackKey;
import thaumictinkerer.registry.ModBlockEntities;
import thaumictinkerer.registry.ModBlocks;
import thaumictinkerer.registry.ModItems;

public final class CropsFeature {
    public static final String RESEARCH = "INFUSED_POTIONS";
    public static final String SEED_DATA = "seed_aspect";

    public static final TtStackKey<Aspect> SEED_ASPECT = TtStackKey.register("seed_aspect", Aspect.CODEC, Aspect.STREAM_CODEC);
    public static final TtStackKey<AspectList> SEED_TENDENCIES = TtStackKey.register("seed_tendencies", AspectList.CODEC, AspectList.STREAM_CODEC);

    public static final DeferredBlock<InfusedFarmlandBlock> INFUSED_FARMLAND = ModBlocks.BLOCKS.registerBlock(
        "infused_farmland",
        InfusedFarmlandBlock::new,
        properties -> properties
            .mapColor(MapColor.DIRT)
            .strength(0.6F)
            .sound(SoundType.GRAVEL)
    );
    public static final DeferredBlock<InfusedGrainBlock> INFUSED_GRAIN = ModBlocks.BLOCKS.registerBlock(
        "infused_grain_crop",
        InfusedGrainBlock::new,
        properties -> properties
            .mapColor(MapColor.PLANT)
            .noCollission()
            .instabreak()
            .sound(SoundType.CROP)
    );

    public static final Supplier<BlockEntityType<InfusedFarmlandBlockEntity>> INFUSED_FARMLAND_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "infused_farmland",
        () -> BlockEntityType.Builder.of(InfusedFarmlandBlockEntity::new,  INFUSED_FARMLAND.get()).build(null)
    );
    public static final Supplier<BlockEntityType<InfusedGrainBlockEntity>> INFUSED_GRAIN_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "infused_grain_crop",
        () -> BlockEntityType.Builder.of(InfusedGrainBlockEntity::new,  INFUSED_GRAIN.get()).build(null)
    );

    public static final DeferredItem<InfusedSeedsItem> INFUSED_SEEDS = ModItems.ITEMS.registerItem(
        "infused_seeds",
        properties -> new InfusedSeedsItem(INFUSED_GRAIN.get(), properties),
        properties -> properties.rarity(Rarity.RARE)
    );

    public static final DeferredItem<InfusedGrainItem> AIR_GRAIN = grain("aer_infused_grain", Aspect.AIR);
    public static final DeferredItem<InfusedGrainItem> FIRE_GRAIN = grain("ignis_infused_grain", Aspect.FIRE);
    public static final DeferredItem<InfusedGrainItem> EARTH_GRAIN = grain("terra_infused_grain", Aspect.EARTH);
    public static final DeferredItem<InfusedGrainItem> WATER_GRAIN = grain("aqua_infused_grain", Aspect.WATER);

    private static final Aspect[] SEED_RECIPES = {Aspect.AIR, Aspect.FIRE, Aspect.EARTH, Aspect.WATER, Aspect.ORDER, Aspect.ENTROPY};
    private static final String[] SHARDS = {"air", "fire", "earth", "water", "order", "entropy"};

    private CropsFeature() {
    }

    private static DeferredItem<InfusedGrainItem> grain(String name, Aspect aspect) {
        return ModItems.ITEMS.registerItem(name, properties -> new InfusedGrainItem(aspect, properties));
    }

    public static void load() {
        TcStack.registerDataHandler(SEED_DATA, (stack, value) -> {
            Aspect aspect = Aspect.getAspect(value);
            if (aspect != null) {
                InfusedSeedsItem.setAspect(stack, aspect);
                InfusedSeedsItem.setTendencies(stack, new AspectList());
            }
        });
    }

    public static void recipes() {
        for (int index = 0; index < SEED_RECIPES.length; index++) {
            TcIngredient shard = TcIngredient.item("thaumcraft:" + SHARDS[index] + "_shard");
            TtRecipes.infusion(
                RESEARCH + index, RESEARCH,
                TcStack.withData("thaumictinkerer:infused_seeds", 1, Map.of(SEED_DATA, SEED_RECIPES[index].getTag())),
                5,
                new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32),
                TcIngredient.item("minecraft:wheat_seeds"),
                shard, shard, shard, shard
            );
        }
    }
}
