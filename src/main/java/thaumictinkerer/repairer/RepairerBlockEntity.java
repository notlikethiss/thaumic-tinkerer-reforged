package thaumictinkerer.repairer;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;
import thaumcraft.essentia.Essentia;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.fx.Fx;
import thaumictinkerer.mechanism.ItemHolderBlockEntity;

public class RepairerBlockEntity extends ItemHolderBlockEntity implements AspectContainer, EssentiaTransport {
    public static final int SPARKLE_EVENT = 1;
    private static final int SUCTION = 128;
    private static final Map<Aspect, Integer> REPAIR_VALUES = new LinkedHashMap<>();

    static {
        REPAIR_VALUES.put(Aspect.TOOL, 8);
        REPAIR_VALUES.put(Aspect.CRAFT, 5);
        REPAIR_VALUES.put(Aspect.ORDER, 3);
    }

    private int ticksExisted;
    private boolean tookLastTick = true;
    private int damageLastTick;
    private boolean powered;

    public RepairerBlockEntity(BlockPos pos, BlockState state) {
        super(RepairerFeature.REPAIRER_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RepairerBlockEntity repairer) {
        if (++repairer.ticksExisted % 10 == 0 && !level.isClientSide()) {
            repairer.repairCycle(level);
        }
    }

    public int ticksExisted() {
        return ticksExisted;
    }

    public boolean tookLastTick() {
        return tookLastTick;
    }

    public Direction orientation() {
        return getBlockState().getValue(RepairerBlock.FACING);
    }

    public boolean isDamaged() {
        ItemStack stack = getTheItem();
        return !stack.isEmpty() && stack.getDamageValue() != 0;
    }

    @Override
    public boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && stack.isDamageableItem();
    }

    public void repairCycle(Level level) {
        boolean previous = tookLastTick;
        ItemStack stack = getTheItem();
        if (!stack.isEmpty() && stack.getDamageValue() > 0) {
            int essentia = drawEssentia(level);
            int damage = stack.getDamageValue();
            stack.setDamageValue(Math.max(0, damage - essentia));
            sync();
            if (damageLastTick != 0 && damageLastTick != damage) {
                level.blockEvent(worldPosition, getBlockState().getBlock(), SPARKLE_EVENT, 0);
                tookLastTick = true;
            } else {
                tookLastTick = false;
            }
        } else {
            tookLastTick = false;
        }
        damageLastTick = stack.isEmpty() ? 0 : stack.getDamageValue();
        if (previous != tookLastTick) {
            sync();
        }
    }

    public int drawEssentia(Level level) {
        Direction orientation = orientation();
        EssentiaTransport source = Essentia.getConnectable(level, worldPosition, orientation);
        if (source == null) {
            return 0;
        }
        Direction face = orientation.getOpposite();
        if (!source.canOutputTo(face)) {
            return 0;
        }
        for (Map.Entry<Aspect, Integer> entry : REPAIR_VALUES.entrySet()) {
            Aspect aspect = entry.getKey();
            if (source.getSuctionType(face) == aspect
                && source.getSuctionAmount(face) < getSuctionAmount(orientation)
                && source.takeEssentia(aspect, 1, face) == 1) {
                return entry.getValue();
            }
        }
        return 0;
    }

    @Override
    public void sync() {
        super.sync();
        if (level != null && !level.isClientSide()) {
            boolean damaged = isDamaged();
            if (damaged != powered) {
                powered = damaged;
                Block block = getBlockState().getBlock();
                level.updateNeighborsAt(worldPosition, block);
                for (Direction direction : Direction.values()) {
                    level.updateNeighborsAt(worldPosition.relative(direction), block);
                }
            }
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == SPARKLE_EVENT && level != null && level.isClientSide()) {
            var random = level.getRandom();
            Fx.get().sparkle(
                level,
                (float) (worldPosition.getX() + 0.25 + random.nextFloat() / 2.0F),
                (float) (worldPosition.getY() + 1 + random.nextFloat() / 2.0F),
                (float) (worldPosition.getZ() + 0.25 + random.nextFloat() / 2.0F),
                1.5F,
                0,
                0.0F
            );
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @Override
    public AspectList getAspects() {
        ItemStack stack = getTheItem();
        AspectList aspects = new AspectList();
        if (!stack.isEmpty() && stack.getDamageValue() > 0) {
            aspects.add(Aspect.ENTROPY, stack.getDamageValue());
        }
        return aspects;
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

    @Override
    public boolean isConnectable(Direction face) {
        return face == orientation();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return isConnectable(face);
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public @Nullable Aspect getSuctionType(Direction face) {
        return face == orientation() ? Aspect.TOOL : null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return face == orientation() ? SUCTION : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tookLastTick = input.getBooleanOr("TookLastTick", true);
        powered = isDamaged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("TookLastTick", tookLastTick);
    }
}
