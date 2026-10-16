package thaumictinkerer.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.essentia.FilledJarItem;
import thaumcraft.essentia.JarBlockEntity;
import thaumcraft.registry.ModBlocks;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumictinkerer.funnel.FunnelBlockEntity;
import thaumictinkerer.funnel.FunnelFeature;
import thaumictinkerer.research.TtResearch;

public final class FunnelGameTests {
    private static final BlockPos FUNNEL = new BlockPos(3, 3, 3);
    private static final BlockPos HOPPER = new BlockPos(3, 2, 3);
    private static final BlockPos JAR = new BlockPos(4, 2, 3);

    private static final TtGameTestSuite SUITE = new TtGameTestSuite("funnel")
        .add("research_registered", FunnelGameTests::researchRegistered)
        .add("recipe_registered", FunnelGameTests::recipeRegistered)
        .add("needs_hopper", FunnelGameTests::needsHopper)
        .add("drains_jar_into_jar", FunnelGameTests::drainsJar)
        .add("respects_jar_contents", FunnelGameTests::respectsContents)
        .add("only_filled_jars", FunnelGameTests::onlyFilledJars)
        .add("persists", FunnelGameTests::persists);

    private FunnelGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem research = ResearchCategories.getResearch("FUNNEL");
        helper.assertTrue(research != null && TtResearch.TT_CATEGORY.equals(research.category()), "research missing");
        helper.assertTrue(research.column() == 0 && research.row() == -7 && research.complexity() == 1, "position");
        helper.assertTrue(research.parents().equals(List.of("BRIGHT_NITOR")), "parents");
        helper.assertTrue(research.parentsHidden().equals(List.of("DISTILESSENTIA")), "hidden parents " + research.parentsHidden());
        helper.assertTrue(research.isSecondary() && research.isConcealed() && !research.isRound(), "flags");
        helper.assertTrue(research.tags().getAmount(Aspect.TOOL) == 1 && research.tags().getAmount(Aspect.TRAVEL) == 2, "aspects");
        helper.assertTrue(research.pages().size() == 2 && research.pages().get(1) instanceof ResearchPage.Arcane, "pages");
        helper.assertTrue(ResearchCategories.getResearch("BRIGHT_NITOR") != null, "parent missing");
        helper.succeed();
    }

    private static void recipeRegistered(GameTestHelper helper) {
        helper.assertTrue(
            TcRecipes.get("FUNNEL") instanceof ShapedArcaneRecipe recipe
                && recipe.width() == 3
                && recipe.height() == 1
                && recipe.aspects().getAmount(Aspect.ORDER) == 1
                && recipe.aspects().getAmount(Aspect.ENTROPY) == 1
                && recipe.result().exists(),
            "arcane recipe"
        );
        helper.succeed();
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

    private static ItemStack jarItem(Aspect aspect, int amount) {
        ItemStack stack = new ItemStack(ModBlocks.FILLED_JAR.get());
        ModBlocks.FILLED_JAR.get().setAspects(stack, new AspectList().add(aspect, amount));
        return stack;
    }

    private static FunnelBlockEntity setup(GameTestHelper helper, ItemStack jar) {
        clear(helper);
        helper.setBlock(HOPPER, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.EAST));
        helper.setBlock(JAR, ModBlocks.WARDED_JAR.get().defaultBlockState());
        helper.setBlock(FUNNEL, FunnelFeature.FUNNEL.get().defaultBlockState());
        FunnelBlockEntity funnel = (FunnelBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(FUNNEL));
        funnel.setTheItem(jar);
        return funnel;
    }

    private static void tick(GameTestHelper helper, FunnelBlockEntity funnel, int times) {
        for (int index = 0; index < times; index++) {
            FunnelBlockEntity.tick(helper.getLevel(), funnel.getBlockPos(), funnel.getBlockState(), funnel);
        }
    }

    private static JarBlockEntity jar(GameTestHelper helper) {
        return (JarBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(JAR));
    }

    private static void needsHopper(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(FUNNEL, Blocks.AIR.defaultBlockState());
        helper.assertTrue(!FunnelFeature.FUNNEL.get().defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(FUNNEL)), "survives without hopper");
        helper.setBlock(HOPPER, Blocks.HOPPER.defaultBlockState());
        helper.assertTrue(FunnelFeature.FUNNEL.get().defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(FUNNEL)), "does not survive on hopper");
        helper.succeed();
    }

    private static void drainsJar(GameTestHelper helper) {
        FunnelBlockEntity funnel = setup(helper, jarItem(Aspect.TOOL, 3));
        tick(helper, funnel, 2);
        helper.assertTrue(jar(helper).getAmount() == 2 && jar(helper).getAspect() == Aspect.TOOL, "jar has " + jar(helper).getAmount());
        tick(helper, funnel, 5);
        helper.assertTrue(jar(helper).getAmount() == 3, "jar overfilled or short " + jar(helper).getAmount());
        AspectList remaining = ModBlocks.FILLED_JAR.get().getAspects(funnel.getTheItem());
        helper.assertTrue(remaining == null || remaining.size() == 0, "item still holds essentia");
        helper.succeed();
    }

    private static void respectsContents(GameTestHelper helper) {
        FunnelBlockEntity funnel = setup(helper, jarItem(Aspect.TOOL, 3));
        jar(helper).addToContainer(Aspect.FIRE, 2);
        tick(helper, funnel, 3);
        helper.assertTrue(jar(helper).getAmount() == 2 && jar(helper).getAspect() == Aspect.FIRE, "mixed aspects");
        jar(helper).takeFromContainer(Aspect.FIRE, 2);
        jar(helper).setAspectFilter(Aspect.WATER);
        tick(helper, funnel, 3);
        helper.assertTrue(jar(helper).getAmount() == 0, "filter ignored");
        jar(helper).setAspectFilter(null);
        jar(helper).addToContainer(Aspect.TOOL, 63);
        tick(helper, funnel, 3);
        helper.assertTrue(jar(helper).getAmount() == 64, "jar capacity " + jar(helper).getAmount());
        helper.succeed();
    }

    private static void onlyFilledJars(GameTestHelper helper) {
        FunnelBlockEntity funnel = setup(helper, ItemStack.EMPTY);
        helper.assertTrue(funnel.accepts(jarItem(Aspect.TOOL, 1)) && !funnel.accepts(new ItemStack(Items.DIRT)), "accepts");
        helper.assertTrue(funnel.getSlotsForFace(Direction.DOWN).length == 0 && funnel.getSlotsForFace(Direction.UP).length == 1, "slots");
        helper.assertTrue(funnel.getMaxStackSize() == 1, "stack limit");
        helper.succeed();
    }

    private static void persists(GameTestHelper helper) {
        FunnelBlockEntity funnel = setup(helper, jarItem(Aspect.ORDER, 7));
        CompoundTag saved = funnel.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity loaded = BlockEntity.loadStatic(funnel.getBlockPos(), funnel.getBlockState(), saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded instanceof FunnelBlockEntity, "did not load");
        ItemStack item = ((FunnelBlockEntity) loaded).getTheItem();
        helper.assertTrue(item.getItem() instanceof FilledJarItem, "jar lost");
        AspectList aspects = ((FilledJarItem) item.getItem()).getAspects(item);
        helper.assertTrue(aspects != null && aspects.getAmount(Aspect.ORDER) == 7, "essentia lost");
        helper.succeed();
    }
}
