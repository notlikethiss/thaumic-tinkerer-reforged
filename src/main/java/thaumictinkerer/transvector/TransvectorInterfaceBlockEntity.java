package thaumictinkerer.transvector;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;
import thaumcraft.essentia.EssentiaTransport;

public class TransvectorInterfaceBlockEntity extends TransvectorBlockEntity implements AspectContainer, EssentiaTransport {
    public static final int DISTANCE = 4;

    public TransvectorInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(TransvectorFeature.INTERFACE_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TransvectorInterfaceBlockEntity entity) {
        if (level.getGameTime() % 100 == 0) {
            level.updateNeighborsAt(pos, state.getBlock());
        }
    }

    public static @Nullable BlockEntity proxyTarget(TransvectorInterfaceBlockEntity entity) {
        BlockEntity target = entity.getTarget();
        return target instanceof TransvectorInterfaceBlockEntity ? null : target;
    }

    @Override
    public int maxDistance() {
        return DISTANCE;
    }

    private @Nullable AspectContainer container() {
        return proxyTarget(this) instanceof AspectContainer container ? container : null;
    }

    private @Nullable EssentiaTransport transport() {
        return proxyTarget(this) instanceof EssentiaTransport transport ? transport : null;
    }

    @Override
    public AspectList getAspects() {
        AspectContainer container = container();
        return container == null ? new AspectList() : container.getAspects();
    }

    @Override
    public void setAspects(AspectList aspects) {
        AspectContainer container = container();
        if (container != null) {
            container.setAspects(aspects);
        }
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        AspectContainer container = container();
        return container != null && container.doesContainerAccept(aspect);
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        AspectContainer container = container();
        return container == null ? amount : container.addToContainer(aspect, amount);
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        AspectContainer container = container();
        return container != null && container.takeFromContainer(aspect, amount);
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        AspectContainer container = container();
        return container != null && container.doesContainerContainAmount(aspect, amount);
    }

    @Override
    public int containerContains(Aspect aspect) {
        AspectContainer container = container();
        return container == null ? 0 : container.containerContains(aspect);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        EssentiaTransport transport = transport();
        return transport != null && transport.canInputFrom(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        EssentiaTransport transport = transport();
        return transport != null && transport.canOutputTo(face);
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
        EssentiaTransport transport = transport();
        if (transport != null) {
            transport.setSuction(aspect, amount);
        }
    }

    @Override
    public @Nullable Aspect getSuctionType(Direction face) {
        EssentiaTransport transport = transport();
        return transport == null ? null : transport.getSuctionType(face);
    }

    @Override
    public int getSuctionAmount(Direction face) {
        EssentiaTransport transport = transport();
        return transport == null ? 0 : transport.getSuctionAmount(face);
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        EssentiaTransport transport = transport();
        return transport == null ? 0 : transport.takeEssentia(aspect, amount, face);
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        EssentiaTransport transport = transport();
        return transport == null ? 0 : transport.addEssentia(aspect, amount, face);
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        EssentiaTransport transport = transport();
        return transport == null ? null : transport.getEssentiaType(face);
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        EssentiaTransport transport = transport();
        return transport == null ? 0 : transport.getEssentiaAmount(face);
    }

    @Override
    public int getMinimumSuction() {
        EssentiaTransport transport = transport();
        return transport == null ? 0 : transport.getMinimumSuction();
    }

    @Override
    public boolean renderExtendedTube() {
        EssentiaTransport transport = transport();
        return transport != null && transport.renderExtendedTube();
    }
}
