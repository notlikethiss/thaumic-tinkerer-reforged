package thaumictinkerer.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumictinkerer.gas.BrightNitorItem;
import thaumictinkerer.gas.FumeDissipatorItem;
import thaumictinkerer.gas.GasFeature;
import thaumictinkerer.gas.NitorGasBlock;
import thaumictinkerer.gas.SpreadingGasBlock;
import thaumictinkerer.research.TtResearch;

public final class GasGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("gas")
        .add("research_registered", GasGameTests::researchRegistered)
        .add("recipes_registered", GasGameTests::recipesRegistered)
        .add("block_properties", GasGameTests::blockProperties)
        .add("light_gas_spreads", GasGameTests::lightGasSpreads)
        .add("bottle_releases_gas", GasGameTests::bottleReleasesGas)
        .add("shadow_replaces_light", GasGameTests::shadowReplacesLight)
        .add("dissipator_removes_gas", GasGameTests::dissipatorRemovesGas)
        .add("nitor_gas_follows_carrier", GasGameTests::nitorGasFollowsCarrier);

    private GasGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static ResearchItem research(GameTestHelper helper, String key) {
        ResearchItem research = ResearchCategories.getResearch(key);
        helper.assertTrue(research != null, key + " is not registered");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), key + " has the wrong category");
        return research;
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem light = research(helper, GasFeature.GASEOUS_LIGHT_RESEARCH);
        helper.assertTrue(light.column() == 0 && light.row() == -3 && light.complexity() == 1, "GASEOUS_LIGHT position");
        helper.assertTrue(light.parents().equals(List.of("NITOR")), "GASEOUS_LIGHT parents " + light.parents());
        helper.assertTrue(!light.isSecondary() && !light.isRound() && !light.isConcealed(), "GASEOUS_LIGHT flags");
        helper.assertTrue(light.tags().getAmount(Aspect.LIGHT) == 2 && light.tags().getAmount(Aspect.AIR) == 1 && light.tags().size() == 2, "GASEOUS_LIGHT aspects");
        helper.assertTrue(light.pages().size() == 2 && light.pages().get(1) instanceof ResearchPage.Crucible, "GASEOUS_LIGHT pages");

        ResearchItem shadow = research(helper, GasFeature.GASEOUS_SHADOW_RESEARCH);
        helper.assertTrue(shadow.column() == -1 && shadow.row() == -5 && shadow.complexity() == 2, "GASEOUS_SHADOW position");
        helper.assertTrue(shadow.parents().equals(List.of("GASEOUS_LIGHT")), "GASEOUS_SHADOW parents");
        helper.assertTrue(shadow.isSecondary() && !shadow.isRound() && !shadow.isConcealed(), "GASEOUS_SHADOW flags");
        helper.assertTrue(shadow.tags().getAmount(Aspect.DARKNESS) == 2 && shadow.tags().getAmount(Aspect.AIR) == 1 && shadow.tags().getAmount(Aspect.MOTION) == 4, "GASEOUS_SHADOW aspects");
        helper.assertTrue(shadow.pages().size() == 2 && shadow.pages().get(1) instanceof ResearchPage.Crucible, "GASEOUS_SHADOW pages");

        ResearchItem remover = research(helper, GasFeature.GAS_REMOVER_RESEARCH);
        helper.assertTrue(remover.column() == -2 && remover.row() == -7 && remover.complexity() == 1, "GAS_REMOVER position");
        helper.assertTrue(remover.parents().equals(List.of("GASEOUS_SHADOW")), "GAS_REMOVER parents");
        helper.assertTrue(remover.isRound() && !remover.isSecondary() && !remover.isConcealed(), "GAS_REMOVER flags");
        helper.assertTrue(remover.tags().getAmount(Aspect.DARKNESS) == 2 && remover.tags().getAmount(Aspect.LIGHT) == 2, "GAS_REMOVER aspects");
        helper.assertTrue(remover.pages().size() == 2 && remover.pages().get(1) instanceof ResearchPage.Arcane, "GAS_REMOVER pages");

        ResearchItem nitor = research(helper, GasFeature.BRIGHT_NITOR_RESEARCH);
        helper.assertTrue(nitor.column() == 1 && nitor.row() == -5 && nitor.complexity() == 2, "BRIGHT_NITOR position");
        helper.assertTrue(nitor.parents().equals(List.of("GASEOUS_LIGHT")), "BRIGHT_NITOR parents");
        helper.assertTrue(nitor.isSecondary() && nitor.isConcealed() && !nitor.isRound(), "BRIGHT_NITOR flags");
        helper.assertTrue(
            nitor.tags().getAmount(Aspect.LIGHT) == 2 && nitor.tags().getAmount(Aspect.FIRE) == 1 && nitor.tags().getAmount(Aspect.ENERGY) == 1 && nitor.tags().getAmount(Aspect.AIR) == 1,
            "BRIGHT_NITOR aspects"
        );
        helper.assertTrue(nitor.pages().size() == 2 && nitor.pages().get(1) instanceof ResearchPage.Crucible, "BRIGHT_NITOR pages");
        helper.assertTrue(ResearchCategories.getResearch("NITOR") != null, "base NITOR research is missing");
        helper.succeed();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        for (String key : List.of("GASEOUS_LIGHT", "GASEOUS_SHADOW", "BRIGHT_NITOR")) {
            helper.assertTrue(TcRecipes.get(key) instanceof CrucibleRecipe recipe && recipe.result().exists(), "crucible recipe " + key);
        }
        CrucibleRecipe light = (CrucibleRecipe) TcRecipes.get("GASEOUS_LIGHT");
        helper.assertTrue(light.aspects().getAmount(Aspect.LIGHT) == 16 && light.aspects().getAmount(Aspect.AIR) == 10 && light.aspects().getAmount(Aspect.MOTION) == 8, "GASEOUS_LIGHT aspects");
        CrucibleRecipe nitor = (CrucibleRecipe) TcRecipes.get("BRIGHT_NITOR");
        helper.assertTrue(nitor.aspects().getAmount(Aspect.ENERGY) == 25 && nitor.aspects().getAmount(Aspect.LIGHT) == 25, "BRIGHT_NITOR aspects");
        helper.assertTrue(TcRecipes.get("GAS_REMOVER") instanceof ShapedArcaneRecipe recipe && recipe.width() == 3 && recipe.height() == 3 && recipe.result().exists(), "arcane recipe GAS_REMOVER");
        helper.succeed();
    }

    private static void blockProperties(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos, GasFeature.GASEOUS_LIGHT.get().defaultBlockState());
        BlockState light = helper.getBlockState(pos);
        helper.assertTrue(light.getLightEmission() == 12, "light gas emission " + light.getLightEmission());
        helper.assertTrue(light.getCollisionShape(helper.getLevel(), helper.absolutePos(pos)).isEmpty(), "light gas has collision");
        helper.assertTrue(light.canBeReplaced(), "light gas is not replaceable");
        helper.setBlock(pos, GasFeature.GASEOUS_SHADOW.get().defaultBlockState());
        BlockState shadow = helper.getBlockState(pos);
        helper.assertTrue(shadow.getLightEmission() == 0 && shadow.getLightBlock(helper.getLevel(), net.minecraft.core.BlockPos.ZERO) == 15, "shadow gas light " + shadow.getLightEmission() + " " + shadow.getLightBlock(helper.getLevel(), net.minecraft.core.BlockPos.ZERO));
        helper.setBlock(pos, GasFeature.NITOR_GAS.get().defaultBlockState());
        helper.assertTrue(helper.getBlockState(pos).getLightEmission() == 12, "nitor gas emission");
        helper.setBlock(pos, GasFeature.NITOR_GAS.get().defaultBlockState().setValue(NitorGasBlock.STRONG, true));
        helper.assertTrue(helper.getBlockState(pos).getLightEmission() == 15, "strong nitor gas emission");
        helper.succeed();
    }

    private static ServerPlayer mockPlayer(GameTestHelper helper) {
        return (ServerPlayer) TtTestSupport.mockServerPlayer(helper, GameType.SURVIVAL);
    }

    private static void clearArea(GameTestHelper helper) {
        for (int x = 0; x < 7; x++) {
            for (int y = 1; y < 7; y++) {
                for (int z = 0; z < 7; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void lightGasSpreads(GameTestHelper helper) {
        clearArea(helper);
        ServerLevel level = helper.getLevel();
        BlockPos center = new BlockPos(3, 3, 3);
        SpreadingGasBlock gas = GasFeature.GASEOUS_LIGHT.get();
        gas.release(level, helper.absolutePos(center), 2);
        BlockState placed = helper.getBlockState(center);
        helper.assertTrue(placed.is(gas) && placed.getValue(SpreadingGasBlock.LEVEL) == 2, "release placed " + placed);
        placed.tick(level, helper.absolutePos(center), level.getRandom());
        helper.assertTrue(helper.getBlockState(center).getValue(SpreadingGasBlock.LEVEL) == 0, "source level did not reset");
        for (Direction direction : Direction.values()) {
            BlockState neighbor = helper.getBlockState(center.relative(direction));
            helper.assertTrue(neighbor.is(gas) && neighbor.getValue(SpreadingGasBlock.LEVEL) == 1, "neighbor " + direction + " is " + neighbor);
        }
        helper.assertTrue(helper.getBlockState(center.east().north()).isAir(), "gas spread diagonally");
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(helper.getBlockState(center.east(2)).is(gas), "second ring was not reached");
            helper.assertTrue(helper.getBlockState(center.east(2)).getValue(SpreadingGasBlock.LEVEL) == 0, "second ring did not settle");
            helper.assertTrue(helper.getBlockState(center.east(3)).isAir(), "gas spread beyond its level");
            helper.succeed();
        });
    }

    private static void bottleReleasesGas(GameTestHelper helper) {
        clearArea(helper);
        ServerPlayer player = mockPlayer(helper);
        BlockPos feet = new BlockPos(3, 2, 3);
        BlockPos absolute = helper.absolutePos(feet);
        player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
        ItemStack bottle = new ItemStack(GasFeature.GASEOUS_ILLUMINAE.get(), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, bottle);
        bottle.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        BlockPos released = new BlockPos((int) player.getX(), (int) player.getY() + 1, (int) player.getZ());
        BlockState placed = helper.getLevel().getBlockState(released);
        helper.assertTrue(placed.is(GasFeature.GASEOUS_LIGHT.get()) && placed.getValue(SpreadingGasBlock.LEVEL) == 4, "bottle placed " + placed);
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "bottle was not consumed: " + player.getMainHandItem().getCount());
        helper.succeed();
    }

    private static void shadowReplacesLight(GameTestHelper helper) {
        clearArea(helper);
        ServerLevel level = helper.getLevel();
        BlockPos center = new BlockPos(3, 3, 3);
        helper.setBlock(center.east(), GasFeature.GASEOUS_LIGHT.get().defaultBlockState());
        helper.setBlock(center.west(), Blocks.STONE.defaultBlockState());
        GasFeature.GASEOUS_SHADOW.get().release(level, helper.absolutePos(center), 3);
        helper.getBlockState(center).tick(level, helper.absolutePos(center), level.getRandom());
        helper.assertTrue(helper.getBlockState(center.east()).is(GasFeature.GASEOUS_SHADOW.get()), "shadow did not replace light gas");
        helper.assertTrue(helper.getBlockState(center.west()).is(Blocks.STONE), "gas replaced stone");
        helper.succeed();
    }

    private static void dissipatorRemovesGas(GameTestHelper helper) {
        clearArea(helper);
        ServerLevel level = helper.getLevel();
        BlockPos center = new BlockPos(3, 3, 3);
        BlockPos near = center.offset(-3, 2, 2);
        BlockPos far = center.offset(3, 0, 0);
        helper.setBlock(near, GasFeature.GASEOUS_LIGHT.get().defaultBlockState());
        helper.setBlock(center, GasFeature.GASEOUS_SHADOW.get().defaultBlockState());
        helper.setBlock(center.above(), GasFeature.NITOR_GAS.get().defaultBlockState());
        helper.setBlock(far, GasFeature.GASEOUS_LIGHT.get().defaultBlockState());
        BlockPos absolute = helper.absolutePos(center);
        int removed = FumeDissipatorItem.dissipate(level, absolute.getX(), absolute.getY(), absolute.getZ());
        helper.assertTrue(removed == 3, "removed " + removed);
        helper.assertTrue(helper.getBlockState(center).isAir() && helper.getBlockState(near).isAir() && helper.getBlockState(center.above()).isAir(), "gas remained in range");
        helper.assertTrue(helper.getBlockState(far).is(GasFeature.GASEOUS_LIGHT.get()), "gas outside the range was removed");
        helper.succeed();
    }

    private static void nitorGasFollowsCarrier(GameTestHelper helper) {
        clearArea(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = mockPlayer(helper);
        BlockPos feet = new BlockPos(3, 2, 3);
        BlockPos absolute = helper.absolutePos(feet);
        player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
        ItemStack nitor = new ItemStack(GasFeature.BRIGHT_NITOR.get());
        player.getInventory().setItem(0, nitor);
        BlockPos gasPos = absolute.above();
        nitor.getItem().inventoryTick(nitor, level, player, 0, true);
        BlockState placed = level.getBlockState(gasPos);
        helper.assertTrue(placed.is(GasFeature.NITOR_GAS.get()) && !placed.getValue(NitorGasBlock.STRONG), "nitor gas not placed: " + placed);
        helper.assertTrue(GasFeature.carriesLight(player), "carrier not detected");
        helper.assertTrue(NitorGasBlock.isCarried(List.of(player)), "gas would not follow the carrier");
        player.getInventory().setItem(0, ItemStack.EMPTY);
        helper.assertTrue(!GasFeature.carriesLight(player), "carrier detected without item");
        helper.assertTrue(!NitorGasBlock.isCarried(List.of(player)), "gas would follow a player without the item");
        GasFeature.registerLightCarrier(candidate -> candidate == player);
        helper.assertTrue(NitorGasBlock.isCarried(List.of(player)), "registered carrier is ignored");
        placed.tick(level, gasPos, level.getRandom());
        helper.assertTrue(level.getBlockState(gasPos).isAir(), "nitor gas stayed without a player nearby");
        BrightNitorItem.placeNitorGas(level, gasPos, true);
        helper.assertTrue(level.getBlockState(gasPos).getValue(NitorGasBlock.STRONG), "strong nitor gas not placed");
        helper.assertTrue(level.getBlockState(gasPos).getLightEmission() == 15, "strong nitor gas emission");
        helper.succeed();
    }
}
