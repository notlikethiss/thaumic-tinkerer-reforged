package thaumictinkerer.crops;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public class InfusedSeedsItem extends BlockItem implements CreativeVariants {
    public InfusedSeedsItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static Aspect getAspect(ItemStack stack) {
        return CropsFeature.SEED_ASPECT.getOrDefault(stack, Aspect.AIR);
    }

    public static void setAspect(ItemStack stack, Aspect aspect) {
        CropsFeature.SEED_ASPECT.set(stack, aspect);
    }

    public static AspectList getTendencies(ItemStack stack) {
        return CropsFeature.SEED_TENDENCIES.getOrDefault(stack, AspectList.EMPTY).copy();
    }

    public static void setTendencies(ItemStack stack, AspectList tendencies) {
        CropsFeature.SEED_TENDENCIES.set(stack, tendencies.copy());
    }

    public static ItemStack stackFor(Aspect aspect) {
        ItemStack stack = new ItemStack(CropsFeature.INFUSED_SEEDS.get());
        setAspect(stack, aspect);
        setTendencies(stack, new AspectList());
        return stack;
    }

    @Override
    public void addCreativeStacks(Consumer<ItemStack> output) {
        for (Aspect primal : Aspect.getPrimalAspects()) {
            output.accept(stackFor(primal));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, builder, flag);
        builder.add(getAspect(stack).getDescription());
        AspectList tendencies = getTendencies(stack);
        for (Aspect aspect : tendencies.getAspects()) {
            builder.add(aspect.getDescription().copy().append(": " + tendencies.getAmount(aspect)));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos above = pos.above();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (!(level.getBlockState(pos).getBlock() instanceof FarmBlock) || level.isOutsideBuildHeight(above)) {
            return InteractionResult.PASS;
        }
        if (player != null && (!player.mayUseItemAt(pos, Direction.UP, stack) || !player.mayUseItemAt(above, Direction.UP, stack))) {
            return InteractionResult.PASS;
        }
        if (!level.getBlockState(above).canBeReplaced()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        plant(level, pos, getAspect(stack), getTendencies(stack));
        level.playSound(null, above, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    public static void plant(Level level, BlockPos farmland, Aspect aspect, AspectList tendencies) {
        if (!(level.getBlockState(farmland).getBlock() instanceof InfusedFarmlandBlock)) {
            level.setBlockAndUpdate(farmland, CropsFeature.INFUSED_FARMLAND.get().defaultBlockState());
        }
        BlockState crop = CropsFeature.INFUSED_GRAIN.get().stateFor(aspect);
        BlockPos above = farmland.above();
        level.setBlockAndUpdate(above, crop);
        if (level.getBlockEntity(above) instanceof InfusedGrainBlockEntity grain) {
            grain.setup(aspect, tendencies);
        }
    }
}
