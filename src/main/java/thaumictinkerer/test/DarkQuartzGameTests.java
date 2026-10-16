package thaumictinkerer.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.crafting.CraftingRecipeRef;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumictinkerer.darkquartz.DarkQuartzFeature;
import thaumictinkerer.research.TtResearch;

public final class DarkQuartzGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("dark_quartz")
        .add("research_registered", DarkQuartzGameTests::researchRegistered)
        .add("recipes_resolve", DarkQuartzGameTests::recipesResolve)
        .add("block_properties", DarkQuartzGameTests::blockProperties)
        .add("placement_and_drops", DarkQuartzGameTests::placementAndDrops);

    private DarkQuartzGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem research = ResearchCategories.getResearch(DarkQuartzFeature.RESEARCH);
        helper.assertTrue(research != null, "DARK_QUARTZ is not registered");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), "wrong category " + research.category());
        helper.assertTrue(research.column() == -2 && research.row() == 2, "wrong position");
        helper.assertTrue(research.isStub() && research.isAutoUnlock() && research.isRound(), "flags are wrong");
        helper.assertTrue(!research.isSecondary() && !research.isConcealed(), "unexpected flags");
        helper.assertTrue(research.parentsHidden().isEmpty() && research.parents().isEmpty(), "unexpected parents");
        helper.assertTrue(research.pages().size() == 7, "expected 7 pages, got " + research.pages().size());
        helper.assertTrue(
            research.pages().getFirst() instanceof ResearchPage.Text text && text.text().equals("tc.research_page.DARK_QUARTZ.1"),
            "text page key is wrong"
        );
        helper.succeed();
    }

    private static void recipesResolve(GameTestHelper helper) {
        for (String key : List.of("DARK_QUARTZ0", "DARK_QUARTZ0_COAL", "DARK_QUARTZ1", "DARK_QUARTZ2", "DARK_QUARTZ3", "DARK_QUARTZ4", "DARK_QUARTZ5")) {
            helper.assertTrue(TcRecipes.get(key) instanceof CraftingRecipeRef, "recipe ref missing: " + key);
            CraftingRecipeRef reference = (CraftingRecipeRef) TcRecipes.get(key);
            helper.assertTrue(reference.result().exists(), "result item missing for " + key);
            boolean present = helper.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, reference.recipe())).isPresent();
            helper.assertTrue(present, "crafting recipe missing in datapack: " + reference.recipe());
        }
        helper.succeed();
    }

    private static void blockProperties(GameTestHelper helper) {
        List<Block> blocks = List.of(
            DarkQuartzFeature.DARK_QUARTZ_BLOCK.get(),
            DarkQuartzFeature.CHISELED_DARK_QUARTZ_BLOCK.get(),
            DarkQuartzFeature.DARK_QUARTZ_PILLAR.get(),
            DarkQuartzFeature.DARK_QUARTZ_SLAB.get(),
            DarkQuartzFeature.DARK_QUARTZ_STAIRS.get()
        );
        BlockPos pos = new BlockPos(1, 1, 1);
        for (Block block : blocks) {
            helper.setBlock(pos, block.defaultBlockState());
            BlockState state = helper.getBlockState(pos);
            helper.assertTrue(state.getDestroySpeed(helper.getLevel(), helper.absolutePos(pos)) == 0.8F, "hardness is wrong for " + block);
            helper.assertTrue(block.getExplosionResistance() == 10.0F, "resistance is wrong for " + block);
            helper.assertTrue(state.requiresCorrectToolForDrops(), "tool is not required for " + block);
        }
        helper.succeed();
    }

    private static void placementAndDrops(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, DarkQuartzFeature.DARK_QUARTZ_PILLAR.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        helper.assertBlockProperty(pos, RotatedPillarBlock.AXIS, Direction.Axis.X);
        assertDrops(helper, pos, DarkQuartzFeature.DARK_QUARTZ_PILLAR.get(), 1);

        helper.setBlock(pos, DarkQuartzFeature.DARK_QUARTZ_SLAB.get().defaultBlockState());
        assertDrops(helper, pos, DarkQuartzFeature.DARK_QUARTZ_SLAB.get(), 1);
        helper.setBlock(pos, DarkQuartzFeature.DARK_QUARTZ_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE));
        assertDrops(helper, pos, DarkQuartzFeature.DARK_QUARTZ_SLAB.get(), 2);

        helper.setBlock(pos, DarkQuartzFeature.DARK_QUARTZ_BLOCK.get().defaultBlockState());
        assertDrops(helper, pos, DarkQuartzFeature.DARK_QUARTZ_BLOCK.get(), 1);
        helper.setBlock(pos, DarkQuartzFeature.CHISELED_DARK_QUARTZ_BLOCK.get().defaultBlockState());
        assertDrops(helper, pos, DarkQuartzFeature.CHISELED_DARK_QUARTZ_BLOCK.get(), 1);
        helper.setBlock(pos, DarkQuartzFeature.DARK_QUARTZ_STAIRS.get().defaultBlockState());
        assertDrops(helper, pos, DarkQuartzFeature.DARK_QUARTZ_STAIRS.get(), 1);
        helper.succeed();
    }

    private static void assertDrops(GameTestHelper helper, BlockPos pos, Block block, int count) {
        List<ItemStack> drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos), null);
        int total = drops.stream().filter(stack -> stack.is(block.asItem())).mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(total == count && drops.size() == 1, block + " drops " + drops + ", expected " + count);
    }
}
