package thaumictinkerer.test;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModItems;
import thaumictinkerer.Config;
import thaumictinkerer.crops.AspectCropLootManager;
import thaumictinkerer.crops.CropKind;
import thaumictinkerer.crops.CropsEvents;
import thaumictinkerer.crops.CropsFeature;
import thaumictinkerer.crops.InfusedFarmlandBlock;
import thaumictinkerer.crops.InfusedFarmlandBlockEntity;
import thaumictinkerer.crops.InfusedGrainBlock;
import thaumictinkerer.crops.InfusedGrainBlockEntity;
import thaumictinkerer.crops.InfusedGrainItem;
import thaumictinkerer.crops.InfusedSeedsItem;

public final class CropsGameTests {
    private static final BlockPos SOIL = new BlockPos(3, 2, 3);
    private static final BlockPos CROP = new BlockPos(3, 3, 3);
    private static final BlockPos NEIGHBOR_SOIL = new BlockPos(4, 2, 3);
    private static final BlockPos NEIGHBOR_CROP = new BlockPos(4, 3, 3);

    private static final TtGameTestSuite SUITE = new TtGameTestSuite("crops")
        .add("recipes_registered", CropsGameTests::recipesRegistered)
        .add("recipe_seeds_carry_aspect", CropsGameTests::recipeSeedsCarryAspect)
        .add("seed_stacks", CropsGameTests::seedStacks)
        .add("grain_items", CropsGameTests::grainItems)
        .add("planting_creates_infused_farmland", CropsGameTests::plantingCreatesFarmland)
        .add("seeds_use_on_farmland", CropsGameTests::seedsUseOnFarmland)
        .add("elemental_hoe_grows_crop", CropsGameTests::elementalHoe)
        .add("farmland_rules", CropsGameTests::farmlandRules)
        .add("growth_needs_light_and_roll", CropsGameTests::growthNeedsLight)
        .add("mature_crop_drops_grain_of_aspect", CropsGameTests::matureDrops)
        .add("immature_crop_drops_only_seeds", CropsGameTests::immatureDrops)
        .add("mutation_is_deterministic", CropsGameTests::mutation)
        .add("tendency_effects", CropsGameTests::tendencyEffects)
        .add("tendency_exchange", CropsGameTests::tendencyExchange)
        .add("farmland_residue_is_capped", CropsGameTests::residueCapped)
        .add("loot_table_per_aspect", CropsGameTests::lootTable)
        .add("bonemeal_respects_config", CropsGameTests::bonemeal)
        .add("persists", CropsGameTests::persists);

    private CropsGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static final class ScriptedRandom extends SingleThreadedRandomSource {
        private final int[] values;
        private int index;

        ScriptedRandom(int... values) {
            super(0L);
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            return values[index++ % values.length] % bound;
        }
    }

    private static void clear(GameTestHelper helper) {
        for (int x = 0; x < 7; x++) {
            for (int y = 1; y < 7; y++) {
                for (int z = 0; z < 7; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static InfusedGrainBlockEntity plant(GameTestHelper helper, BlockPos soil, Aspect aspect, AspectList tendencies) {
        InfusedSeedsItem.plant(helper.getLevel(), helper.absolutePos(soil), aspect, tendencies);
        return (InfusedGrainBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(soil.above()));
    }

    private static InfusedFarmlandBlockEntity soil(GameTestHelper helper, BlockPos pos) {
        return (InfusedFarmlandBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static void setAge(GameTestHelper helper, BlockPos pos, int age) {
        BlockPos absolute = helper.absolutePos(pos);
        BlockState state = helper.getLevel().getBlockState(absolute);
        helper.getLevel().setBlock(absolute, state.setValue(InfusedGrainBlock.AGE, age), Block.UPDATE_ALL);
    }

    private static int count(List<ItemStack> drops, net.minecraft.world.item.Item item) {
        return drops.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        Aspect[] aspects = {Aspect.AIR, Aspect.FIRE, Aspect.EARTH, Aspect.WATER, Aspect.ORDER, Aspect.ENTROPY};
        String[] shards = {"air", "fire", "earth", "water", "order", "entropy"};
        for (int index = 0; index < aspects.length; index++) {
            String key = "INFUSED_POTIONS" + index;
            helper.assertTrue(TcRecipes.get(key) instanceof InfusionRecipe, "infusion recipe missing: " + key);
            InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get(key);
            helper.assertTrue(recipe.research().equals("INFUSED_POTIONS"), "research of " + key + ": " + recipe.research());
            helper.assertTrue(recipe.instability() == 5, "instability of " + key);
            helper.assertTrue(recipe.aspects().getAmount(Aspect.CROP) == 32 && recipe.aspects().getAmount(Aspect.HARVEST) == 32 && recipe.aspects().size() == 2, "aspects of " + key);
            helper.assertTrue(recipe.central().test(new ItemStack(Items.WHEAT_SEEDS)), "central of " + key);
            helper.assertTrue(recipe.components().size() == 4, "components of " + key);
            for (var component : recipe.components()) {
                helper.assertTrue(component.test(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("thaumcraft:" + shards[index] + "_shard")))), "component of " + key);
            }
        }
        helper.succeed();
    }

    private static void recipeSeedsCarryAspect(GameTestHelper helper) {
        Aspect[] aspects = {Aspect.AIR, Aspect.FIRE, Aspect.EARTH, Aspect.WATER, Aspect.ORDER, Aspect.ENTROPY};
        for (int index = 0; index < aspects.length; index++) {
            InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get("INFUSED_POTIONS" + index);
            ItemStack result = recipe.result().create();
            helper.assertTrue(result.is(CropsFeature.INFUSED_SEEDS.get()) && result.getCount() == 1, "result item " + result);
            helper.assertTrue(InfusedSeedsItem.getAspect(result) == aspects[index], "seed aspect " + InfusedSeedsItem.getAspect(result));
            helper.assertTrue(InfusedSeedsItem.getTendencies(result).isEmpty(), "tendencies not empty");
            helper.assertTrue(CropsFeature.SEED_ASPECT.has(result), "aspect component missing");
        }
        helper.succeed();
    }

    private static void seedStacks(GameTestHelper helper) {
        ItemStack stack = InfusedSeedsItem.stackFor(Aspect.MAN);
        InfusedSeedsItem.setTendencies(stack, new AspectList().add(Aspect.FIRE, 3).add(Aspect.ORDER, 2));
        ItemStack copy = stack.copy();
        helper.assertTrue(InfusedSeedsItem.getAspect(copy) == Aspect.MAN, "copy aspect");
        helper.assertTrue(InfusedSeedsItem.getTendencies(copy).getAmount(Aspect.FIRE) == 3 && InfusedSeedsItem.getTendencies(copy).getAmount(Aspect.ORDER) == 2, "copy tendencies");
        helper.assertTrue(ItemStack.isSameItemSameComponents(stack, copy), "stacks differ");
        helper.assertTrue(!ItemStack.isSameItemSameComponents(stack, InfusedSeedsItem.stackFor(Aspect.MAN)), "tendencies ignored");
        helper.assertTrue(InfusedSeedsItem.getAspect(new ItemStack(CropsFeature.INFUSED_SEEDS.get())) == Aspect.AIR, "default aspect");
        List<ItemStack> variants = new ArrayList<>();
        CropsFeature.INFUSED_SEEDS.get().addCreativeStacks(variants::add);
        helper.assertTrue(variants.size() == 6, "creative variants " + variants.size());
        helper.assertTrue(variants.stream().map(InfusedSeedsItem::getAspect).toList().equals(Aspect.getPrimalAspects()), "creative aspects");
        helper.succeed();
    }

    private static void grainItems(GameTestHelper helper) {
        InfusedGrainItem[] items = {CropsFeature.AIR_GRAIN.get(), CropsFeature.FIRE_GRAIN.get(), CropsFeature.EARTH_GRAIN.get(), CropsFeature.WATER_GRAIN.get()};
        Aspect[] aspects = {Aspect.AIR, Aspect.FIRE, Aspect.EARTH, Aspect.WATER};
        for (int index = 0; index < items.length; index++) {
            ItemStack stack = new ItemStack(items[index]);
            AspectList list = items[index].getAspects(stack);
            helper.assertTrue(list.getAmount(aspects[index]) == 1 && list.getAmount(Aspect.CROP) == 1 && list.size() == 2, "grain aspects " + list);
            AspectList bonus = AspectHelper.getBonusTags(stack, null);
            helper.assertTrue(bonus.getAmount(aspects[index]) == 1 && bonus.getAmount(Aspect.CROP) == 1, "bonus tags " + bonus);
            items[index].setAspects(stack, new AspectList().add(Aspect.VOID, 4));
            helper.assertTrue(items[index].getAspects(stack).getAmount(Aspect.VOID) == 0, "grain aspects are mutable");
        }
        helper.succeed();
    }

    private static void plantingCreatesFarmland(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(SOIL, Blocks.FARMLAND.defaultBlockState());
        InfusedGrainBlockEntity grain = plant(helper, SOIL, Aspect.AIR, new AspectList().add(Aspect.WATER, 2));
        helper.assertTrue(helper.getBlockState(SOIL).getBlock() instanceof InfusedFarmlandBlock, "farmland not infused");
        helper.assertTrue(helper.getBlockState(CROP).getBlock() instanceof InfusedGrainBlock, "crop missing");
        helper.assertTrue(grain.getAspect() == Aspect.AIR && grain.tendency(Aspect.WATER) == 2, "crop data");
        helper.assertTrue(helper.getBlockState(CROP).getValue(InfusedGrainBlock.KIND) == CropKind.AER, "primal kind");
        helper.assertTrue(helper.getBlockState(CROP).getValue(InfusedGrainBlock.AGE) == 0, "initial age");
        helper.assertTrue(soil(helper, SOIL) != null, "farmland entity missing");

        InfusedGrainBlockEntity compound = plant(helper, SOIL, Aspect.LIGHT, new AspectList());
        helper.assertTrue(compound.getAspect() == Aspect.LIGHT, "replant aspect");
        helper.assertTrue(helper.getBlockState(CROP).getValue(InfusedGrainBlock.KIND) == CropKind.GENERIC, "compound kind");
        compound.setAspect(Aspect.ENTROPY);
        helper.assertTrue(helper.getBlockState(CROP).getValue(InfusedGrainBlock.KIND) == CropKind.PERDITIO, "kind after setAspect");
        helper.assertTrue(CropKind.of(Aspect.ORDER) == CropKind.ORDO && CropKind.of(Aspect.FIRE) == CropKind.IGNIS && CropKind.of(Aspect.WATER) == CropKind.AQUA && CropKind.of(Aspect.EARTH) == CropKind.TERRA, "kinds");
        helper.succeed();
    }

    private static UseOnContext useOn(GameTestHelper helper, Player player, BlockPos pos, Direction face) {
        BlockPos absolute = helper.absolutePos(pos);
        return new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolute), face, absolute, false));
    }

    private static void seedsUseOnFarmland(GameTestHelper helper) {
        clear(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack seeds = InfusedSeedsItem.stackFor(Aspect.WATER);
        InfusedSeedsItem.setTendencies(seeds, new AspectList().add(Aspect.ORDER, 3));
        seeds.setCount(2);
        player.setItemInHand(InteractionHand.MAIN_HAND, seeds);
        helper.setBlock(SOIL, Blocks.DIRT.defaultBlockState());
        helper.assertTrue(seeds.useOn(useOn(helper, player, SOIL, Direction.UP)) == InteractionResult.PASS, "planted on dirt");
        helper.setBlock(SOIL, Blocks.FARMLAND.defaultBlockState());
        helper.assertTrue(seeds.useOn(useOn(helper, player, SOIL, Direction.NORTH)) == InteractionResult.PASS, "planted on a side face");
        helper.setBlock(CROP, Blocks.STONE.defaultBlockState());
        helper.assertTrue(seeds.useOn(useOn(helper, player, SOIL, Direction.UP)) == InteractionResult.PASS, "planted into stone");
        helper.assertTrue(seeds.getCount() == 2, "seeds consumed on failure");
        helper.setBlock(CROP, Blocks.AIR.defaultBlockState());
        helper.assertTrue(seeds.useOn(useOn(helper, player, SOIL, Direction.UP)).consumesAction(), "did not plant");
        helper.assertTrue(seeds.getCount() == 1, "seeds not consumed");
        helper.assertTrue(helper.getBlockState(SOIL).getBlock() instanceof InfusedFarmlandBlock, "farmland not infused");
        InfusedGrainBlockEntity grain = (InfusedGrainBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(CROP));
        helper.assertTrue(grain != null && grain.getAspect() == Aspect.WATER && grain.tendency(Aspect.ORDER) == 3, "crop data");
        helper.assertTrue(helper.getBlockState(CROP).getValue(InfusedGrainBlock.KIND) == CropKind.AQUA, "crop kind");
        soil(helper, SOIL).getAspects().add(Aspect.EARTH, 5);
        helper.setBlock(CROP, Blocks.AIR.defaultBlockState());
        helper.assertTrue(seeds.useOn(useOn(helper, player, SOIL, Direction.UP)).consumesAction(), "did not plant on infused farmland");
        helper.assertTrue(soil(helper, SOIL).getAspects().getAmount(Aspect.EARTH) == 5, "residue lost when replanting");
        helper.succeed();
    }

    private static void elementalHoe(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(SOIL, Blocks.FARMLAND.defaultBlockState());
        plant(helper, SOIL, Aspect.AIR, new AspectList());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack hoe = new ItemStack(ModItems.ELEMENTAL_HOE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, hoe);
        BlockPos absolute = helper.absolutePos(CROP);
        var event = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, absolute, new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        NeoForge.EVENT_BUS.post(event);
        helper.assertTrue(event.isCanceled(), "hoe use not handled");
        helper.assertTrue(helper.getBlockState(CROP).getValue(InfusedGrainBlock.AGE) >= 2, "hoe did not grow the crop");
        helper.assertTrue(hoe.getDamageValue() == CropsEvents.HOE_DAMAGE, "hoe damage " + hoe.getDamageValue());
        setAge(helper, CROP, 7);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        var other = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, absolute, new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        NeoForge.EVENT_BUS.post(other);
        helper.assertTrue(!other.isCanceled(), "event handled for another item");
        helper.succeed();
    }

    private static void farmlandRules(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(SOIL, CropsFeature.INFUSED_FARMLAND.get().defaultBlockState());
        ServerLevel level = helper.getLevel();
        BlockPos soil = helper.absolutePos(SOIL);
        BlockState farmland = level.getBlockState(soil);
        InfusedGrainBlock crop = CropsFeature.INFUSED_GRAIN.get();
        helper.assertTrue(farmland.canSustainPlant(level, soil, Direction.UP, crop.defaultBlockState()) == TriState.TRUE, "does not sustain infused crop");
        helper.assertTrue(farmland.canSustainPlant(level, soil, Direction.UP, Blocks.WHEAT.defaultBlockState()) == TriState.FALSE, "sustains wheat");
        helper.assertTrue(crop.defaultBlockState().canSurvive(level, soil.above()), "crop cannot stand on infused farmland");
        helper.setBlock(SOIL, Blocks.FARMLAND.defaultBlockState());
        helper.assertTrue(!crop.defaultBlockState().canSurvive(level, soil.above()), "crop stands on vanilla farmland");
        helper.setBlock(SOIL, CropsFeature.INFUSED_FARMLAND.get().defaultBlockState());
        farmland = level.getBlockState(soil);
        helper.assertTrue(farmland.getCollisionShape(level, soil).max(Direction.Axis.Y) == 1.0, "farmland is not a full block");
        for (int index = 0; index < 20; index++) {
            farmland.randomTick(level, soil, level.getRandom());
            level.getBlockState(soil).tick(level, soil, level.getRandom());
        }
        helper.assertTrue(helper.getBlockState(SOIL).getBlock() instanceof InfusedFarmlandBlock, "farmland degraded");
        helper.succeed();
    }

    private static void growthNeedsLight(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(SOIL, Blocks.FARMLAND.defaultBlockState());
        InfusedGrainBlockEntity grain = plant(helper, SOIL, Aspect.FIRE, new AspectList());
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(CROP);
        helper.assertTrue(!grain.tryGrow(level, pos, level.getBlockState(pos), new ScriptedRandom(1)), "grew without a successful roll");
        helper.assertTrue(helper.getBlockState(CROP).getValue(InfusedGrainBlock.AGE) == 0, "age changed without a roll");
        helper.assertTrue(grain.tryGrow(level, pos, level.getBlockState(pos), new ScriptedRandom(0)), "did not grow on successful roll");
        helper.assertTrue(helper.getBlockState(CROP).getValue(InfusedGrainBlock.AGE) == 1, "age after growth");
        helper.assertTrue(helper.getBlockState(CROP).getValue(InfusedGrainBlock.KIND) == CropKind.IGNIS, "kind lost on growth");
        helper.assertTrue(level.getBlockEntity(pos) == grain, "block entity replaced on growth");
        for (int index = 0; index < 10; index++) {
            grain.tryGrow(level, pos, level.getBlockState(pos), new ScriptedRandom(0));
        }
        helper.assertTrue(helper.getBlockState(CROP).getValue(InfusedGrainBlock.AGE) == 7, "age is capped at 7");
        helper.assertTrue(grain.growthRoll(new ScriptedRandom(15059)) == false, "base growth bound is 15060");
        helper.assertTrue(grain.growthRoll(new ScriptedRandom(0)), "base growth roll");
        grain.setup(Aspect.FIRE, new AspectList().add(Aspect.WATER, 50));
        helper.assertTrue(grain.growthRoll(new ScriptedRandom(59)) == false && grain.growthRoll(new ScriptedRandom(60)), "water tendency bound is 60");
        helper.succeed();
    }

    private static void matureDrops(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(SOIL, Blocks.FARMLAND.defaultBlockState());
        plant(helper, SOIL, Aspect.AIR, new AspectList().add(Aspect.FIRE, 4));
        setAge(helper, CROP, 7);
        BlockPos pos = helper.absolutePos(CROP);
        List<ItemStack> drops = Block.getDrops(helper.getBlockState(CROP), helper.getLevel(), pos, null);
        helper.assertTrue(count(drops, CropsFeature.AIR_GRAIN.get()) >= 1, "no grain of aspect " + drops);
        ItemStack seeds = drops.stream().filter(stack -> stack.is(CropsFeature.INFUSED_SEEDS.get())).findFirst().orElse(ItemStack.EMPTY);
        helper.assertTrue(!seeds.isEmpty() && InfusedSeedsItem.getAspect(seeds) == Aspect.AIR, "seed aspect " + seeds);
        helper.assertTrue(InfusedSeedsItem.getTendencies(seeds).getAmount(Aspect.FIRE) == 4, "seed tendencies");
        helper.assertTrue(soil(helper, SOIL).getAspects().getAmount(Aspect.AIR) >= 1, "soil not fertilized");

        helper.setBlock(CROP, CropsFeature.INFUSED_GRAIN.get().stateFor(Aspect.WATER).setValue(InfusedGrainBlock.AGE, 7));
        InfusedGrainBlockEntity water = (InfusedGrainBlockEntity) helper.getLevel().getBlockEntity(pos);
        water.setup(Aspect.WATER, new AspectList());
        setAge(helper, CROP, 7);
        drops = Block.getDrops(helper.getBlockState(CROP), helper.getLevel(), pos, water);
        helper.assertTrue(count(drops, CropsFeature.WATER_GRAIN.get()) >= 1, "no water grain " + drops);
        helper.succeed();
    }

    private static void immatureDrops(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(SOIL, Blocks.FARMLAND.defaultBlockState());
        plant(helper, SOIL, Aspect.EARTH, new AspectList());
        setAge(helper, CROP, 6);
        List<ItemStack> drops = Block.getDrops(helper.getBlockState(CROP), helper.getLevel(), helper.absolutePos(CROP), null);
        helper.assertTrue(drops.size() == 1 && drops.getFirst().is(CropsFeature.INFUSED_SEEDS.get()), "drops " + drops);
        helper.assertTrue(InfusedSeedsItem.getAspect(drops.getFirst()) == Aspect.EARTH, "aspect");
        helper.assertTrue(soil(helper, SOIL).getAspects().isEmpty(), "soil fertilized by immature crop");
        helper.succeed();
    }

    private static void mutation(GameTestHelper helper) {
        AspectList residue = new AspectList().add(Aspect.AIR, 3);
        helper.assertTrue(InfusedGrainBlock.mutate(Aspect.FIRE, residue, 0, new ScriptedRandom(0)) == Aspect.LIGHT, "air + fire is lux");
        helper.assertTrue(InfusedGrainBlock.mutate(Aspect.FIRE, residue, 0, new ScriptedRandom(9)) == Aspect.FIRE, "roll above chance");
        helper.assertTrue(InfusedGrainBlock.mutate(Aspect.FIRE, new AspectList().add(Aspect.AIR, 1), 0, new ScriptedRandom(1)) == Aspect.FIRE, "chance is amount squared");
        helper.assertTrue(InfusedGrainBlock.mutate(Aspect.FIRE, new AspectList().add(Aspect.AIR, 1), 1, new ScriptedRandom(1)) == Aspect.LIGHT, "ignis raises the chance");
        helper.assertTrue(InfusedGrainBlock.mutate(Aspect.AIR, new AspectList().add(Aspect.AIR, 20), 0, new ScriptedRandom(0)) == Aspect.AIR, "same primal has no result");
        helper.assertTrue(InfusedGrainBlock.mutate(Aspect.WATER, new AspectList(), 5, new ScriptedRandom(0)) == Aspect.WATER, "empty soil");
        helper.assertTrue(InfusedGrainBlock.mutate(Aspect.MAN, new AspectList().add(Aspect.EARTH, 2), 0, new ScriptedRandom(0)) == Aspect.MINE, "compound mutation");
        helper.assertTrue(InfusedGrainBlock.mutate(Aspect.FIRE, new AspectList().add(Aspect.AIR, 3).add(Aspect.ENTROPY, 3), 0, new ScriptedRandom(9, 0)) == Aspect.COLD, "second residue is tried");

        clear(helper);
        helper.setBlock(SOIL, Blocks.FARMLAND.defaultBlockState());
        InfusedGrainBlockEntity grain = plant(helper, SOIL, Aspect.FIRE, new AspectList());
        soil(helper, SOIL).getAspects().add(Aspect.AIR, 3);
        setAge(helper, CROP, 7);
        List<ItemStack> drops = InfusedGrainBlock.drops(helper.getLevel(), grain, true, new ScriptedRandom(0, 99));
        ItemStack seeds = drops.getFirst();
        helper.assertTrue(InfusedSeedsItem.getAspect(seeds) == Aspect.LIGHT, "mutated seeds " + InfusedSeedsItem.getAspect(seeds));
        helper.assertTrue(count(drops, CropsFeature.FIRE_GRAIN.get()) == 1, "loot follows the crop aspect, not the seed " + drops);
        helper.assertTrue(soil(helper, SOIL).getAspects().getAmount(Aspect.FIRE) == 1, "soil receives the crop aspect");
        helper.succeed();
    }

    private static void tendencyEffects(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(SOIL, Blocks.FARMLAND.defaultBlockState());
        InfusedGrainBlockEntity grain = plant(helper, SOIL, Aspect.AIR, new AspectList().add(Aspect.ENTROPY, 50));
        setAge(helper, CROP, 7);
        List<ItemStack> drops = InfusedGrainBlock.drops(helper.getLevel(), grain, true, new ScriptedRandom(0, 9999));
        helper.assertTrue(count(drops, CropsFeature.INFUSED_SEEDS.get()) == 2, "entropy seeds " + drops);

        grain.setup(Aspect.AIR, new AspectList().add(Aspect.ORDER, 50));
        drops = InfusedGrainBlock.drops(helper.getLevel(), grain, true, new ScriptedRandom(0, 74, 1, 0, 0, 0, 74));
        helper.assertTrue(count(drops, CropsFeature.AIR_GRAIN.get()) == 2, "order grain " + drops + " residue " + soil(helper, SOIL).getAspects() + " tendencies " + grain.getTendencies());

        soil(helper, SOIL).setAspects(new AspectList());
        grain.setup(Aspect.AIR, new AspectList().add(Aspect.EARTH, 50));
        InfusedGrainBlock.fertilizeSoil(helper.getLevel(), grain, new ScriptedRandom(0, 54));
        helper.assertTrue(soil(helper, SOIL).getAspects().getAmount(Aspect.AIR) == 2, "earth residue " + soil(helper, SOIL).getAspects());

        grain.setup(Aspect.AIR, new AspectList().add(Aspect.AIR, 25).add(Aspect.FIRE, 25).add(Aspect.WATER, 10));
        grain.reduceSaturatedAspects(new ScriptedRandom(0, 1, 2));
        helper.assertTrue(grain.getTendencies().visSize() == 50, "tendency cap " + grain.getTendencies());
        helper.assertTrue(grain.exchangeRoll(new ScriptedRandom(0)) == false, "primal crops do not exchange");
        grain.setAspect(Aspect.MAN);
        helper.assertTrue(grain.exchangeRoll(new ScriptedRandom(0)), "compound crop exchange roll");
        helper.assertTrue(!grain.exchangeRoll(new ScriptedRandom(1)), "exchange roll chance");
        grain.setup(Aspect.MAN, new AspectList().add(Aspect.AIR, 50));
        helper.assertTrue(grain.exchangeRoll(new ScriptedRandom(499)) == false, "air bound is 500");
        helper.assertTrue(grain.exchangeRoll(new ScriptedRandom(0)), "air bound roll");
        helper.succeed();
    }

    private static void tendencyExchange(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(SOIL, Blocks.FARMLAND.defaultBlockState());
        helper.setBlock(NEIGHBOR_SOIL, Blocks.FARMLAND.defaultBlockState());
        InfusedGrainBlockEntity receiver = plant(helper, SOIL, Aspect.MAN, new AspectList());
        InfusedGrainBlockEntity primal = plant(helper, NEIGHBOR_SOIL, Aspect.FIRE, new AspectList());
        ServerLevel level = helper.getLevel();
        for (int index = 0; index < 8; index++) {
            receiver.exchange(level, new ScriptedRandom(0));
        }
        helper.assertTrue(receiver.tendency(Aspect.FIRE) == 5, "primal neighbour gives at most 5 " + receiver.getTendencies());
        helper.assertTrue(primal.getTendencies().isEmpty(), "primal neighbour untouched");

        InfusedGrainBlockEntity donor = plant(helper, NEIGHBOR_SOIL, Aspect.LIGHT, new AspectList().add(Aspect.WATER, 3));
        helper.assertTrue(receiver.exchange(level, new ScriptedRandom(0)), "drain from compound neighbour");
        helper.assertTrue(receiver.tendency(Aspect.WATER) == 1 && donor.tendency(Aspect.WATER) == 2, "drain amounts " + receiver.getTendencies() + " " + donor.getTendencies());
        helper.assertTrue(receiver.exchange(level, new ScriptedRandom(0)), "second drain");
        helper.assertTrue(receiver.tendency(Aspect.WATER) == 2 && donor.tendency(Aspect.WATER) == 1, "second drain amounts");
        receiver.getTendencies().add(Aspect.WATER, 5);
        helper.assertTrue(!receiver.exchange(level, new ScriptedRandom(0)), "no drain from a neighbour with fewer points");
        helper.assertTrue(receiver.tendency(Aspect.WATER) == 7 && donor.tendency(Aspect.WATER) == 1, "unchanged amounts");
        helper.succeed();
    }

    private static void residueCapped(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(SOIL, CropsFeature.INFUSED_FARMLAND.get().defaultBlockState());
        InfusedFarmlandBlockEntity soil = soil(helper, SOIL);
        soil.getAspects().add(Aspect.AIR, 30).add(Aspect.FIRE, 10);
        soil.reduceSaturatedAspects(new ScriptedRandom(0, 1));
        helper.assertTrue(soil.getAspects().visSize() == 20, "residue cap " + soil.getAspects());
        helper.succeed();
    }

    private static void lootTable(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        for (Aspect aspect : Aspect.values()) {
            AspectCropLootManager.getLootForAspect(aspect, registries, RandomSource.create(1L));
        }
        ItemStack air = AspectCropLootManager.getLootForAspect(Aspect.AIR, registries, RandomSource.create(1L));
        helper.assertTrue(air.is(CropsFeature.AIR_GRAIN.get()) && air.getCount() == 1, "air loot " + air);
        ItemStack order = AspectCropLootManager.getLootForAspect(Aspect.ORDER, registries, RandomSource.create(1L));
        helper.assertTrue(order.is(Items.GLASS) && order.getCount() == 64, "order loot " + order);
        ItemStack entropy = AspectCropLootManager.getLootForAspect(Aspect.ENTROPY, registries, RandomSource.create(1L));
        helper.assertTrue(entropy.is(Items.SAND) && entropy.getCount() == 64, "entropy loot " + entropy);
        helper.assertTrue(AspectCropLootManager.getLootForAspect(Aspect.WEATHER, registries, RandomSource.create(1L)).isEmpty(), "weather has loot");
        var eldritch = AspectCropLootManager.entries(Aspect.ELDRITCH, registries);
        helper.assertTrue(eldritch.size() == 2 && eldritch.get(0).weight() == 10 && eldritch.get(1).weight() == 5, "eldritch entries");
        helper.assertTrue(AspectCropLootManager.getLootForAspect(Aspect.ELDRITCH, registries, new ScriptedRandom(9)).is(Items.ENDER_PEARL), "eldritch pearl");
        helper.assertTrue(AspectCropLootManager.getLootForAspect(Aspect.ELDRITCH, registries, new ScriptedRandom(10)).is(Items.ENDER_EYE), "eldritch eye");
        var metal = AspectCropLootManager.entries(Aspect.METAL, registries);
        helper.assertTrue(metal.getFirst().stack().is(Items.IRON_INGOT) && metal.getFirst().stack().getCount() == 4 && metal.getFirst().weight() == 100, "metal entry");
        var aura = AspectCropLootManager.entries(Aspect.AURA, registries);
        helper.assertTrue(aura.size() == Aspect.values().length, "wisp essences " + aura.size());
        var mind = AspectCropLootManager.entries(Aspect.MIND, registries);
        helper.assertTrue(mind.size() > 3 && mind.get(3).stack().is(Items.ENCHANTED_BOOK), "mind books");
        var man = AspectCropLootManager.entries(Aspect.MAN, registries);
        helper.assertTrue(man.size() == 12, "golem cores " + man.size());
        var cloth = AspectCropLootManager.entries(Aspect.CLOTH, registries);
        helper.assertTrue(cloth.size() == 18 && cloth.get(0).weight() == 30 && cloth.get(1).weight() == 10, "cloth entries");
        helper.succeed();
    }

    private static void bonemeal(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(SOIL, Blocks.FARMLAND.defaultBlockState());
        plant(helper, SOIL, Aspect.AIR, new AspectList());
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(CROP);
        boolean wasEnabled = Config.cropsAllowBonemeal();
        helper.assertTrue(!wasEnabled, "bonemeal is enabled by default");
        boolean applied = BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, null);
        helper.assertTrue(!applied && helper.getBlockState(CROP).getValue(InfusedGrainBlock.AGE) == 0, "bonemeal worked while disabled");
        if (Config.SPEC.isLoaded()) {
            Config.CROPS_ALLOW_BONEMEAL.set(true);
            try {
                helper.assertTrue(Config.cropsAllowBonemeal(), "config not applied");
                applied = BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, null);
                helper.assertTrue(applied && helper.getBlockState(CROP).getValue(InfusedGrainBlock.AGE) >= 2, "bonemeal ignored when enabled");
                helper.assertTrue(helper.getBlockState(CROP).getValue(InfusedGrainBlock.KIND) == CropKind.AER, "kind lost on bonemeal");
                setAge(helper, CROP, 7);
                helper.assertTrue(!BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, null), "bonemeal on mature crop");
            } finally {
                Config.CROPS_ALLOW_BONEMEAL.set(false);
            }
        }
        helper.succeed();
    }

    private static void persists(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(SOIL, Blocks.FARMLAND.defaultBlockState());
        InfusedGrainBlockEntity grain = plant(helper, SOIL, Aspect.MAN, new AspectList().add(Aspect.FIRE, 7).add(Aspect.WATER, 2));
        soil(helper, SOIL).getAspects().add(Aspect.EARTH, 4);
        var access = helper.getLevel().registryAccess();
        CompoundTag saved = grain.saveWithFullMetadata(access);
        BlockEntity loaded = BlockEntity.loadStatic(grain.getBlockPos(), grain.getBlockState(), saved, access);
        helper.assertTrue(loaded instanceof InfusedGrainBlockEntity, "crop did not load");
        InfusedGrainBlockEntity restored = (InfusedGrainBlockEntity) loaded;
        helper.assertTrue(restored.getAspect() == Aspect.MAN, "aspect lost");
        helper.assertTrue(restored.getTendencies().equals(grain.getTendencies()), "tendencies lost " + restored.getTendencies());
        CompoundTag update = grain.getUpdateTag(access);
        InfusedGrainBlockEntity synced = new InfusedGrainBlockEntity(grain.getBlockPos(), grain.getBlockState());
        synced.loadCustomOnly(update, access);
        helper.assertTrue(synced.getAspect() == Aspect.MAN && synced.tendency(Aspect.FIRE) == 7, "update tag");

        InfusedFarmlandBlockEntity farmland = soil(helper, SOIL);
        BlockEntity farmlandLoaded = BlockEntity.loadStatic(farmland.getBlockPos(), farmland.getBlockState(), farmland.saveWithFullMetadata(access), access);
        helper.assertTrue(farmlandLoaded instanceof InfusedFarmlandBlockEntity restoredSoil && restoredSoil.getAspects().getAmount(Aspect.EARTH) == 4, "residue lost");
        helper.succeed();
    }
}
