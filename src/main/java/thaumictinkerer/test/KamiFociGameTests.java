package thaumictinkerer.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusItem;
import thaumictinkerer.TtRarity;
import thaumictinkerer.kami.KamiFeature;
import thaumictinkerer.kami.foci.KamiFociFeature;
import thaumictinkerer.kami.foci.RecallFocus;
import thaumictinkerer.kami.foci.ShadowbeamFocus;
import thaumictinkerer.kami.foci.XpDrainFocus;
import thaumictinkerer.kami.gateway.GatewayFeature;
import thaumictinkerer.kami.gateway.PearlLocation;
import thaumictinkerer.kami.gateway.SkyPearlItem;
import thaumictinkerer.kami.gateway.WarpGateBlockEntity;
import thaumictinkerer.research.KamiResearchItem;
import thaumictinkerer.research.TtResearch;

public final class KamiFociGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("kami_foci")
        .isolated()
        .add("research_registered", KamiFociGameTests::researchRegistered)
        .add("recipes_registered", KamiFociGameTests::recipesRegistered)
        .add("vis_costs_and_rarity", KamiFociGameTests::visCosts)
        .add("beam_entity_registered", KamiFociGameTests::beamEntity)
        .add("shadowbeam_damages_mob", KamiFociGameTests::shadowbeamDamages)
        .add("shadowbeam_reflects", KamiFociGameTests::shadowbeamReflects)
        .add("xp_drain_moves_xp_into_vis", KamiFociGameTests::xpDrain)
        .add("xp_drain_needs_xp_and_room", KamiFociGameTests::xpDrainLimits)
        .add("recall_teleports_and_consumes_vis", KamiFociGameTests::recallTeleports)
        .add("recall_refuses_invalid", KamiFociGameTests::recallRefuses);

    private KamiFociGameTests() {
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
        ItemStack wand = Wands.create(WandRegistry.CAP_THAUMIUM, KamiFeature.ichorclothRod, false);
        Wands.setFocus(wand, new ItemStack(focus));
        Wands.fill(wand);
        return wand;
    }

    private static int spent(ItemStack wand, WandItem item, ServerPlayer player, AspectList cost, Aspect aspect) {
        return (int) (cost.getAmount(aspect) * item.getConsumptionModifier(wand, player, aspect, false));
    }

    private static void place(ServerPlayer player, GameTestHelper helper, double x, double y, double z, float yaw, float pitch) {
        player.setPos(helper.absoluteVec(new Vec3(x, y, z)));
        player.setYRot(yaw);
        player.setXRot(pitch);
    }

    private static ResearchItem kami(GameTestHelper helper, String key, int column, int row) {
        ResearchItem research = ResearchCategories.getResearch(key);
        helper.assertTrue(research instanceof KamiResearchItem, key + " is not a kami research");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), key + " has wrong category");
        helper.assertTrue(research.column() == column && research.row() == row, key + " has wrong position");
        helper.assertTrue(research.complexity() == 3 && research.warp() == 0 && research.isConcealed(), key + " has wrong flags");
        helper.assertTrue(research.pages().size() == 2 && research.pages().get(1) instanceof ResearchPage.Infusion, key + " has wrong pages");
        return research;
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem shadowbeam = kami(helper, KamiFociFeature.SHADOWBEAM, 14, 4);
        helper.assertTrue(shadowbeam.parents().equals(List.of("ROD_ICHORCLOTH")), "shadowbeam parents " + shadowbeam.parents());
        helper.assertTrue(
            shadowbeam.tags().getAmount(Aspect.DARKNESS) == 2 && shadowbeam.tags().getAmount(Aspect.MAGIC) == 1
                && shadowbeam.tags().getAmount(Aspect.ELDRITCH) == 1 && shadowbeam.tags().getAmount(Aspect.TAINT) == 1,
            "shadowbeam aspects"
        );
        ResearchItem drain = kami(helper, KamiFociFeature.XP_DRAIN, 12, 3);
        helper.assertTrue(drain.parents().equals(List.of("ROD_ICHORCLOTH")), "xp drain parents " + drain.parents());
        helper.assertTrue(
            drain.tags().getAmount(Aspect.MIND) == 2 && drain.tags().getAmount(Aspect.MAGIC) == 1
                && drain.tags().getAmount(Aspect.AURA) == 1 && drain.tags().getAmount(Aspect.MAN) == 1,
            "xp drain aspects"
        );
        ResearchItem recall = kami(helper, KamiFociFeature.RECALL, 20, 8);
        helper.assertTrue(recall.parents().equals(List.of("WARP_GATE")), "recall parents " + recall.parents());
        helper.assertTrue(recall.parentsHidden().contains("ROD_ICHORCLOTH"), "recall hidden parents " + recall.parentsHidden());
        helper.assertTrue(
            recall.tags().getAmount(Aspect.TRAVEL) == 2 && recall.tags().getAmount(Aspect.ELDRITCH) == 1
                && recall.tags().getAmount(Aspect.FLIGHT) == 1 && recall.tags().getAmount(Aspect.MAGIC) == 1,
            "recall aspects"
        );
        helper.succeed();
    }

    private static InfusionRecipe infusion(GameTestHelper helper, String key, String research, int instability, int components) {
        InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get(key);
        helper.assertTrue(recipe != null, key + " recipe missing");
        helper.assertTrue(recipe.research().equals(research), key + " research " + recipe.research());
        helper.assertTrue(recipe.instability() == instability && recipe.components().size() == components, key + " shape");
        return recipe;
    }

    private static void recipesRegistered(GameTestHelper helper) {
        InfusionRecipe shadowbeam = infusion(helper, KamiFociFeature.SHADOWBEAM, "FOCUS_SHADOWBEAM", 12, 6);
        helper.assertTrue(
            shadowbeam.aspects().getAmount(Aspect.DARKNESS) == 65 && shadowbeam.aspects().getAmount(Aspect.ELDRITCH) == 32
                && shadowbeam.aspects().getAmount(Aspect.MAGIC) == 50 && shadowbeam.aspects().getAmount(Aspect.WEAPON) == 32,
            "shadowbeam recipe aspects"
        );
        helper.assertTrue(shadowbeam.result().item().toString().equals("thaumictinkerer:focus_shadowbeam"), "shadowbeam result");
        InfusionRecipe drain = infusion(helper, KamiFociFeature.XP_DRAIN, "FOCUS_XP_DRAIN", 12, 6);
        helper.assertTrue(
            drain.aspects().getAmount(Aspect.MIND) == 65 && drain.aspects().getAmount(Aspect.TAINT) == 16
                && drain.aspects().getAmount(Aspect.MAGIC) == 50 && drain.aspects().getAmount(Aspect.AURA) == 32,
            "xp drain recipe aspects"
        );
        helper.assertTrue(drain.result().item().toString().equals("thaumictinkerer:focus_xp_drain"), "xp drain result");
        InfusionRecipe recall = infusion(helper, KamiFociFeature.RECALL, "FOCUS_RECALL", 10, 6);
        helper.assertTrue(
            recall.aspects().getAmount(Aspect.TRAVEL) == 100 && recall.aspects().getAmount(Aspect.ELDRITCH) == 64 && recall.aspects().getAmount(Aspect.MAGIC) == 50,
            "recall recipe aspects"
        );
        helper.assertTrue(recall.result().item().toString().equals("thaumictinkerer:focus_recall"), "recall result");
        helper.succeed();
    }

    private static void visCosts(GameTestHelper helper) {
        AspectList shadowbeam = KamiFociFeature.FOCUS_SHADOWBEAM.get().getVisCost(ItemStack.EMPTY);
        helper.assertTrue(
            shadowbeam.getAmount(Aspect.ORDER) == 25 && shadowbeam.getAmount(Aspect.ENTROPY) == 25 && shadowbeam.getAmount(Aspect.AIR) == 15 && shadowbeam.size() == 3,
            "shadowbeam cost"
        );
        helper.assertTrue(KamiFociFeature.FOCUS_SHADOWBEAM.get().isVisCostPerTick(ItemStack.EMPTY), "shadowbeam is not per tick");
        AspectList recall = KamiFociFeature.FOCUS_RECALL.get().getVisCost(ItemStack.EMPTY);
        helper.assertTrue(
            recall.getAmount(Aspect.AIR) == 4000 && recall.getAmount(Aspect.EARTH) == 4000 && recall.getAmount(Aspect.ORDER) == 4000 && recall.size() == 3,
            "recall cost"
        );
        helper.assertTrue(!KamiFociFeature.FOCUS_RECALL.get().isVisCostPerTick(ItemStack.EMPTY), "recall is per tick");
        helper.assertTrue(KamiFociFeature.FOCUS_XP_DRAIN.get().getVisCost(ItemStack.EMPTY).size() == 0, "xp drain costs vis");
        helper.assertTrue(KamiFociFeature.FOCUS_XP_DRAIN.get().isVisCostPerTick(ItemStack.EMPTY), "xp drain is not per tick");
        helper.assertTrue(new ItemStack(KamiFociFeature.FOCUS_SHADOWBEAM.get()).getRarity() == TtRarity.kami(), "shadowbeam rarity");
        helper.assertTrue(new ItemStack(KamiFociFeature.FOCUS_XP_DRAIN.get()).getRarity() == TtRarity.kami(), "xp drain rarity");
        helper.assertTrue(new ItemStack(KamiFociFeature.FOCUS_RECALL.get()).getRarity() == net.minecraft.world.item.Rarity.RARE, "recall rarity");
        helper.succeed();
    }

    private static void beamEntity(GameTestHelper helper) {
        helper.assertTrue(
            BuiltInRegistries.ENTITY_TYPE.getKey(KamiFociFeature.BEAM.get()).toString().equals("thaumictinkerer:shadowbeam_staff_beam"),
            "beam entity type is not registered"
        );
        ServerPlayer player = mockPlayer(helper);
        place(player, helper, 3.5, 1.0, 1.5, 0.0F, 0.0F);
        ShadowbeamFocus.Beam beam = ShadowbeamFocus.Beam.create(helper.getLevel(), player, 0);
        helper.assertTrue(beam.getType() == KamiFociFeature.BEAM.get() && beam.getOwner() == player, "beam is not owned by the shooter");
        helper.assertTrue(Math.abs(beam.movement().length() - ShadowbeamFocus.Beam.STEP) < 1.0E-6 && beam.movement().z > 0.0, "beam does not follow the look vector");
        helper.succeed();
    }

    private static void shadowbeamDamages(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        place(player, helper, 3.5, 1.0, 1.5, 0.0F, 0.0F);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(3, 1, 4));
        float before = zombie.getHealth();
        ShadowbeamFocus focus = KamiFociFeature.FOCUS_SHADOWBEAM.get();
        ItemStack wand = wandWith(focus);
        WandItem item = (WandItem) wand.getItem();
        int order = Wands.getVis(wand, Aspect.ORDER);
        int entropy = Wands.getVis(wand, Aspect.ENTROPY);
        int air = Wands.getVis(wand, Aspect.AIR);
        focus.onUsingFocusTick(wand, player, 72000);
        helper.assertTrue(before - zombie.getHealth() == ShadowbeamFocus.Beam.BASE_DAMAGE, "beam dealt " + (before - zombie.getHealth()));
        helper.assertTrue(player.getHealth() == player.getMaxHealth(), "beam hurt its shooter");
        AspectList cost = focus.getVisCost(ItemStack.EMPTY);
        helper.assertTrue(order - Wands.getVis(wand, Aspect.ORDER) == spent(wand, item, player, cost, Aspect.ORDER), "order cost");
        helper.assertTrue(entropy - Wands.getVis(wand, Aspect.ENTROPY) == spent(wand, item, player, cost, Aspect.ENTROPY), "entropy cost");
        helper.assertTrue(air - Wands.getVis(wand, Aspect.AIR) == spent(wand, item, player, cost, Aspect.AIR), "air cost");

        ItemStack empty = Wands.create(WandRegistry.CAP_THAUMIUM, KamiFeature.ichorclothRod, false);
        Wands.setFocus(empty, new ItemStack(focus));
        float health = zombie.getHealth();
        zombie.invulnerableTime = 0;
        focus.onUsingFocusTick(empty, player, 72000);
        helper.assertTrue(zombie.getHealth() == health, "beam fired without vis");
        helper.assertTrue(focus.onFocusRightClick(wand, helper.getLevel(), player, null) == InteractionResult.CONSUME, "focus does not start channeling");
        helper.succeed();
    }

    private static void shadowbeamReflects(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        place(player, helper, 3.5, 1.0, 3.5, 0.0F, 90.0F);
        ShadowbeamFocus.Beam beam = ShadowbeamFocus.Beam.create(helper.getLevel(), player, 0);
        helper.assertTrue(beam.movement().y < 0.0, "beam does not point down");
        List<Vec3> path = beam.travel();
        helper.assertTrue(path.size() >= 3, "beam did not bounce off the floor: " + path.size());
        helper.assertTrue(beam.movement().y > 0.0, "beam was not reflected upwards");
        Vec3 end = path.get(path.size() - 1);
        helper.assertTrue(end.y > path.get(0).y, "reflected beam did not travel back up");
        helper.succeed();
    }

    private static void xpDrain(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        XpDrainFocus focus = KamiFociFeature.FOCUS_XP_DRAIN.get();
        ItemStack wand = Wands.create(WandRegistry.CAP_THAUMIUM, KamiFeature.ichorclothRod, false);
        Wands.setFocus(wand, new ItemStack(focus));
        player.giveExperiencePoints(100);
        int start = player.totalExperience;
        helper.assertTrue(start == 100, "player xp " + start);
        for (int tick = 0; tick < 6; tick++) {
            focus.onUsingFocusTick(wand, player, 72000 - tick);
        }
        helper.assertTrue(start - player.totalExperience == 6 * XpDrainFocus.XP_USE, "xp drained " + (start - player.totalExperience));
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            helper.assertTrue(Wands.getVis(wand, aspect) == XpDrainFocus.VIS_PER_TICK, aspect + " vis " + Wands.getVis(wand, aspect));
        }
        helper.succeed();
    }

    private static void xpDrainLimits(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        XpDrainFocus focus = KamiFociFeature.FOCUS_XP_DRAIN.get();
        ItemStack wand = Wands.create(WandRegistry.CAP_THAUMIUM, KamiFeature.ichorclothRod, false);
        Wands.setFocus(wand, new ItemStack(focus));
        player.giveExperiencePoints(XpDrainFocus.XP_USE - 1);
        for (int tick = 0; tick < 6; tick++) {
            focus.onUsingFocusTick(wand, player, 72000 - tick);
        }
        helper.assertTrue(player.totalExperience == XpDrainFocus.XP_USE - 1, "xp was drained below the price");
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            helper.assertTrue(Wands.getVis(wand, aspect) == 0, "vis was added without xp");
        }
        player.giveExperiencePoints(1000);
        Wands.fill(wand);
        int xp = player.totalExperience;
        for (int tick = 0; tick < 6; tick++) {
            focus.onUsingFocusTick(wand, player, 72000 - tick);
        }
        helper.assertTrue(player.totalExperience == xp, "xp was drained into a full wand");
        int max = Wands.getMaxVis(wand);
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            Wands.storeVis(wand, aspect, max - 100);
        }
        for (int tick = 0; tick < 6; tick++) {
            focus.onUsingFocusTick(wand, player, 72000 - tick);
        }
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            helper.assertTrue(Wands.getVis(wand, aspect) == max, aspect + " exceeded the wand capacity");
        }
        helper.assertTrue(xp - player.totalExperience == 6 * XpDrainFocus.XP_USE, "xp price");
        helper.succeed();
    }

    private static WarpGateBlockEntity gate(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, GatewayFeature.WARP_GATE.get());
        return (WarpGateBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static ItemStack pearl(GameTestHelper helper, BlockPos gate) {
        ItemStack stack = new ItemStack(GatewayFeature.SKY_PEARL.get());
        SkyPearlItem.attune(stack, new PearlLocation(helper.getLevel().dimension(), helper.absolutePos(gate)));
        return stack;
    }

    private static void channel(ItemStack wand, ServerPlayer player, int ticks) {
        RecallFocus focus = KamiFociFeature.FOCUS_RECALL.get();
        focus.onUsingFocusTick(wand, player, 72000 - ticks);
    }

    private static void recallTeleports(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        BlockPos target = new BlockPos(5, 1, 5);
        gate(helper, target);
        player.getInventory().setItem(2, pearl(helper, target));
        place(player, helper, 1.5, 1.0, 1.5, 0.0F, 0.0F);
        RecallFocus focus = KamiFociFeature.FOCUS_RECALL.get();
        ItemStack wand = wandWith(focus);
        WandItem item = (WandItem) wand.getItem();
        int air = Wands.getVis(wand, Aspect.AIR);
        int earth = Wands.getVis(wand, Aspect.EARTH);
        int order = Wands.getVis(wand, Aspect.ORDER);
        int fire = Wands.getVis(wand, Aspect.FIRE);
        helper.assertTrue(focus.onFocusRightClick(wand, helper.getLevel(), player, null) == InteractionResult.CONSUME, "recall does not start channeling");
        channel(wand, player, RecallFocus.CHANNEL_TICKS);
        helper.assertTrue(player.position().distanceToSqr(helper.absoluteVec(new Vec3(1.5, 1.0, 1.5))) < 0.01, "recall teleported before the channel finished");
        helper.assertTrue(Wands.getVis(wand, Aspect.AIR) == air, "recall consumed vis before the channel finished");
        channel(wand, player, RecallFocus.CHANNEL_TICKS + 1);
        helper.assertTrue(player.position().distanceToSqr(helper.absoluteVec(new Vec3(5.5, 2.6, 5.5))) < 0.01, "recall did not teleport to the bound gate: " + player.position());
        AspectList cost = focus.getVisCost(ItemStack.EMPTY);
        helper.assertTrue(air - Wands.getVis(wand, Aspect.AIR) == spent(wand, item, player, cost, Aspect.AIR), "air cost");
        helper.assertTrue(earth - Wands.getVis(wand, Aspect.EARTH) == spent(wand, item, player, cost, Aspect.EARTH), "earth cost");
        helper.assertTrue(order - Wands.getVis(wand, Aspect.ORDER) == spent(wand, item, player, cost, Aspect.ORDER), "order cost");
        helper.assertTrue(Wands.getVis(wand, Aspect.FIRE) == fire, "recall consumed an unrelated aspect");
        helper.succeed();
    }

    private static void recallRefuses(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        BlockPos target = new BlockPos(5, 1, 5);
        WarpGateBlockEntity destination = gate(helper, target);
        place(player, helper, 1.5, 1.0, 1.5, 0.0F, 0.0F);
        Vec3 home = player.position();
        RecallFocus focus = KamiFociFeature.FOCUS_RECALL.get();
        ItemStack wand = wandWith(focus);
        int air = Wands.getVis(wand, Aspect.AIR);

        channel(wand, player, RecallFocus.CHANNEL_TICKS + 1);
        helper.assertTrue(player.position().equals(home) && Wands.getVis(wand, Aspect.AIR) == air, "recall worked without a pearl");

        player.getInventory().setItem(12, pearl(helper, target));
        channel(wand, player, RecallFocus.CHANNEL_TICKS + 1);
        helper.assertTrue(player.position().equals(home) && Wands.getVis(wand, Aspect.AIR) == air, "recall used a pearl outside the hotbar");
        player.getInventory().setItem(12, ItemStack.EMPTY);

        ItemStack foreign = new ItemStack(GatewayFeature.SKY_PEARL.get());
        SkyPearlItem.attune(foreign, new PearlLocation(Level.NETHER, helper.absolutePos(target)));
        player.getInventory().setItem(0, foreign);
        channel(wand, player, RecallFocus.CHANNEL_TICKS + 1);
        helper.assertTrue(player.position().equals(home) && Wands.getVis(wand, Aspect.AIR) == air, "recall crossed dimensions");

        player.getInventory().setItem(0, new ItemStack(GatewayFeature.SKY_PEARL.get()));
        channel(wand, player, RecallFocus.CHANNEL_TICKS + 1);
        helper.assertTrue(player.position().equals(home) && Wands.getVis(wand, Aspect.AIR) == air, "recall used an unbound pearl");

        player.getInventory().setItem(0, pearl(helper, new BlockPos(1, 1, 5)));
        channel(wand, player, RecallFocus.CHANNEL_TICKS + 1);
        helper.assertTrue(player.position().equals(home) && Wands.getVis(wand, Aspect.AIR) == air, "recall teleported to a missing gate");

        player.getInventory().setItem(0, pearl(helper, target));
        destination.setLocked(true);
        channel(wand, player, RecallFocus.CHANNEL_TICKS + 1);
        helper.assertTrue(player.position().equals(home) && Wands.getVis(wand, Aspect.AIR) == air, "recall teleported into a locked gate or consumed vis");
        destination.setLocked(false);

        ItemStack poor = Wands.create(WandRegistry.CAP_THAUMIUM, KamiFeature.ichorclothRod, false);
        Wands.setFocus(poor, new ItemStack(focus));
        channel(poor, player, RecallFocus.CHANNEL_TICKS + 1);
        helper.assertTrue(player.position().equals(home), "recall teleported without vis");
        helper.succeed();
    }
}
