package thaumictinkerer.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumictinkerer.elements.ElementsEvents;
import thaumictinkerer.elements.ElementsFeature;
import thaumictinkerer.elements.ForcefieldBlock;
import thaumictinkerer.elements.ImbuedFireBlock;
import thaumictinkerer.research.TtResearch;

public final class ElementsGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("elements")
        .add("fire_research", ElementsGameTests::fireResearch)
        .add("potion_research", ElementsGameTests::potionResearch)
        .add("recipes_registered", ElementsGameTests::recipesRegistered)
        .add("fire_air", ElementsGameTests::fireAir)
        .add("fire_earth", ElementsGameTests::fireEarth)
        .add("fire_ignis", ElementsGameTests::fireIgnis)
        .add("fire_water", ElementsGameTests::fireWater)
        .add("fire_order", ElementsGameTests::fireOrder)
        .add("fire_chaos", ElementsGameTests::fireChaos)
        .add("fire_dies_without_target", ElementsGameTests::fireDiesWithoutTarget)
        .add("fire_spreads_to_target_air", ElementsGameTests::fireSpreadsToTargetAir)
        .add("fire_respects_game_rule", ElementsGameTests::fireRespectsGameRule)
        .add("fire_item_places_anywhere", ElementsGameTests::fireItemPlacesAnywhere)
        .add("water_effect_solidifies_lava", ElementsGameTests::waterEffectSolidifiesLava)
        .add("air_effect_knocks_up", ElementsGameTests::airEffectKnocksUp)
        .add("fire_effect_ignites", ElementsGameTests::fireEffectIgnites)
        .add("earth_effect_builds_wall", ElementsGameTests::earthEffectBuildsWall)
        .add("forcefield_properties", ElementsGameTests::forcefieldProperties)
        .add("potions_apply_effects", ElementsGameTests::potionsApplyEffects);

    private ElementsGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static ResearchItem research(GameTestHelper helper, String key) {
        ResearchItem research = ResearchCategories.getResearch(key);
        helper.assertTrue(research != null, key + " is not registered");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), key + " has the wrong category");
        return research;
    }

    private static void checkFire(GameTestHelper helper, String key, int column, int row, int complexity, Aspect first, int firstAmount, Aspect second, int secondAmount) {
        ResearchItem item = research(helper, key);
        helper.assertTrue(item.column() == column && item.row() == row && item.complexity() == complexity, key + " position");
        helper.assertTrue(item.parents().equals(List.of("BRIGHT_NITOR")), key + " parents " + item.parents());
        helper.assertTrue(item.isSecondary() && item.isConcealed() && !item.isRound(), key + " flags");
        helper.assertTrue(item.tags().getAmount(first) == firstAmount, key + " aspect " + first);
        if (second != null) {
            helper.assertTrue(item.tags().getAmount(second) == secondAmount && item.tags().size() == 2, key + " aspect " + second);
        } else {
            helper.assertTrue(item.tags().size() == 1, key + " aspect count");
        }
        helper.assertTrue(item.pages().size() == 2 && item.pages().get(1) instanceof ResearchPage.Crucible, key + " pages");
    }

    private static void fireResearch(GameTestHelper helper) {
        checkFire(helper, "FIRE_AER", 3, -7, 2, Aspect.FIRE, 5, Aspect.AIR, 5);
        checkFire(helper, "FIRE_PERDITIO", 2, -8, 3, Aspect.FIRE, 5, Aspect.ENTROPY, 5);
        checkFire(helper, "FIRE_TERRA", 4, -6, 2, Aspect.FIRE, 5, Aspect.EARTH, 5);
        checkFire(helper, "FIRE_IGNIS", 4, -4, 2, Aspect.FIRE, 10, null, 0);
        checkFire(helper, "FIRE_ORDO", 3, -3, 2, Aspect.FIRE, 5, Aspect.ORDER, 5);
        checkFire(helper, "FIRE_AQUA", 2, -2, 2, Aspect.FIRE, 5, Aspect.WATER, 5);
        helper.assertTrue(ResearchCategories.getResearch("BRIGHT_NITOR") != null, "BRIGHT_NITOR is missing");
        helper.succeed();
    }

    private static void potionResearch(GameTestHelper helper) {
        ResearchItem item = research(helper, "INFUSED_POTIONS");
        helper.assertTrue(item.column() == 7 && item.row() == -5 && item.complexity() == 2, "INFUSED_POTIONS position");
        helper.assertTrue(
            item.parents().equals(List.of("FIRE_PERDITIO", "FIRE_ORDO", "FIRE_IGNIS", "FIRE_TERRA", "FIRE_AER", "FIRE_AQUA")),
            "INFUSED_POTIONS parents " + item.parents()
        );
        helper.assertTrue(item.parentsHidden().contains("INFUSION"), "INFUSED_POTIONS hidden parents " + item.parentsHidden());
        helper.assertTrue(item.isConcealed() && !item.isSecondary() && !item.isRound(), "INFUSED_POTIONS flags");
        helper.assertTrue(item.tags().getAmount(Aspect.WATER) == 5 && item.tags().getAmount(Aspect.ENTROPY) == 5 && item.tags().size() == 2, "INFUSED_POTIONS aspects");
        helper.assertTrue(item.pages().size() == 11, "INFUSED_POTIONS pages " + item.pages().size());
        helper.assertTrue(item.pages().get(6) instanceof ResearchPage.Infusion, "infusion page");
        for (int index = 7; index < 11; index++) {
            helper.assertTrue(item.pages().get(index) instanceof ResearchPage.Crucible, "crucible page " + index);
        }
        helper.succeed();
    }

    private static void checkRecipe(GameTestHelper helper, String key, String result, String catalyst, Aspect first, int firstAmount, Aspect second, int secondAmount, Aspect third, int thirdAmount) {
        helper.assertTrue(TcRecipes.get(key) instanceof CrucibleRecipe, "crucible recipe " + key);
        CrucibleRecipe recipe = (CrucibleRecipe) TcRecipes.get(key);
        helper.assertTrue(recipe.result().item().toString().equals(result), key + " result " + recipe.result().item());
        helper.assertTrue(recipe.catalyst().displayStacks().isEmpty() || recipe.catalyst().test(recipe.catalyst().displayStacks().get(0)), key + " catalyst");
        helper.assertTrue(recipe.aspects().getAmount(first) == firstAmount && recipe.aspects().getAmount(second) == secondAmount, key + " aspects");
        if (third != null) {
            helper.assertTrue(recipe.aspects().getAmount(third) == thirdAmount, key + " third aspect");
        }
        helper.assertTrue(recipe.catalyst().toString().contains(catalyst), key + " catalyst id " + recipe.catalyst());
    }

    private static void recipesRegistered(GameTestHelper helper) {
        checkRecipe(helper, "FIRE_AER", "thaumictinkerer:aer_imbued_fire", "thaumcraft:air_shard", Aspect.FIRE, 5, Aspect.MAGIC, 5, Aspect.AIR, 5);
        checkRecipe(helper, "FIRE_PERDITIO", "thaumictinkerer:perditio_imbued_fire", "thaumcraft:entropy_shard", Aspect.FIRE, 5, Aspect.MAGIC, 5, Aspect.ENTROPY, 5);
        checkRecipe(helper, "FIRE_TERRA", "thaumictinkerer:terra_imbued_fire", "thaumcraft:earth_shard", Aspect.FIRE, 5, Aspect.MAGIC, 5, Aspect.EARTH, 5);
        checkRecipe(helper, "FIRE_IGNIS", "thaumictinkerer:ignis_imbued_fire", "thaumcraft:fire_shard", Aspect.FIRE, 10, Aspect.AIR, 5, null, 0);
        checkRecipe(helper, "FIRE_ORDO", "thaumictinkerer:ordo_imbued_fire", "thaumcraft:order_shard", Aspect.FIRE, 5, Aspect.MAGIC, 5, Aspect.ORDER, 5);
        checkRecipe(helper, "FIRE_AQUA", "thaumictinkerer:aqua_imbued_fire", "thaumcraft:water_shard", Aspect.FIRE, 5, Aspect.MAGIC, 5, Aspect.WATER, 5);
        checkRecipe(helper, "INFUSED_POTIONSPOT0", "thaumictinkerer:aer_infused_potion", "thaumictinkerer:aer_infused_grain", Aspect.AURA, 5, Aspect.AIR, 5, null, 0);
        checkRecipe(helper, "INFUSED_POTIONSPOT1", "thaumictinkerer:ignis_infused_potion", "thaumictinkerer:ignis_infused_grain", Aspect.AURA, 5, Aspect.FIRE, 5, null, 0);
        checkRecipe(helper, "INFUSED_POTIONSPOT2", "thaumictinkerer:terra_infused_potion", "thaumictinkerer:terra_infused_grain", Aspect.AURA, 5, Aspect.EARTH, 5, null, 0);
        checkRecipe(helper, "INFUSED_POTIONSPOT3", "thaumictinkerer:aqua_infused_potion", "thaumictinkerer:aqua_infused_grain", Aspect.AURA, 5, Aspect.WATER, 5, null, 0);
        helper.succeed();
    }

    private static void clearArea(GameTestHelper helper) {
        for (int x = 0; x < 7; x++) {
            for (int y = 1; y < 7; y++) {
                for (int z = 0; z < 7; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void allowFireSpread(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(true, level.getServer());
    }

    private static void transform(GameTestHelper helper, ImbuedFireBlock fire, Block source, Block expected) {
        clearArea(helper);
        ServerLevel level = helper.getLevel();
        BlockPos center = new BlockPos(3, 3, 3);
        helper.setBlock(center.east().below(), source == Blocks.POPPY ? Blocks.DIRT.defaultBlockState() : Blocks.STONE.defaultBlockState());
        helper.setBlock(center, fire.defaultBlockState());
        helper.setBlock(center.east(), source.defaultBlockState());
        helper.assertTrue(fire.isNeighborTarget(level, helper.absolutePos(center)), source + " is not a target of " + fire + ", east is " + helper.getBlockState(center.east()) + ", center is " + helper.getBlockState(center));
        fire.transformNeighbors(level, helper.absolutePos(center), level.getRandom());
        helper.assertTrue(helper.getBlockState(center.east()).is(expected), source + " became " + helper.getBlockState(center.east()) + " instead of " + expected);
    }

    private static void fireAir(GameTestHelper helper) {
        ImbuedFireBlock fire = ElementsFeature.AER_FIRE.get();
        transform(helper, fire, Blocks.OAK_LOG, Blocks.SAND);
        transform(helper, fire, Blocks.BIRCH_LEAVES, Blocks.SANDSTONE);
        transform(helper, fire, Blocks.ICE, Blocks.GLASS);
        transform(helper, fire, Blocks.DIRT, Blocks.SAND);
        transform(helper, fire, Blocks.GRASS_BLOCK, Blocks.SAND);
        transform(helper, fire, Blocks.WATER, Blocks.CAKE);
        helper.succeed();
    }

    private static void fireEarth(GameTestHelper helper) {
        ImbuedFireBlock fire = ElementsFeature.TERRA_FIRE.get();
        transform(helper, fire, Blocks.SAND, Blocks.DIRT);
        transform(helper, fire, Blocks.GRAVEL, Blocks.CLAY);
        transform(helper, fire, Blocks.NETHER_BRICKS, Blocks.OAK_PLANKS);
        transform(helper, fire, Blocks.CACTUS, Blocks.OAK_LOG);
        transform(helper, fire, Blocks.STONE, Blocks.DIRT);
        transform(helper, fire, Blocks.SPAWNER, Blocks.IRON_BLOCK);
        transform(helper, fire, Blocks.COBBLESTONE, Blocks.DIRT);
        transform(helper, fire, Blocks.GLASS, Blocks.DIRT);
        helper.succeed();
    }

    private static void fireIgnis(GameTestHelper helper) {
        ImbuedFireBlock fire = ElementsFeature.IGNIS_FIRE.get();
        transform(helper, fire, Blocks.DIRT, Blocks.NETHERRACK);
        transform(helper, fire, Blocks.SAND, Blocks.SOUL_SAND);
        transform(helper, fire, Blocks.CLAY, Blocks.GLOWSTONE);
        transform(helper, fire, Blocks.IRON_ORE, Blocks.NETHER_QUARTZ_ORE);
        transform(helper, fire, Blocks.WATER, Blocks.LAVA);
        BlockState wart = fire.transformation().result(helper.getLevel(), helper.absolutePos(new BlockPos(3, 3, 3)), Blocks.WHEAT.defaultBlockState());
        helper.assertTrue(wart != null && wart.is(Blocks.NETHER_WART), "wheat became " + wart);
        BlockState mushroom = fire.transformation().result(helper.getLevel(), helper.absolutePos(new BlockPos(3, 3, 3)), Blocks.POPPY.defaultBlockState());
        helper.assertTrue(mushroom != null && mushroom.is(Blocks.BROWN_MUSHROOM), "poppy became " + mushroom);
        helper.succeed();
    }

    private static void fireWater(GameTestHelper helper) {
        ImbuedFireBlock fire = ElementsFeature.AQUA_FIRE.get();
        transform(helper, fire, Blocks.SAND, Blocks.ICE);
        transform(helper, fire, Blocks.NETHERRACK, Blocks.SNOW_BLOCK);
        transform(helper, fire, Blocks.SOUL_SAND, Blocks.ICE);
        transform(helper, fire, Blocks.GLOWSTONE, Blocks.ICE);
        transform(helper, fire, Blocks.LAVA, Blocks.OBSIDIAN);
        helper.succeed();
    }

    private static void fireOrder(GameTestHelper helper) {
        ImbuedFireBlock fire = ElementsFeature.ORDO_FIRE.get();
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        helper.assertTrue(fire.transformation().result(level, pos, Blocks.COAL_ORE.defaultBlockState()) != null, "coal ore is not a target");
        BlockState coal = fire.transformation().result(level, pos, Blocks.COAL_ORE.defaultBlockState());
        helper.assertTrue(coal != null && coal.is(Blocks.COAL_BLOCK), "coal ore became " + coal);
        BlockState diamond = fire.transformation().result(level, pos, Blocks.DIAMOND_ORE.defaultBlockState());
        helper.assertTrue(diamond != null && diamond.is(Blocks.DIAMOND_BLOCK), "diamond ore became " + diamond);
        BlockState redstone = fire.transformation().result(level, pos, Blocks.REDSTONE_ORE.defaultBlockState());
        helper.assertTrue(redstone != null && redstone.is(Blocks.REDSTONE_BLOCK), "redstone ore became " + redstone);
        helper.assertTrue(fire.transformation().result(level, pos, Blocks.STONE.defaultBlockState()) == null, "stone is a target");
        helper.assertTrue(fire.transformation().result(level, pos, Blocks.SAND.defaultBlockState()) == null, "sand is a target");
        helper.assertTrue(fire.transformation().result(level, pos, Blocks.AIR.defaultBlockState()) == null, "air is a target");
        helper.assertTrue(fire.transformation().decayChance(level, pos, Blocks.AIR.defaultBlockState()) == 3, "decay chance without drops");
        helper.succeed();
    }

    private static void fireChaos(GameTestHelper helper) {
        ImbuedFireBlock chaos = ElementsFeature.PERDITIO_FIRE.get();
        helper.assertTrue(chaos.tickDelay() == 1, "chaos tick delay");
        for (ImbuedFireBlock other : List.of(
            ElementsFeature.AER_FIRE.get(), ElementsFeature.AQUA_FIRE.get(), ElementsFeature.TERRA_FIRE.get(), ElementsFeature.IGNIS_FIRE.get(), ElementsFeature.ORDO_FIRE.get()
        )) {
            transform(helper, chaos, other, Blocks.FIRE);
        }
        clearArea(helper);
        BlockPos center = new BlockPos(3, 3, 3);
        helper.setBlock(center, chaos.defaultBlockState());
        helper.setBlock(center.east(), Blocks.STONE.defaultBlockState());
        helper.assertTrue(!chaos.isNeighborTarget(helper.getLevel(), helper.absolutePos(center)), "chaos fire targets stone");
        helper.succeed();
    }

    private static void fireDiesWithoutTarget(GameTestHelper helper) {
        clearArea(helper);
        allowFireSpread(helper);
        ServerLevel level = helper.getLevel();
        BlockPos center = new BlockPos(3, 3, 3);
        ImbuedFireBlock fire = ElementsFeature.AER_FIRE.get();
        helper.setBlock(center, fire.defaultBlockState());
        fire.defaultBlockState().tick(level, helper.absolutePos(center), level.getRandom());
        helper.assertTrue(helper.getBlockState(center).isAir(), "fire stayed without a target neighbor");
        helper.setBlock(center, fire.defaultBlockState());
        helper.setBlock(center.below(), Blocks.DIRT.defaultBlockState());
        fire.defaultBlockState().tick(level, helper.absolutePos(center), level.getRandom());
        helper.assertTrue(helper.getBlockState(center).is(fire), "fire died next to a target");
        helper.succeed();
    }

    private static void fireSpreadsToTargetAir(GameTestHelper helper) {
        clearArea(helper);
        allowFireSpread(helper);
        ServerLevel level = helper.getLevel();
        BlockPos center = new BlockPos(3, 3, 3);
        ImbuedFireBlock fire = ElementsFeature.AER_FIRE.get();
        helper.setBlock(center, fire.defaultBlockState());
        helper.setBlock(center.below(), Blocks.DIRT.defaultBlockState());
        helper.setBlock(center.below().east(2), Blocks.DIRT.defaultBlockState());
        BlockPos spot = center.below().east();
        for (int attempt = 0; attempt < 40 && !helper.getBlockState(spot).is(fire); attempt++) {
            fire.defaultBlockState().tick(level, helper.absolutePos(center), level.getRandom());
        }
        helper.assertTrue(helper.getBlockState(spot).is(fire), "fire did not spread to the air block next to a target");
        helper.succeed();
    }

    private static void fireRespectsGameRule(GameTestHelper helper) {
        clearArea(helper);
        ServerLevel level = helper.getLevel();
        level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(false, level.getServer());
        BlockPos center = new BlockPos(3, 3, 3);
        ImbuedFireBlock fire = ElementsFeature.AER_FIRE.get();
        helper.setBlock(center, fire.defaultBlockState());
        fire.defaultBlockState().tick(level, helper.absolutePos(center), level.getRandom());
        boolean stayed = helper.getBlockState(center).is(fire);
        allowFireSpread(helper);
        helper.assertTrue(stayed, "fire ticked while fire spread is disabled");
        helper.succeed();
    }

    private static void fireItemPlacesAnywhere(GameTestHelper helper) {
        clearArea(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos floor = new BlockPos(3, 1, 3);
        helper.setBlock(floor, Blocks.STONE.defaultBlockState());
        ItemStack stack = new ItemStack(ElementsFeature.AER_FIRE_ITEM.get());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
        BlockPos clicked = helper.absolutePos(floor);
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(clicked), net.minecraft.core.Direction.UP, clicked, false);
        var result = stack.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit));
        helper.assertTrue(result.consumesAction(), "item did not place");
        helper.assertTrue(helper.getBlockState(floor.above()).is(ElementsFeature.AER_FIRE.get()), "fire not placed");
        helper.succeed();
    }

    private static Player playerAt(GameTestHelper helper, BlockPos pos) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolute = helper.absolutePos(pos);
        player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
        return player;
    }

    private static void waterEffectSolidifiesLava(GameTestHelper helper) {
        clearArea(helper);
        Player player = playerAt(helper, new BlockPos(3, 2, 3));
        BlockPos near = new BlockPos(4, 2, 3);
        BlockPos far = new BlockPos(3, 2, 6);
        helper.setBlock(near, Blocks.LAVA.defaultBlockState());
        helper.setBlock(far, Blocks.LAVA.defaultBlockState());
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        helper.assertTrue(helper.getBlockState(near).is(Blocks.LAVA), "lava changed without the effect");
        player.addEffect(new MobEffectInstance(ElementsFeature.WATER_IMBUED, 200));
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
        helper.assertTrue(helper.getBlockState(near).is(Blocks.OBSIDIAN), "nearby lava is " + helper.getBlockState(near));
        helper.assertTrue(helper.getBlockState(far).is(Blocks.LAVA), "distant lava changed");
        helper.succeed();
    }

    private static void airEffectKnocksUp(GameTestHelper helper) {
        clearArea(helper);
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE.defaultBlockState());
            }
        }
        Player player = playerAt(helper, new BlockPos(1, 2, 3));
        player.addEffect(new MobEffectInstance(ElementsFeature.AIR_IMBUED, 200));
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 2, 3));
        double startY = pig.getY();
        pig.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        helper.assertTrue(ElementsEvents.isAirHit(pig), "target was not marked");
        helper.succeedWhen(() -> helper.assertTrue(pig.getY() > startY + 0.05, "target did not rise: " + (pig.getY() - startY)));
    }

    private static void fireEffectIgnites(GameTestHelper helper) {
        clearArea(helper);
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE.defaultBlockState());
            }
        }
        Player player = playerAt(helper, new BlockPos(1, 2, 3));
        player.addEffect(new MobEffectInstance(ElementsFeature.FIRE_IMBUED, 200));
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 2, 3));
        pig.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        helper.assertTrue(ElementsEvents.isFireHit(pig), "target was not marked");
        helper.succeedWhen(() -> helper.assertTrue(pig.isOnFire() || !pig.isAlive(), "target is not burning"));
    }

    private static void earthEffectBuildsWall(GameTestHelper helper) {
        clearArea(helper);
        Player player = playerAt(helper, new BlockPos(1, 2, 3));
        player.addEffect(new MobEffectInstance(ElementsFeature.EARTH_IMBUED, 200));
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(5, 2, 3));
        ElementsEvents.hit(player, pig);
        BlockPos wallCenter = new BlockPos(3, 4, 3);
        helper.assertTrue(helper.getBlockState(wallCenter).is(ElementsFeature.FORCEFIELD.get()), "no wall at the center " + helper.getBlockState(wallCenter));
        helper.assertTrue(helper.getBlockState(wallCenter.north(2)).is(ElementsFeature.FORCEFIELD.get()), "wall is not 5 wide");
        helper.assertTrue(helper.getBlockState(wallCenter.above(2)).is(ElementsFeature.FORCEFIELD.get()), "wall is not 5 high");
        helper.assertTrue(helper.getBlockState(wallCenter.east()).isAir(), "wall is thicker than one block");
        helper.assertTrue(helper.getBlockState(new BlockPos(3, 2, 3)).is(ElementsFeature.FORCEFIELD.get()), "wall does not start at the feet");
        helper.runAfterDelay(ForcefieldBlock.LIFETIME + 5, () -> {
            helper.assertTrue(helper.getBlockState(wallCenter).isAir(), "wall did not decay");
            helper.succeed();
        });
    }

    private static void forcefieldProperties(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos, ElementsFeature.FORCEFIELD.get().defaultBlockState());
        BlockState state = helper.getBlockState(pos);
        BlockPos absolute = helper.absolutePos(pos);
        helper.assertTrue(!state.getCollisionShape(helper.getLevel(), absolute).isEmpty(), "forcefield has no collision");
        helper.assertTrue(state.canBeReplaced(), "forcefield is not replaceable");
        helper.assertTrue(state.getRenderShape() == RenderShape.INVISIBLE, "forcefield is visible");
        helper.assertTrue(!state.isSolidRender(helper.getLevel(), net.minecraft.core.BlockPos.ZERO), "forcefield is opaque");
        helper.succeed();
    }

    private static void potionsApplyEffects(GameTestHelper helper) {
        List<Object[]> cases = List.of(
            new Object[]{ElementsFeature.AER_POTION.get(), ElementsFeature.AIR_IMBUED},
            new Object[]{ElementsFeature.IGNIS_POTION.get(), ElementsFeature.FIRE_IMBUED},
            new Object[]{ElementsFeature.TERRA_POTION.get(), ElementsFeature.EARTH_IMBUED},
            new Object[]{ElementsFeature.AQUA_POTION.get(), ElementsFeature.WATER_IMBUED}
        );
        for (Object[] entry : cases) {
            Player player = playerAt(helper, new BlockPos(3, 2, 3));
            ItemStack stack = new ItemStack((net.minecraft.world.item.Item) entry[0]);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
            ItemStack remainder = stack.finishUsingItem(helper.getLevel(), player);
            var holder = (net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.effect.MobEffect, ?>) entry[1];
            MobEffectInstance effect = player.getEffect(holder);
            helper.assertTrue(effect != null && effect.getDuration() == ElementsFeature.POTION_DURATION && effect.getAmplifier() == 0, "effect of " + entry[0] + ": " + effect);
            helper.assertTrue(remainder.is(net.minecraft.world.item.Items.GLASS_BOTTLE), "remainder of " + entry[0] + ": " + remainder);
        }
        helper.succeed();
    }
}
