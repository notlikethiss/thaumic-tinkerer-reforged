package thaumictinkerer.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModBlocks;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchManager;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;
import thaumictinkerer.enchanting.EnchanterBlockEntity;
import thaumictinkerer.enchanting.EnchantingFeature;
import thaumictinkerer.enchanting.EnchantmentManager;
import thaumictinkerer.enchanting.SpellClothRecipe;
import thaumictinkerer.enchanting.TtEnchantmentEvents;
import thaumictinkerer.enchanting.TtEnchantments;
import thaumictinkerer.research.TtResearch;

public final class EnchantingGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("enchanting")
        .add("research_registered", EnchantingGameTests::researchRegistered)
        .add("recipes_registered", EnchantingGameTests::recipesRegistered)
        .add("enchantments_registered", EnchantingGameTests::enchantmentsRegistered)
        .add("enchantment_compatibility", EnchantingGameTests::compatibility)
        .add("vampirism_heals", EnchantingGameTests::vampirismHeals)
        .add("slow_fall", EnchantingGameTests::slowFall)
        .add("quick_draw", EnchantingGameTests::quickDraw)
        .add("flaming_touch_and_desintegrate", EnchantingGameTests::flamingTouch)
        .add("enchanter_applies_enchant", EnchantingGameTests::enchanterApplies)
        .add("enchanter_rejects_unresearched", EnchantingGameTests::enchanterRejects)
        .add("spell_cloth_recipe", EnchantingGameTests::spellClothRecipe)
        .add("original_enchantment_research", EnchantingGameTests::originalEnchantmentResearch);

    private EnchantingGameTests() {
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

    private static Holder<Enchantment> holder(GameTestHelper helper, ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    private static ItemStack enchanted(GameTestHelper helper, ItemStack stack, ResourceKey<Enchantment> key, int level) {
        stack.enchant(holder(helper, key), level);
        return stack;
    }

    private static void researchRegistered(GameTestHelper helper) {
        Object[][] expected = {
            {EnchantingFeature.SPELL_CLOTH_RESEARCH, 3, 2, 2, false, "ENCHFABRIC"},
            {EnchantingFeature.ENCHANTER_RESEARCH, 5, 4, 3, false, EnchantingFeature.SPELL_CLOTH_RESEARCH},
            {EnchantingFeature.ENCHANT_ASCENT_BOOST, 6, 2, 2, true, EnchantingFeature.ENCHANTER_RESEARCH},
            {EnchantingFeature.ENCHANT_SLOW_FALL, 7, 3, 2, true, EnchantingFeature.ENCHANTER_RESEARCH},
            {EnchantingFeature.ENCHANT_AUTO_SMELT, 7, 5, 2, true, EnchantingFeature.ENCHANTER_RESEARCH},
            {EnchantingFeature.ENCHANT_DESINTEGRATE, 6, 6, 2, true, EnchantingFeature.ENCHANTER_RESEARCH},
            {EnchantingFeature.ENCHANT_QUICK_DRAW, 4, 6, 2, true, EnchantingFeature.ENCHANTER_RESEARCH},
            {EnchantingFeature.ENCHANT_VAMPIRISM, 3, 5, 2, true, EnchantingFeature.ENCHANTER_RESEARCH},
            {EnchantingFeature.ENCHANT_FOCUSED_STRIKE, 2, 7, 2, true, EnchantingFeature.ENCHANT_VAMPIRISM},
            {EnchantingFeature.ENCHANT_DISPERSED_STRIKE, 1, 6, 2, true, EnchantingFeature.ENCHANT_VAMPIRISM},
            {EnchantingFeature.ENCHANT_FINAL_STRIKE, 0, 8, 2, true, EnchantingFeature.ENCHANT_FOCUSED_STRIKE},
            {EnchantingFeature.ENCHANT_POUNCE, 7, 0, 2, true, EnchantingFeature.ENCHANT_ASCENT_BOOST},
            {EnchantingFeature.ENCHANT_SHATTER, 5, 8, 2, true, EnchantingFeature.ENCHANT_DESINTEGRATE},
            {EnchantingFeature.ENCHANT_SHOCKWAVE, 9, 2, 2, true, EnchantingFeature.ENCHANT_SLOW_FALL},
            {EnchantingFeature.ENCHANT_TUNNEL, 9, 6, 2, true, EnchantingFeature.ENCHANT_AUTO_SMELT},
            {EnchantingFeature.ENCHANT_VALIANCE, 1, 4, 2, true, EnchantingFeature.ENCHANT_VAMPIRISM},
        };
        for (Object[] row : expected) {
            String key = (String) row[0];
            ResearchItem research = ResearchCategories.getResearch(key);
            helper.assertTrue(research != null, key + " is not registered");
            helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), key + " wrong category");
            helper.assertTrue(research.column() == (int) row[1] && research.row() == (int) row[2], key + " wrong position");
            helper.assertTrue(research.complexity() == (int) row[3], key + " wrong complexity " + research.complexity());
            helper.assertTrue(research.isSecondary() == (boolean) row[4], key + " wrong secondary flag");
            String parent = (String) row[5];
            helper.assertTrue(research.parents().contains(parent) || research.parentsHidden().contains(parent), key + " missing parent " + parent);
        }
        ResearchItem finalStrike = ResearchCategories.getResearch(EnchantingFeature.ENCHANT_FINAL_STRIKE);
        helper.assertTrue(finalStrike.parents().contains(EnchantingFeature.ENCHANT_DISPERSED_STRIKE), "final strike needs both parents");
        ResearchItem enchanter = ResearchCategories.getResearch(EnchantingFeature.ENCHANTER_RESEARCH);
        helper.assertTrue(enchanter.pages().size() == 4, "enchanter pages");
        helper.succeed();
    }

    private static void originalEnchantmentResearch(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        List<ResourceKey<Enchantment>> higher = List.of(TtEnchantments.FOCUSED_STRIKE, TtEnchantments.DISPERSED_STRIKE, TtEnchantments.VALIANCE, TtEnchantments.FINAL_STRIKE, TtEnchantments.TUNNEL, TtEnchantments.SHATTER, TtEnchantments.SHOCKWAVE, TtEnchantments.POUNCE);
        for (ResourceKey<Enchantment> key : higher) {
            helper.assertTrue(!EnchantmentManager.canEnchantmentBeUsed(player, key), "unresearched higher enchantment accepted");
        }
        ResearchManager.complete(player, EnchantingFeature.ENCHANT_VAMPIRISM);
        for (ResourceKey<Enchantment> key : higher) {
            helper.assertTrue(EnchantmentManager.canEnchantmentBeUsed(player, key), "TT gates higher enchantments on Vampirism: " + key);
        }
        helper.assertTrue(!EnchantmentManager.canEnchantmentBeUsed(player, TtEnchantments.AUTO_SMELT), "lower enchantment must retain its own research gate");
        helper.succeed();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        helper.assertTrue(TcRecipes.get(EnchantingFeature.ENCHANTER_RESEARCH) instanceof InfusionRecipe, "enchanter infusion missing");
        helper.assertTrue(TcRecipes.get(EnchantingFeature.SPELL_CLOTH_RESEARCH) instanceof CrucibleRecipe, "spell cloth crucible missing");
        helper.assertTrue(
            helper.getLevel().getServer().getRecipeManager().byKey(thaumictinkerer.ThaumicTinkerer.id("spell_cloth")).isPresent(),
            "spell cloth crafting recipe missing"
        );
        helper.succeed();
    }

    private static void enchantmentsRegistered(GameTestHelper helper) {
        int[] maxLevels = {4, 3, 1, 1, 2, 2, 5, 5, 5, 5, 5, 5, 5, 5};
        for (int index = 0; index < TtEnchantments.ALL.size(); index++) {
            ResourceKey<Enchantment> key = TtEnchantments.ALL.get(index);
            Holder<Enchantment> holder = holder(helper, key);
            helper.assertTrue(holder.value().getMaxLevel() == maxLevels[index], key + " max level " + holder.value().getMaxLevel());
            helper.assertTrue(!holder.is(EnchantmentTags.IN_ENCHANTING_TABLE), key + " is in the enchanting table");
            helper.assertTrue(!holder.is(EnchantmentTags.TRADEABLE), key + " is tradeable");
            helper.assertTrue(!holder.is(EnchantmentTags.ON_RANDOM_LOOT), key + " is in random loot");
            helper.assertTrue(EnchantmentManager.data(key) != null, key + " has no enchanter data");
        }
        helper.assertTrue(EnchantmentManager.data().size() == 40, "expected 40 enchanter entries, got " + EnchantmentManager.data().size());
        helper.succeed();
    }

    private static void compatibility(GameTestHelper helper) {
        helper.assertTrue(!Enchantment.areCompatible(holder(helper, TtEnchantments.SHATTER), holder(helper, Enchantments.EFFICIENCY)), "shatter and efficiency");
        helper.assertTrue(!Enchantment.areCompatible(holder(helper, TtEnchantments.SLOW_FALL), holder(helper, Enchantments.FEATHER_FALLING)), "slow fall and feather falling");
        helper.assertTrue(!Enchantment.areCompatible(holder(helper, TtEnchantments.VAMPIRISM), holder(helper, Enchantments.FIRE_ASPECT)), "vampirism and fire aspect");
        helper.assertTrue(!Enchantment.areCompatible(holder(helper, TtEnchantments.AUTO_SMELT), holder(helper, Enchantments.SHARPNESS)), "auto smelt and sharpness");
        helper.assertTrue(Enchantment.areCompatible(holder(helper, TtEnchantments.AUTO_SMELT), holder(helper, Enchantments.UNBREAKING)), "auto smelt and unbreaking");
        helper.assertTrue(Enchantment.areCompatible(holder(helper, TtEnchantments.FINAL_STRIKE), holder(helper, Enchantments.SHARPNESS)), "final strike and sharpness");
        helper.assertTrue(!Enchantment.areCompatible(holder(helper, TtEnchantments.FINAL_STRIKE), holder(helper, TtEnchantments.VALIANCE)), "final strike and valiance");
        helper.assertTrue(Enchantment.areCompatible(holder(helper, TtEnchantments.POUNCE), holder(helper, Enchantments.PROTECTION)), "pounce and protection");
        helper.succeed();
    }

    private static void vampirismHeals(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, enchanted(helper, new ItemStack(Items.DIAMOND_SWORD), TtEnchantments.VAMPIRISM, 2));
        player.setHealth(10.0F);
        float damage = TtEnchantmentEvents.modifyDamage(player, TtTestSupport.mockServerPlayer(helper, GameType.SURVIVAL), 5.0F);
        helper.assertTrue(player.getHealth() == 12.0F, "attacker health " + player.getHealth());
        helper.assertTrue(damage == 5.0F, "damage changed " + damage);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, enchanted(helper, new ItemStack(Items.DIAMOND_SWORD), TtEnchantments.VALIANCE, 5));
        player.setHealth(4.0F);
        float boosted = TtEnchantmentEvents.modifyDamage(player, TtTestSupport.mockServerPlayer(helper, GameType.SURVIVAL), 10.0F);
        helper.assertTrue(Math.abs(boosted - 15.0F) < 0.001F, "valiance damage " + boosted);
        helper.succeed();
    }

    private static void slowFall(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        player.setItemSlot(EquipmentSlot.FEET, enchanted(helper, new ItemStack(Items.DIAMOND_BOOTS), TtEnchantments.SLOW_FALL, 3));
        player.setDeltaMovement(0.0, -1.0, 0.0);
        player.setKnownMovement(new Vec3(0.0, -1.0, 0.0));
        player.fallDistance = 10.0F;
        TtEnchantmentEvents.applySlowFall(player);
        helper.assertTrue(Math.abs(player.getDeltaMovement().y - (-1.0 / 1.99)) < 0.001, "motion " + player.getDeltaMovement().y);
        helper.assertTrue(Math.abs(player.fallDistance - 9.0) < 0.001, "fall distance " + player.fallDistance);
        player.setShiftKeyDown(true);
        player.setDeltaMovement(0.0, -1.0, 0.0);
        TtEnchantmentEvents.applySlowFall(player);
        helper.assertTrue(player.getDeltaMovement().y == -1.0, "sneaking must disable slow fall");
        helper.succeed();
    }

    private static void quickDraw(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        ItemStack bow = enchanted(helper, new ItemStack(Items.BOW), TtEnchantments.QUICK_DRAW, 2);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, bow);
        int max = bow.getUseDuration(player);
        helper.assertTrue(TtEnchantmentEvents.quickDrawDuration(player, bow, max - 4) == max - 5, "extra tick on multiples of 4");
        helper.assertTrue(TtEnchantmentEvents.quickDrawDuration(player, bow, max - 3) == max - 3, "no extra tick off the cycle");
        helper.succeed();
    }

    private static void flamingTouch(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockPos absolute = helper.absolutePos(pos);
        ItemStack axe = enchanted(helper, new ItemStack(Items.DIAMOND_PICKAXE), TtEnchantments.AUTO_SMELT, 1);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, axe);
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        Float speed = TtEnchantmentEvents.breakSpeed(player, axe, log, absolute, 1.0F);
        helper.assertTrue(speed != null && speed == Float.MAX_VALUE, "wood must break instantly");
        helper.assertTrue(axe.getDamageValue() == 1, "tool must take durability damage, got " + axe.getDamageValue());
        helper.assertTrue(TtEnchantmentEvents.breakSpeed(player, axe, Blocks.STONE.defaultBlockState(), absolute, 1.0F) == null, "stone must be blocked");
        ItemStack shovel = enchanted(helper, new ItemStack(Items.DIAMOND_SHOVEL), TtEnchantments.DESINTEGRATE, 1);
        Float dirt = TtEnchantmentEvents.breakSpeed(player, shovel, Blocks.DIRT.defaultBlockState(), absolute, 1.0F);
        helper.assertTrue(dirt != null && dirt == Float.MAX_VALUE, "dirt must break instantly");
        helper.assertTrue(TtEnchantmentEvents.breakSpeed(player, shovel, Blocks.OBSIDIAN.defaultBlockState(), absolute, 1.0F) == null, "hard block must be blocked");
        helper.succeed();
    }

    private static void buildPillars(GameTestHelper helper) {
        int[][] bases = {{1, 1, 1}, {1, 1, 3}, {1, 1, 5}, {5, 1, 1}, {5, 1, 3}, {5, 1, 5}};
        for (int[] base : bases) {
            helper.setBlock(new BlockPos(base[0], base[1], base[2]), ModBlocks.OBSIDIAN_TOTEM.get().defaultBlockState());
            helper.setBlock(new BlockPos(base[0], base[1] + 1, base[2]), ModBlocks.OBSIDIAN_TOTEM.get().defaultBlockState());
            helper.setBlock(new BlockPos(base[0], base[1] + 2, base[2]), ModBlocks.NITOR_LIGHT.get().defaultBlockState());
        }
    }

    private static EnchanterBlockEntity enchanter(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, EnchantingFeature.ENCHANTER.get().defaultBlockState());
        return (EnchanterBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static void run(GameTestHelper helper, EnchanterBlockEntity enchanter, int ticks) {
        for (int tick = 0; tick < ticks && enchanter.isWorking(); tick++) {
            EnchanterBlockEntity.serverTick(helper.getLevel(), enchanter.getBlockPos(), enchanter.getBlockState(), enchanter);
        }
    }

    private static void enchanterApplies(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        buildPillars(helper);
        EnchanterBlockEntity enchanter = enchanter(helper);
        ItemStack wand = Wands.create(WandRegistry.CAP_THAUMIUM, WandRegistry.ROD_GREATWOOD, false);
        Wands.fill(wand);
        int before = Wands.getVis(wand, Aspect.ORDER);
        enchanter.setItem(EnchanterBlockEntity.WAND, wand);
        enchanter.setItem(EnchanterBlockEntity.TOOL, new ItemStack(Items.DIAMOND_SWORD));
        ResearchManager.complete(player, EnchantingFeature.ENCHANT_VAMPIRISM);
        enchanter.applyEdit(player, TtEnchantments.FOCUSED_STRIKE, 0);
        helper.assertTrue(enchanter.getEnchantments().equals(List.of(TtEnchantments.FOCUSED_STRIKE)), "enchant not selected");
        helper.assertTrue(enchanter.getTotalAspects().getAmount(Aspect.ORDER) == 14, "total order " + enchanter.getTotalAspects().getAmount(Aspect.ORDER));
        helper.assertTrue(enchanter.startWorking(), "start failed");
        run(helper, enchanter, 200);
        helper.assertTrue(!enchanter.isWorking(), "still working");
        helper.assertTrue(enchanter.getTool().getEnchantmentLevel(holder(helper, TtEnchantments.FOCUSED_STRIKE)) == 1, "enchant not applied");
        helper.assertTrue(Wands.getVis(wand, Aspect.ORDER) < before, "vis was not consumed");
        helper.assertTrue(enchanter.getEnchantments().isEmpty(), "selection not cleared");
        helper.succeed();
    }

    private static void enchanterRejects(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        EnchanterBlockEntity enchanter = enchanter(helper);
        enchanter.setItem(EnchanterBlockEntity.TOOL, new ItemStack(Items.DIAMOND_SWORD));
        enchanter.applyEdit(player, TtEnchantments.FOCUSED_STRIKE, 0);
        helper.assertTrue(enchanter.getEnchantments().isEmpty(), "unresearched enchant accepted");
        enchanter.applyEdit(player, Enchantments.PROTECTION, 0);
        helper.assertTrue(enchanter.getEnchantments().isEmpty(), "incompatible item accepted");
        enchanter.applyEdit(player, Enchantments.SHARPNESS, 0);
        enchanter.applyEdit(player, Enchantments.SHARPNESS, 9);
        helper.assertTrue(enchanter.getLevels().equals(List.of(5)), "level not clamped " + enchanter.getLevels());
        enchanter.applyEdit(player, Enchantments.SMITE, 0);
        helper.assertTrue(enchanter.getEnchantments().size() == 1, "exclusive enchant accepted");
        enchanter.setItem(EnchanterBlockEntity.TOOL, ItemStack.EMPTY);
        helper.assertTrue(enchanter.getEnchantments().isEmpty(), "selection must clear when the tool leaves");
        helper.succeed();
    }

    private static void spellClothRecipe(GameTestHelper helper) {
        ItemStack sword = enchanted(helper, new ItemStack(Items.DIAMOND_SWORD), Enchantments.SHARPNESS, 3);
        ItemStack cloth = new ItemStack(EnchantingFeature.SPELL_CLOTH.get());
        helper.assertTrue(cloth.getMaxDamage() == 35, "cloth durability");
        CraftingInput input = CraftingInput.of(2, 1, List.of(sword, cloth));
        helper.assertTrue(SpellClothRecipe.INSTANCE.matches(input, helper.getLevel()), "recipe must match");
        ItemStack result = SpellClothRecipe.INSTANCE.assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(result.is(Items.DIAMOND_SWORD) && !result.isEnchanted(), "enchantments not removed");
        var remaining = SpellClothRecipe.INSTANCE.getRemainingItems(input);
        helper.assertTrue(remaining.get(0).isEmpty() && remaining.get(1).getDamageValue() == 1, "cloth must lose one use");
        cloth.setDamageValue(34);
        helper.assertTrue(SpellClothRecipe.INSTANCE.getRemainingItems(CraftingInput.of(2, 1, List.of(sword, cloth))).get(1).isEmpty(), "cloth must break on the last use");
        helper.assertTrue(!SpellClothRecipe.INSTANCE.matches(CraftingInput.of(2, 1, List.of(new ItemStack(Items.DIAMOND_SWORD), cloth)), helper.getLevel()), "unenchanted item must not match");
        helper.assertTrue(!SpellClothRecipe.INSTANCE.matches(CraftingInput.of(3, 1, List.of(sword, cloth, new ItemStack(Items.STICK))), helper.getLevel()), "extra item must not match");
        helper.succeed();
    }
}
