package thaumictinkerer.test;

import com.mojang.serialization.DataResult;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumictinkerer.Config;
import thaumictinkerer.kami.tool.AwakenedToolItem;
import thaumictinkerer.kami.tool.AwakenedToolsFeature;
import thaumictinkerer.kami.tool.SoulHeartHandler;
import thaumictinkerer.kami.tool.ToolHandler;
import thaumictinkerer.research.KamiResearchItem;
import thaumictinkerer.research.TtResearch;

public final class AwakenedToolsGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("awakened_tools")
        .isolated()
        .add("research_registered", AwakenedToolsGameTests::researchRegistered)
        .add("recipes_registered", AwakenedToolsGameTests::recipesRegistered)
        .add("mode_switch_persists", AwakenedToolsGameTests::modeSwitchPersists)
        .add("pick_square_mode", AwakenedToolsGameTests::pickSquareMode)
        .add("pick_line_mode", AwakenedToolsGameTests::pickLineMode)
        .add("pick_respects_permissions", AwakenedToolsGameTests::pickRespectsPermissions)
        .add("block_mode_breaks_one", AwakenedToolsGameTests::blockModeBreaksOne)
        .add("shovel_column_mode", AwakenedToolsGameTests::shovelColumnMode)
        .add("axe_square_mode", AwakenedToolsGameTests::axeSquareMode)
        .add("axe_fells_tree", AwakenedToolsGameTests::axeFellsTree)
        .add("sword_area_mode", AwakenedToolsGameTests::swordAreaMode)
        .add("sword_soul_mode", AwakenedToolsGameTests::swordSoulMode)
        .add("soul_hearts_absorb_damage", AwakenedToolsGameTests::soulHeartsAbsorbDamage);

    private AwakenedToolsGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static ResearchItem gem(GameTestHelper helper, String key, int column, int row, int pages) {
        ResearchItem research = ResearchCategories.getResearch(key);
        helper.assertTrue(research instanceof KamiResearchItem, key + " is not a kami research");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), key + " has wrong category");
        helper.assertTrue(research.column() == column && research.row() == row, key + " has wrong position");
        helper.assertTrue(research.complexity() == 5 || research.complexity() == 3, key + " has wrong complexity " + research.complexity());
        helper.assertTrue(research.isConcealed(), key + " must be concealed");
        helper.assertTrue(research.parents().equals(List.of("ICHOR_TOOLS")), key + " parents are wrong " + research.parents());
        helper.assertTrue(research.pages().size() == pages, key + " page count is " + research.pages().size());
        return research;
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem pick = gem(helper, "ICHOR_PICK_GEM", 13, 15, 3);
        helper.assertTrue(pick.tags().getAmount(Aspect.FIRE) == 2 && pick.tags().getAmount(Aspect.MINE) == 1, "pick aspects");
        gem(helper, "ICHOR_SHOVEL_GEM", 15, 15, 2);
        gem(helper, "ICHOR_AXE_GEM", 16, 14, 2);
        ResearchItem sword = gem(helper, "ICHOR_SWORD_GEM", 16, 12, 3);
        helper.assertTrue(sword.tags().getAmount(Aspect.SOUL) == 1 && sword.tags().getAmount(Aspect.HUNGER) == 1, "sword aspects");
        helper.assertTrue(Config.kamiEnabled(), "kami is enabled by default");
        helper.succeed();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        for (String key : List.of("ICHOR_PICK_GEM", "ICHOR_SHOVEL_GEM", "ICHOR_AXE_GEM", "ICHOR_SWORD_GEM")) {
            InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get(key);
            helper.assertTrue(recipe != null, "infusion recipe missing: " + key);
            helper.assertTrue(recipe.research().equals(key), key + " research key");
            helper.assertTrue(recipe.instability() == 15 && recipe.components().size() == 12, key + " shape");
        }
        InfusionRecipe pick = (InfusionRecipe) TcRecipes.get("ICHOR_PICK_GEM");
        helper.assertTrue(pick.aspects().getAmount(Aspect.MINE) == 64 && pick.aspects().getAmount(Aspect.FIRE) == 50, "pick aspects");
        helper.assertTrue(pick.result().item().equals("thaumictinkerer:awakened_ichorium_pickaxe"), "pick result");
        InfusionRecipe sword = (InfusionRecipe) TcRecipes.get("ICHOR_SWORD_GEM");
        helper.assertTrue(sword.aspects().getAmount(Aspect.HUNGER) == 64 && sword.aspects().getAmount(Aspect.SOUL) == 32, "sword aspects");
        helper.succeed();
    }

    private static ItemStack tool(Item item, int mode) {
        ItemStack stack = new ItemStack(item);
        ToolHandler.setMode(stack, mode);
        return stack;
    }

    private static ServerPlayer player(GameTestHelper helper, ItemStack stack, float yRot) {
        ServerPlayer player = GearGameTests.mockPlayer(helper);
        player.setPos(helper.absoluteVec(new Vec3(3.5, 1.0, 0.5)));
        player.setYRot(yRot);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return player;
    }

    private static int dropped(GameTestHelper helper, Item item) {
        AABB plot = new AABB(helper.absolutePos(BlockPos.ZERO)).expandTowards(7, 7, 7).inflate(2);
        int count = 0;
        for (ItemEntity drop : helper.getLevel().getEntitiesOfClass(ItemEntity.class, plot)) {
            if (drop.getItem().is(item)) {
                count += drop.getItem().getCount();
            }
        }
        return count;
    }

    private static void fill(GameTestHelper helper, int fromX, int fromY, int fromZ, int toX, int toY, int toZ, Block block) {
        for (int x = fromX; x <= toX; x++) {
            for (int y = fromY; y <= toY; y++) {
                for (int z = fromZ; z <= toZ; z++) {
                    helper.setBlock(new BlockPos(x, y, z), block.defaultBlockState());
                }
            }
        }
    }

    private static void modeSwitchPersists(GameTestHelper helper) {
        ServerPlayer player = player(helper, new ItemStack(AwakenedToolsFeature.AWAKENED_PICKAXE.get()), 0.0F);
        ItemStack stack = player.getMainHandItem();
        helper.assertValueEqual(ToolHandler.getMode(stack), 0, "initial mode");
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(ToolHandler.getMode(stack), 0, "mode must not change without sneaking");
        player.setShiftKeyDown(true);
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(ToolHandler.getMode(stack), 1, "first switch");
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(ToolHandler.getMode(stack), 2, "second switch");
        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        DataResult<Tag> encoded = ItemStack.CODEC.encodeStart(ops, stack);
        ItemStack restored = ItemStack.CODEC.parse(ops, encoded.getOrThrow()).getOrThrow();
        helper.assertValueEqual(ToolHandler.getMode(restored), 2, "mode survives serialization");
        helper.assertTrue(restored.has(DataComponents.UNBREAKABLE), "restored tool stays unbreakable");
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(ToolHandler.getMode(stack), 0, "third switch wraps");
        for (Item item : List.of(AwakenedToolsFeature.AWAKENED_SHOVEL.get(), AwakenedToolsFeature.AWAKENED_AXE.get(), AwakenedToolsFeature.AWAKENED_SWORD.get())) {
            ItemStack other = new ItemStack(item);
            player.setItemInHand(InteractionHand.MAIN_HAND, other);
            other.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertValueEqual(ToolHandler.getMode(other), 1, "switch on " + item);
        }
        helper.succeed();
    }

    private static void pickSquareMode(GameTestHelper helper) {
        ServerPlayer player = player(helper, tool(AwakenedToolsFeature.AWAKENED_PICKAXE.get(), 1), 180.0F);
        fill(helper, 1, 1, 3, 5, 5, 3, Blocks.STONE);
        helper.setBlock(new BlockPos(4, 3, 3), Blocks.BEDROCK.defaultBlockState());
        helper.setBlock(new BlockPos(2, 4, 3), Blocks.DIRT.defaultBlockState());
        helper.setBlock(new BlockPos(3, 3, 4), Blocks.STONE.defaultBlockState());
        boolean destroyed = player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(3, 2, 3)));
        helper.assertTrue(destroyed, "origin was not destroyed");
        int remainingStone = 0;
        for (int x = 1; x <= 5; x++) {
            for (int y = 1; y <= 5; y++) {
                if (helper.getBlockState(new BlockPos(x, y, 3)).is(Blocks.STONE)) {
                    remainingStone++;
                }
            }
        }
        helper.assertValueEqual(remainingStone, 0, "stone left in the square");
        helper.assertTrue(helper.getBlockState(new BlockPos(4, 3, 3)).is(Blocks.BEDROCK), "bedrock must stay");
        helper.assertTrue(helper.getBlockState(new BlockPos(2, 4, 3)).is(Blocks.DIRT), "pickaxe must skip dirt");
        helper.assertTrue(helper.getBlockState(new BlockPos(3, 3, 4)).is(Blocks.STONE), "blocks behind the square stay");
        helper.assertValueEqual(dropped(helper, Items.COBBLESTONE), 23, "cobblestone drops");
        helper.succeed();
    }

    private static void pickLineMode(GameTestHelper helper) {
        ServerPlayer player = player(helper, tool(AwakenedToolsFeature.AWAKENED_PICKAXE.get(), 2), -90.0F);
        fill(helper, 0, 2, 3, 6, 2, 3, Blocks.STONE);
        fill(helper, 0, 3, 3, 6, 3, 3, Blocks.STONE);
        player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(0, 2, 3)));
        for (int x = 0; x <= 6; x++) {
            helper.assertTrue(helper.getBlockState(new BlockPos(x, 2, 3)).isAir(), "line block " + x + " remains");
            helper.assertTrue(helper.getBlockState(new BlockPos(x, 3, 3)).is(Blocks.STONE), "row above must stay " + x);
        }
        helper.assertValueEqual(dropped(helper, Items.COBBLESTONE), 7, "cobblestone drops");
        helper.succeed();
    }

    private static void pickRespectsPermissions(GameTestHelper helper) {
        ServerPlayer player = player(helper, tool(AwakenedToolsFeature.AWAKENED_PICKAXE.get(), 1), 180.0F);
        fill(helper, 1, 1, 3, 5, 5, 3, Blocks.STONE);
        BlockPos protectedPos = helper.absolutePos(new BlockPos(4, 3, 3));
        Object listener = new Object() {
            @SubscribeEvent
            public void onBreak(BreakBlockEvent event) {
                if (event.getPos().equals(protectedPos)) {
                    event.setCanceled(true);
                }
            }
        };
        NeoForge.EVENT_BUS.register(listener);
        try {
            player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(3, 2, 3)));
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        helper.assertTrue(helper.getBlockState(new BlockPos(4, 3, 3)).is(Blocks.STONE), "protected block was broken");
        helper.assertTrue(helper.getBlockState(new BlockPos(2, 2, 3)).isAir(), "unprotected neighbour stays");
        helper.assertValueEqual(dropped(helper, Items.COBBLESTONE), 24, "cobblestone drops");
        helper.succeed();
    }

    private static void blockModeBreaksOne(GameTestHelper helper) {
        ServerPlayer player = player(helper, tool(AwakenedToolsFeature.AWAKENED_PICKAXE.get(), 0), 180.0F);
        fill(helper, 1, 1, 3, 5, 5, 3, Blocks.STONE);
        player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(3, 3, 3)));
        helper.assertTrue(helper.getBlockState(new BlockPos(3, 3, 3)).isAir(), "origin remains");
        helper.assertTrue(helper.getBlockState(new BlockPos(2, 3, 3)).is(Blocks.STONE), "neighbour must stay");
        helper.assertValueEqual(dropped(helper, Items.COBBLESTONE), 1, "single drop");
        helper.succeed();
    }

    private static void shovelColumnMode(GameTestHelper helper) {
        ServerPlayer player = player(helper, tool(AwakenedToolsFeature.AWAKENED_SHOVEL.get(), 2), 180.0F);
        fill(helper, 3, 1, 3, 3, 6, 3, Blocks.DIRT);
        helper.setBlock(new BlockPos(3, 5, 3), Blocks.STONE.defaultBlockState());
        helper.setBlock(new BlockPos(2, 3, 3), Blocks.DIRT.defaultBlockState());
        player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(3, 3, 3)));
        for (int y = 1; y <= 6; y++) {
            if (y == 5) {
                helper.assertTrue(helper.getBlockState(new BlockPos(3, y, 3)).is(Blocks.STONE), "shovel must skip stone");
            } else {
                helper.assertTrue(helper.getBlockState(new BlockPos(3, y, 3)).isAir(), "column block " + y + " remains");
            }
        }
        helper.assertTrue(helper.getBlockState(new BlockPos(2, 3, 3)).is(Blocks.DIRT), "neighbour column must stay");
        helper.assertValueEqual(dropped(helper, Items.DIRT), 5, "dirt drops");
        helper.succeed();
    }

    private static void axeSquareMode(GameTestHelper helper) {
        ServerPlayer player = player(helper, tool(AwakenedToolsFeature.AWAKENED_AXE.get(), 1), 180.0F);
        fill(helper, 1, 1, 3, 5, 5, 3, Blocks.OAK_PLANKS);
        helper.setBlock(new BlockPos(4, 3, 3), Blocks.STONE.defaultBlockState());
        player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(3, 2, 3)));
        helper.assertTrue(helper.getBlockState(new BlockPos(1, 1, 3)).isAir() && helper.getBlockState(new BlockPos(5, 5, 3)).isAir(), "planks remain");
        helper.assertTrue(helper.getBlockState(new BlockPos(4, 3, 3)).is(Blocks.STONE), "axe must skip stone");
        helper.assertValueEqual(dropped(helper, Items.OAK_PLANKS), 24, "plank drops");
        helper.succeed();
    }

    private static void axeFellsTree(GameTestHelper helper) {
        ServerPlayer player = player(helper, tool(AwakenedToolsFeature.AWAKENED_AXE.get(), 2), 180.0F);
        fill(helper, 2, 4, 2, 4, 5, 4, Blocks.OAK_LEAVES);
        fill(helper, 3, 1, 3, 3, 5, 3, Blocks.OAK_LOG);
        helper.setBlock(new BlockPos(6, 1, 6), Blocks.OAK_LOG.defaultBlockState());
        player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(3, 1, 3)));
        for (int y = 1; y <= 5; y++) {
            helper.assertTrue(helper.getBlockState(new BlockPos(3, y, 3)).isAir(), "trunk block " + y + " remains");
        }
        helper.assertTrue(helper.getBlockState(new BlockPos(6, 1, 6)).is(Blocks.OAK_LOG), "separate log must stay");
        helper.assertValueEqual(dropped(helper, Items.OAK_LOG), 5, "log drops");
        helper.succeed();
    }

    private static Zombie zombie(GameTestHelper helper, BlockPos pos) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, pos);
        zombie.setPersistenceRequired();
        return zombie;
    }

    private static void swordAreaMode(GameTestHelper helper) {
        ServerPlayer player = player(helper, tool(AwakenedToolsFeature.AWAKENED_SWORD.get(), 1), 0.0F);
        Zombie target = zombie(helper, new BlockPos(3, 1, 3));
        Zombie near = zombie(helper, new BlockPos(3, 1, 5));
        Zombie far = zombie(helper, new BlockPos(0, 1, 0));
        float nearHealth = near.getHealth();
        float farHealth = far.getHealth();
        player.attack(target);
        helper.assertTrue(target.getHealth() < target.getMaxHealth(), "target was not hurt");
        helper.assertTrue(near.getHealth() < nearHealth, "nearby zombie was not hurt");
        helper.assertTrue(far.getHealth() == farHealth, "far zombie must not be hurt");
        helper.assertValueEqual(SoulHeartHandler.getHP(player), 0, "area mode gives no soul hearts");
        helper.succeed();
    }

    private static void swordSoulMode(GameTestHelper helper) {
        ServerPlayer player = player(helper, tool(AwakenedToolsFeature.AWAKENED_SWORD.get(), 2), 0.0F);
        for (int hit = 1; hit <= SoulHeartHandler.MAX_HP + 3; hit++) {
            Zombie target = zombie(helper, new BlockPos(3, 1, 3));
            player.attack(target);
            helper.assertValueEqual(SoulHeartHandler.getHP(player), Math.min(hit, SoulHeartHandler.MAX_HP), "soul hearts after hit " + hit);
            helper.assertTrue(target.getHealth() < target.getMaxHealth(), "target was not hurt");
            target.discard();
        }
        ServerPlayer other = player(helper, tool(AwakenedToolsFeature.AWAKENED_SWORD.get(), 0), 0.0F);
        Zombie target = zombie(helper, new BlockPos(3, 1, 3));
        other.attack(target);
        helper.assertValueEqual(SoulHeartHandler.getHP(other), 0, "single mode gives no soul hearts");
        helper.succeed();
    }

    private static void soulHeartsAbsorbDamage(GameTestHelper helper) {
        ServerPlayer player = player(helper, ItemStack.EMPTY, 0.0F);
        ServerLevel level = helper.getLevel();
        SoulHeartHandler.setHP(player, 5);
        float health = player.getHealth();
        player.hurtServer(level, level.damageSources().generic(), 3.0F);
        helper.assertTrue(player.getHealth() == health, "soul hearts must absorb the hit");
        helper.assertValueEqual(SoulHeartHandler.getHP(player), 2, "soul hearts left");
        player.damageCooldownTime = 0;
        player.hurtServer(level, level.damageSources().generic(), 4.0F);
        helper.assertValueEqual(SoulHeartHandler.getHP(player), 0, "soul hearts used up");
        helper.assertTrue(Math.abs((health - player.getHealth()) - 2.0F) < 0.001F, "overflow damage taken " + (health - player.getHealth()));
        SoulHeartHandler.setHP(player, 100);
        helper.assertValueEqual(SoulHeartHandler.getHP(player), SoulHeartHandler.MAX_HP, "soul hearts are capped");
        helper.assertTrue(!SoulHeartHandler.addHP(player, 1), "no hearts added at the cap");
        helper.succeed();
    }
}
