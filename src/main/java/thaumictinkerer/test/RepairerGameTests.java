package thaumictinkerer.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.essentia.TubeBlockEntity;
import thaumcraft.registry.ModBlocks;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumictinkerer.repairer.RepairerBlock;
import thaumictinkerer.repairer.RepairerBlockEntity;
import thaumictinkerer.repairer.RepairerFeature;
import thaumictinkerer.research.TtResearch;

public final class RepairerGameTests {
    private static final BlockPos REPAIRER = new BlockPos(3, 2, 3);
    private static final BlockPos TUBE = new BlockPos(3, 2, 2);

    private static final TtGameTestSuite SUITE = new TtGameTestSuite("repairer")
        .add("research_registered", RepairerGameTests::researchRegistered)
        .add("recipe_registered", RepairerGameTests::recipeRegistered)
        .add("accepts_only_damageable", RepairerGameTests::acceptsOnlyDamageable)
        .add("repairs_with_essentia", RepairerGameTests::repairsWithEssentia)
        .add("does_nothing_without_essentia", RepairerGameTests::nothingWithoutEssentia)
        .add("emits_redstone_while_damaged", RepairerGameTests::redstone)
        .add("persists", RepairerGameTests::persists);

    private RepairerGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem research = ResearchCategories.getResearch("REPAIRER");
        helper.assertTrue(research != null && TtResearch.TT_CATEGORY.equals(research.category()), "research missing");
        helper.assertTrue(research.column() == -1 && research.row() == -9 && research.complexity() == 3, "position");
        helper.assertTrue(research.parents().equals(List.of("FUNNEL")), "parents");
        helper.assertTrue(research.parentsHidden().containsAll(List.of("THAUMIUM", "ENCHFABRIC", "INFUSION")), "hidden parents " + research.parentsHidden());
        helper.assertTrue(research.isConcealed() && !research.isSecondary() && !research.isRound(), "flags");
        helper.assertTrue(
            research.tags().getAmount(Aspect.TOOL) == 2 && research.tags().getAmount(Aspect.CRAFT) == 1
                && research.tags().getAmount(Aspect.ORDER) == 1 && research.tags().getAmount(Aspect.MAGIC) == 1,
            "aspects"
        );
        helper.assertTrue(research.pages().size() == 2 && research.pages().get(1) instanceof ResearchPage.Infusion, "pages");
        helper.succeed();
    }

    private static void recipeRegistered(GameTestHelper helper) {
        helper.assertTrue(
            TcRecipes.get("REPAIRER") instanceof InfusionRecipe recipe
                && recipe.instability() == 8
                && recipe.components().size() == 8
                && recipe.aspects().getAmount(Aspect.CRAFT) == 20
                && recipe.result().exists(),
            "infusion recipe"
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

    private static RepairerBlockEntity setup(GameTestHelper helper, int damage) {
        clear(helper);
        helper.setBlock(REPAIRER, RepairerFeature.REPAIRER.get().defaultBlockState().setValue(RepairerBlock.FACING, Direction.NORTH));
        RepairerBlockEntity repairer = (RepairerBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(REPAIRER));
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.setDamageValue(damage);
        repairer.setTheItem(pickaxe);
        return repairer;
    }

    private static TubeBlockEntity tube(GameTestHelper helper, int essentia) {
        helper.setBlock(TUBE, ModBlocks.ESSENTIA_TUBE.get().defaultBlockState());
        TubeBlockEntity tube = (TubeBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(TUBE));
        tube.setSuction(Aspect.TOOL, 100);
        if (essentia > 0) {
            tube.addEssentia(Aspect.TOOL, essentia, Direction.SOUTH);
        }
        return tube;
    }

    private static void acceptsOnlyDamageable(GameTestHelper helper) {
        RepairerBlockEntity repairer = setup(helper, 0);
        helper.assertTrue(repairer.accepts(new ItemStack(Items.DIAMOND_SWORD)), "tool rejected");
        helper.assertTrue(!repairer.accepts(new ItemStack(Items.DIRT)) && !repairer.accepts(ItemStack.EMPTY), "non tool accepted");
        helper.succeed();
    }

    private static void repairsWithEssentia(GameTestHelper helper) {
        RepairerBlockEntity repairer = setup(helper, 100);
        tube(helper, 2);
        repairer.repairCycle(helper.getLevel());
        helper.assertTrue(repairer.getTheItem().getDamageValue() == 92, "damage " + repairer.getTheItem().getDamageValue());
        repairer.repairCycle(helper.getLevel());
        helper.assertTrue(repairer.getTheItem().getDamageValue() == 92, "repaired without essentia");
        helper.succeed();
    }

    private static void nothingWithoutEssentia(GameTestHelper helper) {
        RepairerBlockEntity repairer = setup(helper, 100);
        repairer.repairCycle(helper.getLevel());
        helper.assertTrue(repairer.getTheItem().getDamageValue() == 100, "repaired without a tube");
        tube(helper, 0);
        repairer.repairCycle(helper.getLevel());
        helper.assertTrue(repairer.getTheItem().getDamageValue() == 100, "repaired with an empty tube");
        helper.succeed();
    }

    private static void redstone(GameTestHelper helper) {
        RepairerBlockEntity repairer = setup(helper, 10);
        BlockPos absolute = helper.absolutePos(REPAIRER);
        helper.assertTrue(helper.getBlockState(REPAIRER).getDirectSignal(helper.getLevel(), absolute, Direction.UP) == 15, "no signal while damaged");
        repairer.getTheItem().setDamageValue(0);
        helper.assertTrue(helper.getBlockState(REPAIRER).getDirectSignal(helper.getLevel(), absolute, Direction.UP) == 0, "signal on repaired item");
        helper.succeed();
    }

    private static void persists(GameTestHelper helper) {
        RepairerBlockEntity repairer = setup(helper, 42);
        CompoundTag saved = repairer.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity loaded = BlockEntity.loadStatic(repairer.getBlockPos(), repairer.getBlockState(), saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded instanceof RepairerBlockEntity, "did not load");
        ItemStack item = ((RepairerBlockEntity) loaded).getTheItem();
        helper.assertTrue(item.is(Items.IRON_PICKAXE) && item.getDamageValue() == 42, "item lost");
        helper.succeed();
    }
}
