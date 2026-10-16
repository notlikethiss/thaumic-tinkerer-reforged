package thaumictinkerer.mobilizer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.fx.Fx;

public class RelayBlockEntity extends TcBlockEntity implements MovableTile {
    private static final int SEARCH_RANGE = 32;

    private boolean hasPartner;
    private int partnerX;
    private int partnerZ;

    public RelayBlockEntity(BlockPos pos, BlockState state) {
        super(MobilizerFeature.RELAY_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RelayBlockEntity relay) {
        relay.tick(level);
    }

    public boolean hasPartner() {
        return hasPartner;
    }

    public int partnerX() {
        return partnerX;
    }

    public int partnerZ() {
        return partnerZ;
    }

    private void tick(Level level) {
        long time = level.getGameTime();
        if (level.isClientSide()) {
            if (hasPartner) {
                sparkle(level, time);
            }
            return;
        }
        verifyPartner();
        if (time % 40 == 0) {
            checkForPartner();
        }
        if (hasPartner && time % 40 == 0) {
            linkMobilizers(level);
        }
    }

    public void verifyPartner() {
        if (level == null) {
            return;
        }
        BlockPos partnerPos = new BlockPos(partnerX, worldPosition.getY(), partnerZ);
        if (!level.hasChunkAt(partnerPos)) {
            return;
        }
        boolean valid = hasPartner
            && level.getBlockEntity(partnerPos) instanceof RelayBlockEntity partner
            && partner.partnerX == worldPosition.getX()
            && partner.partnerZ == worldPosition.getZ();
        if (!valid && hasPartner) {
            hasPartner = false;
            sync();
        }
    }

    public void checkForPartner() {
        if (hasPartner) {
            return;
        }
        for (int offset = -SEARCH_RANGE; offset < SEARCH_RANGE; offset++) {
            if (offset != 0) {
                setPartners(relayAt(worldPosition.getX(), worldPosition.getZ() + offset));
                setPartners(relayAt(worldPosition.getX() + offset, worldPosition.getZ()));
            }
        }
    }

    private @Nullable RelayBlockEntity relayAt(int x, int z) {
        BlockPos pos = new BlockPos(x, worldPosition.getY(), z);
        if (level == null || !level.hasChunkAt(pos)) {
            return null;
        }
        return level.getBlockEntity(pos) instanceof RelayBlockEntity relay ? relay : null;
    }

    private void setPartners(@Nullable RelayBlockEntity other) {
        if (other == null) {
            return;
        }
        other.partnerX = worldPosition.getX();
        other.partnerZ = worldPosition.getZ();
        partnerX = other.worldPosition.getX();
        partnerZ = other.worldPosition.getZ();
        hasPartner = true;
        other.hasPartner = true;
        sync();
        other.sync();
    }

    private void linkMobilizers(Level level) {
        int x = worldPosition.getX();
        int z = worldPosition.getZ();
        int stepX = step(partnerX - x);
        int stepZ = step(partnerZ - z);
        int i = x;
        do {
            int j = z;
            do {
                BlockPos pos = new BlockPos(i, worldPosition.getY(), j);
                if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof MobilizerBlockEntity mobilizer) {
                    mobilizer.verifyRelay();
                    if (!mobilizer.isLinked()) {
                        mobilizer.link(x, z, partnerX, partnerZ, x != partnerX ? Direction.EAST : Direction.NORTH);
                    }
                }
                j += stepZ;
            } while (j < partnerZ);
            i += stepX;
        } while (i < partnerX);
    }

    private void sparkle(Level level, long time) {
        int x = worldPosition.getX();
        int z = worldPosition.getZ();
        int stepX = step(partnerX - x);
        int stepZ = step(partnerZ - z);
        float zIncrement = partnerZ - z != 0 ? Math.copySign(0.05F, partnerZ - z) * (time % 20) : 0.0F;
        float xIncrement = partnerX - x != 0 ? Math.copySign(0.05F, partnerX - x) * (time % 20) : 0.0F;
        int color = x < partnerX || z > partnerX ? 2 : 14;
        int i = x;
        do {
            int j = z;
            do {
                Fx.get().sparkle(level, 0.5F + i + xIncrement, worldPosition.getY() + 0.5F, j + 0.5F + zIncrement, 1.5F, color, 0.0F);
                j += stepZ;
            } while (j < partnerZ);
            i += stepX;
        } while (i < partnerX);
    }

    private static int step(int difference) {
        return difference < 0 ? -1 : 1;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hasPartner = input.getBooleanOr("HasPartner", false);
        partnerX = input.getIntOr("PartnerX", 0);
        partnerZ = input.getIntOr("PartnerZ", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("HasPartner", hasPartner);
        output.putInt("PartnerX", partnerX);
        output.putInt("PartnerZ", partnerZ);
    }
}
