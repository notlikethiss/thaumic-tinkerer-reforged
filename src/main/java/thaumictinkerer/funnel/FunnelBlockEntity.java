package thaumictinkerer.funnel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectProvidingItem;
import thaumcraft.essentia.FilledJarItem;
import thaumcraft.essentia.JarBlockEntity;
import thaumictinkerer.mechanism.ItemHolderBlockEntity;

public class FunnelBlockEntity extends ItemHolderBlockEntity implements AspectContainer {
    public FunnelBlockEntity(BlockPos pos, BlockState state) {
        super(FunnelFeature.FUNNEL_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FunnelBlockEntity funnel) {
        funnel.transfer(level, pos);
    }

    @Override
    public boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof FilledJarItem;
    }

    @Override
    protected boolean exposesSlot(Direction face) {
        return face != Direction.DOWN;
    }

    private void transfer(Level level, BlockPos pos) {
        ItemStack stack = getTheItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof FilledJarItem jarItem)) {
            return;
        }
        AspectList aspects = jarItem.getAspects(stack);
        if (aspects == null || aspects.size() != 1) {
            return;
        }
        Aspect aspect = aspects.getAspects().get(0);
        BlockPos hopperPos = pos.below();
        BlockEntity hopper = level.getBlockEntity(hopperPos);
        if (!(hopper instanceof HopperBlockEntity)) {
            return;
        }
        Direction facing = hopper.getBlockState().getValue(HopperBlock.FACING);
        if (!(level.getBlockEntity(hopperPos.relative(facing)) instanceof JarBlockEntity target)) {
            return;
        }
        AspectList contained = target.getAspects();
        boolean accepts = contained.size() == 0
            ? target.getAspectFilter() == null || target.getAspectFilter() == aspect
            : contained.getAspects().get(0) == aspect && (contained.getAmount(aspect) < target.getMaxAmount() || target.isVoid());
        if (accepts) {
            target.addToContainer(aspect, 1);
            jarItem.setAspects(stack, aspects.remove(aspect, 1));
            sync();
        }
    }

    @Override
    public AspectList getAspects() {
        ItemStack stack = getTheItem();
        if (!stack.isEmpty() && stack.getItem() instanceof AspectProvidingItem provider) {
            AspectList aspects = provider.getAspects(stack);
            if (aspects != null) {
                return aspects;
            }
        }
        return new AspectList();
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return false;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return 0;
    }
}
