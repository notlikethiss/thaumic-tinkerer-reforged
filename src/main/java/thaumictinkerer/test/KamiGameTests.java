package thaumictinkerer.test;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.ArcaneCrafting;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.Knowledge;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.wand.WandManager;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;
import thaumictinkerer.Config;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.TtRarity;
import thaumictinkerer.kami.DimensionalShardDrops;
import thaumictinkerer.kami.KamiFeature;
import thaumictinkerer.research.KamiResearchItem;
import thaumictinkerer.research.TtResearch;

public final class KamiGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("kami")
        .add("research_registered", KamiGameTests::researchRegistered)
        .add("recipes_registered", KamiGameTests::recipesRegistered)
        .add("resources_rarity", KamiGameTests::resourcesRarity)
        .add("shard_drops", KamiGameTests::shardDrops)
        .add("wand_parts", KamiGameTests::wandParts)
        .add("tools_unbreakable", KamiGameTests::toolsUnbreakable)
        .add("armor_discount", KamiGameTests::armorDiscount);

    private KamiGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static ResearchItem kami(GameTestHelper helper, String key, int column, int row, int warp) {
        ResearchItem research = ResearchCategories.getResearch(key);
        helper.assertTrue(research instanceof KamiResearchItem, key + " is not a kami research");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), key + " has wrong category");
        helper.assertTrue(research.column() == column && research.row() == row, key + " has wrong position");
        helper.assertTrue(research.warp() == warp, key + " has wrong warp " + research.warp());
        helper.assertTrue(research.isConcealed(), key + " must be concealed");
        return research;
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem shards = ResearchCategories.getResearch("DIMENSION_SHARDS");
        helper.assertTrue(shards instanceof KamiResearchItem && shards.column() == 7 && shards.row() == 8, "shards research is wrong");
        helper.assertTrue(shards.isStub() && shards.isAutoUnlock() && shards.isRound(), "shards flags are wrong");

        ResearchItem ichor = kami(helper, "ICHOR", 9, 8, 5);
        helper.assertTrue(ichor.pages().size() == 2, "ichor pages are wrong");
        helper.assertTrue(TtResearch.kamiResearch() == ichor, "kami research is not ichor");
        helper.assertTrue(ichor.parentsHidden().contains("THAUMIUM") && ichor.parentsHidden().contains("CLEANSING_TALISMAN"), "ichor hidden parents are wrong " + ichor.parentsHidden());
        helper.assertTrue(!ichor.parentsHidden().contains("ICHOR_CLOTH"), "kami research must not be swept");

        ResearchItem cloth = kami(helper, "ICHOR_CLOTH", 11, 7, 0);
        helper.assertTrue(cloth.parents().equals(List.of("ICHOR")) && cloth.pages().size() == 2, "cloth is wrong");
        ResearchItem ichorium = kami(helper, "ICHORIUM", 11, 9, 0);
        helper.assertTrue(ichorium.parents().equals(List.of("ICHOR")) && ichorium.parentsHidden().contains("ICHOR_CLOTH"), "ichorium is wrong");
        ResearchItem cap = kami(helper, "CAP_ICHOR", 11, 11, 2);
        helper.assertTrue(cap.parents().equals(List.of("ICHORIUM")), "cap parents are wrong");
        ResearchItem rod = kami(helper, "ROD_ICHORCLOTH", 14, 2, 1);
        helper.assertTrue(rod.parents().equals(List.of("ICHOR_CLOTH")) && rod.parentsHidden().contains("CAP_ICHOR"), "rod parents are wrong");
        ResearchItem armor = kami(helper, "ICHORCLOTH_ARMOR", 17, 5, 4);
        helper.assertTrue(armor.parents().equals(List.of("ICHOR_CLOTH")) && armor.pages().size() == 5, "armor is wrong");
        ResearchItem tools = kami(helper, "ICHOR_TOOLS", 13, 12, 2);
        helper.assertTrue(tools.parents().equals(List.of("ICHORIUM")) && tools.parentsHidden().contains("ROD_ICHORCLOTH") && tools.pages().size() == 5, "tools are wrong");
        helper.assertTrue(Config.kamiEnabled(), "kami is enabled by default");
        helper.succeed();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        InfusionRecipe ichor = (InfusionRecipe) TcRecipes.get("ICHOR");
        helper.assertTrue(ichor != null && ichor.instability() == 7 && ichor.components().size() == 4, "ichor infusion is wrong");
        helper.assertTrue(ichor.aspects().getAmount(Aspect.SOUL) == 64, "ichor aspects are wrong");
        InfusionRecipe rod = (InfusionRecipe) TcRecipes.get("ROD_ICHORCLOTH");
        helper.assertTrue(rod != null && rod.instability() == 9 && rod.components().size() == 6, "rod infusion is wrong");
        for (String key : List.of("ICHOR_CLOTH", "ICHORIUM", "CAP_ICHOR", "ICHORCLOTH_HELM", "ICHORCLOTH_CHEST", "ICHORCLOTH_LEGS", "ICHORCLOTH_BOOTS", "ICHOR_PICK", "ICHOR_SHOVEL", "ICHOR_AXE", "ICHOR_SWORD")) {
            helper.assertTrue(TcRecipes.get(key) instanceof ShapedArcaneRecipe, "arcane recipe missing: " + key);
        }
        ShapedArcaneRecipe cloth = (ShapedArcaneRecipe) TcRecipes.get("ICHOR_CLOTH");
        helper.assertTrue(cloth.aspects().getAmount(Aspect.ENTROPY) == 125 && cloth.result().count() == 3, "cloth recipe is wrong");
        ShapedArcaneRecipe helm = (ShapedArcaneRecipe) TcRecipes.get("ICHORCLOTH_HELM");
        helper.assertTrue("ICHORCLOTH_ARMOR".equals(helm.research()), "helm research is wrong");
        boolean nuggets = helper.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, ThaumicTinkerer.id("ichorium_nugget"))).isPresent();
        helper.assertTrue(nuggets, "nugget recipe missing");
        helper.succeed();
    }

    private static void resourcesRarity(GameTestHelper helper) {
        Rarity kami = TtRarity.kami();
        for (Item item : List.of(KamiFeature.ICHOR.get(), KamiFeature.ICHOR_CLOTH.get(), KamiFeature.ICHORIUM_INGOT.get(), KamiFeature.ICHORIUM_NUGGET.get(), KamiFeature.ICHORIUM_CAP.get(), KamiFeature.ICHORCLOTH_ROD.get(), KamiFeature.ICHORIUM_PICKAXE.get(), KamiFeature.ICHORCLOTH_COWL.get())) {
            helper.assertTrue(new ItemStack(item).getRarity() == kami, "wrong rarity for " + item);
        }
        helper.assertTrue(new ItemStack(KamiFeature.NETHER_SHARD.get()).getRarity() == Rarity.COMMON, "nether shard rarity");
        helper.assertTrue(new ItemStack(KamiFeature.ENDER_SHARD.get()).getRarity() == Rarity.COMMON, "ender shard rarity");
        helper.succeed();
    }

    private static int shards(List<ItemEntity> drops, Item item) {
        int count = 0;
        for (ItemEntity drop : drops) {
            if (drop.getItem().is(item)) {
                count += drop.getItem().getCount();
            }
        }
        return count;
    }

    private static void shardDrops(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        DamageSource playerSource = helper.getLevel().damageSources().playerAttack(player);
        DamageSource otherSource = helper.getLevel().damageSources().generic();
        Enderman enderman = EntityTypes.ENDERMAN.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ZombifiedPiglin piglin = EntityTypes.ZOMBIFIED_PIGLIN.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        Zombie zombie = EntityTypes.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        RandomSource always = new FixedRandom(0.0);
        RandomSource never = new FixedRandom(0.99);
        RandomSource edgeEnder = new FixedRandom(1.0 / 32.0);
        RandomSource edgeNether = new FixedRandom(1.0 / 16.0);
        RandomSource missEnder = new FixedRandom(1.0 / 32.0 + 0.001);

        List<ItemEntity> drops = new ArrayList<>();
        DimensionalShardDrops.apply(enderman, Level.END, playerSource, drops, always);
        helper.assertValueEqual(shards(drops, KamiFeature.ENDER_SHARD.get()), 1, "ender shard in the end");
        helper.assertValueEqual(shards(drops, KamiFeature.NETHER_SHARD.get()), 0, "no nether shard from enderman");

        drops.clear();
        DimensionalShardDrops.apply(enderman, Level.OVERWORLD, playerSource, drops, always);
        DimensionalShardDrops.apply(enderman, Level.NETHER, playerSource, drops, always);
        helper.assertTrue(drops.isEmpty(), "enderman outside the end must not drop shards");

        DimensionalShardDrops.apply(enderman, Level.END, otherSource, drops, always);
        helper.assertTrue(drops.isEmpty(), "non player kill must not drop shards");

        DimensionalShardDrops.apply(enderman, Level.END, playerSource, drops, never);
        helper.assertTrue(drops.isEmpty(), "failed chance must not drop shards");
        DimensionalShardDrops.apply(enderman, Level.END, playerSource, drops, missEnder);
        helper.assertTrue(drops.isEmpty(), "chance above one in thirty two must not drop");
        DimensionalShardDrops.apply(enderman, Level.END, playerSource, drops, edgeEnder);
        helper.assertValueEqual(shards(drops, KamiFeature.ENDER_SHARD.get()), 1, "chance is inclusive");

        drops.clear();
        DimensionalShardDrops.apply(piglin, Level.NETHER, playerSource, drops, always);
        helper.assertValueEqual(shards(drops, KamiFeature.NETHER_SHARD.get()), 1, "nether shard in the nether");
        drops.clear();
        DimensionalShardDrops.apply(piglin, Level.END, playerSource, drops, always);
        DimensionalShardDrops.apply(piglin, Level.OVERWORLD, playerSource, drops, always);
        DimensionalShardDrops.apply(zombie, Level.NETHER, playerSource, drops, always);
        helper.assertTrue(drops.isEmpty(), "only zombified piglins in the nether drop nether shards");
        DimensionalShardDrops.apply(piglin, Level.NETHER, playerSource, drops, never);
        helper.assertTrue(drops.isEmpty(), "nether chance miss");
        DimensionalShardDrops.apply(piglin, Level.NETHER, playerSource, drops, edgeNether);
        helper.assertValueEqual(shards(drops, KamiFeature.NETHER_SHARD.get()), 1, "nether chance is one in sixteen");
        helper.succeed();
    }

    private static void wandParts(GameTestHelper helper) {
        var cap = WandRegistry.findCap("ICHOR");
        var rod = WandRegistry.findRod("ICHORCLOTH");
        helper.assertTrue(cap != null && rod != null, "wand parts are not registered");
        helper.assertTrue(cap.baseCostModifier() == 0.7F && cap.craftCost() == 10, "cap numbers are wrong");
        helper.assertValueEqual(cap.getCostModifier(Aspect.FIRE), 0.7F, "cap discount");
        helper.assertValueEqual(cap.research(), "CAP_ICHOR", "cap research key");
        helper.assertValueEqual(rod.research(), "ROD_ICHORCLOTH", "rod research key");
        helper.assertTrue(rod.capacity() == 1000 && rod.craftCost() == 10 && rod.glowing() && !rod.staff() && rod.onUpdate() == null, "rod numbers are wrong");
        helper.assertValueEqual(cap.texture().toString(), "thaumictinkerer:textures/model/capichor.png", "cap texture");
        helper.assertValueEqual(rod.texture().toString(), "thaumictinkerer:textures/model/rodichorcloth.png", "rod texture");

        ItemStack wand = Wands.create(cap, rod, false);
        helper.assertValueEqual(Wands.getMaxVis(wand), 100000, "max vis");
        helper.assertTrue(Wands.getCap(wand) == cap && Wands.getRod(wand) == rod, "wand parts are not kept");

        Player player = GearGameTests.mockPlayer(helper);
        List<ItemStack> grid = new ArrayList<>();
        for (int slot = 0; slot < 9; slot++) {
            grid.add(ItemStack.EMPTY);
        }
        grid.set(6, new ItemStack(KamiFeature.ICHORIUM_CAP.get()));
        grid.set(2, new ItemStack(KamiFeature.ICHORIUM_CAP.get()));
        grid.set(4, new ItemStack(KamiFeature.ICHORCLOTH_ROD.get()));
        helper.assertTrue(ArcaneCrafting.findResult(grid, player).isEmpty(), "wand crafted without research");
        Knowledge.get(player).addResearch("CAP_ICHOR");
        helper.assertTrue(ArcaneCrafting.findResult(grid, player).isEmpty(), "wand crafted without rod research");
        Knowledge.get(player).addResearch("ROD_ICHORCLOTH");
        ItemStack crafted = ArcaneCrafting.findResult(grid, player);
        helper.assertTrue(Wands.isWand(crafted), "ichor wand not built");
        helper.assertValueEqual(Wands.getCap(crafted).tag(), "ICHOR", "crafted cap");
        helper.assertValueEqual(Wands.getRod(crafted).tag(), "ICHORCLOTH", "crafted rod");
        helper.assertValueEqual(ArcaneCrafting.findAspects(grid, player).getAmount(Aspect.ORDER), 100, "order cost");
        helper.succeed();
    }

    private static void toolsUnbreakable(GameTestHelper helper) {
        for (Item item : List.of(KamiFeature.ICHORIUM_PICKAXE.get(), KamiFeature.ICHORIUM_SHOVEL.get(), KamiFeature.ICHORIUM_AXE.get(), KamiFeature.ICHORIUM_SWORD.get(), KamiFeature.ICHORCLOTH_COWL.get(), KamiFeature.ICHORCLOTH_ROBE.get(), KamiFeature.ICHORCLOTH_LEGGINGS.get(), KamiFeature.ICHORCLOTH_BOOTS.get())) {
            ItemStack stack = new ItemStack(item);
            helper.assertTrue(stack.has(DataComponents.UNBREAKABLE), "not unbreakable: " + item);
            stack.setDamageValue(5);
            helper.assertTrue(!stack.isDamaged() || stack.has(DataComponents.UNBREAKABLE), "damage must not apply");
        }
        ItemStack pickaxe = new ItemStack(KamiFeature.ICHORIUM_PICKAXE.get());
        helper.assertTrue(pickaxe.getDestroySpeed(Blocks.OBSIDIAN.defaultBlockState()) == 10.0F, "pickaxe speed is " + pickaxe.getDestroySpeed(Blocks.OBSIDIAN.defaultBlockState()));
        helper.assertTrue(pickaxe.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "pickaxe must harvest obsidian");
        helper.assertTrue(pickaxe.isCorrectToolForDrops(Blocks.ANCIENT_DEBRIS.defaultBlockState()), "pickaxe must harvest netherite level blocks");
        ItemStack shovel = new ItemStack(KamiFeature.ICHORIUM_SHOVEL.get());
        helper.assertTrue(shovel.getDestroySpeed(Blocks.DIRT.defaultBlockState()) == 10.0F, "shovel speed");
        ItemStack axe = new ItemStack(KamiFeature.ICHORIUM_AXE.get());
        helper.assertTrue(axe.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState()) == 10.0F, "axe speed");
        ItemStack sword = new ItemStack(KamiFeature.ICHORIUM_SWORD.get());
        helper.assertTrue(sword.getDestroySpeed(Blocks.COBWEB.defaultBlockState()) > 1.0F, "sword cuts cobwebs");
        helper.succeed();
    }

    private static void armorDiscount(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        float before = WandManager.getTotalVisDiscount(player, Aspect.FIRE);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(KamiFeature.ICHORCLOTH_COWL.get()));
        helper.assertTrue(Math.abs(WandManager.getTotalVisDiscount(player, Aspect.FIRE) - before - 0.04F) < 0.0001F, "cowl discount");
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(KamiFeature.ICHORCLOTH_ROBE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(KamiFeature.ICHORCLOTH_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(KamiFeature.ICHORCLOTH_BOOTS.get()));
        helper.assertTrue(Math.abs(WandManager.getTotalVisDiscount(player, Aspect.AIR) - before - 0.15F) < 0.0001F, "full set discount");
        ItemStack helmet = new ItemStack(KamiFeature.ICHORCLOTH_COWL.get());
        helper.assertTrue(helmet.has(DataComponents.EQUIPPABLE), "cowl is equippable");
        helper.succeed();
    }

    private static final class FixedRandom implements RandomSource {
        private final double value;

        FixedRandom(double value) {
            this.value = value;
        }

        @Override
        public double nextDouble() {
            return value;
        }

        @Override
        public RandomSource fork() {
            return this;
        }

        @Override
        public net.minecraft.world.level.levelgen.PositionalRandomFactory forkPositional() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSeed(long seed) {
        }

        @Override
        public int nextInt() {
            return 0;
        }

        @Override
        public int nextInt(int bound) {
            return 0;
        }

        @Override
        public long nextLong() {
            return 0;
        }

        @Override
        public boolean nextBoolean() {
            return false;
        }

        @Override
        public float nextFloat() {
            return (float) value;
        }

        @Override
        public double nextGaussian() {
            return 0;
        }
    }
}
