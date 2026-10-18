package thaumictinkerer.enchanting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModSounds;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;

public class EnchanterBlockEntity extends TcBlockEntity implements Container, MenuProvider {
    public static final int TOOL = 0;
    public static final int WAND = 1;
    public static final int PILLAR_COUNT = 6;
    public static final int PILLAR_RANGE = 4;
    public static final int MIN_PILLAR_TOTEMS = 2;
    public static final int MAX_PILLAR_TOTEMS = 12;
    public static final int VIS_PER_UNIT = 100;
    public static final int EVENT_RUNES = 0;

    private static final Codec<List<ResourceLocation>> ENCHANTMENT_LIST = ResourceLocation.CODEC.listOf();
    private static final Codec<List<Integer>> LEVEL_LIST = Codec.INT.listOf();

    private static final class Pillar {
        private final BlockPos base;
        private int top;

        private Pillar(BlockPos base, int top) {
            this.base = base;
            this.top = top;
        }
    }

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final List<ResourceKey<Enchantment>> enchantments = new ArrayList<>();
    private final List<Integer> levels = new ArrayList<>();
    private final List<Pillar> pillars = new ArrayList<>();
    private AspectList totalAspects = new AspectList();
    private AspectList currentAspects = new AspectList();
    private boolean working;

    public EnchanterBlockEntity(BlockPos pos, BlockState state) {
        super(EnchantingFeature.ENCHANTER_ENTITY.get(), pos, state);
    }

    public List<ResourceKey<Enchantment>> getEnchantments() {
        return enchantments;
    }

    public List<Integer> getLevels() {
        return levels;
    }

    public AspectList getTotalAspects() {
        return totalAspects;
    }

    public AspectList getCurrentAspects() {
        return currentAspects;
    }

    public boolean isWorking() {
        return working;
    }

    public ItemStack getTool() {
        return items.get(TOOL);
    }

    public ItemStack getWand() {
        return items.get(WAND);
    }

    public static boolean isUsableWand(ItemStack stack) {
        return Wands.isWand(stack) && !Wands.isStaff(stack);
    }

    public static boolean isAcceptedWand(ItemStack stack) {
        return isUsableWand(stack) && Wands.getCap(stack).baseCostModifier() <= 1.0F;
    }

    private void pushUpdate() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void setChanged() {
        if (level != null && !level.isClientSide() && !working) {
            clearSelection();
        }
        pushUpdate();
    }

    private void clearSelection() {
        enchantments.clear();
        levels.clear();
        totalAspects = new AspectList();
    }

    public void updateAspectList() {
        totalAspects = new AspectList();
        for (int index = 0; index < enchantments.size() && index < levels.size(); index++) {
            EnchantmentManager.Data data = EnchantmentManager.data(enchantments.get(index));
            if (data != null) {
                totalAspects.add(data.aspects(levels.get(index)));
            }
        }
    }

    public void removeEnchantment(ResourceKey<Enchantment> key) {
        int index = enchantments.indexOf(key);
        if (index != -1) {
            levels.remove(index);
            enchantments.remove(index);
        }
    }

    public boolean addEnchantment(Player player, ResourceKey<Enchantment> key) {
        ItemStack tool = getTool();
        if (working || enchantments.contains(key) || !EnchantmentManager.canEnchantStack(tool)) {
            return false;
        }
        if (!EnchantmentManager.canApply(player.level().registryAccess(), tool, key, enchantments) || !EnchantmentManager.canEnchantmentBeUsed(player, key)) {
            return false;
        }
        enchantments.add(key);
        levels.add(1);
        return true;
    }

    public void setLevel(ResourceKey<Enchantment> key, int level) {
        int index = enchantments.indexOf(key);
        if (index == -1 || level < 1 || level > maxLevel(key)) {
            return;
        }
        levels.set(index, level);
    }

    public int maxLevel(ResourceKey<Enchantment> key) {
        return TtEnchantments.holder(level.registryAccess(), key).map(holder -> holder.value().getMaxLevel()).orElse(0);
    }

    public void applyEdit(Player player, ResourceKey<Enchantment> key, int requestedLevel) {
        if (working || level == null || level.isClientSide()) {
            return;
        }
        if (requestedLevel == -1) {
            removeEnchantment(key);
        } else if (!enchantments.contains(key)) {
            addEnchantment(player, key);
        } else {
            setLevel(key, Math.max(1, Math.min(maxLevel(key), requestedLevel)));
        }
        updateAspectList();
        pushUpdate();
    }

    public boolean startWorking() {
        if (level == null || level.isClientSide() || working || enchantments.isEmpty() || levels.isEmpty() || enchantments.size() != levels.size()) {
            return false;
        }
        if (!EnchantmentManager.canEnchantStack(getTool())) {
            return false;
        }
        working = true;
        pushUpdate();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnchanterBlockEntity enchanter) {
        enchanter.tick(level, pos);
    }

    private void tick(Level level, BlockPos pos) {
        ItemStack tool = getTool();
        if (tool.isEmpty() && !(enchantments.isEmpty() && levels.isEmpty())) {
            clearSelection();
            pushUpdate();
        }
        if (!working) {
            return;
        }
        if (tool.isEmpty()) {
            working = false;
            pushUpdate();
            return;
        }
        if (!checkPillars()) {
            return;
        }
        boolean complete = true;
        for (Aspect aspect : EnchantmentManager.PRIMAL_ASPECTS) {
            if (currentAspects.getAmount(aspect) < totalAspects.getAmount(aspect)) {
                complete = false;
                break;
            }
        }
        if (complete) {
            finish(level, pos, tool);
            return;
        }
        ItemStack wand = getWand();
        if (isUsableWand(wand) && wand.getItem() instanceof WandItem wandItem) {
            drainWand(level, wand, wandItem);
        }
    }

    private void finish(Level level, BlockPos pos, ItemStack tool) {
        working = false;
        currentAspects = new AspectList();
        totalAspects = new AspectList();
        for (int index = 0; index < enchantments.size() && index < levels.size(); index++) {
            Holder<Enchantment> holder = TtEnchantments.holder(level.registryAccess(), enchantments.get(index)).orElse(null);
            if (holder != null) {
                tool.enchant(holder, levels.get(index));
            }
        }
        enchantments.clear();
        levels.clear();
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        pushUpdate();
    }

    private void drainWand(Level level, ItemStack wand, WandItem wandItem) {
        AspectList wandAspects = Wands.getAllVis(wand);
        List<Aspect> candidates = new ArrayList<>();
        for (Aspect aspect : EnchantmentManager.PRIMAL_ASPECTS) {
            int missing = totalAspects.getAmount(aspect) - currentAspects.getAmount(aspect);
            if (missing > 0 && wandAspects.getAmount(aspect) >= VIS_PER_UNIT) {
                candidates.add(aspect);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        RandomSource random = level.getRandom();
        int index = random.nextInt(candidates.size());
        Aspect aspect = candidates.get(index);
        if (!consumeVis(wand, wandItem, aspect)) {
            return;
        }
        currentAspects.add(aspect, 1);
        super.setChanged();
        if (index < pillars.size() && random.nextBoolean()) {
            level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_RUNES, index);
            if (random.nextInt(5) == 0) {
                BlockPos base = pillars.get(index).base;
                level.playSound(null, base.getX() + 0.5, pillars.get(index).top + 0.5, base.getZ() + 0.5, ModSounds.BRAIN.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            }
        }
    }

    private boolean consumeVis(ItemStack wand, WandItem wandItem, Aspect aspect) {
        int cost = (int) (VIS_PER_UNIT * wandItem.getConsumptionModifier(wand, null, aspect, true));
        int current = Wands.getVis(wand, aspect);
        if (current < cost) {
            return false;
        }
        Wands.storeVis(wand, aspect, current - cost);
        return true;
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id != EVENT_RUNES || level == null || !level.isClientSide()) {
            return super.triggerEvent(id, type);
        }
        if (pillars.size() != PILLAR_COUNT) {
            pillars.clear();
            assignPillars();
        }
        RandomSource random = level.getRandom();
        if (type >= 0 && type < pillars.size()) {
            Pillar pillar = pillars.get(type);
            Fx.get().blockRunes(level, pillar.base.getX(), pillar.top - 0.75, pillar.base.getZ(), 0.3F + random.nextFloat() * 0.7F, 0.0F, 0.3F + random.nextFloat() * 0.7F, 15, random.nextFloat() / 8.0F);
        }
        Fx.get().blockRunes(level, worldPosition.getX(), worldPosition.getY() + 0.25, worldPosition.getZ(), 0.3F + random.nextFloat() * 0.7F, 0.0F, 0.3F + random.nextFloat() * 0.7F, 15, random.nextFloat() / 8.0F);
        return true;
    }

    public boolean checkPillars() {
        if (pillars.isEmpty()) {
            if (assignPillars()) {
                working = false;
                currentAspects = new AspectList();
                pushUpdate();
                return false;
            }
            return true;
        }
        for (Pillar pillar : pillars) {
            int height = findPillar(pillar.base);
            if (height == -1) {
                pillars.clear();
                return checkPillars();
            }
            pillar.top = height;
        }
        return true;
    }

    public boolean assignPillars() {
        int y = worldPosition.getY();
        for (int x = worldPosition.getX() - PILLAR_RANGE; x <= worldPosition.getX() + PILLAR_RANGE; x++) {
            for (int z = worldPosition.getZ() - PILLAR_RANGE; z <= worldPosition.getZ() + PILLAR_RANGE; z++) {
                BlockPos base = new BlockPos(x, y, z);
                int height = findPillar(base);
                if (height != -1) {
                    pillars.add(new Pillar(base, height));
                }
                if (pillars.size() == PILLAR_COUNT) {
                    return false;
                }
            }
        }
        pillars.clear();
        return true;
    }

    public int findPillar(BlockPos base) {
        int totems = 0;
        for (int offset = 0; ; offset++) {
            BlockPos pos = base.above(offset);
            if (pos.getY() > level.getMaxBuildHeight()) {
                return -1;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.OBSIDIAN_TOTEM.get())) {
                totems++;
                continue;
            }
            if (state.is(ModBlocks.NITOR_LIGHT.get()) && totems >= MIN_PILLAR_TOTEMS && totems < MAX_PILLAR_TOTEMS + 1) {
                return pos.getY();
            }
            return -1;
        }
    }

    public int pillarCount() {
        return pillars.size();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, this);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new EnchanterMenu(containerId, inventory, worldPosition);
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return items.get(TOOL).isEmpty() && items.get(WAND).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input.tag(), items, input.lookup());
        working = input.getBooleanOr("working", false);
        totalAspects = input.read("totalAspects", AspectList.CODEC).orElseGet(AspectList::new);
        currentAspects = input.read("currentAspects", AspectList.CODEC).orElseGet(AspectList::new);
        enchantments.clear();
        for (ResourceLocation id : input.read("enchantments", ENCHANTMENT_LIST).orElse(List.of())) {
            enchantments.add(ResourceKey.create(Registries.ENCHANTMENT, id));
        }
        levels.clear();
        levels.addAll(input.read("levels", LEVEL_LIST).orElse(List.of()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.tag(), items, output.lookup());
        output.putBoolean("working", working);
        output.store("totalAspects", AspectList.CODEC, totalAspects);
        output.store("currentAspects", AspectList.CODEC, currentAspects);
        output.store("enchantments", ENCHANTMENT_LIST, enchantments.stream().map(ResourceKey::location).toList());
        output.store("levels", LEVEL_LIST, List.copyOf(levels));
    }
}
