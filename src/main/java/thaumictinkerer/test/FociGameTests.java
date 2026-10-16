package thaumictinkerer.test;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusUpgradeType;
import thaumictinkerer.foci.DislocatedBlock;
import thaumictinkerer.foci.DislocationFocus;
import thaumictinkerer.foci.FociFeature;
import thaumictinkerer.foci.HealFocus;
import thaumictinkerer.foci.SmeltFocus;
import thaumictinkerer.research.TtResearch;

public final class FociGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("foci")
        .add("research_registered", FociGameTests::researchRegistered)
        .add("recipes_registered", FociGameTests::recipesRegistered)
        .add("vis_costs", FociGameTests::visCosts)
        .add("upgrades", FociGameTests::upgrades)
        .add("smelt_converts_block", FociGameTests::smeltConvertsBlock)
        .add("dislocation_round_trip", FociGameTests::dislocationRoundTrip)
        .add("dislocation_refuses_unbreakable", FociGameTests::dislocationRefusesUnbreakable)
        .add("heal_heals", FociGameTests::healHeals)
        .add("flight_launches", FociGameTests::flightLaunches)
        .add("telekinesis_pulls_items", FociGameTests::telekinesisPullsItems)
        .add("deflect_moves_projectiles", FociGameTests::deflectMovesProjectiles)
        .add("ender_chest_opens", FociGameTests::enderChestOpens);

    private FociGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static ServerPlayer mockPlayer(GameTestHelper helper) {
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(player.getGameProfile(), false);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        player.connection = new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override
            public void send(Packet<?> packet) {
            }
        };
        helper.getLevel().addNewPlayer(player);
        player.connection.markClientLoaded();
        return player;
    }

    private static ItemStack wandWith(FocusItem focus) {
        ItemStack wand = Wands.create(WandRegistry.CAP_THAUMIUM, WandRegistry.ROD_GREATWOOD, false);
        Wands.setFocus(wand, new ItemStack(focus));
        Wands.fill(wand);
        return wand;
    }

    private static void place(ServerPlayer player, GameTestHelper helper, double x, double y, double z, float yaw, float pitch) {
        Vec3 position = helper.absoluteVec(new Vec3(x, y, z));
        player.setPos(position);
        player.setYRot(yaw);
        player.setXRot(pitch);
    }

    private static int spent(ItemStack wand, WandItem item, ServerPlayer player, AspectList cost, Aspect aspect) {
        return (int) (cost.getAmount(aspect) * item.getConsumptionModifier(wand, player, aspect, false));
    }

    private static void researchRegistered(GameTestHelper helper) {
        Object[][] expected = {
            {FociFeature.SMELT, -2, -2, 2, false, true, 0, "FOCUSEXCAVATION"},
            {FociFeature.FLIGHT, -3, -4, 2, false, true, 0, FociFeature.SMELT},
            {FociFeature.DISLOCATION, -5, -5, 2, true, true, 0, FociFeature.FLIGHT},
            {FociFeature.TELEKINESIS, -4, -6, 2, true, true, 0, FociFeature.FLIGHT},
            {FociFeature.DEFLECT, -4, -3, 3, true, true, 0, FociFeature.SMELT},
            {FociFeature.ENDER_CHEST, -6, -2, 2, false, true, 1, FociFeature.DEFLECT},
            {FociFeature.HEAL, -6, -4, 2, true, true, 0, FociFeature.DEFLECT},
        };
        for (Object[] row : expected) {
            String key = (String) row[0];
            ResearchItem research = ResearchCategories.getResearch(key);
            helper.assertTrue(research != null, key + " is not registered");
            helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), key + " wrong category");
            helper.assertTrue(research.column() == (int) row[1] && research.row() == (int) row[2], key + " wrong position");
            helper.assertTrue(research.complexity() == Math.min(3, (int) row[3]), key + " wrong complexity " + research.complexity());
            helper.assertTrue(research.isSecondary() == (boolean) row[4], key + " wrong secondary flag");
            helper.assertTrue(research.isConcealed() == (boolean) row[5], key + " wrong concealed flag");
            helper.assertTrue(research.warp() == (int) row[6], key + " wrong warp");
            helper.assertTrue(research.parents().equals(List.of((String) row[7])), key + " wrong parents " + research.parents());
        }
        helper.assertTrue(ResearchCategories.getResearch(FociFeature.DISLOCATION).pages().size() == 3, "dislocation page count");
        helper.assertTrue(ResearchCategories.getResearch(FociFeature.SMELT).pages().get(1) instanceof ResearchPage.Arcane, "smelt arcane page");
        helper.assertTrue(ResearchCategories.getResearch(FociFeature.ENDER_CHEST).pages().get(1) instanceof ResearchPage.Arcane, "ender chest arcane page");
        helper.assertTrue(ResearchCategories.getResearch(FociFeature.FLIGHT).pages().get(1) instanceof ResearchPage.Infusion, "flight infusion page");
        AspectList tags = ResearchCategories.getResearch(FociFeature.FLIGHT).tags();
        helper.assertTrue(tags.getAmount(Aspect.MOTION) == 1 && tags.getAmount(Aspect.MAGIC) == 1 && tags.getAmount(Aspect.AIR) == 2, "flight aspects");
        helper.succeed();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        Object[][] infusions = {
            {FociFeature.FLIGHT, 3, new AspectList().add(Aspect.AIR, 15).add(Aspect.MOTION, 20).add(Aspect.TRAVEL, 10), 7},
            {FociFeature.DISLOCATION, 8, new AspectList().add(Aspect.ELDRITCH, 20).add(Aspect.DARKNESS, 10).add(Aspect.VOID, 25).add(Aspect.MAGIC, 20).add(Aspect.TAINT, 5), 8},
            {FociFeature.TELEKINESIS, 5, new AspectList().add(Aspect.MOTION, 10).add(Aspect.AIR, 20).add(Aspect.ENTROPY, 20).add(Aspect.MIND, 10), 7},
            {FociFeature.HEAL, 4, new AspectList().add(Aspect.HEAL, 10).add(Aspect.SOUL, 10).add(Aspect.LIFE, 15), 4},
            {FociFeature.DEFLECT, 5, new AspectList().add(Aspect.AIR, 15).add(Aspect.ARMOR, 5).add(Aspect.ORDER, 20), 4},
        };
        for (Object[] row : infusions) {
            String key = (String) row[0];
            helper.assertTrue(TcRecipes.get(key) instanceof InfusionRecipe, key + " infusion recipe missing");
            InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get(key);
            helper.assertTrue(recipe.instability() == (int) row[1], key + " instability");
            helper.assertTrue(recipe.aspects().equals(row[2]), key + " aspects " + recipe.aspects());
            helper.assertTrue(recipe.components().size() == (int) row[3], key + " component count " + recipe.components().size());
            helper.assertTrue(recipe.research().equals(key), key + " research key");
            helper.assertTrue(recipe.result().exists() && recipe.central().exists(), key + " items missing");
            for (var component : recipe.components()) {
                helper.assertTrue(component.exists(), key + " component missing");
            }
        }
        helper.assertTrue(TcRecipes.get(FociFeature.SMELT) instanceof ShapedArcaneRecipe, "smelt arcane recipe");
        ShapedArcaneRecipe smelt = (ShapedArcaneRecipe) TcRecipes.get(FociFeature.SMELT);
        helper.assertTrue(smelt.width() == 3 && smelt.height() == 1, "smelt shape");
        helper.assertTrue(smelt.aspects().equals(new AspectList().add(Aspect.FIRE, 10).add(Aspect.ORDER, 5).add(Aspect.ENTROPY, 6)), "smelt aspects");
        helper.assertTrue(smelt.grid().stream().allMatch(ingredient -> ingredient.exists()), "smelt ingredients");
        ShapedArcaneRecipe chest = (ShapedArcaneRecipe) TcRecipes.get(FociFeature.ENDER_CHEST);
        helper.assertTrue(chest.width() == 1 && chest.height() == 3, "ender chest shape");
        helper.assertTrue(chest.aspects().equals(new AspectList().add(Aspect.ORDER, 10).add(Aspect.ENTROPY, 10)), "ender chest aspects");
        helper.assertTrue(chest.grid().stream().allMatch(ingredient -> ingredient.exists()), "ender chest ingredients");
        helper.succeed();
    }

    private static void visCosts(GameTestHelper helper) {
        ItemStack stack = ItemStack.EMPTY;
        helper.assertTrue(FociFeature.FOCUS_FLIGHT.get().getVisCost(stack).equals(new AspectList().add(Aspect.AIR, 15)), "flight cost");
        helper.assertTrue(
            FociFeature.FOCUS_DISLOCATION.get().getVisCost(stack).equals(new AspectList().add(Aspect.ENTROPY, 500).add(Aspect.ORDER, 500).add(Aspect.EARTH, 100)),
            "dislocation cost"
        );
        helper.assertTrue(FociFeature.FOCUS_TELEKINESIS.get().getVisCost(stack).equals(new AspectList().add(Aspect.AIR, 5).add(Aspect.ENTROPY, 5)), "telekinesis cost");
        helper.assertTrue(FociFeature.FOCUS_SMELT.get().getVisCost(stack).equals(new AspectList().add(Aspect.FIRE, 45).add(Aspect.ENTROPY, 12)), "smelt cost");
        helper.assertTrue(FociFeature.FOCUS_HEAL.get().getVisCost(stack).equals(new AspectList().add(Aspect.EARTH, 45).add(Aspect.WATER, 45)), "heal cost");
        helper.assertTrue(FociFeature.FOCUS_ENDER_CHEST.get().getVisCost(stack).equals(new AspectList().add(Aspect.ENTROPY, 100).add(Aspect.ORDER, 100)), "ender chest cost");
        helper.assertTrue(FociFeature.FOCUS_DEFLECT.get().getVisCost(stack).equals(new AspectList().add(Aspect.ORDER, 8).add(Aspect.AIR, 4)), "deflect cost");
        helper.assertTrue(FociFeature.FOCUS_DEFLECT.get().isVisCostPerTick(stack), "deflect per tick");
        helper.assertTrue(FociFeature.FOCUS_TELEKINESIS.get().isVisCostPerTick(stack), "telekinesis per tick");
        helper.assertTrue(!FociFeature.FOCUS_SMELT.get().isVisCostPerTick(stack), "smelt not per tick");
        ItemStack frugal = new ItemStack(FociFeature.FOCUS_DEFLECT.get());
        FociFeature.FOCUS_DEFLECT.get().applyUpgrade(frugal, FocusUpgradeType.FRUGAL, 1);
        helper.assertTrue(
            FociFeature.FOCUS_DEFLECT.get().getVisCost(frugal).equals(new AspectList().add(Aspect.ORDER, 7).add(Aspect.AIR, 4)),
            "deflect frugal cost " + FociFeature.FOCUS_DEFLECT.get().getVisCost(frugal)
        );
        helper.succeed();
    }

    private static void upgrades(GameTestHelper helper) {
        ItemStack stack = ItemStack.EMPTY;
        FocusUpgradeType[] both = {FocusUpgradeType.TREASURE, FocusUpgradeType.POTENCY};
        helper.assertTrue(java.util.Arrays.equals(FociFeature.FOCUS_FLIGHT.get().getPossibleUpgradesByRank(stack, 1), both), "flight upgrades");
        helper.assertTrue(java.util.Arrays.equals(FociFeature.FOCUS_SMELT.get().getPossibleUpgradesByRank(stack, 2), both), "smelt upgrades");
        helper.assertTrue(java.util.Arrays.equals(FociFeature.FOCUS_HEAL.get().getPossibleUpgradesByRank(stack, 3), both), "heal upgrades");
        helper.assertTrue(FociFeature.FOCUS_TELEKINESIS.get().getPossibleUpgradesByRank(stack, 1).length == 0, "telekinesis upgrades");
        helper.assertTrue(java.util.Arrays.equals(FociFeature.FOCUS_DEFLECT.get().getPossibleUpgradesByRank(stack, 1), new FocusUpgradeType[]{FocusUpgradeType.FRUGAL}), "deflect upgrades");
        helper.assertTrue(FociFeature.FOCUS_DISLOCATION.get().getPossibleUpgradesByRank(stack, 1) == null, "dislocation upgrades");
        helper.succeed();
    }

    private static void smeltConvertsBlock(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = mockPlayer(helper);
        place(player, helper, 1.5, 1.0, 0.5, 0.0F, 0.0F);
        BlockPos target = new BlockPos(1, 2, 3);
        helper.setBlock(target, Blocks.COBBLESTONE);
        SmeltFocus focus = FociFeature.FOCUS_SMELT.get();
        ItemStack wand = wandWith(focus);
        WandItem item = (WandItem) wand.getItem();
        int fireBefore = Wands.getVis(wand, Aspect.FIRE);
        int entropyBefore = Wands.getVis(wand, Aspect.ENTROPY);
        for (int tick = 0; tick < 40 && !helper.getBlockState(target).is(Blocks.STONE); tick++) {
            focus.onUsingFocusTick(wand, player, 72000 - tick);
        }
        helper.assertTrue(helper.getBlockState(target).is(Blocks.STONE), "cobblestone was not smelted into stone: " + helper.getBlockState(target));
        helper.assertTrue(
            fireBefore - Wands.getVis(wand, Aspect.FIRE) == spent(wand, item, player, focus.getVisCost(ItemStack.EMPTY), Aspect.FIRE),
            "smelt fire vis cost"
        );
        helper.assertTrue(
            entropyBefore - Wands.getVis(wand, Aspect.ENTROPY) == spent(wand, item, player, focus.getVisCost(ItemStack.EMPTY), Aspect.ENTROPY),
            "smelt entropy vis cost"
        );
        BlockPos plain = new BlockPos(1, 2, 3);
        helper.setBlock(plain, Blocks.OBSIDIAN);
        SmeltFocus.clearStates();
        for (int tick = 0; tick < 30; tick++) {
            focus.onUsingFocusTick(wand, player, 72000 - tick);
        }
        helper.assertTrue(helper.getBlockState(plain).is(Blocks.OBSIDIAN), "a block without a block smelting result changed");
        helper.assertTrue(level != null, "level");
        helper.succeed();
    }

    private static int countDiamonds(ChestBlockEntity chest) {
        int total = 0;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            ItemStack stack = chest.getItem(slot);
            if (stack.is(Items.DIAMOND)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void dislocationRoundTrip(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = mockPlayer(helper);
        place(player, helper, 3.5, 1.0, 5.5, 0.0F, 0.0F);
        BlockPos chestPos = new BlockPos(1, 1, 1);
        BlockPos floorPos = new BlockPos(4, 1, 1);
        helper.setBlock(chestPos, Blocks.CHEST);
        helper.setBlock(floorPos, Blocks.STONE);
        ChestBlockEntity chest = helper.getBlockEntity(chestPos, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 5));
        chest.setItem(13, new ItemStack(Items.DIAMOND, 7));
        chest.setItem(26, new ItemStack(Items.STICK, 3));
        DislocationFocus focus = FociFeature.FOCUS_DISLOCATION.get();
        ItemStack wand = wandWith(focus);
        WandItem item = (WandItem) wand.getItem();
        int entropyBefore = Wands.getVis(wand, Aspect.ENTROPY);
        BlockPos chestAbsolute = helper.absolutePos(chestPos);
        BlockHitResult pickHit = new BlockHitResult(Vec3.atCenterOf(chestAbsolute), Direction.UP, chestAbsolute, false);
        helper.assertTrue(focus.onFocusRightClick(wand, level, player, pickHit) == InteractionResult.SUCCESS, "pick did not complete");
        helper.assertTrue(helper.getBlockState(chestPos).isAir(), "chest was not removed");
        helper.assertTrue(level.getBlockEntity(chestAbsolute) == null, "chest block entity was not removed");
        AABB area = AABB.encapsulatingFullBlocks(helper.absolutePos(new BlockPos(0, 0, 0)), helper.absolutePos(new BlockPos(7, 7, 7)));
        helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, area).isEmpty(), "pickup dropped the chest contents");
        DislocatedBlock held = DislocationFocus.held(Wands.getFocusItem(wand));
        helper.assertTrue(held != null && held.state().is(Blocks.CHEST) && !held.tile().isEmpty(), "focus does not hold the chest");
        int tileCost = (int) (2500 * item.getConsumptionModifier(wand, player, Aspect.ENTROPY, false));
        helper.assertTrue(entropyBefore - Wands.getVis(wand, Aspect.ENTROPY) == tileCost, "tile entity pickup cost " + (entropyBefore - Wands.getVis(wand, Aspect.ENTROPY)));

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, level.registryAccess());
        JsonElement saved = ItemStack.CODEC.encodeStart(ops, wand).getOrThrow();
        ItemStack loaded = ItemStack.CODEC.parse(ops, saved).getOrThrow();
        helper.assertTrue(DislocationFocus.held(Wands.getFocusItem(loaded)) != null, "held block lost on save and load");

        BlockPos floorAbsolute = helper.absolutePos(floorPos);
        BlockHitResult placeHit = new BlockHitResult(Vec3.atCenterOf(floorAbsolute), Direction.UP, floorAbsolute, false);
        int entropyAfterPickup = Wands.getVis(loaded, Aspect.ENTROPY);
        helper.assertTrue(focus.onFocusRightClick(loaded, level, player, placeHit) == InteractionResult.SUCCESS, "place did not complete");
        BlockPos placed = floorPos.above();
        helper.assertTrue(helper.getBlockState(placed).is(Blocks.CHEST), "chest was not placed");
        ChestBlockEntity moved = helper.getBlockEntity(placed, ChestBlockEntity.class);
        helper.assertTrue(countDiamonds(moved) == 12, "diamonds changed: " + countDiamonds(moved));
        helper.assertTrue(moved.getItem(0).getCount() == 5 && moved.getItem(13).getCount() == 7, "slots changed");
        helper.assertTrue(moved.getItem(26).is(Items.STICK) && moved.getItem(26).getCount() == 3, "stick slot changed");
        helper.assertTrue(moved.getBlockPos().equals(helper.absolutePos(placed)), "moved block entity has a stale position");
        helper.assertTrue(DislocationFocus.held(Wands.getFocusItem(loaded)) == null, "held block was not cleared after placement");
        helper.assertTrue(Wands.getVis(loaded, Aspect.ENTROPY) == entropyAfterPickup, "placement cost vis");
        helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, area).isEmpty(), "placement spawned items");
        helper.assertTrue(helper.getBlockState(chestPos).isAir(), "source position was refilled");
        helper.succeed();
    }

    private static void dislocationRefusesUnbreakable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = mockPlayer(helper);
        place(player, helper, 3.5, 1.0, 5.5, 0.0F, 0.0F);
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, Blocks.BEDROCK);
        DislocationFocus focus = FociFeature.FOCUS_DISLOCATION.get();
        ItemStack wand = wandWith(focus);
        int before = Wands.getVis(wand, Aspect.ENTROPY);
        BlockPos absolute = helper.absolutePos(pos);
        focus.onFocusRightClick(wand, level, player, new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        helper.assertTrue(helper.getBlockState(pos).is(Blocks.BEDROCK), "bedrock was picked up");
        helper.assertTrue(Wands.getVis(wand, Aspect.ENTROPY) == before, "vis was spent on a refused pickup");
        helper.assertTrue(DislocationFocus.held(Wands.getFocusItem(wand)) == null, "refused block was stored");
        helper.setBlock(pos, Blocks.MOVING_PISTON);
        focus.onFocusRightClick(wand, level, player, new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        helper.assertTrue(DislocationFocus.held(Wands.getFocusItem(wand)) == null, "moving piston was stored");
        helper.succeed();
    }

    private static void healHeals(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        HealFocus focus = FociFeature.FOCUS_HEAL.get();
        ItemStack wand = wandWith(focus);
        WandItem item = (WandItem) wand.getItem();
        player.setHealth(10.0F);
        int earthBefore = Wands.getVis(wand, Aspect.EARTH);
        for (int tick = 0; tick < HealFocus.threshold(0) - 1; tick++) {
            focus.onUsingFocusTick(wand, player, 72000 - tick);
        }
        helper.assertTrue(player.getHealth() == 10.0F, "healed before the threshold");
        focus.onUsingFocusTick(wand, player, 1);
        helper.assertTrue(player.getHealth() == 11.0F, "did not heal one point: " + player.getHealth());
        helper.assertTrue(
            earthBefore - Wands.getVis(wand, Aspect.EARTH) == spent(wand, item, player, focus.getVisCost(ItemStack.EMPTY), Aspect.EARTH),
            "heal vis cost"
        );
        player.setHealth(player.getMaxHealth());
        int full = Wands.getVis(wand, Aspect.EARTH);
        for (int tick = 0; tick < 40; tick++) {
            focus.onUsingFocusTick(wand, player, 72000 - tick);
        }
        helper.assertTrue(Wands.getVis(wand, Aspect.EARTH) == full, "vis spent at full health");
        helper.succeed();
    }

    private static void flightLaunches(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = mockPlayer(helper);
        place(player, helper, 3.5, 1.0, 3.5, 0.0F, -45.0F);
        player.fallDistance = 12.0;
        var focus = FociFeature.FOCUS_FLIGHT.get();
        ItemStack wand = wandWith(focus);
        WandItem item = (WandItem) wand.getItem();
        int airBefore = Wands.getVis(wand, Aspect.AIR);
        helper.assertTrue(focus.onFocusRightClick(wand, level, player, null) == InteractionResult.SUCCESS, "flight did not complete");
        Vec3 look = player.getLookAngle();
        Vec3 motion = player.getDeltaMovement();
        double force = 1 / 1.5;
        helper.assertTrue(
            Math.abs(motion.x - look.x * force) < 1.0E-6 && Math.abs(motion.y - look.y * force) < 1.0E-6 && Math.abs(motion.z - look.z * force) < 1.0E-6,
            "flight motion " + motion
        );
        helper.assertTrue(player.fallDistance == 0.0, "fall distance was not reset");
        helper.assertTrue(airBefore - Wands.getVis(wand, Aspect.AIR) == spent(wand, item, player, focus.getVisCost(ItemStack.EMPTY), Aspect.AIR), "flight vis cost");
        ItemStack empty = Wands.create(WandRegistry.CAP_IRON, WandRegistry.ROD_WOOD, false);
        Wands.setFocus(empty, new ItemStack(focus));
        player.setDeltaMovement(Vec3.ZERO);
        focus.onFocusRightClick(empty, level, player, null);
        helper.assertTrue(player.getDeltaMovement().equals(Vec3.ZERO), "flight worked without vis");
        helper.succeed();
    }

    private static void telekinesisPullsItems(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = mockPlayer(helper);
        place(player, helper, 3.5, 1.0, 3.5, 0.0F, 0.0F);
        player.setShiftKeyDown(true);
        var focus = FociFeature.FOCUS_TELEKINESIS.get();
        ItemStack wand = wandWith(focus);
        WandItem item = (WandItem) wand.getItem();
        Vec3 spawn = helper.absoluteVec(new Vec3(1.5, 2.0, 3.5));
        ItemEntity entity = new ItemEntity(level, spawn.x, spawn.y, spawn.z, new ItemStack(Items.DIAMOND));
        entity.setNoPickUpDelay();
        entity.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(entity);
        int airBefore = Wands.getVis(wand, Aspect.AIR);
        focus.onUsingFocusTick(wand, player, 72000);
        Vec3 motion = entity.getDeltaMovement();
        Vec3 expectedDirection = player.position().add(0.0, player.getBbHeight() / 2.0 + 0.5, 0.0).subtract(entity.position().add(0.0, entity.getBbHeight() / 2.0, 0.0));
        helper.assertTrue(motion.length() > 0.2 && motion.length() < 0.34, "pull strength " + motion.length());
        helper.assertTrue(motion.normalize().dot(expectedDirection.normalize()) > 0.99, "item was not pulled to the caster");
        helper.assertTrue(airBefore - Wands.getVis(wand, Aspect.AIR) == spent(wand, item, player, focus.getVisCost(ItemStack.EMPTY), Aspect.AIR), "telekinesis vis cost");
        int idle = Wands.getVis(wand, Aspect.AIR);
        entity.discard();
        focus.onUsingFocusTick(wand, player, 72000);
        helper.assertTrue(Wands.getVis(wand, Aspect.AIR) == idle, "vis spent without items in range");
        helper.succeed();
    }

    private static void deflectMovesProjectiles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = mockPlayer(helper);
        place(player, helper, 3.5, 1.0, 3.5, 0.0F, 0.0F);
        var focus = FociFeature.FOCUS_DEFLECT.get();
        ItemStack wand = wandWith(focus);
        WandItem item = (WandItem) wand.getItem();
        Vec3 start = player.position().add(2.0, 1.0, 0.0);
        Projectile snowball = EntityTypes.SNOWBALL.create(level, EntitySpawnReason.TRIGGERED);
        snowball.setPos(start);
        snowball.setDeltaMovement(-0.5, 0.0, 0.0);
        level.addFreshEntity(snowball);
        Projectile bottle = EntityTypes.EXPERIENCE_BOTTLE.create(level, EntitySpawnReason.TRIGGERED);
        bottle.setPos(start);
        bottle.setDeltaMovement(-0.5, 0.0, 0.0);
        level.addFreshEntity(bottle);
        Projectile own = EntityTypes.SNOWBALL.create(level, EntitySpawnReason.TRIGGERED);
        own.setOwner(player);
        own.setPos(start);
        own.setDeltaMovement(-0.5, 0.0, 0.0);
        level.addFreshEntity(own);
        int orderBefore = Wands.getVis(wand, Aspect.ORDER);
        focus.onUsingFocusTick(wand, player, 72000);
        helper.assertTrue(snowball.getX() < player.getX() - 1.0, "snowball was not carried past the caster: " + snowball.getX());
        helper.assertTrue(bottle.getX() == start.x, "experience bottle was deflected");
        helper.assertTrue(own.getX() == start.x, "own projectile was deflected");
        helper.assertTrue(orderBefore - Wands.getVis(wand, Aspect.ORDER) == (int) (8 * item.getConsumptionModifier(wand, player, Aspect.ORDER, false)), "deflect vis cost");
        helper.succeed();
    }

    private static void enderChestOpens(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = mockPlayer(helper);
        var focus = FociFeature.FOCUS_ENDER_CHEST.get();
        ItemStack wand = wandWith(focus);
        WandItem item = (WandItem) wand.getItem();
        int entropyBefore = Wands.getVis(wand, Aspect.ENTROPY);
        int orderBefore = Wands.getVis(wand, Aspect.ORDER);
        player.getEnderChestInventory().setItem(0, new ItemStack(Items.EMERALD, 3));
        helper.assertTrue(focus.onFocusRightClick(wand, level, player, null) == InteractionResult.SUCCESS, "ender chest did not complete");
        helper.assertTrue(player.containerMenu instanceof ChestMenu, "ender chest menu did not open");
        ChestMenu menu = (ChestMenu) player.containerMenu;
        helper.assertTrue(menu.getContainer() == player.getEnderChestInventory(), "menu is not backed by the ender chest");
        helper.assertTrue(menu.getContainer().getItem(0).is(Items.EMERALD), "ender chest contents missing");
        helper.assertTrue(entropyBefore - Wands.getVis(wand, Aspect.ENTROPY) == (int) (100 * item.getConsumptionModifier(wand, player, Aspect.ENTROPY, false)), "ender chest entropy cost");
        helper.assertTrue(orderBefore - Wands.getVis(wand, Aspect.ORDER) == (int) (100 * item.getConsumptionModifier(wand, player, Aspect.ORDER, false)), "ender chest order cost");
        helper.succeed();
    }
}
