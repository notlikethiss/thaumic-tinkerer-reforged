package thaumictinkerer.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.item.gear.Goggles;
import thaumcraft.node.Revealer;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumcraft.wand.WandManager;
import thaumictinkerer.TtRarity;
import thaumictinkerer.awakenedarmor.AwakenedArmor;
import thaumictinkerer.awakenedarmor.AwakenedArmorEvents;
import thaumictinkerer.awakenedarmor.AwakenedArmorFeature;
import thaumictinkerer.gas.GasFeature;
import thaumictinkerer.network.ToggleArmorPayload;
import thaumictinkerer.research.KamiResearchItem;
import thaumictinkerer.research.TtResearch;

public final class AwakenedArmorGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("awakened_armor")
        .add("research_registered", AwakenedArmorGameTests::researchRegistered)
        .add("recipes_registered", AwakenedArmorGameTests::recipesRegistered)
        .add("items", AwakenedArmorGameTests::items)
        .add("cowl_night_vision", AwakenedArmorGameTests::cowlNightVision)
        .add("robes_flight", AwakenedArmorGameTests::robesFlight)
        .add("robes_flight_creative", AwakenedArmorGameTests::robesFlightCreative)
        .add("leggings_fire_immunity", AwakenedArmorGameTests::leggingsFireImmunity)
        .add("boots_step_height", AwakenedArmorGameTests::bootsStepHeight)
        .add("boots_effects", AwakenedArmorGameTests::bootsEffects)
        .add("toggle_payload", AwakenedArmorGameTests::togglePayload)
        .add("piece_toggle", AwakenedArmorGameTests::pieceToggle)
        .add("global_toggle_disables", AwakenedArmorGameTests::globalToggleDisables);

    private AwakenedArmorGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static ServerPlayer mockPlayer(GameTestHelper helper, GameType gameType) {
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(gameType);
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
        player.setPos(helper.absoluteVec(new Vec3(2.5, 2.0, 2.5)));
        return player;
    }

    private static void tick(ServerPlayer player) {
        player.baseTick();
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(player));
    }

    private static void research(GameTestHelper helper, String key, int column, int row, String... pageTypes) {
        ResearchItem research = ResearchCategories.getResearch(key);
        helper.assertTrue(research instanceof KamiResearchItem, key + " is not a kami research");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), key + " has wrong category");
        helper.assertTrue(research.column() == column && research.row() == row, key + " has wrong position");
        helper.assertTrue(research.complexity() == 3, key + " has wrong complexity " + research.complexity());
        helper.assertTrue(research.isConcealed(), key + " must be concealed");
        helper.assertTrue(research.parents().equals(List.of("ICHORCLOTH_ARMOR")), key + " has wrong parents " + research.parents());
        helper.assertTrue(research.pages().size() == pageTypes.length, key + " has wrong page count " + research.pages().size());
        for (int index = 0; index < pageTypes.length; index++) {
            ResearchPage page = research.pages().get(index);
            boolean matches = switch (pageTypes[index]) {
                case "text" -> page instanceof ResearchPage.Text;
                default -> page instanceof ResearchPage.Infusion;
            };
            helper.assertTrue(matches, key + " page " + index + " has wrong type");
        }
    }

    private static void researchRegistered(GameTestHelper helper) {
        research(helper, "ICHORCLOTH_HELM_GEM", 18, 3, "text", "infusion");
        research(helper, "ICHORCLOTH_CHEST_GEM", 17, 7, "text", "infusion");
        research(helper, "ICHORCLOTH_LEGS_GEM", 17, 9, "text", "infusion", "text");
        research(helper, "ICHORCLOTH_BOOTS_GEM", 15, 10, "text", "infusion");
        var tags = ResearchCategories.getResearch("ICHORCLOTH_CHEST_GEM").tags();
        helper.assertTrue(tags.getAmount(Aspect.AIR) == 2 && tags.getAmount(Aspect.FLIGHT) == 1 && tags.getAmount(Aspect.ELDRITCH) == 1, "chest aspects");
        helper.succeed();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        Object[][] expected = {
            {"ICHORCLOTH_HELM_GEM", Aspect.WATER, "thaumictinkerer:cowl_of_the_abyssal_depths", "thaumictinkerer:ichorcloth_cowl"},
            {"ICHORCLOTH_CHEST_GEM", Aspect.AIR, "thaumictinkerer:robes_of_the_stratosphere", "thaumictinkerer:ichorcloth_robe"},
            {"ICHORCLOTH_LEGS_GEM", Aspect.FIRE, "thaumictinkerer:leggings_of_the_burning_mantle", "thaumictinkerer:ichorcloth_leggings"},
            {"ICHORCLOTH_BOOTS_GEM", Aspect.EARTH, "thaumictinkerer:boots_of_the_horizontal_shield", "thaumictinkerer:ichorcloth_boots"},
        };
        for (Object[] row : expected) {
            String key = (String) row[0];
            InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get(key);
            helper.assertTrue(recipe != null, key + " is not registered");
            helper.assertTrue(recipe.instability() == 13, key + " has wrong instability");
            helper.assertTrue(recipe.components().size() == 12, key + " has wrong component count " + recipe.components().size());
            helper.assertTrue(recipe.aspects().getAmount((Aspect) row[1]) == 50, key + " has wrong primary aspect");
            helper.assertTrue(recipe.aspects().getAmount(Aspect.ARMOR) == 32 && recipe.aspects().getAmount(Aspect.LIGHT) == 64, key + " has wrong aspects");
            helper.assertTrue(recipe.result().item().toString().equals(row[2]), key + " has wrong result " + recipe.result().item());
            helper.assertTrue(recipe.central().displayStacks().get(0).getItem().builtInRegistryHolder().key().identifier().toString().equals(row[3]), key + " has wrong central item");
        }
        helper.succeed();
    }

    private static void items(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        float before = WandManager.getTotalVisDiscount(player, Aspect.FIRE);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(AwakenedArmorFeature.COWL.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(AwakenedArmorFeature.ROBES.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(AwakenedArmorFeature.LEGGINGS.get()));
        helper.assertTrue(Math.abs(WandManager.getTotalVisDiscount(player, Aspect.FIRE) - before - 0.12F) < 0.0001F, "pieces discount");
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(AwakenedArmorFeature.BOOTS.get()));
        helper.assertTrue(Math.abs(WandManager.getTotalVisDiscount(player, Aspect.FIRE) - before - 0.15F) < 0.0001F, "full set discount");
        ItemStack cowl = new ItemStack(AwakenedArmorFeature.COWL.get());
        helper.assertTrue(cowl.getItem() instanceof Goggles goggles && goggles.showIngamePopups(cowl, player), "cowl goggles");
        helper.assertTrue(cowl.getItem() instanceof Revealer revealer && revealer.showNodes(cowl, player), "cowl revealer");
        helper.assertTrue(cowl.has(DataComponents.UNBREAKABLE) && cowl.has(DataComponents.EQUIPPABLE), "cowl components");
        helper.assertTrue(cowl.getRarity() == TtRarity.kami(), "cowl rarity");
        helper.succeed();
    }

    private static void cowlNightVision(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper, GameType.SURVIVAL);
        BlockPos eyes = BlockPos.containing(player.getEyePosition());
        helper.getLevel().setBlock(eyes, Blocks.WATER.defaultBlockState(), 2);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(AwakenedArmorFeature.COWL.get()));
        tick(player);
        helper.assertTrue(player.isEyeInFluid(FluidTags.WATER), "eyes are not in water");
        MobEffectInstance effect = player.getEffect(MobEffects.NIGHT_VISION);
        helper.assertTrue(effect != null && effect.isAmbient(), "night vision missing while worn underwater");
        helper.assertTrue(player.getAirSupply() == 300, "air supply not restored");
        player.removeEffect(MobEffects.NIGHT_VISION);
        AwakenedArmorFeature.ARMOR_ENABLED.set(player, false);
        tick(player);
        helper.assertTrue(player.getEffect(MobEffects.NIGHT_VISION) == null, "night vision granted while armor disabled");
        AwakenedArmorFeature.ARMOR_ENABLED.set(player, true);
        tick(player);
        helper.assertTrue(player.getEffect(MobEffects.NIGHT_VISION) != null, "night vision not restored after enabling");
        ItemStack removed = player.getItemBySlot(EquipmentSlot.HEAD);
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        AwakenedArmorEvents.release(player, EquipmentSlot.HEAD);
        helper.assertTrue(removed.is(AwakenedArmorFeature.COWL.get()), "cowl was not worn");
        helper.assertTrue(player.getEffect(MobEffects.NIGHT_VISION) == null, "night vision remained after unequip");
        tick(player);
        helper.assertTrue(player.getEffect(MobEffects.NIGHT_VISION) == null, "night vision reapplied after unequip");
        helper.succeed();
    }

    private static void robesFlight(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper, GameType.SURVIVAL);
        helper.assertTrue(!player.getAbilities().mayfly, "survival player can already fly");
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(AwakenedArmorFeature.ROBES.get()));
        tick(player);
        helper.assertTrue(player.getAbilities().mayfly, "flight not granted");
        helper.assertTrue(AwakenedArmorFeature.FLIGHT_GRANTED.has(player) && AwakenedArmorFeature.FLIGHT_GRANTED.get(player), "grant not recorded");
        player.getAbilities().flying = true;
        AwakenedArmorFeature.ARMOR_ENABLED.set(player, false);
        tick(player);
        helper.assertTrue(!player.getAbilities().mayfly && !player.getAbilities().flying, "flight not revoked when disabled");
        AwakenedArmorFeature.ARMOR_ENABLED.set(player, true);
        tick(player);
        helper.assertTrue(player.getAbilities().mayfly, "flight not granted again");
        player.getAbilities().flying = true;
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        tick(player);
        helper.assertTrue(!player.getAbilities().mayfly && !player.getAbilities().flying, "flight not revoked after unequip");
        helper.assertTrue(!AwakenedArmorFeature.FLIGHT_GRANTED.has(player), "grant marker remained");
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(AwakenedArmorFeature.ROBES.get()));
        player.getAbilities().mayfly = false;
        tick(player);
        helper.assertTrue(player.getAbilities().mayfly, "flight not regranted after external reset");
        helper.succeed();
    }

    private static void robesFlightCreative(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper, GameType.CREATIVE);
        helper.assertTrue(player.getAbilities().mayfly, "creative player cannot fly");
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(AwakenedArmorFeature.ROBES.get()));
        tick(player);
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        tick(player);
        helper.assertTrue(player.getAbilities().mayfly, "creative flight was revoked");
        helper.succeed();
    }

    private static void leggingsFireImmunity(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper, GameType.SURVIVAL);
        helper.assertTrue(!AwakenedArmor.carriesLight(player), "light carried without leggings");
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(AwakenedArmorFeature.LEGGINGS.get()));
        tick(player);
        MobEffectInstance effect = player.getEffect(MobEffects.FIRE_RESISTANCE);
        helper.assertTrue(effect != null && effect.getAmplifier() == 10, "fire resistance missing while worn");
        helper.assertTrue(GasFeature.carriesLight(player), "leggings are not a light carrier");
        BlockPos at = BlockPos.containing(player.getX(), (int) player.getY() + 1, player.getZ());
        helper.assertTrue(helper.getLevel().getBlockState(at).is(GasFeature.NITOR_GAS.get()), "nitor gas not placed at the wearer");
        ItemStack stack = player.getItemBySlot(EquipmentSlot.LEGS);
        AwakenedArmorFeature.PIECE_DISABLED.set(stack, true);
        helper.assertTrue(!GasFeature.carriesLight(player), "disabled leggings still carry light");
        AwakenedArmorFeature.PIECE_DISABLED.set(stack, false);
        player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        AwakenedArmorEvents.release(player, EquipmentSlot.LEGS);
        helper.assertTrue(player.getEffect(MobEffects.FIRE_RESISTANCE) == null, "fire resistance remained after unequip");
        helper.assertTrue(!GasFeature.carriesLight(player), "light carried after unequip");
        helper.succeed();
    }

    private static void bootsStepHeight(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper, GameType.SURVIVAL);
        double base = player.getAttributeValue(Attributes.STEP_HEIGHT);
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(AwakenedArmorFeature.BOOTS.get()));
        tick(player);
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.STEP_HEIGHT) - (base + AwakenedArmorEvents.STEP_BONUS)) < 1.0E-6, "step height not raised");
        player.setShiftKeyDown(true);
        tick(player);
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.STEP_HEIGHT) - base) < 1.0E-6, "step height not lowered while sneaking");
        player.setShiftKeyDown(false);
        tick(player);
        helper.assertTrue(player.getAttributeValue(Attributes.STEP_HEIGHT) > base, "step height not raised after sneaking");
        player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        AwakenedArmorEvents.release(player, EquipmentSlot.FEET);
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.STEP_HEIGHT) - base) < 1.0E-6, "step height remained after unequip");
        helper.assertTrue(player.getAttribute(Attributes.STEP_HEIGHT).getModifier(AwakenedArmorEvents.STEP_MODIFIER) == null, "step modifier remained");
        helper.succeed();
    }

    private static void bootsEffects(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper, GameType.SURVIVAL);
        BlockPos dirt = BlockPos.containing(player.getX(), Math.floor(player.getY()) - 1, player.getZ());
        helper.getLevel().setBlock(dirt, Blocks.DIRT.defaultBlockState(), 2);
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(AwakenedArmorFeature.BOOTS.get()));
        tick(player);
        MobEffectInstance haste = player.getEffect(MobEffects.HASTE);
        helper.assertTrue(haste != null && haste.getAmplifier() == 1, "haste missing while worn");
        helper.assertTrue(helper.getLevel().getBlockState(dirt).is(Blocks.GRASS_BLOCK), "dirt was not turned into grass");
        float health = player.getHealth();
        player.causeFallDamage(20.0, 1.0F, player.damageSources().fall());
        helper.assertTrue(player.getHealth() == health, "fall damage taken with boots");
        double before = player.getDeltaMovement().y;
        player.jumpFromGround();
        helper.assertTrue(player.getDeltaMovement().y - before > 0.5F + AwakenedArmorEvents.JUMP_BOOST - 0.2F, "jump not boosted " + (player.getDeltaMovement().y - before));
        player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        AwakenedArmorEvents.release(player, EquipmentSlot.FEET);
        helper.assertTrue(player.getEffect(MobEffects.HASTE) == null, "haste remained after unequip");
        helper.succeed();
    }

    private static void togglePayload(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper, GameType.SURVIVAL);
        ToggleArmorPayload.handle(new ToggleArmorPayload(false), player);
        helper.assertTrue(AwakenedArmor.isEnabled(player), "state changed without wearing armor");
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(AwakenedArmorFeature.BOOTS.get()));
        ToggleArmorPayload.handle(new ToggleArmorPayload(false), player);
        helper.assertTrue(!AwakenedArmor.isEnabled(player), "toggle did not disable");
        ToggleArmorPayload.handle(new ToggleArmorPayload(false), player);
        helper.assertTrue(!AwakenedArmor.isEnabled(player), "repeated toggle changed state");
        ToggleArmorPayload.handle(new ToggleArmorPayload(true), player);
        helper.assertTrue(AwakenedArmor.isEnabled(player), "toggle did not enable");
        Player other = helper.makeMockPlayer(GameType.SURVIVAL);
        other.setItemSlot(EquipmentSlot.FEET, new ItemStack(AwakenedArmorFeature.BOOTS.get()));
        ToggleArmorPayload.handle(new ToggleArmorPayload(false), other);
        helper.assertTrue(AwakenedArmor.isEnabled(other), "non server player was toggled");
        helper.succeed();
    }

    private static void pieceToggle(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper, GameType.SURVIVAL);
        ItemStack stack = new ItemStack(AwakenedArmorFeature.LEGGINGS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setShiftKeyDown(true);
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(AwakenedArmor.isPieceDisabled(stack), "piece not disabled");
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(!AwakenedArmor.isPieceDisabled(stack), "piece not enabled");
        helper.succeed();
    }

    private static void globalToggleDisables(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper, GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(AwakenedArmorFeature.LEGGINGS.get()));
        helper.assertTrue(AwakenedArmor.carriesLight(player), "leggings inactive by default");
        AwakenedArmorFeature.ARMOR_ENABLED.set(player, false);
        helper.assertTrue(!AwakenedArmor.carriesLight(player), "leggings active while armor disabled");
        helper.succeed();
    }
}
