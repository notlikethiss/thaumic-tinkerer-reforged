package thaumictinkerer.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumictinkerer.mobilizer.MobilizerBlockEntity;
import thaumictinkerer.mobilizer.MobilizerFeature;
import thaumictinkerer.mobilizer.RelayBlockEntity;
import thaumictinkerer.research.TtResearch;

public final class MobilizerGameTests {
    private static final BlockPos FIRST_RELAY = new BlockPos(0, 2, 3);
    private static final BlockPos SECOND_RELAY = new BlockPos(6, 2, 3);
    private static final BlockPos MOBILIZER = new BlockPos(1, 2, 3);

    private static final TtGameTestSuite SUITE = new TtGameTestSuite("mobilizer")
        .add("research_registered", MobilizerGameTests::researchRegistered)
        .add("recipes_registered", MobilizerGameTests::recipesRegistered)
        .add("relays_pair_and_link", MobilizerGameTests::relaysPairAndLink)
        .add("moves_chest_without_duplication", MobilizerGameTests::movesChest)
        .add("leaves_unbreakable_blocks", MobilizerGameTests::leavesUnbreakable)
        .add("bounces_at_obstacle", MobilizerGameTests::bounces)
        .add("persists", MobilizerGameTests::persists);

    private MobilizerGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem research = ResearchCategories.getResearch("LEVITATOR_LOCOMOTIVE");
        helper.assertTrue(research != null && TtResearch.TT_CATEGORY.equals(research.category()), "research missing");
        helper.assertTrue(research.column() == -7 && research.row() == 5 && research.complexity() == 3, "position");
        helper.assertTrue(research.parents().equals(List.of("MAGNETS")), "parents " + research.parents());
        helper.assertTrue(research.isSecondary() && !research.isConcealed() && !research.isRound(), "flags");
        helper.assertTrue(research.tags().getAmount(Aspect.MOTION) == 2 && research.tags().getAmount(Aspect.ORDER) == 2, "aspects");
        helper.assertTrue(
            research.pages().size() == 3 && research.pages().get(1) instanceof ResearchPage.Infusion && research.pages().get(2) instanceof ResearchPage.Arcane,
            "pages"
        );
        helper.succeed();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        helper.assertTrue(
            TcRecipes.get("LEVITATOR_LOCOMOTIVE") instanceof InfusionRecipe recipe
                && recipe.instability() == 4
                && recipe.components().size() == 4
                && recipe.aspects().getAmount(Aspect.ORDER) == 20,
            "infusion recipe"
        );
        helper.assertTrue(
            TcRecipes.get("LEVITATOR_RELAY") instanceof ShapedArcaneRecipe recipe
                && recipe.width() == 3
                && recipe.height() == 3
                && recipe.research().equals("LEVITATOR_LOCOMOTIVE")
                && recipe.result().exists(),
            "arcane recipe"
        );
        helper.succeed();
    }

    private static void clear(GameTestHelper helper) {
        for (int x = 0; x < 7; x++) {
            for (int y = 1; y < 7; y++) {
                for (int z = 0; z < 7; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static RelayBlockEntity relay(GameTestHelper helper, BlockPos pos) {
        return (RelayBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static MobilizerBlockEntity mobilizer(GameTestHelper helper, BlockPos pos) {
        BlockEntity entity = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        helper.assertTrue(entity instanceof MobilizerBlockEntity, "no mobilizer at " + pos);
        return (MobilizerBlockEntity) entity;
    }

    private static MobilizerBlockEntity setup(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(FIRST_RELAY, MobilizerFeature.RELAY.get().defaultBlockState());
        helper.setBlock(SECOND_RELAY, MobilizerFeature.RELAY.get().defaultBlockState());
        helper.setBlock(MOBILIZER, MobilizerFeature.MOBILIZER.get().defaultBlockState());
        relay(helper, FIRST_RELAY).checkForPartner();
        relay(helper, FIRST_RELAY).linkMobilizers(helper.getLevel());
        return mobilizer(helper, MOBILIZER);
    }

    private static void relaysPairAndLink(GameTestHelper helper) {
        MobilizerBlockEntity mobilizer = setup(helper);
        RelayBlockEntity first = relay(helper, FIRST_RELAY);
        RelayBlockEntity second = relay(helper, SECOND_RELAY);
        helper.assertTrue(first.hasPartner() && second.hasPartner(), "relays are not paired");
        helper.assertTrue(first.partnerX() == helper.absolutePos(SECOND_RELAY).getX() && second.partnerX() == helper.absolutePos(FIRST_RELAY).getX(), "partner coordinates");
        helper.assertTrue(mobilizer.isLinked() && mobilizer.movementDirection() == net.minecraft.core.Direction.EAST, "mobilizer link");
        helper.succeed();
    }

    private static void movesChest(GameTestHelper helper) {
        MobilizerBlockEntity mobilizer = setup(helper);
        BlockPos chestPos = MOBILIZER.above();
        helper.setBlock(chestPos, Blocks.CHEST.defaultBlockState());
        ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(chestPos));
        chest.setItem(0, new ItemStack(Items.DIAMOND, 5));
        chest.setItem(13, new ItemStack(Items.APPLE, 3));
        helper.assertTrue(mobilizer.move(helper.getLevel()), "mobilizer did not move");
        BlockPos newMobilizer = MOBILIZER.east();
        BlockPos newChest = chestPos.east();
        helper.assertTrue(helper.getBlockState(MOBILIZER).isAir() && helper.getBlockState(chestPos).isAir(), "old blocks remain");
        helper.assertTrue(helper.getBlockState(newMobilizer).is(MobilizerFeature.MOBILIZER.get()), "mobilizer not moved");
        helper.assertTrue(helper.getBlockState(newChest).is(Blocks.CHEST), "chest not moved");
        ChestBlockEntity moved = (ChestBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(newChest));
        helper.assertTrue(moved.getItem(0).is(Items.DIAMOND) && moved.getItem(0).getCount() == 5, "diamonds lost");
        helper.assertTrue(moved.getItem(13).is(Items.APPLE) && moved.getItem(13).getCount() == 3, "apples lost");
        int total = 0;
        for (int slot = 0; slot < moved.getContainerSize(); slot++) {
            total += moved.getItem(slot).getCount();
        }
        helper.assertTrue(total == 8, "item total " + total);
        ServerLevel level = helper.getLevel();
        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, AABB.encapsulatingFullBlocks(helper.absolutePos(new BlockPos(0, 1, 2)), helper.absolutePos(new BlockPos(7, 6, 5))));
        helper.assertTrue(drops.isEmpty(), "items dropped: " + drops.size());
        MobilizerBlockEntity next = mobilizer(helper, newMobilizer);
        helper.assertTrue(next.isLinked() && next.movementDirection() == net.minecraft.core.Direction.EAST, "link state lost");
        helper.succeed();
    }

    private static void leavesUnbreakable(GameTestHelper helper) {
        MobilizerBlockEntity mobilizer = setup(helper);
        helper.setBlock(MOBILIZER.above(), Blocks.BEDROCK.defaultBlockState());
        helper.assertTrue(mobilizer.move(helper.getLevel()), "mobilizer did not move");
        helper.assertTrue(helper.getBlockState(MOBILIZER.above()).is(Blocks.BEDROCK), "bedrock was removed");
        helper.assertTrue(helper.getBlockState(MOBILIZER.above().east()).isAir(), "bedrock was duplicated");
        helper.succeed();
    }

    private static void bounces(GameTestHelper helper) {
        MobilizerBlockEntity mobilizer = setup(helper);
        helper.setBlock(MOBILIZER.east(), Blocks.STONE.defaultBlockState());
        mobilizer.bounce(helper.getLevel());
        helper.assertTrue(mobilizer.movementDirection() == net.minecraft.core.Direction.WEST, "direction " + mobilizer.movementDirection());
        helper.assertTrue(helper.getBlockState(MOBILIZER).is(MobilizerFeature.MOBILIZER.get()), "mobilizer moved while blocked");
        helper.succeed();
    }

    private static void persists(GameTestHelper helper) {
        MobilizerBlockEntity mobilizer = setup(helper);
        CompoundTag saved = mobilizer.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity loaded = BlockEntity.loadStatic(mobilizer.getBlockPos(), mobilizer.getBlockState(), saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded instanceof MobilizerBlockEntity, "did not load");
        MobilizerBlockEntity copy = (MobilizerBlockEntity) loaded;
        helper.assertTrue(copy.isLinked() && copy.movementDirection() == mobilizer.movementDirection(), "mobilizer state");
        helper.assertTrue(copy.firstRelayX() == mobilizer.firstRelayX() && copy.secondRelayZ() == mobilizer.secondRelayZ(), "relay coordinates");
        CompoundTag relaySaved = relay(helper, FIRST_RELAY).saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity relayLoaded = BlockEntity.loadStatic(helper.absolutePos(FIRST_RELAY), helper.getBlockState(FIRST_RELAY), relaySaved, helper.getLevel().registryAccess());
        helper.assertTrue(relayLoaded instanceof RelayBlockEntity relay && relay.hasPartner() && relay.partnerX() == relay(helper, FIRST_RELAY).partnerX(), "relay state");
        helper.succeed();
    }
}
