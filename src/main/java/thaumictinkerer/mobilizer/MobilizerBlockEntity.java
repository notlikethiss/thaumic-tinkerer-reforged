package thaumictinkerer.mobilizer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.registry.ModBlocks;
import thaumcraft.wand.focus.FocusUtils;

public class MobilizerBlockEntity extends BlockEntity {
    private static final int PERIOD = 100;
    private static final int CARRY_FLAGS = Block.UPDATE_ALL;
    private static final int CLEAR_FLAGS = Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS;

    private boolean linked;
    private int firstRelayX;
    private int firstRelayZ;
    private int secondRelayX;
    private int secondRelayZ;
    private Direction movementDirection = Direction.DOWN;

    public MobilizerBlockEntity(BlockPos pos, BlockState state) {
        super(MobilizerFeature.MOBILIZER_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MobilizerBlockEntity mobilizer) {
        mobilizer.tick(level);
    }

    public boolean isLinked() {
        return linked;
    }

    public Direction movementDirection() {
        return movementDirection;
    }

    public int firstRelayX() {
        return firstRelayX;
    }

    public int firstRelayZ() {
        return firstRelayZ;
    }

    public int secondRelayX() {
        return secondRelayX;
    }

    public int secondRelayZ() {
        return secondRelayZ;
    }

    public void link(int firstX, int firstZ, int secondX, int secondZ, Direction direction) {
        firstRelayX = firstX;
        firstRelayZ = firstZ;
        secondRelayX = secondX;
        secondRelayZ = secondZ;
        movementDirection = direction;
        linked = true;
        setChanged();
    }

    private void copyFrom(MobilizerBlockEntity other) {
        linked = other.linked;
        firstRelayX = other.firstRelayX;
        firstRelayZ = other.firstRelayZ;
        secondRelayX = other.secondRelayX;
        secondRelayZ = other.secondRelayZ;
        movementDirection = other.movementDirection;
        setChanged();
    }

    public void verifyRelay() {
        if (level == null) {
            return;
        }
        BlockPos relayPos = new BlockPos(firstRelayX, worldPosition.getY(), firstRelayZ);
        RelayBlockEntity relay = level.hasChunkAt(relayPos) && level.getBlockEntity(relayPos) instanceof RelayBlockEntity found ? found : null;
        if (relay != null && !level.isClientSide()) {
            relay.verifyPartner();
        }
        if (linked && !(relay != null && relay.partnerX() == secondRelayX && relay.partnerZ() == secondRelayZ)) {
            linked = false;
            setChanged();
        }
    }

    private void tick(Level level) {
        if (isRemoved() || level.isClientSide()) {
            return;
        }
        verifyRelay();
        long time = level.getGameTime();
        if (linked && time % PERIOD == 0 && !level.hasNeighborSignal(worldPosition)) {
            bounce(level);
        }
        if (linked && time % PERIOD == 1 && !level.hasNeighborSignal(worldPosition)) {
            move(level);
        }
    }

    private BlockPos targetPos() {
        return new BlockPos(worldPosition.getX() + movementDirection.getStepX(), worldPosition.getY(), worldPosition.getZ() + movementDirection.getStepZ());
    }

    public void bounce(Level level) {
        BlockPos target = targetPos();
        if (!level.hasChunkAt(target) || !level.getBlockState(target).isAir() || !level.getBlockState(target.above()).isAir()) {
            movementDirection = movementDirection.getOpposite();
            setChanged();
        }
    }

    public boolean move(Level level) {
        if (!(level instanceof ServerLevel serverLevel) || isRemoved()) {
            return false;
        }
        BlockState self = getBlockState();
        if (!self.is(MobilizerFeature.MOBILIZER.get())) {
            return false;
        }
        BlockPos target = targetPos();
        if (!serverLevel.hasChunkAt(target) || !serverLevel.getBlockState(target).isAir()) {
            return false;
        }
        BlockPos passengerPos = worldPosition.above();
        BlockPos passengerTarget = target.above();
        BlockState passenger = serverLevel.getBlockState(passengerPos);
        BlockEntity passengerEntity = serverLevel.getBlockEntity(passengerPos);
        CompoundTag passengerData = passengerEntity != null && isMovable(passengerEntity)
            ? passengerEntity.saveWithFullMetadata(serverLevel.registryAccess())
            : null;

        if (!serverLevel.setBlock(target, self, Block.UPDATE_ALL) || !(serverLevel.getBlockEntity(target) instanceof MobilizerBlockEntity moved)) {
            return false;
        }
        moved.copyFrom(this);

        if (canCarry(serverLevel, passengerPos, passenger, passengerEntity, passengerTarget)) {
            BlockEntity carried = null;
            if (passengerEntity != null) {
                carried = BlockEntity.loadStatic(passengerTarget, passenger, passengerData, serverLevel.registryAccess());
            }
            if (passengerEntity == null || carried != null) {
                carry(serverLevel, passengerPos, passenger, passengerTarget, carried);
            }
        }

        serverLevel.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), CARRY_FLAGS);
        return true;
    }

    private static boolean canCarry(ServerLevel level, BlockPos from, BlockState passenger, @Nullable BlockEntity passengerEntity, BlockPos to) {
        if (passenger.isAir()
            || passenger.is(FocusUtils.PORTABLE_HOLE_BLACKLIST)
            || passenger.is(ModBlocks.HOLE.get())
            || passenger.getDestroySpeed(level, from) < 0.0F) {
            return false;
        }
        if (passengerEntity != null && !isMovable(passengerEntity)) {
            return false;
        }
        return level.hasChunkAt(to) && level.getBlockState(to).canBeReplaced() && passenger.canSurvive(level, to);
    }

    private static boolean isMovable(BlockEntity entity) {
        return entity instanceof MovableTile || "minecraft".equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entity.getType()).getNamespace());
    }

    private static void carry(ServerLevel level, BlockPos from, BlockState passenger, BlockPos to, @Nullable BlockEntity carried) {
        if (!level.setBlock(to, passenger, CARRY_FLAGS) || !level.getBlockState(to).is(passenger.getBlock())) {
            return;
        }
        if (carried != null) {
            level.setBlockEntity(carried);
        }
        level.removeBlockEntity(from);
        level.setBlock(from, Blocks.AIR.defaultBlockState(), CLEAR_FLAGS);
    }

    @Override
    protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ValueInput input = ValueInput.of(tag, registries);
        linked = input.getBooleanOr("Linked", false);
        firstRelayX = input.getIntOr("FirstRelayX", 0);
        firstRelayZ = input.getIntOr("FirstRelayZ", 0);
        secondRelayX = input.getIntOr("SecondRelayX", 0);
        secondRelayZ = input.getIntOr("SecondRelayZ", 0);
        movementDirection = Direction.from3DDataValue(input.getIntOr("Direction", 0));
    }

    @Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ValueOutput output = ValueOutput.of(tag, registries);
        output.putBoolean("Linked", linked);
        output.putInt("FirstRelayX", firstRelayX);
        output.putInt("FirstRelayZ", firstRelayZ);
        output.putInt("SecondRelayX", secondRelayX);
        output.putInt("SecondRelayZ", secondRelayZ);
        output.putInt("Direction", movementDirection.get3DDataValue());
    }
}
