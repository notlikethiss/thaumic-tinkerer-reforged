package thaumictinkerer.foci;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusUpgradeType;
import thaumcraft.wand.focus.FocusUtils;
import thaumictinkerer.network.FociEffectPayload;

public class SmeltFocus extends TtFocus {
    private static final AspectList COST = new AspectList().add(Aspect.FIRE, 45).add(Aspect.ENTROPY, 12);
    private static final double RANGE = 10.0;
    private static final int SOUND_COOLDOWN = 3;
    private static final Map<UUID, State> STATES = new HashMap<>();

    public SmeltFocus(Properties properties) {
        super(properties, "focussmelt", true, false);
    }

    public static final class State {
        public final BlockPos pos;
        public int progress;

        public State(BlockPos pos, int progress) {
            this.pos = pos;
            this.progress = progress;
        }
    }

    public static @Nullable State state(UUID player) {
        return STATES.get(player);
    }

    public static void clearStates() {
        STATES.clear();
    }

    public static @Nullable BlockState smeltResult(ServerLevel level, BlockState state) {
        Item item = state.getBlock().asItem();
        if (item == Items.AIR) {
            return null;
        }
        SingleRecipeInput input = new SingleRecipeInput(new ItemStack(item));
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level);
        ItemStack result = recipe.map(holder -> holder.value().assemble(input)).orElse(ItemStack.EMPTY);
        if (result.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }
        return null;
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "SMELT";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0xFF0000;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Override
    public FocusUpgradeType @Nullable [] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[]{FocusUpgradeType.TREASURE, FocusUpgradeType.POTENCY};
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        WandItem item = (WandItem) wand.getItem();
        if (!item.consumeAllVis(wand, player, COST.copy(), false, false)) {
            return;
        }
        BlockHitResult hit = FocusUtils.getTargetBlock(level, player, true, RANGE);
        if (hit == null) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState current = level.getBlockState(pos);
        BlockState result = smeltResult(level, current);
        if (result == null || result == current || level.getBlockEntity(pos) != null) {
            return;
        }
        boolean decremented = false;
        State data = STATES.get(player.getUUID());
        if (data != null && data.pos.equals(pos)) {
            data.progress--;
            decremented = true;
            if (data.progress <= 0) {
                if (level.mayInteract(player, pos) && item.consumeAllVis(wand, player, COST.copy(), true, false)) {
                    level.setBlock(pos, result, Block.UPDATE_ALL);
                }
                STATES.remove(player.getUUID());
                decremented = false;
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.6F, 1.0F);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
                FociEffects.send(player, FociEffectPayload.SMELT_DONE, pos);
            }
        }
        if (!decremented) {
            int potency = Wands.getFocusUpgrade(wand, FocusUpgradeType.POTENCY);
            STATES.put(player.getUUID(), new State(pos.immutable(), 20 - Math.min(3, potency) * 5));
        } else {
            if (count % SOUND_COOLDOWN == 0) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.2F, 1.0F);
            }
            FociEffects.send(player, FociEffectPayload.SMELT_PROGRESS, pos);
        }
        FociEffects.send(player, FociEffectPayload.SMELT_BEAM, pos);
    }
}
