package thaumictinkerer.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumictinkerer.attractor.AttractorBlock;
import thaumictinkerer.attractor.AttractorBlockEntity;
import thaumictinkerer.attractor.AttractorFeature;
import thaumictinkerer.attractor.CorporealAttractorBlockEntity;
import thaumictinkerer.attractor.CorporealAttractorMenu;
import thaumictinkerer.attractor.SoulMouldItem;
import thaumictinkerer.network.MobMagnetButtonPayload;
import thaumictinkerer.research.TtResearch;

public final class AttractorGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("attractor")
        .add("research_registered", AttractorGameTests::researchRegistered)
        .add("recipes_registered", AttractorGameTests::recipesRegistered)
        .add("kinetic_pulls_when_powered", AttractorGameTests::kineticPulls)
        .add("kinetic_pushes_in_red_mode", AttractorGameTests::kineticPushes)
        .add("corporeal_filter", AttractorGameTests::corporealFilter)
        .add("corporeal_age", AttractorGameTests::corporealAge)
        .add("corporeal_persists", AttractorGameTests::corporealPersists)
        .add("button_payload_validated", AttractorGameTests::buttonPayloadValidated)
        .add("soul_mould_pattern", AttractorGameTests::soulMouldPattern);

    private AttractorGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem research = ResearchCategories.getResearch(AttractorFeature.MAGNETS);
        helper.assertTrue(research != null, "MAGNETS is not registered");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), "wrong category");
        helper.assertTrue(research.column() == -6 && research.row() == 3 && research.complexity() == 3, "wrong placement");
        helper.assertTrue(research.parents().equals(List.of("INTERFACE")), "wrong parents " + research.parents());
        helper.assertTrue(research.parentsHidden().contains("FOCUS_TELEKINESIS"), "wrong hidden parents " + research.parentsHidden());
        helper.assertTrue(research.isConcealed() && !research.isSecondary(), "wrong flags");
        helper.assertTrue(
            research.tags().getAmount(Aspect.MECHANISM) == 2 && research.tags().getAmount(Aspect.MOTION) == 1 && research.tags().getAmount(Aspect.SENSES) == 1,
            "wrong aspects"
        );
        helper.assertTrue(research.pages().size() == 5, "expected 5 pages, got " + research.pages().size());
        helper.succeed();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        ShapedArcaneRecipe magnet = (ShapedArcaneRecipe) TcRecipes.get("MAGNET");
        helper.assertTrue(magnet != null && magnet.research().equals("MAGNETS"), "MAGNET recipe");
        helper.assertTrue(magnet.aspects().getAmount(Aspect.AIR) == 20 && magnet.aspects().getAmount(Aspect.EARTH) == 15, "MAGNET aspects");
        helper.assertTrue(magnet.result().item().toString().equals("thaumictinkerer:kinetic_attractor"), "MAGNET result");
        helper.assertTrue(magnet.grid().get(1).equals(TcIngredient.item("minecraft:iron_ingot")), "MAGNET iron");
        helper.assertTrue(magnet.grid().get(7).equals(TcIngredient.item("thaumictinkerer:focus_telekinesis")), "MAGNET focus");
        ShapedArcaneRecipe mob = (ShapedArcaneRecipe) TcRecipes.get("MOB_MAGNET");
        helper.assertTrue(mob != null && mob.research().equals("MAGNETS"), "MOB_MAGNET recipe");
        helper.assertTrue(mob.result().item().toString().equals("thaumictinkerer:corporeal_attractor"), "MOB_MAGNET result");
        helper.assertTrue(mob.grid().get(1).equals(TcIngredient.tag("c:ingots/copper")) && mob.grid().get(1).exists(), "MOB_MAGNET copper");
        helper.assertTrue(mob.grid().get(5).equals(TcIngredient.item("thaumcraft:earth_shard")) && mob.grid().get(3).equals(TcIngredient.item("thaumcraft:air_shard")), "MOB_MAGNET shards");
        CrucibleRecipe crucible = (CrucibleRecipe) TcRecipes.get("MAGNETS");
        helper.assertTrue(crucible != null && crucible.research().equals("MAGNETS"), "MAGNETS crucible recipe");
        helper.assertTrue(crucible.result().item().toString().equals("thaumictinkerer:soul_mould") && crucible.result().exists(), "soul mould result");
        helper.assertTrue(crucible.aspects().getAmount(Aspect.BEAST) == 4 && crucible.aspects().getAmount(Aspect.MIND) == 8 && crucible.aspects().getAmount(Aspect.SENSES) == 8, "soul mould aspects");
        helper.succeed();
    }

    private static ItemEntity spawnItem(GameTestHelper helper, Vec3 relative) {
        Vec3 absolute = helper.absoluteVec(relative);
        ItemEntity item = new ItemEntity(helper.getLevel(), absolute.x, absolute.y, absolute.z, new ItemStack(Items.DIAMOND));
        item.setPickUpDelay(32767);
        item.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(item);
        return item;
    }

    private static void kineticPulls(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 3);
        helper.setBlock(pos, AttractorFeature.KINETIC_ATTRACTOR.get());
        ItemEntity item = spawnItem(helper, new Vec3(5.5, 1.1, 3.5));
        double start = item.getX();
        helper.runAfterDelay(10, () -> helper.assertTrue(Math.abs(item.getX() - start) < 0.01, "unpowered attractor moved the item"));
        helper.runAfterDelay(11, () -> helper.setBlock(new BlockPos(1, 2, 3), Blocks.REDSTONE_BLOCK));
        helper.runAfterDelay(21, () -> {
            helper.assertTrue(start - item.getX() > 0.8, "powered attractor did not pull the item, moved " + (start - item.getX()));
            helper.setBlock(new BlockPos(1, 2, 3), Blocks.AIR);
        });
        helper.runAfterDelay(45, () -> {
            double settled = item.getX();
            helper.runAfterDelay(10, () -> {
                helper.assertTrue(Math.abs(item.getX() - settled) < 0.05, "attractor kept pulling after losing power");
                helper.succeed();
            });
        });
    }

    private static void kineticPushes(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 3);
        helper.setBlock(pos, AttractorFeature.KINETIC_ATTRACTOR.get().defaultBlockState().setValue(AttractorBlock.PULL, false));
        helper.setBlock(new BlockPos(1, 2, 3), Blocks.REDSTONE_BLOCK);
        ItemEntity item = spawnItem(helper, new Vec3(2.5, 1.1, 3.5));
        double start = item.getX();
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(item.getX() - start > 0.8, "red attractor did not push the item, moved " + (item.getX() - start));
            helper.succeed();
        });
    }

    private static CorporealAttractorBlockEntity corporeal(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, AttractorFeature.CORPOREAL_ATTRACTOR.get());
        return (CorporealAttractorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static ItemStack mould(String pattern) {
        ItemStack stack = new ItemStack(AttractorFeature.SOUL_MOULD.get());
        if (!pattern.isEmpty()) {
            AttractorFeature.SOUL_PATTERN.set(stack, pattern);
        }
        return stack;
    }

    private static void corporealFilter(GameTestHelper helper) {
        CorporealAttractorBlockEntity attractor = corporeal(helper, new BlockPos(2, 1, 2));
        Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 2));
        var cow = helper.spawnWithNoFreeWill(EntityTypes.COW, new BlockPos(4, 1, 3));
        var item = spawnItem(helper, new Vec3(4.5, 1.1, 1.5));
        helper.assertTrue(attractor.applies(pig) && attractor.applies(cow), "empty filter should accept all adults");
        helper.assertTrue(!attractor.applies(item), "corporeal attractor must not take items");
        attractor.setItem(0, mould("minecraft:pig"));
        helper.assertTrue(attractor.applies(pig) && !attractor.applies(cow), "pig filter");
        attractor.setItem(0, mould(""));
        helper.assertTrue(!attractor.applies(pig) && !attractor.applies(cow), "blank mould accepts nothing");
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        attractor.setItem(0, ItemStack.EMPTY);
        helper.assertTrue(!attractor.applies(player), "players must be ignored");
        helper.succeed();
    }

    private static void corporealAge(GameTestHelper helper) {
        CorporealAttractorBlockEntity attractor = corporeal(helper, new BlockPos(2, 1, 2));
        Pig adult = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 2));
        Pig child = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 3));
        child.setBaby(true);
        helper.assertTrue(attractor.applies(adult) && !attractor.applies(child), "adult mode");
        attractor.setAdult(false);
        helper.assertTrue(!attractor.applies(adult) && attractor.applies(child), "child mode");
        helper.succeed();
    }

    private static void corporealPersists(GameTestHelper helper) {
        CorporealAttractorBlockEntity attractor = corporeal(helper, new BlockPos(2, 1, 2));
        attractor.setAdult(false);
        attractor.setItem(0, mould("minecraft:cow"));
        CompoundTag saved = attractor.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity loaded = BlockEntity.loadStatic(attractor.getBlockPos(), attractor.getBlockState(), saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded instanceof CorporealAttractorBlockEntity, "block entity did not load");
        CorporealAttractorBlockEntity copy = (CorporealAttractorBlockEntity) loaded;
        helper.assertTrue(!copy.isAdult(), "adult flag lost");
        helper.assertTrue(SoulMouldItem.getPatternName(copy.filter()).equals("minecraft:cow"), "filter lost");
        helper.succeed();
    }

    private static void buttonPayloadValidated(GameTestHelper helper) {
        CorporealAttractorBlockEntity attractor = corporeal(helper, new BlockPos(2, 1, 2));
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        MobMagnetButtonPayload.handle(new MobMagnetButtonPayload(false), player);
        helper.assertTrue(attractor.isAdult(), "payload without an open menu changed the attractor");
        player.setPos(helper.absoluteVec(new Vec3(2.5, 1.0, 2.5)));
        player.containerMenu = new CorporealAttractorMenu(7, player.getInventory(), attractor);
        MobMagnetButtonPayload.handle(new MobMagnetButtonPayload(false), player);
        helper.assertTrue(!attractor.isAdult(), "payload did not apply");
        CorporealAttractorMenu menu = (CorporealAttractorMenu) player.containerMenu;
        helper.assertTrue(!menu.getSlot(0).mayPlace(new ItemStack(Items.DIAMOND)) && menu.getSlot(0).mayPlace(mould("")), "slot filter");
        player.setPos(helper.absoluteVec(new Vec3(40.0, 1.0, 40.0)));
        MobMagnetButtonPayload.handle(new MobMagnetButtonPayload(true), player);
        helper.assertTrue(!attractor.isAdult(), "distant player changed the attractor");
        helper.succeed();
    }

    private static void soulMouldPattern(GameTestHelper helper) {
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(2, 1, 2));
        ItemStack stack = mould("");
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.getItem().interactLivingEntity(stack, player, pig, InteractionHand.MAIN_HAND);
        helper.assertTrue(SoulMouldItem.getPatternName(stack).equals("minecraft:pig"), "pattern not stored: " + SoulMouldItem.getPatternName(stack));
        stack.getItem().interactLivingEntity(stack, player, player, InteractionHand.MAIN_HAND);
        helper.assertTrue(SoulMouldItem.getPatternName(stack).equals("minecraft:pig"), "player changed the pattern");
        player.setShiftKeyDown(true);
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(SoulMouldItem.getPatternName(stack).isEmpty(), "sneak click did not clear the pattern");
        helper.succeed();
    }
}
