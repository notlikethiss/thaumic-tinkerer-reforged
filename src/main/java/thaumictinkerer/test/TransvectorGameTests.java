package thaumictinkerer.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.material.Fluids;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.platform.Transfer;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumictinkerer.camo.CamoBlockEntity;
import thaumictinkerer.research.TtResearch;
import thaumictinkerer.transvector.EtherealPlatformBlock;
import thaumictinkerer.transvector.TransvectorBinderItem;
import thaumictinkerer.transvector.TransvectorDislocatorBlock;
import thaumictinkerer.transvector.TransvectorDislocatorBlockEntity;
import thaumictinkerer.transvector.TransvectorFeature;
import thaumictinkerer.transvector.TransvectorInterfaceBlockEntity;

public final class TransvectorGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("transvector")
        .add("research_registered", TransvectorGameTests::researchRegistered)
        .add("recipes_registered", TransvectorGameTests::recipesRegistered)
        .add("interface_items", TransvectorGameTests::interfaceItems)
        .add("interface_fluids_and_sides", TransvectorGameTests::interfaceFluidsAndSides)
        .add("interface_essentia", TransvectorGameTests::interfaceEssentia)
        .add("interface_unbinds_without_target", TransvectorGameTests::interfaceUnbindsWithoutTarget)
        .add("binding_persists", TransvectorGameTests::bindingPersists)
        .add("binder_flow", TransvectorGameTests::binderFlow)
        .add("dislocator_moves_chest", TransvectorGameTests::dislocatorMovesChest)
        .add("dislocator_refuses_unbreakable", TransvectorGameTests::dislocatorRefusesUnbreakable)
        .add("dislocator_moves_entities", TransvectorGameTests::dislocatorMovesEntities)
        .add("dislocator_redstone", TransvectorGameTests::dislocatorRedstone)
        .add("dislocator_wand_orientation", TransvectorGameTests::dislocatorWandOrientation)
        .add("camo_stored_and_synced", TransvectorGameTests::camoStoredAndSynced)
        .add("camo_rejects_invalid", TransvectorGameTests::camoRejectsInvalid)
        .add("platform_collision", TransvectorGameTests::platformCollision);

    private TransvectorGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem research = ResearchCategories.getResearch(TransvectorFeature.INTERFACE);
        helper.assertTrue(research != null, "INTERFACE is not registered");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), "wrong category");
        helper.assertTrue(research.column() == -4 && research.row() == 2, "wrong position");
        helper.assertTrue(research.complexity() == 1, "wrong complexity " + research.complexity());
        helper.assertTrue(research.parents().equals(List.of("DARK_QUARTZ")), "wrong parents " + research.parents());
        helper.assertTrue(!research.isConcealed() && !research.isSecondary(), "unexpected flags");
        helper.assertTrue(research.tags().getAmount(Aspect.ENTROPY) == 4 && research.tags().getAmount(Aspect.ORDER) == 4, "wrong aspects");
        helper.assertTrue(research.pages().size() == 5, "expected 5 pages, got " + research.pages().size());
        helper.assertTrue(research.pages().get(0) instanceof ResearchPage.Text text && text.text().equals("tc.research_page.INTERFACE.1"), "first page");
        helper.assertTrue(research.pages().get(1) instanceof ResearchPage.Arcane arcane && arcane.recipes().equals(List.of("INTERFACE")), "arcane page");
        helper.assertTrue(research.pages().get(2) instanceof ResearchPage.Text text && text.text().equals("tc.research_page.INTERFACE.2"), "second page");
        helper.assertTrue(research.pages().get(3) instanceof ResearchPage.Arcane arcane && arcane.recipes().equals(List.of("INTERFACE1")), "binder page");
        helper.assertTrue(research.pages().get(4) instanceof ResearchPage.Text text && text.text().equals("tc.research_page.INTERFACE.3"), "third page");

        ResearchItem dislocator = ResearchCategories.getResearch(TransvectorFeature.DISLOCATOR);
        helper.assertTrue(dislocator != null, "DISLOCATOR is not registered");
        helper.assertTrue(dislocator.column() == -6 && dislocator.row() == 1 && dislocator.complexity() == 3, "wrong dislocator placement");
        helper.assertTrue(dislocator.isConcealed() && dislocator.isSecondary(), "dislocator flags are wrong");
        helper.assertTrue(dislocator.parents().equals(List.of("INTERFACE")), "dislocator parents " + dislocator.parents());
        helper.assertTrue(dislocator.parentsHidden().equals(List.of("MIRROR")), "dislocator hidden parents " + dislocator.parentsHidden());
        helper.assertTrue(
            dislocator.tags().getAmount(Aspect.TRAVEL) == 2 && dislocator.tags().getAmount(Aspect.MECHANISM) == 1 && dislocator.tags().getAmount(Aspect.ELDRITCH) == 1,
            "dislocator aspects"
        );
        helper.assertTrue(dislocator.pages().size() == 2, "dislocator pages");
        helper.assertTrue(dislocator.isAvailable() == thaumcraft.Config.allowMirrors(), "dislocator availability must follow allowMirrors");

        ResearchItem platform = ResearchCategories.getResearch(TransvectorFeature.PLATFORM);
        helper.assertTrue(platform != null, "PLATFORM is not registered");
        helper.assertTrue(platform.column() == -2 && platform.row() == 6 && platform.complexity() == 3, "wrong platform placement");
        helper.assertTrue(platform.isConcealed() && platform.isSecondary(), "platform flags are wrong");
        helper.assertTrue(platform.parents().equals(List.of("CLEANSING_TALISMAN")), "platform parents " + platform.parents());
        helper.assertTrue(
            platform.tags().getAmount(Aspect.SENSES) == 2 && platform.tags().getAmount(Aspect.TREE) == 1 && platform.tags().getAmount(Aspect.MOTION) == 1,
            "platform aspects"
        );
        helper.assertTrue(platform.pages().size() == 2, "platform pages");
        helper.succeed();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        ShapedArcaneRecipe interfaceRecipe = (ShapedArcaneRecipe) TcRecipes.get("INTERFACE");
        helper.assertTrue(interfaceRecipe != null, "INTERFACE recipe missing");
        helper.assertTrue(interfaceRecipe.research().equals("INTERFACE") && interfaceRecipe.width() == 3 && interfaceRecipe.height() == 3, "interface shape");
        helper.assertTrue(interfaceRecipe.aspects().getAmount(Aspect.ORDER) == 12 && interfaceRecipe.aspects().getAmount(Aspect.ENTROPY) == 16, "interface aspects");
        helper.assertTrue(interfaceRecipe.result().item().toString().equals("thaumictinkerer:transvector_interface") && interfaceRecipe.result().exists(), "interface result");
        helper.assertTrue(interfaceRecipe.grid().get(0).equals(TcIngredient.item("thaumcraft:arcane_stone")), "interface corner");
        helper.assertTrue(interfaceRecipe.grid().get(1).equals(TcIngredient.item("minecraft:redstone")), "interface redstone");
        helper.assertTrue(interfaceRecipe.grid().get(3).equals(TcIngredient.item("minecraft:lapis_lazuli")), "interface lapis");
        helper.assertTrue(interfaceRecipe.grid().get(4).equals(TcIngredient.item("minecraft:ender_pearl")), "interface pearl");
        assertIngredientsExist(helper, interfaceRecipe);

        ShapedArcaneRecipe binder = (ShapedArcaneRecipe) TcRecipes.get("INTERFACE1");
        helper.assertTrue(binder != null, "INTERFACE1 recipe missing");
        helper.assertTrue(binder.research().equals("INTERFACE") && binder.aspects().getAmount(Aspect.ORDER) == 2 && binder.aspects().size() == 1, "binder research/aspects");
        helper.assertTrue(binder.result().item().toString().equals("thaumictinkerer:transvector_binder") && binder.result().exists(), "binder result");
        helper.assertTrue(binder.grid().get(1).equals(TcIngredient.item("minecraft:iron_ingot")) && binder.grid().get(5).equals(TcIngredient.item("minecraft:iron_ingot")), "binder iron");
        helper.assertTrue(binder.grid().get(4).equals(TcIngredient.item("minecraft:stick")), "binder stick");
        helper.assertTrue(binder.grid().get(6).equals(TcIngredient.item("thaumcraft:order_shard")), "binder shard");
        assertIngredientsExist(helper, binder);

        ShapedArcaneRecipe dislocator = (ShapedArcaneRecipe) TcRecipes.get("DISLOCATOR");
        helper.assertTrue(dislocator != null, "DISLOCATOR recipe missing");
        helper.assertTrue(dislocator.research().equals("DISLOCATOR"), "dislocator research");
        helper.assertTrue(dislocator.aspects().getAmount(Aspect.EARTH) == 5 && dislocator.aspects().getAmount(Aspect.ENTROPY) == 5, "dislocator aspects");
        helper.assertTrue(dislocator.grid().get(1).equals(TcIngredient.item("thaumcraft:mirrored_glass")), "dislocator mirror");
        helper.assertTrue(dislocator.grid().get(4).equals(TcIngredient.item("thaumictinkerer:transvector_interface")), "dislocator interface");
        helper.assertTrue(dislocator.grid().get(7).equals(TcIngredient.item("minecraft:comparator")), "dislocator comparator");
        assertIngredientsExist(helper, dislocator);

        ShapedArcaneRecipe platform = (ShapedArcaneRecipe) TcRecipes.get("PLATFORM");
        helper.assertTrue(platform != null, "PLATFORM recipe missing");
        helper.assertTrue(platform.research().equals("PLATFORM") && platform.width() == 3 && platform.height() == 2, "platform shape");
        helper.assertTrue(platform.result().count() == 2 && platform.result().item().toString().equals("thaumictinkerer:ethereal_platform"), "platform result");
        helper.assertTrue(platform.aspects().getAmount(Aspect.AIR) == 2 && platform.aspects().getAmount(Aspect.ENTROPY) == 4, "platform aspects");
        helper.assertTrue(platform.grid().get(1).equals(TcIngredient.item("thaumcraft:silverwood_planks")), "platform silverwood");
        helper.assertTrue(platform.grid().get(3).equals(TcIngredient.item("thaumcraft:greatwood_planks")) && platform.grid().get(5).equals(TcIngredient.item("thaumcraft:greatwood_planks")), "platform greatwood");
        assertIngredientsExist(helper, platform);
        helper.succeed();
    }

    private static void assertIngredientsExist(GameTestHelper helper, ShapedArcaneRecipe recipe) {
        for (TcIngredient ingredient : recipe.grid()) {
            helper.assertTrue(ingredient.exists(), "ingredient does not exist in " + recipe.key() + ": " + ingredient);
        }
    }

    private static TransvectorInterfaceBlockEntity placeInterface(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, TransvectorFeature.TRANSVECTOR_INTERFACE.get());
        return (TransvectorInterfaceBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static int count(Level level, BlockPos pos, net.minecraft.world.item.Item item) {
        return ((Container) level.getBlockEntity(pos)).countItem(item);
    }

    private static void interfaceItems(GameTestHelper helper) {
        Level level = helper.getLevel();
        BlockPos chest = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CHEST);
        TransvectorInterfaceBlockEntity entity = placeInterface(helper, new BlockPos(3, 1, 1));
        BlockPos interfacePos = entity.getBlockPos();

        ItemStack refused = Transfer.insert(level, interfacePos, Direction.UP, new ItemStack(Items.DIAMOND, 5), false);
        helper.assertTrue(refused.getCount() == 5, "unbound interface accepted items");

        entity.bind(chest);
        helper.assertTrue(Transfer.hasItems(level, interfacePos, Direction.UP), "bound interface exposes no items");
        ItemStack leftover = Transfer.insert(level, interfacePos, Direction.UP, new ItemStack(Items.DIAMOND, 5), false);
        helper.assertTrue(leftover.isEmpty(), "interface refused items, leftover " + leftover);
        helper.assertTrue(count(level, chest, Items.DIAMOND) == 5, "chest has " + count(level, chest, Items.DIAMOND) + " diamonds");
        helper.assertTrue(Transfer.count(level, interfacePos, Direction.UP, stack -> stack.is(Items.DIAMOND)) == 5, "interface reports wrong count");

        ItemStack simulated = Transfer.extract(level, interfacePos, Direction.UP, stack -> stack.is(Items.DIAMOND), 3, true);
        helper.assertTrue(simulated.getCount() == 3 && count(level, chest, Items.DIAMOND) == 5, "simulated extract changed the chest");
        ItemStack extracted = Transfer.extract(level, interfacePos, Direction.UP, stack -> stack.is(Items.DIAMOND), 3, false);
        helper.assertTrue(extracted.getCount() == 3, "extracted " + extracted);
        helper.assertTrue(count(level, chest, Items.DIAMOND) == 2, "chest has " + count(level, chest, Items.DIAMOND) + " diamonds after extract");
        helper.succeed();
    }

    private static void interfaceFluidsAndSides(GameTestHelper helper) {
        Level level = helper.getLevel();
        helper.setBlock(new BlockPos(1, 1, 1), ModBlocks.ARCANE_SPA.get());
        BlockPos spa = helper.absolutePos(new BlockPos(1, 1, 1));
        TransvectorInterfaceBlockEntity entity = placeInterface(helper, new BlockPos(3, 1, 1));
        BlockPos interfacePos = entity.getBlockPos();
        entity.bind(spa);

        FluidStack leftover = Transfer.insertFluid(level, interfacePos, Direction.DOWN, new FluidStack(Fluids.WATER, 500), false);
        helper.assertTrue(leftover.isEmpty(), "interface refused fluid, leftover " + leftover);
        helper.assertTrue(Transfer.countFluid(level, spa, Direction.DOWN, stack -> stack.is(Fluids.WATER)) == 500, "spa did not receive water");
        helper.assertTrue(Transfer.countFluid(level, interfacePos, Direction.DOWN, stack -> stack.is(Fluids.WATER)) == 500, "interface reports wrong fluid");

        FluidStack top = Transfer.insertFluid(level, interfacePos, Direction.UP, new FluidStack(Fluids.WATER, 100), false);
        helper.assertTrue(top.getAmount() == 100, "side restriction of the target was not applied");
        FluidStack drained = Transfer.extractFluid(level, interfacePos, Direction.DOWN, stack -> stack.is(Fluids.WATER), 200, false);
        helper.assertTrue(drained.getAmount() == 200, "drained " + drained);
        helper.assertTrue(Transfer.countFluid(level, spa, Direction.DOWN, stack -> stack.is(Fluids.WATER)) == 300, "spa lost the wrong amount");
        helper.succeed();
    }

    private static void interfaceEssentia(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), ModBlocks.WARDED_JAR.get());
        BlockPos jar = helper.absolutePos(new BlockPos(1, 1, 1));
        TransvectorInterfaceBlockEntity entity = placeInterface(helper, new BlockPos(3, 1, 1));
        helper.assertTrue(entity.isConnectable(Direction.UP), "interface must always connect");
        helper.assertTrue(entity.getEssentiaAmount(Direction.UP) == 0 && entity.getEssentiaType(Direction.UP) == null, "unbound interface holds essentia");
        helper.assertTrue(entity.addToContainer(Aspect.AIR, 5) == 5, "unbound interface swallowed essentia");
        entity.bind(jar);
        helper.assertTrue(entity.addToContainer(Aspect.AIR, 5) == 0, "jar refused essentia");
        helper.assertTrue(entity.getAspects().getAmount(Aspect.AIR) == 5, "interface reports " + entity.getAspects());
        helper.assertTrue(entity.getEssentiaAmount(Direction.UP) == 5 && entity.getEssentiaType(Direction.UP) == Aspect.AIR, "essentia transport is not proxied");
        helper.assertTrue(entity.takeFromContainer(Aspect.AIR, 2), "take failed");
        helper.assertTrue(entity.getAspects().getAmount(Aspect.AIR) == 3, "aspect list is not proxied");
        helper.succeed();
    }

    private static void interfaceUnbindsWithoutTarget(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CHEST);
        TransvectorInterfaceBlockEntity entity = placeInterface(helper, new BlockPos(3, 1, 1));
        entity.bind(helper.absolutePos(new BlockPos(1, 1, 1)));
        helper.assertTrue(entity.getTarget() != null, "target not resolved");
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE);
        helper.assertTrue(entity.getTarget() == null && !entity.isBound(), "binding survived the target block entity");
        entity.bind(entity.getBlockPos().offset(5, 0, 0));
        helper.assertTrue(entity.getTarget() == null && !entity.isBound(), "out of range target was not dropped");
        helper.succeed();
    }

    private static BlockEntity roundTrip(GameTestHelper helper, BlockEntity entity) {
        CompoundTag saved = entity.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity loaded = BlockEntity.loadStatic(entity.getBlockPos(), entity.getBlockState(), saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded != null, "block entity did not load");
        return loaded;
    }

    private static void bindingPersists(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CHEST);
        TransvectorInterfaceBlockEntity entity = placeInterface(helper, new BlockPos(3, 1, 1));
        BlockPos chest = helper.absolutePos(new BlockPos(1, 1, 1));
        entity.bind(chest);
        entity.setCamo(Blocks.OAK_PLANKS.defaultBlockState());
        TransvectorInterfaceBlockEntity loaded = (TransvectorInterfaceBlockEntity) roundTrip(helper, entity);
        helper.assertTrue(chest.equals(loaded.targetPos()), "binding was lost, got " + loaded.targetPos());
        helper.assertTrue(Blocks.OAK_PLANKS.defaultBlockState().equals(loaded.getCamo()), "camo was lost");

        TransvectorInterfaceBlockEntity fresh = placeInterface(helper, new BlockPos(5, 1, 1));
        TransvectorInterfaceBlockEntity freshLoaded = (TransvectorInterfaceBlockEntity) roundTrip(helper, fresh);
        helper.assertTrue(!freshLoaded.isBound() && freshLoaded.getCamo() == null, "unbound interface gained data");
        helper.succeed();
    }

    private static UseOnContext useBinder(GameTestHelper helper, Player player, ItemStack stack, BlockPos pos) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(pos);
        return new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
    }

    private static void binderFlow(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack binder = new ItemStack(TransvectorFeature.TRANSVECTOR_BINDER.get());
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CHEST);
        TransvectorInterfaceBlockEntity first = placeInterface(helper, new BlockPos(3, 1, 1));
        TransvectorInterfaceBlockEntity second = placeInterface(helper, new BlockPos(3, 1, 3));

        binder.useOn(useBinder(helper, player, binder, new BlockPos(1, 1, 1)));
        helper.assertTrue(TransvectorBinderItem.origin(binder) == null, "binder accepted a plain block as origin");

        binder.useOn(useBinder(helper, player, binder, new BlockPos(3, 1, 1)));
        helper.assertTrue(TransvectorBinderItem.origin(binder) != null && TransvectorBinderItem.origin(binder).pos().equals(first.getBlockPos()), "origin was not stored");

        binder.useOn(useBinder(helper, player, binder, new BlockPos(3, 1, 3)));
        helper.assertTrue(!first.isBound() && TransvectorBinderItem.origin(binder) != null, "interface was bound to another interface");

        helper.setBlock(new BlockPos(3, 1, 6), Blocks.CHEST);
        binder.useOn(useBinder(helper, player, binder, new BlockPos(3, 1, 6)));
        helper.assertTrue(!first.isBound() && TransvectorBinderItem.origin(binder) != null, "interface was bound beyond its range");

        binder.useOn(useBinder(helper, player, binder, new BlockPos(1, 1, 1)));
        helper.assertTrue(helper.absolutePos(new BlockPos(1, 1, 1)).equals(first.targetPos()), "binding failed, got " + first.targetPos());
        helper.assertTrue(TransvectorBinderItem.origin(binder) == null, "origin was not cleared");
        helper.assertTrue(!second.isBound(), "wrong interface was bound");

        binder.useOn(useBinder(helper, player, binder, new BlockPos(3, 1, 3)));
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.AIR);
        binder.useOn(useBinder(helper, player, binder, new BlockPos(1, 1, 1)));
        helper.assertTrue(TransvectorBinderItem.origin(binder) == null, "binder kept a missing origin");
        helper.succeed();
    }

    private static TransvectorDislocatorBlockEntity placeDislocator(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, TransvectorFeature.TRANSVECTOR_DISLOCATOR.get().defaultBlockState().setValue(TransvectorDislocatorBlock.FACING, facing));
        return (TransvectorDislocatorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static int itemEntities(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(4.0)).size();
    }

    private static void dislocatorMovesChest(GameTestHelper helper) {
        Level level = helper.getLevel();
        BlockPos start = new BlockPos(3, 1, 1);
        BlockPos end = new BlockPos(2, 1, 3);
        helper.setBlock(end, Blocks.CHEST);
        Container chest = (Container) level.getBlockEntity(helper.absolutePos(end));
        chest.setItem(0, new ItemStack(Items.DIAMOND, 3));
        chest.setItem(5, new ItemStack(Items.GOLD_INGOT, 7));
        helper.setBlock(start, Blocks.OAK_PLANKS);
        TransvectorDislocatorBlockEntity dislocator = placeDislocator(helper, new BlockPos(2, 1, 1), Direction.EAST);
        dislocator.bind(helper.absolutePos(end));

        dislocator.receiveRedstonePulse();
        helper.assertTrue(helper.getBlockState(start).is(Blocks.CHEST), "chest did not arrive");
        helper.assertTrue(helper.getBlockState(end).is(Blocks.OAK_PLANKS), "planks did not arrive");
        Container moved = (Container) level.getBlockEntity(helper.absolutePos(start));
        helper.assertTrue(moved.countItem(Items.DIAMOND) == 3 && moved.countItem(Items.GOLD_INGOT) == 7, "chest contents changed");
        helper.assertTrue(moved.getItem(5).is(Items.GOLD_INGOT), "slot layout changed");
        helper.assertTrue(level.getBlockEntity(helper.absolutePos(end)) == null, "planks gained a block entity");
        helper.assertTrue(itemEntities(helper) == 0, "swap dropped items");
        helper.assertTrue(dislocator.cooldown() == 10, "cooldown was not set");

        dislocator.receiveRedstonePulse();
        helper.assertTrue(helper.getBlockState(start).is(Blocks.CHEST), "second pulse ignored the cooldown");
        helper.assertTrue(itemEntities(helper) == 0, "stored pulse dropped items");
        helper.succeed();
    }

    private static void dislocatorRefusesUnbreakable(GameTestHelper helper) {
        BlockPos start = new BlockPos(3, 1, 1);
        BlockPos end = new BlockPos(2, 1, 3);
        helper.setBlock(end, Blocks.BEDROCK);
        helper.setBlock(start, Blocks.OAK_PLANKS);
        TransvectorDislocatorBlockEntity dislocator = placeDislocator(helper, new BlockPos(2, 1, 1), Direction.EAST);
        dislocator.bind(helper.absolutePos(end));
        dislocator.receiveRedstonePulse();
        helper.assertTrue(helper.getBlockState(end).is(Blocks.BEDROCK), "bedrock was moved");
        helper.assertTrue(helper.getBlockState(start).is(Blocks.OAK_PLANKS), "planks were moved");

        BlockPos nodeEnd = new BlockPos(5, 1, 3);
        helper.setBlock(nodeEnd, ModBlocks.AURA_NODE.get());
        TransvectorDislocatorBlockEntity second = placeDislocator(helper, new BlockPos(5, 1, 1), Direction.EAST);
        helper.setBlock(new BlockPos(6, 1, 1), Blocks.OAK_PLANKS);
        second.bind(helper.absolutePos(nodeEnd));
        second.receiveRedstonePulse();
        helper.assertTrue(helper.getBlockState(nodeEnd).is(ModBlocks.AURA_NODE.get()), "aura node was moved");
        helper.succeed();
    }

    private static void dislocatorMovesEntities(GameTestHelper helper) {
        BlockPos start = new BlockPos(3, 1, 1);
        BlockPos end = new BlockPos(2, 1, 3);
        helper.setBlock(end, Blocks.AIR);
        helper.setBlock(start, Blocks.AIR);
        TransvectorDislocatorBlockEntity dislocator = placeDislocator(helper, new BlockPos(2, 1, 1), Direction.EAST);
        dislocator.bind(helper.absolutePos(end));
        Entity atStart = helper.spawnWithNoFreeWill(EntityTypes.PIG, start);
        Entity atEnd = helper.spawnWithNoFreeWill(EntityTypes.PIG, end);
        Vec3 startBefore = atStart.position();
        Vec3 endBefore = atEnd.position();
        dislocator.receiveRedstonePulse();
        Vec3 startShift = atStart.position().subtract(startBefore);
        Vec3 endShift = atEnd.position().subtract(endBefore);
        helper.assertTrue(Math.abs(startShift.x - -1.0) < 1.0E-6 && Math.abs(startShift.z - 2.0) < 1.0E-6, "entity at the swap position moved by " + startShift);
        helper.assertTrue(Math.abs(endShift.x - 1.0) < 1.0E-6 && Math.abs(endShift.z - -2.0) < 1.0E-6, "entity at the bound position moved by " + endShift);
        helper.succeed();
    }

    private static void dislocatorRedstone(GameTestHelper helper) {
        BlockPos start = new BlockPos(3, 1, 1);
        BlockPos end = new BlockPos(2, 1, 3);
        helper.setBlock(end, Blocks.CHEST);
        helper.setBlock(start, Blocks.OAK_PLANKS);
        TransvectorDislocatorBlockEntity dislocator = placeDislocator(helper, new BlockPos(2, 1, 1), Direction.EAST);
        dislocator.bind(helper.absolutePos(end));
        helper.setBlock(new BlockPos(2, 2, 1), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(helper.getBlockState(start).is(Blocks.CHEST), "redstone did not trigger the swap");
            helper.assertBlockProperty(new BlockPos(2, 1, 1), TransvectorDislocatorBlock.POWERED, true);
            helper.succeed();
        });
    }

    private static void dislocatorWandOrientation(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        placeDislocator(helper, new BlockPos(2, 1, 1), Direction.NORTH);
        ItemStack wand = new ItemStack(ModItems.WAND.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        BlockPos absolute = helper.absolutePos(new BlockPos(2, 1, 1));
        BlockState state = helper.getBlockState(new BlockPos(2, 1, 1));
        InteractionResult result = state.useItemOn(wand, helper.getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolute), Direction.WEST, absolute, false));
        helper.assertTrue(result.consumesAction(), "wand click was not consumed");
        helper.assertBlockProperty(new BlockPos(2, 1, 1), TransvectorDislocatorBlock.FACING, Direction.WEST);
        helper.succeed();
    }

    private static InteractionResult click(GameTestHelper helper, Player player, ItemStack stack, BlockPos pos, Direction face) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(pos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), face, absolute, false);
        BlockState state = helper.getLevel().getBlockState(absolute);
        if (stack.isEmpty()) {
            return state.useWithoutItem(helper.getLevel(), player, hit);
        }
        return state.useItemOn(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
    }

    private static void camoStoredAndSynced(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(2, 1, 1);
        helper.setBlock(pos, TransvectorFeature.ETHEREAL_PLATFORM.get());
        CamoBlockEntity entity = (CamoBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        helper.assertTrue(entity.getCamo() == null && entity.getModelData().getProperties().isEmpty(), "fresh platform has camo");

        ItemStack stone = new ItemStack(Items.STONE);
        InteractionResult result = click(helper, player, stone, pos, Direction.UP);
        helper.assertTrue(result.consumesAction(), "camo click was not consumed");
        helper.assertTrue(Blocks.STONE.defaultBlockState().equals(entity.getCamo()), "camo is " + entity.getCamo());
        helper.assertTrue(stone.getCount() == 1, "camo consumed the item");
        helper.assertTrue(Blocks.STONE.defaultBlockState().equals(entity.getModelData().get(CamoBlockEntity.CAMO)), "model data is not provided");

        CompoundTag update = entity.getUpdateTag(helper.getLevel().registryAccess());
        helper.assertTrue(update.contains("camo"), "update tag lacks camo");
        CamoBlockEntity loaded = (CamoBlockEntity) roundTrip(helper, entity);
        helper.assertTrue(Blocks.STONE.defaultBlockState().equals(loaded.getCamo()), "camo did not survive save and load");
        helper.assertTrue(entity.getUpdatePacket() != null, "no update packet");

        click(helper, player, new ItemStack(Items.OAK_LOG), pos, Direction.UP);
        helper.assertTrue(Blocks.OAK_LOG.defaultBlockState().equals(entity.getCamo()), "camo was not replaced, got " + entity.getCamo());

        click(helper, player, new ItemStack(Items.CARVED_PUMPKIN), pos, Direction.NORTH);
        helper.assertTrue(
            entity.getCamo().is(Blocks.CARVED_PUMPKIN) && entity.getCamo().getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH,
            "directional block was not oriented, got " + entity.getCamo()
        );

        InteractionResult cleared = click(helper, player, ItemStack.EMPTY, pos, Direction.UP);
        helper.assertTrue(cleared.consumesAction() && entity.getCamo() == null, "empty hand did not clear the camo");
        helper.succeed();
    }

    private static void camoRejectsInvalid(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(2, 1, 1);
        helper.setBlock(pos, TransvectorFeature.TRANSVECTOR_INTERFACE.get());
        CamoBlockEntity entity = (CamoBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        entity.setCamo(Blocks.STONE.defaultBlockState());
        for (ItemStack stack : List.of(
            new ItemStack(Items.STICK),
            new ItemStack(TransvectorFeature.TRANSVECTOR_BINDER.get()),
            new ItemStack(TransvectorFeature.ETHEREAL_PLATFORM.get()),
            new ItemStack(TransvectorFeature.TRANSVECTOR_INTERFACE.get()),
            new ItemStack(Items.CHEST),
            new ItemStack(Items.TORCH)
        )) {
            InteractionResult result = click(helper, player, stack, pos, Direction.UP);
            helper.assertTrue(!result.consumesAction(), stack + " was consumed");
            helper.assertTrue(Blocks.STONE.defaultBlockState().equals(entity.getCamo()), stack + " changed the camo to " + entity.getCamo());
        }
        helper.succeed();
    }

    private static void platformCollision(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 1);
        helper.setBlock(pos, TransvectorFeature.ETHEREAL_PLATFORM.get());
        BlockPos absolute = helper.absolutePos(pos);
        BlockState state = helper.getBlockState(pos);
        Level level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        player.setPos(absolute.getX() + 0.5, absolute.getY() + 1.0, absolute.getZ() + 0.5);
        player.setShiftKeyDown(false);
        helper.assertTrue(!state.getCollisionShape(level, absolute, CollisionContext.of(player)).isEmpty(), "player standing on top falls through");
        helper.assertTrue(EtherealPlatformBlock.solidFor(player, absolute), "solidFor disagrees for a player on top");

        player.setShiftKeyDown(true);
        helper.assertTrue(state.getCollisionShape(level, absolute, CollisionContext.of(player)).isEmpty(), "sneaking player collides");
        player.setShiftKeyDown(false);

        player.setPos(absolute.getX() + 0.5, absolute.getY() - 1.0, absolute.getZ() + 0.5);
        helper.assertTrue(state.getCollisionShape(level, absolute, CollisionContext.of(player)).isEmpty(), "player below collides");

        player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
        helper.assertTrue(state.getCollisionShape(level, absolute, CollisionContext.of(player)).isEmpty(), "player at the same level collides");

        Entity cow = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(2, 2, 1));
        helper.assertTrue(!state.getCollisionShape(level, absolute, CollisionContext.of(cow)).isEmpty(), "mob on top falls through");
        cow.setPos(absolute.getX() + 0.5, absolute.getY() - 1.0, absolute.getZ() + 0.5);
        helper.assertTrue(state.getCollisionShape(level, absolute, CollisionContext.of(cow)).isEmpty(), "mob below collides");
        helper.assertTrue(state.getCollisionShape(level, absolute, CollisionContext.empty()).isEmpty(), "empty context collides");
        helper.assertTrue(!state.getShape(level, absolute).isEmpty(), "platform is not selectable");
        helper.assertTrue(state.isPathfindable(net.minecraft.world.level.pathfinder.PathComputationType.LAND), "platform blocks pathfinding");
        helper.succeed();
    }
}
