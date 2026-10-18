package thaumictinkerer.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.platform.BaubleType;
import thaumcraft.platform.Baubles;
import thaumcraft.registry.ModItems;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.wand.WandManager;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusPouchItem;
import thaumictinkerer.kami.KamiFeature;
import thaumictinkerer.kami.trinkets.BlackHoleRingItem;
import thaumictinkerer.kami.trinkets.BlockStorage;
import thaumictinkerer.kami.trinkets.CatAmuletItem;
import thaumictinkerer.kami.trinkets.IchorPouchMenu;
import thaumictinkerer.kami.trinkets.ProtoclayItem;
import thaumictinkerer.kami.trinkets.TrinketsFeature;
import thaumictinkerer.kami.trinkets.WearerAvoidGoal;
import thaumictinkerer.kami.trinkets.WorldshaperGlassItem;
import thaumictinkerer.research.KamiResearchItem;
import thaumictinkerer.research.TtResearch;

public final class TrinketsGameTests {
    private static final BlockPos FLOOR = new BlockPos(5, 2, 5);

    private static final TtGameTestSuite SUITE = new TtGameTestSuite("trinkets")
        .isolated()
        .add("research_registered", TrinketsGameTests::researchRegistered)
        .add("recipes_registered", TrinketsGameTests::recipesRegistered)
        .add("ring_sets_block_and_places", TrinketsGameTests::ringSetsBlockAndPlaces)
        .add("ring_absorbs_without_dupes", TrinketsGameTests::ringAbsorbsWithoutDupes)
        .add("ring_dispenses_stack", TrinketsGameTests::ringDispensesStack)
        .add("ring_fills_container", TrinketsGameTests::ringFillsContainer)
        .add("ring_toggles", TrinketsGameTests::ringToggles)
        .add("mirror_places_square", TrinketsGameTests::mirrorPlacesSquare)
        .add("mirror_needs_enough_blocks", TrinketsGameTests::mirrorNeedsEnoughBlocks)
        .add("mirror_sizes_and_block", TrinketsGameTests::mirrorSizesAndBlock)
        .add("pouch_round_trip", TrinketsGameTests::pouchRoundTrip)
        .add("pouch_found_by_wand_switcher", TrinketsGameTests::pouchFoundByWandSwitcher)
        .add("cat_amulet_stops_creepers", TrinketsGameTests::catAmuletStopsCreepers)
        .add("protoclay_swaps_tools", TrinketsGameTests::protoclaySwapsTools);

    private TrinketsGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static ResearchItem kami(GameTestHelper helper, String key, int column, int row, List<String> parents) {
        ResearchItem research = ResearchCategories.getResearch(key);
        helper.assertTrue(research instanceof KamiResearchItem, key + " is not a kami research");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), key + " has wrong category");
        helper.assertTrue(research.column() == column && research.row() == row, key + " has wrong position");
        helper.assertTrue(research.isConcealed(), key + " must be concealed");
        helper.assertTrue(research.parents().equals(parents), key + " parents are wrong " + research.parents());
        helper.assertTrue(research.pages().size() == 2, key + " pages are wrong");
        return research;
    }

    private static void researchRegistered(GameTestHelper helper) {
        kami(helper, "BLOCK_TALISMAN", 14, 17, List.of("ICHOR_PICK_GEM", "ICHOR_SHOVEL_GEM"));
        kami(helper, "PLACEMENT_MIRROR", 17, 16, List.of("BLOCK_TALISMAN"));
        ResearchItem protoclay = kami(helper, "PROTOCLAY", 12, 17, List.of("ICHOR_PICK_GEM"));
        helper.assertTrue(protoclay.parentsHidden().contains("ICHOR_SHOVEL_GEM"), "protoclay hidden parent is wrong " + protoclay.parentsHidden());
        kami(helper, "ICHOR_POUCH", 13, 6, List.of("ICHOR_CLOTH"));
        kami(helper, "CAT_AMULET", 13, 10, List.of("ICHORIUM"));
        helper.succeed();
    }

    private static void infusion(GameTestHelper helper, String key, int instability, int components, Aspect aspect, int amount) {
        InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get(key);
        helper.assertTrue(recipe != null, key + " infusion is missing");
        helper.assertTrue(recipe.instability() == instability, key + " instability is " + recipe.instability());
        helper.assertTrue(recipe.components().size() == components, key + " components are " + recipe.components().size());
        helper.assertTrue(recipe.aspects().getAmount(aspect) == amount, key + " aspect amount is wrong");
        helper.assertTrue(key.equals(recipe.research()), key + " research is wrong");
    }

    private static void recipesRegistered(GameTestHelper helper) {
        infusion(helper, "BLOCK_TALISMAN", 9, 6, Aspect.VOID, 65);
        infusion(helper, "PLACEMENT_MIRROR", 12, 6, Aspect.CRAFT, 65);
        infusion(helper, "PROTOCLAY", 4, 4, Aspect.MINE, 16);
        infusion(helper, "ICHOR_POUCH", 9, 6, Aspect.AIR, 64);
        infusion(helper, "CAT_AMULET", 8, 6, Aspect.ORDER, 32);
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper, GameType type) {
        ServerPlayer player = (ServerPlayer) TtTestSupport.mockServerPlayer(helper, type);
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(player.getGameProfile(), false);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        player.connection = new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override
            public void send(Packet<?> packet) {
            }
        };
        helper.getLevel().addNewPlayer(player);
        return player;
    }

    private static long total(ServerPlayer player, Item item) {
        long count = 0;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
            if (stack.getItem() instanceof BlackHoleRingItem && BlackHoleRingItem.getItem(stack) == item) {
                count += BlackHoleRingItem.getCount(stack);
            }
        }
        return count;
    }

    private static ItemStack ring(Item item, int count, boolean active) {
        ItemStack ring = new ItemStack(TrinketsFeature.BLOCK_TALISMAN.get());
        TrinketsFeature.TALISMAN_BLOCK.set(ring, BlockStorage.idOf(item));
        TrinketsFeature.TALISMAN_COUNT.set(ring, count);
        BlackHoleRingItem.setActive(ring, active);
        return ring;
    }

    private static UseOnContext click(GameTestHelper helper, ServerPlayer player, BlockPos relative, Direction face) {
        BlockPos absolute = helper.absolutePos(relative);
        return new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolute), face, absolute, false));
    }

    private static void ringSetsBlockAndPlaces(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(new Vec3(1.5, 2.0, 1.5)));
        helper.setBlock(FLOOR, Blocks.STONE.defaultBlockState());
        helper.setBlock(FLOOR.above(), Blocks.AIR.defaultBlockState());
        ItemStack ring = new ItemStack(TrinketsFeature.BLOCK_TALISMAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, ring);

        var result = ring.getItem().onItemUseFirst(ring, click(helper, player, FLOOR, Direction.UP));
        helper.assertTrue(result.consumesAction(), "setting the block must succeed");
        helper.assertTrue(BlackHoleRingItem.getItem(ring) == Items.STONE && BlackHoleRingItem.getCount(ring) == 0, "block was not stored");
        helper.assertTrue(helper.getBlockState(FLOOR.above()).isAir(), "setting the block must not place it");

        BlackHoleRingItem.add(ring, 3);
        result = ring.getItem().onItemUseFirst(ring, click(helper, player, FLOOR, Direction.UP));
        helper.assertTrue(result.consumesAction(), "placing must succeed");
        helper.assertTrue(helper.getBlockState(FLOOR.above()).is(Blocks.STONE), "stone was not placed");
        helper.assertTrue(BlackHoleRingItem.getCount(ring) == 2, "placing must take exactly one block, now " + BlackHoleRingItem.getCount(ring));

        helper.setBlock(FLOOR.above(), Blocks.AIR.defaultBlockState());
        BlackHoleRingItem.remove(ring, 2);
        helper.assertTrue(BlackHoleRingItem.getCount(ring) == 0, "ring must be empty");
        helper.setBlock(FLOOR, Blocks.DIRT.defaultBlockState());
        ring.getItem().onItemUseFirst(ring, click(helper, player, FLOOR, Direction.UP));
        helper.assertTrue(BlackHoleRingItem.getItem(ring) == Items.DIRT, "empty ring must switch the stored block");
        helper.assertTrue(helper.getBlockState(FLOOR.above()).isAir(), "an empty ring must not place anything");
        helper.succeed();
    }

    private static void ringAbsorbsWithoutDupes(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        Inventory inventory = player.getInventory();
        ItemStack ring = ring(Items.STONE, 0, true);
        inventory.setItem(30, ring);
        inventory.setItem(0, new ItemStack(Items.STONE, 40));
        inventory.setItem(1, new ItemStack(Items.STONE, 40));
        inventory.setItem(2, new ItemStack(Items.COBBLESTONE, 12));
        ItemStack named = new ItemStack(Items.STONE, 7);
        named.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("keep"));
        inventory.setItem(3, named);

        ((BlackHoleRingItem) ring.getItem()).onWornTick(ring, player);
        helper.assertTrue(total(player, Items.STONE) == 87, "stone total changed to " + total(player, Items.STONE));
        helper.assertTrue(inventory.getItem(0).getCount() == 64, "one stack must be topped up to 64");
        helper.assertTrue(inventory.getItem(1).isEmpty(), "the other stack must be absorbed");
        helper.assertTrue(BlackHoleRingItem.getCount(ring) == 16, "ring holds " + BlackHoleRingItem.getCount(ring));
        helper.assertTrue(inventory.getItem(2).getCount() == 12 && inventory.getItem(3).getCount() == 7, "other items must stay untouched");

        for (int tick = 0; tick < 5; tick++) {
            ((BlackHoleRingItem) ring.getItem()).onWornTick(ring, player);
        }
        helper.assertTrue(total(player, Items.STONE) == 87, "repeated ticks changed the stone total to " + total(player, Items.STONE));

        BlackHoleRingItem.setActive(ring, false);
        inventory.setItem(5, new ItemStack(Items.STONE, 10));
        ((BlackHoleRingItem) ring.getItem()).onWornTick(ring, player);
        helper.assertTrue(inventory.getItem(5).getCount() == 10, "an inactive ring must not absorb");
        helper.succeed();
    }

    private static void ringDispensesStack(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        Inventory inventory = player.getInventory();
        ItemStack ring = ring(Items.STONE, 100, true);
        Baubles.set(player, 1, ring);
        ((BlackHoleRingItem) ring.getItem()).onWornTick(ring, player);
        helper.assertTrue(total(player, Items.STONE) + BlackHoleRingItem.getCount(ring) == 164 - 64, "dispensing must conserve blocks");
        helper.assertTrue(BlackHoleRingItem.getCount(ring) == 36, "ring must hand out one stack, left " + BlackHoleRingItem.getCount(ring));
        int given = 0;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (inventory.getItem(slot).is(Items.STONE)) {
                given += inventory.getItem(slot).getCount();
            }
        }
        helper.assertTrue(given == 64, "player must receive 64 stone, got " + given);
        helper.succeed();
    }

    private static void ringFillsContainer(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(new Vec3(1.5, 2.0, 1.5)));
        BlockPos chestPos = new BlockPos(3, 2, 3);
        helper.setBlock(chestPos, Blocks.CHEST.defaultBlockState());
        ChestBlockEntity chest = (ChestBlockEntity) helper.<ChestBlockEntity>getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.STONE, 60));
        ItemStack ring = ring(Items.STONE, 100, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, ring);

        var result = ring.getItem().onItemUseFirst(ring, click(helper, player, chestPos, Direction.UP));
        helper.assertTrue(result.consumesAction(), "filling a container must succeed");
        int inChest = 0;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            ItemStack stack = chest.getItem(slot);
            if (stack.is(Items.STONE)) {
                helper.assertTrue(stack.getCount() <= 64, "stack above the limit");
                inChest += stack.getCount();
            }
        }
        helper.assertTrue(inChest + BlackHoleRingItem.getCount(ring) == 160, "blocks were duplicated or lost: chest " + inChest + " ring " + BlackHoleRingItem.getCount(ring));
        helper.assertTrue(chest.getItem(0).getCount() == 64, "existing stack must be topped up");
        helper.assertTrue(BlackHoleRingItem.getCount(ring) == 0, "ring must give everything it can");
        helper.succeed();
    }

    private static void ringToggles(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        ItemStack ring = new ItemStack(TrinketsFeature.BLOCK_TALISMAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, ring);
        player.setShiftKeyDown(true);
        ring.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(!BlackHoleRingItem.isActive(ring), "a ring without a block must not toggle");
        TrinketsFeature.TALISMAN_BLOCK.set(ring, BlockStorage.idOf(Items.STONE));
        ring.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(BlackHoleRingItem.isActive(ring), "sneak use must activate");
        ring.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(!BlackHoleRingItem.isActive(ring), "sneak use must deactivate");
        helper.assertTrue(((BlackHoleRingItem) ring.getItem()).getBaubleType(ring) == BaubleType.RING, "ring bauble type");
        helper.succeed();
    }

    private static void lookDown(GameTestHelper helper, ServerPlayer player) {
        player.setPos(helper.absoluteVec(new Vec3(5.5, 4.0, 5.5)));
        player.setXRot(90.0F);
        player.setYRot(0.0F);
    }

    private static void clearArea(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 9; z++) {
                    helper.setBlock(new BlockPos(1 + x, 3 + y, 1 + z), Blocks.AIR.defaultBlockState());
                }
            }
        }
        helper.setBlock(FLOOR, Blocks.STONE.defaultBlockState());
    }

    private static ItemStack mirror(Item item, int size) {
        ItemStack mirror = new ItemStack(TrinketsFeature.PLACEMENT_MIRROR.get());
        TrinketsFeature.MIRROR_BLOCK.set(mirror, BlockStorage.idOf(item));
        TrinketsFeature.MIRROR_SIZE.set(mirror, size);
        return mirror;
    }

    private static int countBlocks(GameTestHelper helper, BlockState state) {
        int count = 0;
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 9; z++) {
                    if (helper.getBlockState(new BlockPos(1 + x, 3 + y, 1 + z)).is(state.getBlock())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static void mirrorPlacesSquare(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        clearArea(helper);
        lookDown(helper, player);
        Inventory inventory = player.getInventory();
        inventory.setItem(10, new ItemStack(Items.STONE, 5));
        ItemStack ring = ring(Items.STONE, 20, false);
        inventory.setItem(1, ring);
        ItemStack mirror = mirror(Items.STONE, 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, mirror);

        List<BlockPos> targets = WorldshaperGlassItem.getBlocksToPlace(mirror, player);
        helper.assertTrue(targets.size() == 9, "3x3 mirror must target 9 positions, got " + targets.size());
        for (BlockPos target : targets) {
            helper.assertTrue(target.getY() == helper.absolutePos(FLOOR).getY() + 1, "targets must lie on top of the clicked block");
        }

        int placed = WorldshaperGlassItem.placeAllBlocks(mirror, player);
        helper.assertTrue(placed == 9, "placed " + placed);
        helper.assertTrue(countBlocks(helper, Blocks.STONE.defaultBlockState()) == 9, "nine stone blocks expected on the layer above");
        helper.assertTrue(inventory.getItem(10).isEmpty(), "inventory stack must be used first");
        helper.assertTrue(BlackHoleRingItem.getCount(ring) == 16, "ring must pay the remaining four, holds " + BlackHoleRingItem.getCount(ring));

        clearArea(helper);
        TrinketsFeature.MIRROR_SIZE.set(mirror, 5);
        inventory.setItem(10, new ItemStack(Items.STONE, 64));
        TrinketsFeature.TALISMAN_COUNT.set(ring, 0);
        placed = WorldshaperGlassItem.placeAllBlocks(mirror, player);
        helper.assertTrue(placed == 25, "5x5 must place 25, placed " + placed);
        helper.assertTrue(inventory.getItem(10).getCount() == 39, "exactly 25 blocks must be consumed, left " + inventory.getItem(10).getCount());

        clearArea(helper);
        ServerPlayer creative = player(helper, GameType.CREATIVE);
        lookDown(helper, creative);
        creative.getInventory().setItem(0, new ItemStack(Items.STONE, 1));
        ItemStack creativeMirror = mirror(Items.STONE, 7);
        placed = WorldshaperGlassItem.placeAllBlocks(creativeMirror, creative);
        helper.assertTrue(placed == 49, "creative 7x7 must place 49, placed " + placed);
        helper.assertTrue(creative.getInventory().getItem(0).getCount() == 1, "creative must not consume");
        helper.succeed();
    }

    private static void mirrorNeedsEnoughBlocks(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        clearArea(helper);
        lookDown(helper, player);
        Inventory inventory = player.getInventory();
        inventory.setItem(0, new ItemStack(Items.STONE, 8));
        inventory.setItem(1, new ItemStack(Items.COBBLESTONE, 64));
        ItemStack mirror = mirror(Items.STONE, 3);
        int placed = WorldshaperGlassItem.placeAllBlocks(mirror, player);
        helper.assertTrue(placed == 0, "an unaffordable square must place nothing, placed " + placed);
        helper.assertTrue(inventory.getItem(0).getCount() == 8, "nothing may be consumed");
        helper.assertTrue(countBlocks(helper, Blocks.STONE.defaultBlockState()) == 0, "nothing may be placed");

        ItemStack empty = new ItemStack(TrinketsFeature.PLACEMENT_MIRROR.get());
        helper.assertTrue(WorldshaperGlassItem.placeAllBlocks(empty, player) == 0, "a mirror without a block must place nothing");
        helper.succeed();
    }

    private static void mirrorSizesAndBlock(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(new Vec3(1.5, 2.0, 1.5)));
        ItemStack mirror = new ItemStack(TrinketsFeature.PLACEMENT_MIRROR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, mirror);
        helper.assertTrue(WorldshaperGlassItem.getSize(mirror) == 3, "default size is 3");
        player.setShiftKeyDown(true);
        int[] expected = {5, 7, 9, 11, 3};
        for (int size : expected) {
            mirror.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertTrue(WorldshaperGlassItem.getSize(mirror) == size, "size must become " + size + ", is " + WorldshaperGlassItem.getSize(mirror));
        }
        helper.setBlock(FLOOR, Blocks.STONE.defaultBlockState());
        mirror.getItem().onItemUseFirst(mirror, click(helper, player, FLOOR, Direction.UP));
        helper.assertTrue(WorldshaperGlassItem.getItem(mirror) == Items.STONE, "sneak click must pick the block");
        helper.setBlock(FLOOR, Blocks.CHEST.defaultBlockState());
        mirror.getItem().onItemUseFirst(mirror, click(helper, player, FLOOR, Direction.UP));
        helper.assertTrue(WorldshaperGlassItem.getItem(mirror) == Items.STONE, "a block entity block must not be picked");
        helper.succeed();
    }

    private static ItemStack fireFocus() {
        return new ItemStack(ModItems.FOCUS_FIRE.get());
    }

    private static void pouchRoundTrip(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        ItemStack pouch = new ItemStack(TrinketsFeature.ICHOR_POUCH.get());
        helper.assertTrue(pouch.getItem() instanceof FocusPouchItem, "ichor pouch must be a focus pouch");
        helper.assertTrue(((FocusPouchItem) pouch.getItem()).getBaubleType(pouch) == BaubleType.BELT, "pouch must be a belt");
        player.getInventory().setItem(0, pouch);

        IchorPouchMenu menu = new IchorPouchMenu(1, player.getInventory(), pouch);
        helper.assertTrue(menu.slots.size() == IchorPouchMenu.SLOT_END, "slot count is " + menu.slots.size());
        Slot focusSlot = menu.slots.get(0);
        Slot freeSlot = menu.slots.get(40);
        helper.assertTrue(focusSlot.mayPlace(fireFocus()) && focusSlot.mayPlace(new ItemStack(Items.STONE)), "every slot takes foci and items");
        helper.assertTrue(freeSlot.mayPlace(new ItemStack(Items.STONE)) && freeSlot.mayPlace(fireFocus()), "other slots take anything");
        helper.assertTrue(!freeSlot.mayPlace(new ItemStack(TrinketsFeature.ICHOR_POUCH.get())), "pouches must not nest");
        helper.assertTrue(!freeSlot.mayPlace(new ItemStack(ModItems.FOCUS_POUCH.get())), "focus pouches must not nest");
        focusSlot.set(fireFocus());
        freeSlot.set(new ItemStack(Items.STONE, 64));
        menu.slots.get(IchorPouchMenu.POUCH_SLOTS - 1).set(new ItemStack(Items.DIAMOND, 3));
        menu.removed(player);

        helper.assertTrue(FocusPouchItem.getInventory(pouch).get(0).getItem() instanceof FocusItem, "focus must be stored in the base pouch data");
        helper.assertTrue(FocusPouchItem.getInventory(pouch).get(40).getCount() == 64, "items must be stored in the base pouch data");

        IchorPouchMenu reopened = new IchorPouchMenu(2, player.getInventory(), pouch);
        helper.assertTrue(reopened.slots.get(0).getItem().getItem() instanceof FocusItem, "focus was lost");
        helper.assertTrue(reopened.slots.get(40).getItem().getCount() == 64, "stone was lost");
        helper.assertTrue(reopened.slots.get(IchorPouchMenu.POUCH_SLOTS - 1).getItem().is(Items.DIAMOND), "last slot item was lost");

        ItemStack copy = pouch.copy();
        helper.assertTrue(FocusPouchItem.getInventory(copy).stream().filter(stack -> !stack.isEmpty()).count() == 3, "copy must keep the items");
        helper.succeed();
    }

    private static void pouchFoundByWandSwitcher(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        ItemStack pouch = new ItemStack(TrinketsFeature.ICHOR_POUCH.get());
        player.getInventory().setItem(9, pouch);
        IchorPouchMenu menu = new IchorPouchMenu(1, player.getInventory(), pouch);
        ItemStack focus = fireFocus();
        menu.slots.get(100).set(focus.copy());
        menu.slots.get(23).set(new ItemStack(Items.EMERALD, 9));
        menu.removed(player);

        ItemStack wand = Wands.create(WandRegistry.CAP_THAUMIUM, WandRegistry.ROD_GREATWOOD, false);
        player.getInventory().setItem(0, wand);
        String key = ((FocusItem) focus.getItem()).getSortingHelper(focus);
        WandManager.changeFocus(wand, helper.getLevel(), player, key);
        helper.assertTrue(Wands.getFocus(wand) != null, "the wand switcher must take the focus out of the ichor pouch");
        helper.assertTrue(FocusPouchItem.getInventory(pouch).get(100).isEmpty(), "the focus must leave the pouch");
        helper.assertTrue(FocusPouchItem.getInventory(pouch).get(23).is(Items.EMERALD) && FocusPouchItem.getInventory(pouch).get(23).getCount() == 9, "items in the other slots must survive a focus change");

        WandManager.changeFocus(wand, helper.getLevel(), player, WandManager.REMOVE_FOCUS);
        helper.assertTrue(Wands.getFocus(wand) == null, "focus must be removed from the wand");
        helper.assertTrue(FocusPouchItem.getInventory(pouch).stream().anyMatch(stack -> stack.getItem() instanceof FocusItem), "the focus must return into the pouch");
        helper.assertTrue(FocusPouchItem.getInventory(pouch).get(23).is(Items.EMERALD) && FocusPouchItem.getInventory(pouch).get(23).getCount() == 9, "items in the other slots must survive returning the focus");

        player.getInventory().setItem(9, ItemStack.EMPTY);
        Baubles.set(player, 3, pouch);
        WandManager.changeFocus(wand, helper.getLevel(), player, key);
        helper.assertTrue(Wands.getFocus(wand) != null, "the switcher must also find a worn ichor pouch");
        helper.succeed();
    }

    private static void catAmuletStopsCreepers(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(new Vec3(3.5, 2.0, 3.5)));
        Creeper creeper = helper.spawn(EntityType.CREEPER, new BlockPos(5, 2, 3));
        helper.assertTrue(creeper.goalSelector.getAvailableGoals().stream().anyMatch(wrapped -> wrapped.getGoal() instanceof WearerAvoidGoal), "creepers must get the avoid goal");
        creeper.setTarget(player);
        helper.assertTrue(creeper.getTarget() == player, "creeper must target a player without the amulet");

        ItemStack amulet = new ItemStack(TrinketsFeature.CAT_AMULET.get());
        helper.assertTrue(!CatAmuletItem.isWearing(player), "not wearing yet");
        Baubles.set(player, 0, amulet);
        helper.assertTrue(CatAmuletItem.isWearing(player), "amulet must count as worn");
        helper.assertTrue(((CatAmuletItem) amulet.getItem()).getBaubleType(amulet) == BaubleType.AMULET, "amulet bauble type");
        ((CatAmuletItem) amulet.getItem()).onWornTick(amulet, player);
        helper.assertTrue(creeper.getTarget() == null, "the amulet must drop the creeper target");
        creeper.setTarget(player);
        helper.assertTrue(creeper.getTarget() == null, "creepers near the wearer must not pick a new target");
        helper.assertTrue(creeper.getSwellDir() < 0, "the fuse must be held back");

        Baubles.set(player, 0, ItemStack.EMPTY);
        creeper.setTarget(player);
        helper.assertTrue(creeper.getTarget() == player, "without the amulet creepers target again");
        helper.succeed();
    }

    private static String toolType(ItemStack stack) {
        if (stack.is(KamiFeature.ICHORIUM_PICKAXE.get())) {
            return ProtoclayItem.PICK;
        }
        if (stack.is(KamiFeature.ICHORIUM_SHOVEL.get())) {
            return ProtoclayItem.SHOVEL;
        }
        if (stack.is(KamiFeature.ICHORIUM_SWORD.get())) {
            return ProtoclayItem.SWORD;
        }
        return null;
    }

    private static void protoclaySwapsTools(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        Inventory inventory = player.getInventory();
        ItemStack pick = new ItemStack(KamiFeature.ICHORIUM_PICKAXE.get());
        ItemStack shovel = new ItemStack(KamiFeature.ICHORIUM_SHOVEL.get());
        inventory.selected = 2;
        inventory.setItem(2, pick);
        inventory.setItem(20, shovel);
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();

        helper.assertTrue(!ProtoclayItem.swapTool(player, dirt, TrinketsGameTests::toolType), "no swap without protoclay");
        inventory.setItem(30, new ItemStack(TrinketsFeature.PROTOCLAY.get()));
        helper.assertTrue(!ProtoclayItem.swapTool(player, stone, TrinketsGameTests::toolType), "the right tool must stay");
        helper.assertTrue(inventory.getItem(2) == pick, "pick must stay in hand");
        helper.assertTrue(ProtoclayItem.swapTool(player, dirt, TrinketsGameTests::toolType), "dirt needs the shovel");
        helper.assertTrue(inventory.getItem(2) == shovel && inventory.getItem(20) == pick, "tools must trade places");
        helper.assertTrue(ProtoclayItem.swapTool(player, stone, TrinketsGameTests::toolType), "stone needs the pick");
        helper.assertTrue(inventory.getItem(2) == pick && inventory.getItem(20) == shovel, "tools must trade back");
        helper.assertTrue(!ProtoclayItem.swapTool(player, Blocks.OAK_LOG.defaultBlockState(), TrinketsGameTests::toolType), "no axe in the inventory means no swap");
        helper.assertTrue(inventory.getItem(2) == pick, "hand must stay unchanged without a matching tool");

        ItemStack sword = new ItemStack(KamiFeature.ICHORIUM_SWORD.get());
        inventory.setItem(2, sword);
        helper.assertTrue(!ProtoclayItem.swapTool(player, dirt, TrinketsGameTests::toolType), "swords are never swapped");
        helper.assertTrue(ProtoclayItem.advancedToolType(pick) == null, "plain ichorium tools are not advanced tools");
        helper.assertTrue(ProtoclayItem.typeFor(Blocks.OAK_LEAVES.defaultBlockState()).equals(ProtoclayItem.AXE), "leaves need the axe");
        helper.assertTrue(ProtoclayItem.typeFor(Blocks.SAND.defaultBlockState()).equals(ProtoclayItem.SHOVEL), "sand needs the shovel");
        helper.succeed();
    }
}
