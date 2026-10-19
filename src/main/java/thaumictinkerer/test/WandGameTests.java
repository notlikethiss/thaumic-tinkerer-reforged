package thaumictinkerer.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.arcane.ArcaneWorkbenchBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.ArcaneCrafting;
import thaumcraft.menu.ArcaneWorkbenchMenu;
import thaumcraft.registry.ModBlocks;
import thaumcraft.research.Knowledge;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;
import thaumictinkerer.Config;
import thaumictinkerer.kami.WandVariant;
import thaumictinkerer.registry.ModCreativeTabs;

public final class WandGameTests {
    private static final TtGameTestSuite SUITE = new TtGameTestSuite("wands")
        .add("variants", WandGameTests::variants)
        .add("creative_and_kami", WandGameTests::creativeAndKami)
        .add("silverwood_workbench", helper -> workbench(helper, WandVariant.SILVERWOOD))
        .add("ichorcloth_workbench", helper -> workbench(helper, WandVariant.ICHORCLOTH));

    private WandGameTests() {
    }

    public static void register(IEventBus bus) {
        SUITE.register(bus);
    }

    private static void variants(GameTestHelper helper) {
        ServerPlayer player = GearGameTests.mockPlayer(helper);
        for (WandVariant variant : WandVariant.values()) {
            ItemStack result = variant.result();
            int capacity = variant == WandVariant.SILVERWOOD ? 10000 : 100000;
            int cost = variant == WandVariant.SILVERWOOD ? 54 : 100;
            String cap = variant == WandVariant.SILVERWOOD ? "thaumium" : "ICHOR";
            String rod = variant == WandVariant.SILVERWOOD ? "silverwood" : "ICHORCLOTH";
            helper.assertTrue(Wands.isWand(result) && !Wands.isStaff(result) && !Wands.isSceptre(result), "not a casting wand: " + variant);
            helper.assertValueEqual(Wands.getCap(result).tag(), cap, "cap");
            helper.assertValueEqual(Wands.getRod(result).tag(), rod, "rod");
            helper.assertValueEqual(Wands.getMaxVis(result), capacity, "capacity");
            List<ItemStack> grid = variant.grid();
            helper.assertTrue(grid.get(6).is(grid.get(2).getItem()), "caps differ");
            helper.assertValueEqual(grid.stream().filter(stack -> !stack.isEmpty()).count(), 3L, "ingredient count");
            Knowledge.get(player).addResearch(variant.cap().research());
            Knowledge.get(player).addResearch(variant.rod().research());
            helper.assertTrue(ItemStack.isSameItemSameComponents(ArcaneCrafting.findResult(grid, player), result), "display result differs from dynamic recipe");
            var matched = ArcaneCrafting.findAspects(grid, player);
            helper.assertValueEqual(matched.size(), 6, "primal aspect count");
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                helper.assertValueEqual(Wands.getVis(result, aspect), 0, "crafted wand must be empty");
                helper.assertValueEqual(variant.aspects().getAmount(aspect), cost, "display cost");
                helper.assertValueEqual(matched.getAmount(aspect), cost, "dynamic cost");
            }
            ItemStack creative = variant.creativeStack();
            helper.assertValueEqual(Wands.getParts(creative), Wands.getParts(result), "charge changes wand identity");
            Wands.setFocus(creative, new ItemStack(thaumcraft.registry.ModItems.FOCUS_FIRE.get()));
            helper.assertValueEqual(Wands.getParts(creative), Wands.getParts(result), "focus changes wand identity");
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                helper.assertValueEqual(Wands.getVis(creative, aspect), capacity, "creative wand must be full");
            }
        }
        helper.succeed();
    }

    private static void creativeAndKami(GameTestHelper helper) {
        helper.assertTrue(Config.SPEC.isLoaded(), "config must be loaded");
        boolean before = Config.kamiEnabled();
        var tab = ModCreativeTabs.THAUMIC_TINKERER.get();
        var parameters = new CreativeModeTab.ItemDisplayParameters(helper.getLevel().enabledFeatures(), false, helper.getLevel().registryAccess());
        try {
            for (boolean enabled : new boolean[]{false, true}) {
                Config.KAMI_ENABLED.set(enabled);
                tab.buildContents(parameters);
                List<ItemStack> wands = tab.getDisplayItems().stream().filter(Wands::isWand).toList();
                helper.assertValueEqual(wands.size(), enabled ? 2 : 1, "creative wand count");
                for (WandVariant variant : WandVariant.values()) {
                    helper.assertTrue(variant.available() == (variant == WandVariant.SILVERWOOD || enabled), "variant visibility");
                    long count = wands.stream().filter(stack -> ItemStack.isSameItemSameComponents(stack, variant.creativeStack())).count();
                    helper.assertValueEqual(count, variant.available() ? 1L : 0L, "creative variant count");
                }
            }
        } finally {
            Config.KAMI_ENABLED.set(before);
            tab.buildContents(parameters);
        }
        helper.succeed();
    }

    private static void workbench(GameTestHelper helper, WandVariant variant) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.ARCANE_WORKTABLE.get());
        ArcaneWorkbenchBlockEntity table = helper.getBlockEntity(pos);
        ServerPlayer player = GearGameTests.mockPlayer(helper);
        ItemStack source = WandVariant.SILVERWOOD.creativeStack();
        table.setItem(ArcaneWorkbenchBlockEntity.WAND_SLOT, source);
        List<ItemStack> grid = variant.grid();
        for (int slot = 0; slot < grid.size(); slot++) {
            table.setItem(slot, grid.get(slot).copy());
        }
        ArcaneWorkbenchMenu menu = new ArcaneWorkbenchMenu(1, player.getInventory(), helper.absolutePos(pos));
        try {
            helper.assertTrue(menu.getSlot(ArcaneWorkbenchMenu.RESULT_SLOT).getItem().isEmpty(), "crafted without research");
            Knowledge.get(player).addResearch(variant.cap().research());
            menu.onCraftMatrixChanged();
            helper.assertTrue(menu.getSlot(ArcaneWorkbenchMenu.RESULT_SLOT).getItem().isEmpty(), "crafted without rod research");
            Knowledge.get(player).addResearch(variant.rod().research());
            menu.onCraftMatrixChanged();
            helper.assertTrue(ItemStack.isSameItemSameComponents(menu.getSlot(ArcaneWorkbenchMenu.RESULT_SLOT).getItem(), variant.result()), "result missing with research and charge");
            var aspects = variant.aspects();
            WandItem sourceItem = (WandItem) source.getItem();
            int fireCost = (int) (aspects.getAmount(Aspect.FIRE) * 100 * sourceItem.getConsumptionModifier(source, player, Aspect.FIRE, true));
            Wands.storeVis(source, Aspect.FIRE, fireCost - 1);
            menu.broadcastChanges();
            helper.assertTrue(menu.getSlot(ArcaneWorkbenchMenu.RESULT_SLOT).getItem().isEmpty(), "crafted with insufficient fire vis");
            menu.clicked(ArcaneWorkbenchMenu.RESULT_SLOT, 0, ClickType.PICKUP, player);
            helper.assertTrue(menu.getCarried().isEmpty(), "took result with insufficient vis");
            helper.assertValueEqual(Wands.getVis(source, Aspect.FIRE), fireCost - 1, "failed craft consumed vis");
            for (int slot = 0; slot < grid.size(); slot++) {
                helper.assertTrue(ItemStack.matches(table.getItem(slot), grid.get(slot)), "failed craft consumed ingredient");
            }
            Wands.fill(source);
            menu.broadcastChanges();
            helper.assertTrue(Wands.isWand(menu.getSlot(ArcaneWorkbenchMenu.RESULT_SLOT).getItem()), "result did not return after charging");
            menu.clicked(ArcaneWorkbenchMenu.RESULT_SLOT, 0, ClickType.PICKUP, player);
            helper.assertTrue(ItemStack.isSameItemSameComponents(menu.getCarried(), variant.result()), "wrong crafted wand");
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                int cost = (int) (aspects.getAmount(aspect) * 100 * sourceItem.getConsumptionModifier(source, player, aspect, true));
                helper.assertValueEqual(Wands.getVis(source, aspect), 10000 - cost, "vis consumption");
                helper.assertValueEqual(Wands.getVis(menu.getCarried(), aspect), 0, "crafted wand is charged");
            }
            for (int slot = 0; slot < grid.size(); slot++) {
                helper.assertTrue(table.getItem(slot).isEmpty(), "ingredient not consumed");
            }
        } finally {
            menu.removed(player);
        }
        helper.succeed();
    }
}
