package thaumictinkerer.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModItems;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumictinkerer.research.TtResearch;
import thaumictinkerer.tablet.DynamismTabletBlock;
import thaumictinkerer.tablet.DynamismTabletBlockEntity;
import thaumictinkerer.tablet.TabletFeature;

public final class TabletGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("tablet")
        .add("research_registered", TabletGameTests::researchRegistered)
        .add("recipe_registered", TabletGameTests::recipeRegistered)
        .add("persists_inventory", TabletGameTests::persistsInventory)
        .add("break_mode_collects_drops", TabletGameTests::breakMode)
        .add("redstone_mode_waits_for_signal", TabletGameTests::redstoneMode)
        .add("right_click_places_block", TabletGameTests::rightClick)
        .add("wand_rotates_when_idle", TabletGameTests::wandRotates);

    private TabletGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem research = ResearchCategories.getResearch(TabletFeature.ANIMATION_TABLET);
        helper.assertTrue(research != null, "ANIMATION_TABLET is not registered");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), "wrong category");
        helper.assertTrue(research.column() == -8 && research.row() == 2, "wrong position");
        helper.assertTrue(research.warp() == 1, "wrong warp " + research.warp());
        helper.assertTrue(research.parents().equals(List.of("MAGNETS")), "wrong parents " + research.parents());
        helper.assertTrue(
            research.tags().getAmount(Aspect.MECHANISM) == 2 && research.tags().getAmount(Aspect.METAL) == 1
                && research.tags().getAmount(Aspect.MOTION) == 1 && research.tags().getAmount(Aspect.ENERGY) == 1,
            "wrong aspects"
        );
        helper.assertTrue(research.pages().size() == 2, "wrong page count");
        helper.succeed();
    }

    private static void recipeRegistered(GameTestHelper helper) {
        ShapedArcaneRecipe recipe = (ShapedArcaneRecipe) TcRecipes.get("ANIMATION_TABLET");
        helper.assertTrue(recipe != null && recipe.research().equals("ANIMATION_TABLET"), "recipe missing");
        helper.assertTrue(recipe.width() == 3 && recipe.height() == 2, "shape");
        helper.assertTrue(recipe.aspects().getAmount(Aspect.AIR) == 25 && recipe.aspects().getAmount(Aspect.ORDER) == 15 && recipe.aspects().getAmount(Aspect.FIRE) == 10, "aspects");
        helper.assertTrue(recipe.grid().get(0).equals(TcIngredient.item("minecraft:gold_ingot")) && recipe.grid().get(1).equals(TcIngredient.item("minecraft:iron_ingot")), "ingots");
        helper.assertTrue(recipe.grid().get(4).equals(TcIngredient.item("thaumcraft:golem_core_blank")) && recipe.grid().get(4).exists(), "core");
        helper.assertTrue(recipe.result().item().toString().equals("thaumictinkerer:dynamism_tablet"), "result");
        helper.succeed();
    }

    private static DynamismTabletBlockEntity place(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, TabletFeature.DYNAMISM_TABLET.get().defaultBlockState().setValue(DynamismTabletBlock.FACING, facing));
        return (DynamismTabletBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static void persistsInventory(GameTestHelper helper) {
        DynamismTabletBlockEntity tablet = place(helper, new BlockPos(2, 1, 3), Direction.NORTH);
        tablet.setItem(0, new ItemStack(Items.DIAMOND_PICKAXE));
        tablet.setModes(false, true);
        CompoundTag saved = tablet.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity loaded = BlockEntity.loadStatic(tablet.getBlockPos(), tablet.getBlockState(), saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded instanceof DynamismTabletBlockEntity, "block entity did not load");
        DynamismTabletBlockEntity copy = (DynamismTabletBlockEntity) loaded;
        helper.assertTrue(copy.getItem(0).is(Items.DIAMOND_PICKAXE), "item lost");
        helper.assertTrue(!copy.isLeftClick() && copy.isRedstone(), "modes lost");
        helper.succeed();
    }

    private static int dropCount(GameTestHelper helper, BlockPos around, net.minecraft.world.item.Item item) {
        AABB box = new AABB(helper.absolutePos(around)).inflate(2.0);
        int total = 0;
        for (ItemEntity entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, box)) {
            if (entity.getItem().is(item)) {
                total += entity.getItem().getCount();
            }
        }
        return total;
    }

    private static void breakMode(GameTestHelper helper) {
        BlockPos target = new BlockPos(2, 1, 2);
        helper.setBlock(target, Blocks.NETHERRACK);
        DynamismTabletBlockEntity tablet = place(helper, new BlockPos(2, 1, 3), Direction.NORTH);
        tablet.setItem(0, new ItemStack(Items.DIAMOND_PICKAXE));
        helper.runAfterDelay(80, () -> {
            helper.assertTrue(helper.getBlockState(target).isAir(), "block was not broken");
            helper.assertTrue(dropCount(helper, target, Items.NETHERRACK) >= 1, "drops were not collected");
            helper.assertTrue(tablet.getItem(0).is(Items.DIAMOND_PICKAXE) && tablet.getItem(0).getDamageValue() > 0, "tool was not used");
            helper.succeed();
        });
    }

    private static void redstoneMode(GameTestHelper helper) {
        BlockPos target = new BlockPos(2, 1, 2);
        helper.setBlock(target, Blocks.NETHERRACK);
        DynamismTabletBlockEntity tablet = place(helper, new BlockPos(2, 1, 3), Direction.NORTH);
        tablet.setItem(0, new ItemStack(Items.DIAMOND_PICKAXE));
        tablet.setModes(true, true);
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(helper.getBlockState(target).is(Blocks.NETHERRACK), "tablet acted without a redstone signal");
            helper.setBlock(new BlockPos(2, 2, 3), Blocks.REDSTONE_BLOCK);
        });
        helper.runAfterDelay(90, () -> {
            helper.assertTrue(helper.getBlockState(target).isAir(), "tablet ignored the redstone signal");
            helper.succeed();
        });
    }

    private static void rightClick(GameTestHelper helper) {
        BlockPos target = new BlockPos(2, 1, 2);
        DynamismTabletBlockEntity tablet = place(helper, new BlockPos(2, 1, 3), Direction.NORTH);
        tablet.setItem(0, new ItemStack(Items.COBBLESTONE, 5));
        tablet.setModes(false, true);
        helper.setBlock(new BlockPos(2, 2, 3), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(helper.getBlockState(target).is(Blocks.COBBLESTONE), "block was not placed");
            helper.assertTrue(tablet.getItem(0).getCount() == 4, "stack count is " + tablet.getItem(0).getCount());
            helper.succeed();
        });
    }

    private static void wandRotates(GameTestHelper helper) {
        place(helper, new BlockPos(2, 1, 3), Direction.NORTH);
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack wand = new ItemStack(ModItems.WAND.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        BlockPos absolute = helper.absolutePos(new BlockPos(2, 1, 3));
        BlockState state = helper.getBlockState(new BlockPos(2, 1, 3));
        InteractionResult result = state.useItemOn(wand, helper.getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        helper.assertTrue(result.consumesAction(), "wand click was not consumed");
        helper.assertBlockProperty(new BlockPos(2, 1, 3), DynamismTabletBlock.FACING, Direction.SOUTH);
        helper.succeed();
    }
}
