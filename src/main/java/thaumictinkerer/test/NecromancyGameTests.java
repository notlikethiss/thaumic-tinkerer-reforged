package thaumictinkerer.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.CraftingRecipeRef;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModBlocks;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumictinkerer.gear.BloodSwordEvents;
import thaumictinkerer.gear.GearFeature;
import thaumictinkerer.gear.SoulDrops;
import thaumictinkerer.necromancy.MobAspect;
import thaumictinkerer.necromancy.NecromancyFeature;
import thaumictinkerer.necromancy.NecromancyTabletBlockEntity;
import thaumictinkerer.necromancy.SoulAspectItem;
import thaumictinkerer.research.TtResearch;
import thaumcraft.infusion.PedestalBlockEntity;

public final class NecromancyGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("necromancy")
        .add("research_registered", NecromancyGameTests::researchRegistered)
        .add("recipes_registered", NecromancyGameTests::recipesRegistered)
        .add("items_have_aspects", NecromancyGameTests::itemsHaveAspects)
        .add("mob_table", NecromancyGameTests::mobTable)
        .add("tablet_summons_and_consumes", NecromancyGameTests::tabletSummonsAndConsumes)
        .add("tablet_ignores_unknown_combination", NecromancyGameTests::tabletIgnoresUnknown)
        .add("tablet_infused_keeps_souls", NecromancyGameTests::tabletInfusedKeepsSouls)
        .add("tablet_respects_redstone", NecromancyGameTests::tabletRespectsRedstone)
        .add("blood_sword_drops_soul_aspects", NecromancyGameTests::bloodSwordDropsSouls);

    private static final BlockPos TABLET = new BlockPos(2, 1, 2);
    private static final BlockPos[] PEDESTALS = {new BlockPos(1, 1, 1), new BlockPos(1, 1, 2), new BlockPos(2, 1, 1)};

    private NecromancyGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void researchRegistered(GameTestHelper helper) {
        ResearchItem research = ResearchCategories.getResearch(NecromancyFeature.SUMMON);
        helper.assertTrue(research != null, "SUMMON is not registered");
        helper.assertTrue(TtResearch.TT_CATEGORY.equals(research.category()), "wrong category");
        helper.assertTrue(research.column() == -5 && research.row() == 8, "wrong position");
        helper.assertTrue(research.warp() == 3, "wrong warp " + research.warp());
        helper.assertTrue(!research.isSecondary() && !research.isConcealed(), "wrong flags");
        helper.assertTrue(research.parents().equals(List.of(GearFeature.BLOOD_SWORD_RESEARCH)), "wrong parents " + research.parents());
        helper.assertTrue(
            research.tags().getAmount(Aspect.WEAPON) == 1 && research.tags().getAmount(Aspect.BEAST) == 3 && research.tags().getAmount(Aspect.MAGIC) == 3,
            "wrong aspects"
        );
        helper.assertTrue(research.pages().size() == 5, "wrong page count " + research.pages().size());
        helper.succeed();
    }

    private static void recipesRegistered(GameTestHelper helper) {
        ShapedArcaneRecipe arcane = (ShapedArcaneRecipe) TcRecipes.get(NecromancyFeature.SUMMON_TABLET);
        helper.assertTrue(arcane != null && arcane.research().equals(NecromancyFeature.SUMMON), "tablet recipe missing");
        helper.assertTrue(arcane.aspects().getAmount(Aspect.ORDER) == 50 && arcane.aspects().getAmount(Aspect.ENTROPY) == 50, "tablet aspects");
        helper.assertTrue(arcane.width() == 3 && arcane.height() == 2, "tablet shape");
        helper.assertTrue(arcane.result().item().toString().equals("thaumictinkerer:necromancy_tablet"), "tablet result");
        for (Aspect aspect : NecromancyFeature.ASPECTS) {
            String tag = aspect.getTag();
            CraftingRecipeRef condensed = (CraftingRecipeRef) TcRecipes.get(NecromancyFeature.CONDENSE_PREFIX + tag.toUpperCase());
            helper.assertTrue(condensed != null && condensed.research().equals(NecromancyFeature.SUMMON), "condensed recipe missing for " + tag);
            helper.assertTrue(condensed.result().item().toString().equals("thaumictinkerer:condensed_" + tag + "_soul_aspect"), "condensed result for " + tag);
            helper.assertTrue(
                helper.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, condensed.recipe())).isPresent(),
                "crafting recipe missing in datapack for " + tag
            );
            InfusionRecipe infusion = (InfusionRecipe) TcRecipes.get(NecromancyFeature.INFUSE_PREFIX + tag.toUpperCase());
            helper.assertTrue(infusion != null && infusion.research().equals(NecromancyFeature.SUMMON), "infusion recipe missing for " + tag);
            helper.assertTrue(infusion.instability() == 4 && infusion.components().size() == 8, "infusion shape for " + tag);
            helper.assertTrue(infusion.aspects().getAmount(aspect) == 10 && infusion.aspects().size() == 1, "infusion aspects for " + tag);
            helper.assertTrue(infusion.result().item().toString().equals("thaumictinkerer:infused_" + tag + "_soul_aspect"), "infusion result for " + tag);
        }
        helper.succeed();
    }

    private static void itemsHaveAspects(GameTestHelper helper) {
        helper.assertValueEqual(NecromancyFeature.ASPECTS.size(), 17, "aspect count");
        for (int index = 0; index < NecromancyFeature.ASPECT_COUNT; index++) {
            Aspect aspect = NecromancyFeature.ASPECTS.get(index);
            SoulAspectItem base = NecromancyFeature.BASE.get(index).get();
            helper.assertTrue(base.aspect() == aspect && base.form() == SoulAspectItem.Form.BASE, "base item for " + aspect.getTag());
            helper.assertTrue(NecromancyFeature.CONDENSED.get(index).get().aspect() == aspect, "condensed item for " + aspect.getTag());
            ItemStack infused = new ItemStack(NecromancyFeature.INFUSED.get(index).get());
            helper.assertTrue(SoulAspectItem.isInfused(infused) && infused.hasFoil(), "infused item for " + aspect.getTag());
            helper.assertValueEqual(infused.getMaxStackSize(), 16, "stack size");
            AspectList tags = AspectRegistry.active().getObjectTags(base);
            helper.assertTrue(tags != null && tags.getAmount(aspect) == 8, "base item aspects for " + aspect.getTag());
        }
        helper.succeed();
    }

    private static void mobTable(GameTestHelper helper) {
        helper.assertValueEqual(MobAspect.ALL.size(), 29, "mob count");
        for (MobAspect mob : MobAspect.ALL) {
            helper.assertTrue(mob.entityType().get() != null, "entity type for " + mob.name());
            helper.assertValueEqual(mob.aspects().size(), 3, "aspect count for " + mob.name());
            for (Aspect aspect : mob.aspects()) {
                helper.assertTrue(NecromancyFeature.ASPECTS.contains(aspect), mob.name() + " uses a non soul aspect " + aspect.getTag());
            }
        }
        helper.assertTrue(MobAspect.forAspects(List.of(Aspect.WATER, Aspect.WATER, Aspect.MAN)).name().equals("SnowMan"), "snow golem recipe");
        helper.assertTrue(MobAspect.forAspects(List.of(Aspect.MAN, Aspect.WATER, Aspect.WATER)).name().equals("SnowMan"), "recipe order is irrelevant");
        helper.assertTrue(MobAspect.forAspects(List.of(Aspect.MAGIC, Aspect.UNDEAD, Aspect.FLESH)).name().equals("BrainyZombie"), "brainy zombie recipe");
        helper.assertTrue(MobAspect.forAspects(List.of(Aspect.AIR, Aspect.MAGIC, Aspect.MAGIC)).name().equals("Wisp"), "wisp recipe");
        helper.assertTrue(MobAspect.forAspects(List.of(Aspect.FLIGHT, Aspect.FIRE, Aspect.MAGIC)).name().equals("Firebat"), "fire bat recipe");
        helper.assertTrue(MobAspect.forAspects(List.of(Aspect.WATER, Aspect.FIRE, Aspect.EARTH)) == null, "unknown combination");
        helper.succeed();
    }

    private static NecromancyTabletBlockEntity placeTablet(GameTestHelper helper) {
        helper.setBlock(TABLET, NecromancyFeature.TABLET.get().defaultBlockState());
        return (NecromancyTabletBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(TABLET));
    }

    private static PedestalBlockEntity[] placePedestals(GameTestHelper helper, ItemStack... stacks) {
        PedestalBlockEntity[] pedestals = new PedestalBlockEntity[stacks.length];
        for (int index = 0; index < stacks.length; index++) {
            helper.setBlock(PEDESTALS[index], ModBlocks.ARCANE_PEDESTAL.get().defaultBlockState());
            pedestals[index] = (PedestalBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(PEDESTALS[index]));
            pedestals[index].setStack(stacks[index]);
        }
        return pedestals;
    }

    private static <T extends Entity> List<T> around(GameTestHelper helper, Class<T> type) {
        return helper.getLevel().getEntitiesOfClass(type, new AABB(helper.absolutePos(TABLET)).inflate(3.0));
    }

    private static void tabletSummonsAndConsumes(GameTestHelper helper) {
        NecromancyTabletBlockEntity tablet = placeTablet(helper);
        PedestalBlockEntity[] pedestals = placePedestals(
            helper,
            NecromancyFeature.soulStack(Aspect.WATER),
            NecromancyFeature.soulStack(Aspect.MAN),
            NecromancyFeature.soulStack(Aspect.WATER)
        );
        helper.assertTrue(tablet.attempt(helper.getLevel(), 300), "tablet found no recipe");
        helper.assertValueEqual(around(helper, SnowGolem.class).size(), 1, "snow golems summoned");
        for (PedestalBlockEntity pedestal : pedestals) {
            helper.assertTrue(pedestal.getStack().isEmpty(), "soul aspects were not consumed");
        }
        helper.assertTrue(!tablet.attempt(helper.getLevel(), 600), "empty pedestals must do nothing");
        around(helper, SnowGolem.class).forEach(Entity::discard);
        helper.succeed();
    }

    private static void tabletIgnoresUnknown(GameTestHelper helper) {
        NecromancyTabletBlockEntity tablet = placeTablet(helper);
        PedestalBlockEntity[] pedestals = placePedestals(
            helper,
            NecromancyFeature.soulStack(Aspect.WATER),
            NecromancyFeature.soulStack(Aspect.FIRE),
            NecromancyFeature.soulStack(Aspect.EARTH)
        );
        helper.assertTrue(!tablet.attempt(helper.getLevel(), 300), "unknown combination must not summon");
        for (PedestalBlockEntity pedestal : pedestals) {
            helper.assertTrue(!pedestal.getStack().isEmpty(), "souls must stay on the pedestals");
        }
        helper.succeed();
    }

    private static void tabletInfusedKeepsSouls(GameTestHelper helper) {
        NecromancyTabletBlockEntity tablet = placeTablet(helper);
        PedestalBlockEntity[] pedestals = placePedestals(
            helper,
            NecromancyFeature.infusedStack(Aspect.FLESH),
            NecromancyFeature.infusedStack(Aspect.UNDEAD),
            NecromancyFeature.infusedStack(Aspect.FLESH)
        );
        helper.assertTrue(!tablet.attempt(helper.getLevel(), 300), "infused souls only work every 1200 ticks");
        helper.assertValueEqual(around(helper, Zombie.class).size(), 0, "zombies before the slow tick");
        helper.assertTrue(tablet.attempt(helper.getLevel(), 1200), "infused recipe was not found");
        helper.assertValueEqual(around(helper, Zombie.class).size(), 1, "zombies summoned");
        BlockPos absolute = helper.absolutePos(TABLET);
        for (PedestalBlockEntity pedestal : pedestals) {
            helper.assertTrue(SoulAspectItem.isInfused(pedestal.getStack()), "infused souls must stay");
            helper.assertTrue(absolute.equals(NecromancyFeature.LAST_TABLET.get(pedestal.getStack())), "souls must remember the tablet");
        }
        helper.assertTrue(tablet.attempt(helper.getLevel(), 2400), "second infused summon");
        helper.assertValueEqual(around(helper, Zombie.class).size(), 2, "zombies after second summon");
        for (PedestalBlockEntity pedestal : pedestals) {
            ItemStack moved = pedestal.getStack().copy();
            NecromancyFeature.LAST_TABLET.set(moved, absolute.east(20));
            pedestal.setStack(moved);
        }
        helper.assertTrue(tablet.attempt(helper.getLevel(), 3600), "foreign souls still match the recipe");
        helper.assertValueEqual(around(helper, Zombie.class).size(), 2, "souls bound to another tablet must not summon");
        helper.assertTrue(absolute.equals(NecromancyFeature.LAST_TABLET.get(pedestals[0].getStack())), "souls rebind to this tablet");
        around(helper, Zombie.class).forEach(Entity::discard);
        helper.succeed();
    }

    private static void tabletRespectsRedstone(GameTestHelper helper) {
        NecromancyTabletBlockEntity tablet = placeTablet(helper);
        PedestalBlockEntity[] pedestals = placePedestals(
            helper,
            NecromancyFeature.soulStack(Aspect.WATER),
            NecromancyFeature.soulStack(Aspect.WATER),
            NecromancyFeature.soulStack(Aspect.WATER)
        );
        helper.setBlock(new BlockPos(3, 1, 2), net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK);
        helper.assertTrue(!tablet.attempt(helper.getLevel(), 300), "powered tablet must not summon");
        helper.assertTrue(!pedestals[0].getStack().isEmpty(), "powered tablet must not consume");
        helper.setBlock(new BlockPos(3, 1, 2), net.minecraft.world.level.block.Blocks.AIR);
        helper.assertTrue(tablet.attempt(helper.getLevel(), 300), "unpowered tablet summons");
        helper.assertValueEqual(around(helper, net.minecraft.world.entity.animal.squid.Squid.class).size(), 1, "squids summoned");
        around(helper, net.minecraft.world.entity.animal.squid.Squid.class).forEach(Entity::discard);
        helper.succeed();
    }

    private static void bloodSwordDropsSouls(GameTestHelper helper) {
        BloodSwordEvents.reset();
        ServerLevel level = helper.getLevel();
        ServerPlayer player = GearGameTests.mockPlayer(helper);
        ItemStack sword = new ItemStack(GearFeature.BLOOD_SWORD.get());
        GearFeature.ACTIVE.set(sword, true);
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        Zombie zombie = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
        zombie.setPos(helper.absoluteVec(new Vec3(2.5, 2.0, 2.5)));
        level.addFreshEntity(zombie);
        zombie.hurtServer(level, level.damageSources().playerAttack(player), 1000.0F);
        int flesh = 0;
        int undead = 0;
        int others = 0;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(zombie.blockPosition()).inflate(3.0))) {
            ItemStack stack = entity.getItem();
            if (stack.is(NecromancyFeature.BASE.get(NecromancyFeature.ASPECTS.indexOf(Aspect.FLESH)).get())) {
                flesh += stack.getCount();
            } else if (stack.is(NecromancyFeature.BASE.get(NecromancyFeature.ASPECTS.indexOf(Aspect.UNDEAD)).get())) {
                undead += stack.getCount();
            } else {
                others += stack.getCount();
            }
            entity.discard();
        }
        helper.assertValueEqual(flesh, 2, "flesh soul aspects dropped");
        helper.assertValueEqual(undead, 1, "undead soul aspects dropped");
        helper.assertValueEqual(others, 0, "vanilla drops must be replaced");
        helper.assertTrue(SoulDrops.soul(Aspect.FLESH).is(NecromancyFeature.BASE.get(7).get()), "soul lookup");
        BloodSwordEvents.reset();
        helper.succeed();
    }
}
