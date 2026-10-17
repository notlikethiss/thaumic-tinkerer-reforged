package thaumictinkerer.test;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.CraftingRecipeRef;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.item.ScribingToolsItem;
import thaumcraft.item.gear.Goggles;
import thaumcraft.item.gear.Repairable;
import thaumcraft.node.Revealer;
import thaumcraft.registry.ModEffects;
import thaumcraft.research.Knowledge;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchManager;
import thaumcraft.wand.VisDiscountGear;
import thaumictinkerer.gear.BloodSwordEvents;
import thaumictinkerer.gear.BloodSwordItem;
import thaumictinkerer.gear.CleansingTalismanItem;
import thaumictinkerer.gear.GearFeature;
import thaumictinkerer.gear.ShareData;
import thaumictinkerer.gear.ShareTomeItem;
import thaumictinkerer.gear.ShareTomeRecipe;
import thaumictinkerer.gear.SoulDrops;
import thaumictinkerer.gear.XpTalismanItem;
import thaumictinkerer.research.TtResearch;

public final class GearGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("gear")
        .add("research_registered", GearGameTests::researchRegistered)
        .add("recipes_registered", GearGameTests::recipesRegistered)
        .add("cleansing_talisman_cleanses", GearGameTests::cleansingTalismanCleanses)
        .add("cleansing_talisman_wears_out", GearGameTests::cleansingTalismanWearsOut)
        .add("xp_talisman_absorbs_and_returns", GearGameTests::xpTalismanAbsorbsAndReturns)
        .add("xp_talisman_caps", GearGameTests::xpTalismanCaps)
        .add("revealing_helm_interfaces", GearGameTests::revealingHelmInterfaces)
        .add("share_tome_transfers_research", GearGameTests::shareTomeTransfersResearch)
        .add("blood_sword_stats", GearGameTests::bloodSwordStats)
        .add("blood_sword_strikes_back", GearGameTests::bloodSwordStrikesBack)
        .add("inkwell_is_scribing_tool", GearGameTests::inkwellIsScribingTool);

    private GearGameTests() {
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

    private static ResearchItem research(GameTestHelper helper, String key, int column, int row) {
        ResearchItem research = ResearchCategories.getResearch(key);
        helper.assertTrue(research != null, key + " is not registered");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), key + " has wrong category " + research.category());
        helper.assertTrue(research.column() == column && research.row() == row, key + " has wrong position");
        return research;
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem cleansing = research(helper, "CLEANSING_TALISMAN", -3, 4);
        helper.assertTrue(cleansing.isSecondary() && !cleansing.isConcealed(), "cleansing flags are wrong");
        helper.assertTrue(cleansing.parents().equals(List.of("DARK_QUARTZ")), "cleansing parents are wrong " + cleansing.parents());
        helper.assertTrue(cleansing.pages().size() == 2, "cleansing pages are wrong");

        ResearchItem xp = research(helper, "XP_TALISMAN", 4, -1);
        helper.assertTrue(xp.isSecondary() && xp.isConcealed(), "xp flags are wrong");
        helper.assertTrue(xp.parents().equals(List.of("JARBRAIN", "SPELL_CLOTH")), "xp parents are wrong " + xp.parents());
        helper.assertTrue(xp.pages().size() == 2, "xp pages are wrong");

        ResearchItem blood = research(helper, "BLOOD_SWORD", -4, 6);
        helper.assertTrue(blood.isSecondary() && blood.warp() == 1, "blood sword flags are wrong");
        helper.assertTrue(blood.parents().equals(List.of("CLEANSING_TALISMAN")), "blood sword parents are wrong");
        helper.assertTrue(blood.pages().size() == 3, "blood sword pages are wrong");

        ResearchItem helm = research(helper, "REVEALING_HELM", 0, 0);
        helper.assertTrue(!helm.isSecondary() && !helm.isConcealed(), "helm flags are wrong");
        helper.assertTrue(helm.parents().equals(List.of("GOGGLES")), "helm parents are wrong");
        helper.assertTrue(helm.parentsHidden().contains("THAUMIUM"), "helm hidden parents are wrong " + helm.parentsHidden());
        helper.assertTrue(helm.pages().size() == 2, "helm pages are wrong");

        ResearchItem tome = research(helper, "SHARE_TOME", 0, -1);
        helper.assertTrue(tome.isStub() && tome.isAutoUnlock() && tome.isRound(), "tome flags are wrong");
        helper.assertTrue(tome.parents().isEmpty(), "tome should have no parents");
        helper.assertTrue(ResearchCategories.getResearch("INFUSED_INKWELL") == null, "inkwell must have no research");
        helper.succeed();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        InfusionRecipe cleansing = (InfusionRecipe) TcRecipes.get("CLEANSING_TALISMAN");
        helper.assertTrue(cleansing != null && cleansing.instability() == 5, "cleansing infusion is wrong");
        helper.assertTrue(cleansing.aspects().getAmount(Aspect.MAN) == 20 && cleansing.components().size() == 6, "cleansing infusion details are wrong");
        InfusionRecipe xp = (InfusionRecipe) TcRecipes.get("XP_TALISMAN");
        helper.assertTrue(xp != null && xp.instability() == 6 && xp.components().size() == 4, "xp infusion is wrong");
        InfusionRecipe blood = (InfusionRecipe) TcRecipes.get("BLOOD_SWORD");
        helper.assertTrue(blood != null && blood.instability() == 6 && blood.components().size() == 6, "blood sword infusion is wrong");
        ShapedArcaneRecipe helm = (ShapedArcaneRecipe) TcRecipes.get("REVEALING_HELM");
        helper.assertTrue(helm != null && helm.aspects().getAmount(Aspect.ORDER) == 5 && helm.aspects().getAmount(Aspect.ENTROPY) == 5, "helm arcane recipe is wrong");
        InfusionRecipe inkwell = (InfusionRecipe) TcRecipes.get("INFUSED_INKWELL");
        helper.assertTrue(inkwell != null && inkwell.instability() == 2 && inkwell.components().size() == 3, "inkwell infusion is wrong");
        for (String key : List.of("SHARE_TOME", "INFUSED_INKWELL0")) {
            helper.assertTrue(TcRecipes.get(key) instanceof CraftingRecipeRef, "recipe ref missing: " + key);
            CraftingRecipeRef reference = (CraftingRecipeRef) TcRecipes.get(key);
            helper.assertTrue(reference.result().exists(), "result item missing for " + key);
            boolean present = helper.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, reference.recipe())).isPresent();
            helper.assertTrue(present, "crafting recipe missing in datapack: " + reference.recipe());
        }
        var tome = helper.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, ((CraftingRecipeRef) TcRecipes.get("SHARE_TOME")).recipe()));
        helper.assertTrue(tome.isPresent() && tome.get().value() instanceof ShareTomeRecipe, "share tome recipe has wrong type");
        helper.succeed();
    }

    private static void cleansingTalismanCleanses(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        ServerLevel level = helper.getLevel();
        ItemStack stack = new ItemStack(GearFeature.CLEANSING_TALISMAN.get());
        helper.assertValueEqual(stack.getMaxDamage(), 100, "talisman uses");
        helper.assertTrue(!CleansingTalismanItem.isEnabled(stack), "talisman should start disabled");

        player.addEffect(new MobEffectInstance(MobEffects.POISON, 400));
        player.tickCount = 20;
        GearFeature.CLEANSING_TALISMAN.get().onWornTick(stack, player);
        helper.assertTrue(player.hasEffect(MobEffects.POISON), "disabled talisman must not cleanse");

        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.getItem().use(level, player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        helper.assertTrue(CleansingTalismanItem.isEnabled(stack), "sneak use should enable the talisman");

        player.tickCount = 19;
        GearFeature.CLEANSING_TALISMAN.get().onWornTick(stack, player);
        helper.assertTrue(player.hasEffect(MobEffects.POISON), "talisman acts only every twenty ticks");

        player.tickCount = 20;
        GearFeature.CLEANSING_TALISMAN.get().onWornTick(stack, player);
        helper.assertTrue(!player.hasEffect(MobEffects.POISON), "poison should be removed");
        helper.assertValueEqual(stack.getDamageValue(), 1, "cleansing poison costs one use");

        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 400));
        player.addEffect(new MobEffectInstance(ModEffects.WARP_WARD, 400));
        GearFeature.CLEANSING_TALISMAN.get().onWornTick(stack, player);
        helper.assertTrue(player.hasEffect(MobEffects.SPEED) && player.hasEffect(ModEffects.WARP_WARD), "good effects and warp ward must stay");
        helper.assertValueEqual(stack.getDamageValue(), 1, "no use is spent without a bad effect");

        player.addEffect(new MobEffectInstance(ModEffects.BLURRED_VISION, 400));
        GearFeature.CLEANSING_TALISMAN.get().onWornTick(stack, player);
        helper.assertTrue(!player.hasEffect(ModEffects.BLURRED_VISION), "blurred vision should be removed");
        helper.assertValueEqual(stack.getDamageValue(), 11, "warp effects cost ten uses");

        player.addEffect(new MobEffectInstance(ModEffects.THAUMARHIA, 400));
        GearFeature.CLEANSING_TALISMAN.get().onWornTick(stack, player);
        helper.assertValueEqual(stack.getDamageValue(), 12, "thaumarhia costs a single use");

        player.setRemainingFireTicks(100);
        GearFeature.CLEANSING_TALISMAN.get().onWornTick(stack, player);
        helper.assertTrue(!player.isOnFire(), "fire should be extinguished");
        helper.assertValueEqual(stack.getDamageValue(), 13, "fire costs a single use");
        helper.succeed();
    }

    private static void cleansingTalismanWearsOut(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        ItemStack stack = new ItemStack(GearFeature.CLEANSING_TALISMAN.get());
        CleansingTalismanItem.flipEnabled(stack);
        stack.setDamageValue(95);
        player.addEffect(new MobEffectInstance(ModEffects.DEATH_GAZE, 400));
        player.tickCount = 40;
        GearFeature.CLEANSING_TALISMAN.get().onWornTick(stack, player);
        helper.assertTrue(stack.isEmpty(), "talisman should break when uses run out");
        helper.succeed();
    }

    private static ExperienceOrb orb(GameTestHelper helper, ServerPlayer player, int value, int count) {
        ExperienceOrb orb = new ExperienceOrb(helper.getLevel(), player.getX() + 1.0, player.getY(), player.getZ(), value);
        orb.count = count;
        helper.getLevel().addFreshEntity(orb);
        return orb;
    }

    private static int bottles(ServerPlayer player) {
        int total = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.EXPERIENCE_BOTTLE)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void xpTalismanAbsorbsAndReturns(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        player.setPos(helper.absoluteVec(new Vec3(2.5, 2.0, 2.5)));
        ServerLevel level = helper.getLevel();
        ItemStack stack = new ItemStack(GearFeature.XP_TALISMAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        ExperienceOrb first = orb(helper, player, 10, 1);
        ExperienceOrb merged = orb(helper, player, 5, 4);
        GearFeature.XP_TALISMAN.get().onWornTick(stack, player);
        helper.assertTrue(!first.isRemoved() && !merged.isRemoved(), "inactive talisman must not absorb");

        player.setShiftKeyDown(true);
        stack.getItem().use(level, player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        helper.assertTrue(XpTalismanItem.isAbsorbing(stack), "sneak use should enable absorbing");

        GearFeature.XP_TALISMAN.get().onWornTick(stack, player);
        helper.assertTrue(first.isRemoved() && merged.isRemoved(), "orbs in range should be absorbed");
        helper.assertValueEqual(XpTalismanItem.getXp(stack), 30, "absorbed xp");

        ExperienceOrb far = new ExperienceOrb(level, player.getX() + 10.0, player.getY(), player.getZ(), 7);
        level.addFreshEntity(far);
        GearFeature.XP_TALISMAN.get().onWornTick(stack, player);
        helper.assertTrue(!far.isRemoved(), "far orbs must stay");
        far.discard();

        helper.assertTrue(stack.getItem().use(level, player, InteractionHand.MAIN_HAND) == net.minecraft.world.InteractionResult.PASS, "withdraw needs a glass bottle");
        helper.assertValueEqual(XpTalismanItem.getXp(stack), 30, "xp unchanged without bottle");

        player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE, 5));
        for (int bottle = 1; bottle <= 3; bottle++) {
            stack.getItem().use(level, player, InteractionHand.MAIN_HAND);
            helper.assertValueEqual(XpTalismanItem.getXp(stack), 30 - bottle * 10, "xp after bottle " + bottle);
            helper.assertValueEqual(bottles(player), bottle, "bottle count after withdrawal " + bottle);
        }
        stack.getItem().use(level, player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(XpTalismanItem.getXp(stack), 0, "no more xp to withdraw");
        helper.assertValueEqual(bottles(player), 3, "no extra bottle without xp");
        helper.succeed();
    }

    private static void xpTalismanCaps(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        player.setPos(helper.absoluteVec(new Vec3(2.5, 2.0, 2.5)));
        ItemStack stack = new ItemStack(GearFeature.XP_TALISMAN.get());
        XpTalismanItem.setXp(stack, 1495);
        XpTalismanItem.setAbsorbing(stack, true);
        ExperienceOrb big = orb(helper, player, 10, 1);
        ExperienceOrb small = orb(helper, player, 5, 1);
        GearFeature.XP_TALISMAN.get().onWornTick(stack, player);
        helper.assertValueEqual(XpTalismanItem.getXp(stack), 1500, "xp is capped");
        helper.assertTrue(!big.isRemoved() && small.isRemoved(), "only the orb that fits is absorbed");
        XpTalismanItem.setAbsorbing(stack, true);
        GearFeature.XP_TALISMAN.get().onWornTick(stack, player);
        helper.assertTrue(!XpTalismanItem.isAbsorbing(stack), "full talisman turns itself off");
        helper.assertValueEqual(XpTalismanItem.getXp(stack), 1500, "xp stays capped");
        big.discard();
        helper.succeed();
    }

    private static void revealingHelmInterfaces(GameTestHelper helper) {
        ItemStack stack = new ItemStack(GearFeature.REVEALING_HELM.get());
        helper.assertTrue(stack.getItem() instanceof Goggles goggles && goggles.showIngamePopups(stack, null), "helm should be goggles");
        helper.assertTrue(stack.getItem() instanceof Revealer revealer && revealer.showNodes(stack, null), "helm should reveal nodes");
        helper.assertTrue(stack.getItem() instanceof VisDiscountGear gear && gear.getVisDiscount(stack, null, Aspect.FIRE) == 5, "helm should give five percent vis discount");
        helper.assertTrue(stack.getItem() instanceof Repairable, "helm should be repairable");
        helper.assertValueEqual(stack.getMaxDamage(), 500, "helm durability");
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        helper.assertTrue(equippable != null && equippable.slot() == EquipmentSlot.HEAD, "helm should go on the head");
        helper.assertTrue(equippable.assetId().isPresent() && equippable.assetId().get().identifier().equals(thaumictinkerer.ThaumicTinkerer.id("revealing_helm")), "helm uses addon equipment asset");
        helper.succeed();
    }

    private static void shareTomeTransfersResearch(GameTestHelper helper) {
        ServerPlayer owner = mockPlayer(helper);
        ServerPlayer student = mockPlayer(helper);
        ServerLevel level = helper.getLevel();
        Knowledge.get(owner);
        Knowledge.get(student);
        helper.assertTrue(ResearchManager.complete(owner, "FUNNEL", false), "owner research");
        helper.assertTrue(ResearchManager.complete(owner, "DARK_QUARTZ", false), "owner research");
        helper.assertTrue(!ResearchManager.isComplete(student, "FUNNEL"), "student starts without research");

        ItemStack tome = new ItemStack(GearFeature.SHARE_TOME.get());
        helper.assertTrue(ShareTomeItem.data(tome) == null, "tome starts unassigned");
        owner.setItemInHand(InteractionHand.MAIN_HAND, tome);
        tome.getItem().use(level, owner, InteractionHand.MAIN_HAND);
        ShareData data = ShareTomeItem.data(tome);
        helper.assertTrue(data != null && data.research().contains("FUNNEL") && data.research().contains("DARK_QUARTZ"), "tome should record owner research");
        helper.assertTrue(data.player().equals(owner.getGameProfile().name()), "tome should record owner name");

        ItemStack copy = tome.copy();
        student.setItemInHand(InteractionHand.MAIN_HAND, copy);
        copy.getItem().use(level, student, InteractionHand.MAIN_HAND);
        helper.assertTrue(ResearchManager.isComplete(student, "FUNNEL") && ResearchManager.isComplete(student, "DARK_QUARTZ"), "student should receive research");
        helper.assertTrue(ShareTomeItem.data(copy).equals(data), "reading must not change the tome");

        JsonElement json = ShareData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        helper.assertTrue(ShareData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().equals(data), "share data survives serialization");
        helper.succeed();
    }

    private static void bloodSwordStats(GameTestHelper helper) {
        ItemStack stack = new ItemStack(GearFeature.BLOOD_SWORD.get());
        helper.assertValueEqual(stack.getMaxDamage(), 950, "blood sword durability");
        helper.assertTrue(stack.getItem() instanceof Repairable, "blood sword should be repairable");
        helper.assertTrue(stack.getItem().getUseAnimation(stack) == ItemUseAnimation.BLOCK, "blood sword blocks");
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        double damage = 0;
        double speed = 0;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE.unwrapKey().get())) {
                damage += entry.modifier().amount();
            }
            if (entry.attribute().is(Attributes.MOVEMENT_SPEED.unwrapKey().get())) {
                speed += entry.modifier().amount();
                helper.assertTrue(entry.modifier().operation() == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE, "speed bonus is a percentage");
            }
        }
        helper.assertTrue(damage == 10.0, "blood sword attack damage is " + damage);
        helper.assertTrue(speed == 0.25, "blood sword speed bonus is " + speed);

        ServerPlayer player = mockPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.assertTrue(!BloodSwordItem.isActivated(stack), "blood sword starts deactivated");
        player.setShiftKeyDown(true);
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(BloodSwordItem.isActivated(stack), "sneak use activates essentia harvest");
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(!BloodSwordItem.isActivated(stack), "second sneak use deactivates it");

        Zombie zombie = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        List<Aspect> aspects = List.of(SoulDrops.aspectsFor(zombie));
        helper.assertTrue(aspects.equals(List.of(Aspect.FLESH, Aspect.FLESH, Aspect.UNDEAD)), "zombie soul aspects are wrong " + aspects);
        helper.succeed();
    }

    private static void bloodSwordStrikesBack(GameTestHelper helper) {
        BloodSwordEvents.reset();
        ServerPlayer player = mockPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(GearFeature.BLOOD_SWORD.get()));
        Zombie zombie = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        zombie.setPos(helper.absoluteVec(new Vec3(2.5, 2.0, 2.5)));
        helper.getLevel().addFreshEntity(zombie);
        float before = player.getHealth();
        zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0F);
        helper.assertTrue(before - player.getHealth() == 2.0F, "wielder should take one heart, lost " + (before - player.getHealth()));
        zombie.discard();
        BloodSwordEvents.reset();
        helper.succeed();
    }

    private static void inkwellIsScribingTool(GameTestHelper helper) {
        ItemStack stack = new ItemStack(GearFeature.INFUSED_INKWELL.get());
        helper.assertTrue(stack.getItem() instanceof ScribingToolsItem, "infused inkwell should be a scribing tool");
        helper.assertValueEqual(stack.getMaxDamage(), 800, "inkwell durability");
        helper.assertValueEqual(stack.getMaxStackSize(), 1, "inkwell stack size");
        helper.assertTrue(stack.isEnchanted() || stack.hasFoil(), "inkwell glints");
        helper.succeed();
    }
}
