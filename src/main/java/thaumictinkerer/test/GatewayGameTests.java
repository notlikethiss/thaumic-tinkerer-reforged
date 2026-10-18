package thaumictinkerer.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumictinkerer.TtRarity;
import thaumictinkerer.kami.gateway.GatewayFeature;
import thaumictinkerer.kami.gateway.PearlLocation;
import thaumictinkerer.kami.gateway.SkyPearlItem;
import thaumictinkerer.kami.gateway.WarpGateBlockEntity;
import thaumictinkerer.kami.gateway.WarpGateMenu;
import thaumictinkerer.network.WarpGateButtonPayload;
import thaumictinkerer.network.WarpGateTeleportPayload;
import thaumictinkerer.research.KamiResearchItem;
import thaumictinkerer.research.TtResearch;

public final class GatewayGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("gateway")
        .add("research_registered", GatewayGameTests::researchRegistered)
        .add("recipes_registered", GatewayGameTests::recipesRegistered)
        .add("pearl_attunes_to_gate", GatewayGameTests::pearlAttunes)
        .add("teleports_to_installed_pearl", GatewayGameTests::teleports)
        .add("rejects_forged_destinations", GatewayGameTests::rejectsForged)
        .add("locked_gate_refuses", GatewayGameTests::lockedRefuses)
        .add("persists_inventory_and_binding", GatewayGameTests::persists)
        .add("menu_accepts_only_pearls", GatewayGameTests::menuSlots)
        .add("button_payload_validated", GatewayGameTests::buttonPayload)
        .add("drops_pearls_when_broken", GatewayGameTests::dropsPearls);

    private GatewayGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static ServerPlayer mockPlayer(GameTestHelper helper) {
        ServerPlayer player = (ServerPlayer) TtTestSupport.mockServerPlayer(helper, GameType.SURVIVAL);
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

    private static WarpGateBlockEntity gate(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, GatewayFeature.WARP_GATE.get());
        return (WarpGateBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static ItemStack pearl(GameTestHelper helper, BlockPos relativeGate) {
        ItemStack stack = new ItemStack(GatewayFeature.SKY_PEARL.get());
        SkyPearlItem.attune(stack, new PearlLocation(helper.getLevel().dimension(), helper.absolutePos(relativeGate)));
        return stack;
    }

    private static void stand(GameTestHelper helper, ServerPlayer player, BlockPos relativeGate) {
        player.setPos(helper.absoluteVec(new Vec3(relativeGate.getX() + 0.5, relativeGate.getY() + 1.0, relativeGate.getZ() + 0.5)));
    }

    private static Vec3 landing(GameTestHelper helper, BlockPos relativeGate) {
        return helper.absoluteVec(new Vec3(relativeGate.getX() + 0.5, relativeGate.getY() + 1.6, relativeGate.getZ() + 0.5));
    }

    private static boolean at(ServerPlayer player, Vec3 expected) {
        return player.position().distanceToSqr(expected) < 0.01;
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem research = ResearchCategories.getResearch(GatewayFeature.WARP_GATE_RESEARCH);
        helper.assertTrue(research instanceof KamiResearchItem, "WARP_GATE is not a kami research");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), "wrong category");
        helper.assertTrue(research.column() == 19 && research.row() == 6, "wrong position");
        helper.assertTrue(research.complexity() == 3 && research.warp() == 0 && research.isConcealed(), "wrong flags");
        helper.assertTrue(research.parents().equals(List.of("ICHORCLOTH_CHEST_GEM")), "wrong parents " + research.parents());
        helper.assertTrue(research.parentsHidden().contains("ICHORCLOTH_BOOTS_GEM"), "wrong hidden parents " + research.parentsHidden());
        helper.assertTrue(
            research.tags().getAmount(Aspect.TRAVEL) == 2 && research.tags().getAmount(Aspect.ELDRITCH) == 1
                && research.tags().getAmount(Aspect.FLIGHT) == 1 && research.tags().getAmount(Aspect.MECHANISM) == 1,
            "wrong aspects"
        );
        helper.assertTrue(research.pages().size() == 4, "wrong page count");
        helper.assertTrue(research.pages().get(1) instanceof ResearchPage.Infusion && research.pages().get(3) instanceof ResearchPage.Infusion, "wrong infusion pages");
        helper.succeed();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        InfusionRecipe pearl = (InfusionRecipe) TcRecipes.get(GatewayFeature.SKY_PEARL_RECIPE);
        helper.assertTrue(pearl != null && pearl.research().equals("WARP_GATE"), "pearl recipe missing");
        helper.assertTrue(pearl.instability() == 6 && pearl.components().size() == 4 && pearl.result().count() == 2, "pearl recipe shape");
        helper.assertTrue(
            pearl.aspects().getAmount(Aspect.TRAVEL) == 32 && pearl.aspects().getAmount(Aspect.ELDRITCH) == 32
                && pearl.aspects().getAmount(Aspect.FLIGHT) == 32 && pearl.aspects().getAmount(Aspect.AIR) == 16,
            "pearl aspects"
        );
        helper.assertTrue(pearl.result().item().toString().equals("thaumictinkerer:sky_pearl"), "pearl result");
        InfusionRecipe gate = (InfusionRecipe) TcRecipes.get(GatewayFeature.WARP_GATE_RESEARCH);
        helper.assertTrue(gate != null && gate.research().equals("WARP_GATE"), "gate recipe missing");
        helper.assertTrue(gate.instability() == 8 && gate.components().size() == 6, "gate recipe shape");
        helper.assertTrue(
            gate.aspects().getAmount(Aspect.TRAVEL) == 64 && gate.aspects().getAmount(Aspect.ELDRITCH) == 50 && gate.aspects().getAmount(Aspect.FLIGHT) == 50,
            "gate aspects"
        );
        helper.assertTrue(gate.result().item().toString().equals("thaumictinkerer:warp_gate"), "gate result");
        helper.assertTrue(new ItemStack(GatewayFeature.SKY_PEARL.get()).getRarity() == TtRarity.kami(), "pearl rarity");
        helper.succeed();
    }

    private static UseOnContext context(GameTestHelper helper, ServerPlayer player, ItemStack stack, BlockPos relative) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos pos = helper.absolutePos(relative);
        return new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static void pearlAttunes(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        BlockPos first = new BlockPos(1, 1, 1);
        BlockPos second = new BlockPos(5, 1, 5);
        BlockPos stone = new BlockPos(3, 1, 3);
        gate(helper, first);
        gate(helper, second);
        helper.setBlock(stone, Blocks.STONE);
        ItemStack stack = new ItemStack(GatewayFeature.SKY_PEARL.get());
        helper.assertTrue(!SkyPearlItem.isAttuned(stack) && !stack.getItem().isFoil(stack), "fresh pearl is attuned");
        stack.getItem().useOn(context(helper, player, stack, stone));
        helper.assertTrue(!SkyPearlItem.isAttuned(stack), "pearl attuned to a plain block");
        stack.getItem().useOn(context(helper, player, stack, first));
        PearlLocation location = SkyPearlItem.location(stack);
        helper.assertTrue(location != null && location.pos().equals(helper.absolutePos(first)) && location.dimension().equals(helper.getLevel().dimension()), "pearl not bound to the gate");
        helper.assertTrue(stack.getItem().isFoil(stack), "attuned pearl has no glint");
        stack.getItem().useOn(context(helper, player, stack, second));
        helper.assertTrue(SkyPearlItem.location(stack).pos().equals(helper.absolutePos(first)), "attuned pearl was rebound");
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(SkyPearlItem.isAttuned(stack), "pearl cleared without sneaking");
        player.setShiftKeyDown(true);
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(!SkyPearlItem.isAttuned(stack), "sneak use did not clear the pearl");
        helper.succeed();
    }

    private static void teleports(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        BlockPos origin = new BlockPos(1, 1, 1);
        BlockPos target = new BlockPos(5, 1, 5);
        WarpGateBlockEntity source = gate(helper, origin);
        gate(helper, target);
        source.setItem(2, pearl(helper, target));
        stand(helper, player, origin);
        source.teleportPlayer(player, 2);
        helper.assertTrue(at(player, landing(helper, target)), "player did not arrive at the destination gate: " + player.position());
        helper.succeed();
    }

    private static void rejectsForged(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        BlockPos origin = new BlockPos(1, 1, 1);
        BlockPos target = new BlockPos(5, 1, 5);
        BlockPos missing = new BlockPos(5, 1, 1);
        WarpGateBlockEntity source = gate(helper, origin);
        gate(helper, target);
        source.setItem(0, pearl(helper, missing));
        source.setItem(1, pearl(helper, target));
        ItemStack foreign = new ItemStack(GatewayFeature.SKY_PEARL.get());
        SkyPearlItem.attune(foreign, new PearlLocation(Level.NETHER, helper.absolutePos(target)));
        source.setItem(3, foreign);
        Vec3 home = helper.absoluteVec(new Vec3(1.5, 2.0, 1.5));
        stand(helper, player, origin);

        source.teleportPlayer(player, 0);
        helper.assertTrue(at(player, home), "teleported to a position without a gate");
        source.teleportPlayer(player, 2);
        helper.assertTrue(at(player, home), "teleported through an empty slot");
        source.teleportPlayer(player, -1);
        source.teleportPlayer(player, 10);
        source.teleportPlayer(player, 99);
        helper.assertTrue(at(player, home), "teleported through an out of range slot");
        source.teleportPlayer(player, 3);
        helper.assertTrue(at(player, home), "teleported through a pearl bound to another dimension");

        WarpGateTeleportPayload.handle(new WarpGateTeleportPayload(helper.absolutePos(missing), 1), player);
        helper.assertTrue(at(player, home), "payload for a position without a gate teleported");
        WarpGateTeleportPayload.handle(new WarpGateTeleportPayload(helper.absolutePos(target), 1), player);
        helper.assertTrue(at(player, home), "payload for a gate without that pearl teleported");

        player.setPos(helper.absoluteVec(new Vec3(3.5, 2.0, 3.5)));
        WarpGateTeleportPayload.handle(new WarpGateTeleportPayload(helper.absolutePos(origin), 1), player);
        helper.assertTrue(player.position().distanceToSqr(helper.absoluteVec(new Vec3(3.5, 2.0, 3.5))) < 0.01, "player away from the gate teleported");

        stand(helper, player, origin);
        WarpGateTeleportPayload.handle(new WarpGateTeleportPayload(helper.absolutePos(origin), 1), player);
        helper.assertTrue(at(player, landing(helper, target)), "valid payload did not teleport");
        helper.succeed();
    }

    private static void lockedRefuses(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        BlockPos origin = new BlockPos(1, 1, 1);
        BlockPos target = new BlockPos(5, 1, 5);
        WarpGateBlockEntity source = gate(helper, origin);
        WarpGateBlockEntity destination = gate(helper, target);
        source.setItem(0, pearl(helper, target));
        destination.setLocked(true);
        Vec3 home = helper.absoluteVec(new Vec3(1.5, 2.0, 1.5));
        stand(helper, player, origin);
        source.teleportPlayer(player, 0);
        helper.assertTrue(at(player, home), "teleported into a gate that does not accept teleports");
        destination.setLocked(false);
        source.teleportPlayer(player, 0);
        helper.assertTrue(at(player, landing(helper, target)), "unlocked gate refused a teleport");
        helper.succeed();
    }

    private static void persists(GameTestHelper helper) {
        BlockPos origin = new BlockPos(1, 1, 1);
        BlockPos target = new BlockPos(5, 1, 5);
        WarpGateBlockEntity source = gate(helper, origin);
        source.setItem(4, pearl(helper, target));
        source.setItem(9, new ItemStack(GatewayFeature.SKY_PEARL.get()));
        source.setLocked(true);
        CompoundTag saved = source.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity loaded = BlockEntity.loadStatic(source.getBlockPos(), source.getBlockState(), saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded instanceof WarpGateBlockEntity, "block entity did not load");
        WarpGateBlockEntity copy = (WarpGateBlockEntity) loaded;
        helper.assertTrue(copy.isLocked(), "locked flag lost");
        PearlLocation location = SkyPearlItem.location(copy.getItem(4));
        helper.assertTrue(location != null && location.pos().equals(helper.absolutePos(target)) && location.dimension().equals(helper.getLevel().dimension()), "pearl binding lost");
        helper.assertTrue(copy.getItem(9).getItem() instanceof SkyPearlItem && !SkyPearlItem.isAttuned(copy.getItem(9)), "unbound pearl lost");
        helper.assertTrue(copy.getItem(0).isEmpty() && copy.getItem(5).isEmpty(), "phantom items");
        helper.succeed();
    }

    private static void menuSlots(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        WarpGateBlockEntity source = gate(helper, new BlockPos(1, 1, 1));
        WarpGateMenu menu = new WarpGateMenu(3, player.getInventory(), source);
        Slot slot = menu.getSlot(0);
        helper.assertTrue(slot.mayPlace(new ItemStack(GatewayFeature.SKY_PEARL.get())) && !slot.mayPlace(new ItemStack(Items.ENDER_PEARL)), "slot filter");
        helper.assertTrue(slot.getMaxStackSize() == 1 && source.getMaxStackSize() == 1, "stack limit");
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND));
        player.getInventory().setItem(1, pearl(helper, new BlockPos(5, 1, 5)));
        int diamond = WarpGateBlockEntity.SIZE + 27;
        helper.assertTrue(menu.quickMoveStack(player, diamond).isEmpty() && source.isEmpty(), "shift click moved a non pearl into the gate");
        menu.quickMoveStack(player, diamond + 1);
        helper.assertTrue(source.getItem(0).getItem() instanceof SkyPearlItem, "shift click did not move the pearl into the gate");
        helper.assertTrue(player.getInventory().getItem(1).isEmpty(), "pearl duplicated by shift click");
        menu.quickMoveStack(player, 0);
        helper.assertTrue(source.isEmpty() && player.getInventory().countItem(GatewayFeature.SKY_PEARL.get()) == 1, "shift click out of the gate failed");
        helper.succeed();
    }

    private static void buttonPayload(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        WarpGateBlockEntity source = gate(helper, new BlockPos(1, 1, 1));
        WarpGateButtonPayload.handle(new WarpGateButtonPayload(true), player);
        helper.assertTrue(!source.isLocked(), "payload without an open menu changed the gate");
        player.setPos(helper.absoluteVec(new Vec3(1.5, 2.0, 1.5)));
        player.containerMenu = new WarpGateMenu(7, player.getInventory(), source);
        WarpGateButtonPayload.handle(new WarpGateButtonPayload(true), player);
        helper.assertTrue(source.isLocked(), "payload did not apply");
        helper.assertTrue(((WarpGateMenu) player.containerMenu).isLocked(), "menu data not updated");
        player.setPos(helper.absoluteVec(new Vec3(40.0, 1.0, 40.0)));
        WarpGateButtonPayload.handle(new WarpGateButtonPayload(false), player);
        helper.assertTrue(source.isLocked(), "distant player changed the gate");
        helper.succeed();
    }

    private static void dropsPearls(GameTestHelper helper) {
        BlockPos origin = new BlockPos(1, 1, 1);
        WarpGateBlockEntity source = gate(helper, origin);
        source.setItem(0, pearl(helper, new BlockPos(5, 1, 5)));
        source.setItem(1, pearl(helper, new BlockPos(5, 1, 3)));
        helper.setBlock(origin, Blocks.AIR);
        AABB box = new AABB(helper.absolutePos(origin)).inflate(2.0);
        int pearls = 0;
        for (ItemEntity entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, box)) {
            if (entity.getItem().getItem() instanceof SkyPearlItem && SkyPearlItem.isAttuned(entity.getItem())) {
                pearls += entity.getItem().getCount();
            }
        }
        helper.assertTrue(pearls == 2, "gate dropped " + pearls + " bound pearls");
        helper.succeed();
    }
}
