package thaumictinkerer.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.bus.api.IEventBus;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.decor.DecorFeature;
import thaumictinkerer.decor.WardedSlabBlockEntity;

public final class DecorGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("decor")
        .add("recipes_present", DecorGameTests::recipesPresent)
        .add("block_properties", DecorGameTests::blockProperties)
        .add("slab_drops", DecorGameTests::slabDrops)
        .add("travel_effect", DecorGameTests::travelEffect)
        .add("ward_slab_places_aura", DecorGameTests::wardSlabPlacesAura);

    private DecorGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void recipesPresent(GameTestHelper helper) {
        for (String recipe : List.of("travel_slab", "travel_stairs", "ward_slab")) {
            boolean present = helper.getLevel().getServer().getRecipeManager().byKey(ThaumicTinkerer.id(recipe)).isPresent();
            helper.assertTrue(present, "crafting recipe missing: " + recipe);
        }
        helper.succeed();
    }

    private static void blockProperties(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        List<Block> blocks = List.of(DecorFeature.TRAVEL_SLAB.get(), DecorFeature.WARD_SLAB.get());
        for (Block block : blocks) {
            helper.setBlock(pos, block.defaultBlockState());
            BlockState state = helper.getBlockState(pos);
            helper.assertTrue(state.getDestroySpeed(helper.getLevel(), helper.absolutePos(pos)) == 0.8F, "hardness is wrong for " + block);
            helper.assertTrue(block.getExplosionResistance() == 10.0F, "resistance is wrong for " + block);
        }
        helper.setBlock(pos, DecorFeature.TRAVEL_STAIRS.get().defaultBlockState());
        helper.assertTrue(helper.getBlockState(pos).getDestroySpeed(helper.getLevel(), helper.absolutePos(pos)) == 2.0F, "stairs hardness is wrong");
        helper.succeed();
    }

    private static void slabDrops(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        for (Block block : List.of(DecorFeature.TRAVEL_SLAB.get(), DecorFeature.WARD_SLAB.get())) {
            helper.setBlock(pos, block.defaultBlockState());
            assertDrops(helper, pos, block, 1);
            helper.setBlock(pos, block.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE));
            assertDrops(helper, pos, block, 2);
        }
        helper.setBlock(pos, DecorFeature.TRAVEL_STAIRS.get().defaultBlockState());
        assertDrops(helper, pos, DecorFeature.TRAVEL_STAIRS.get(), 1);
        helper.succeed();
    }

    private static void assertDrops(GameTestHelper helper, BlockPos pos, Block block, int count) {
        List<ItemStack> drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos), null);
        int total = drops.stream().filter(stack -> stack.is(block.asItem())).mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(total == count && drops.size() == 1, block + " drops " + drops + ", expected " + count);
    }

    private static void travelEffect(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        for (Block block : List.of(DecorFeature.TRAVEL_SLAB.get(), DecorFeature.TRAVEL_STAIRS.get(), DecorFeature.WARD_SLAB.get())) {
            helper.setBlock(pos, block.defaultBlockState());
            Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(2, 2, 2));
            pig.removeAllEffects();
            block.stepOn(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), pig);
            MobEffectInstance speed = pig.getEffect(MobEffects.MOVEMENT_SPEED);
            MobEffectInstance jump = pig.getEffect(MobEffects.JUMP);
            helper.assertTrue(speed != null && speed.getAmplifier() == 1 && speed.getDuration() == 40, "speed effect of " + block);
            helper.assertTrue(jump != null && jump.getAmplifier() == 0 && jump.getDuration() == 40, "jump effect of " + block);
            pig.discard();
        }
        helper.succeed();
    }

    private static void wardSlabPlacesAura(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, DecorFeature.WARD_SLAB.get().defaultBlockState());
        helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(pos)) instanceof WardedSlabBlockEntity, "no block entity");
        WardedSlabBlockEntity entity = (WardedSlabBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        for (int tick = 0; tick < 200; tick++) {
            WardedSlabBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(pos), entity);
        }
        helper.assertTrue(helper.getBlockState(pos.above()).is(thaumcraft.registry.ModBlocks.WARDING_AURA.get()), "no aura above the slab");
        helper.assertTrue(helper.getBlockState(pos.above(2)).is(thaumcraft.registry.ModBlocks.WARDING_AURA.get()), "no aura two blocks above the slab");
        helper.succeed();
    }
}
