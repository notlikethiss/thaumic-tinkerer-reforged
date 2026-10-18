package thaumictinkerer.crops;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectContainer;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;

public class InfusedFarmlandBlockEntity extends TcBlockEntity implements AspectContainer {
    public static final int MAX_ASPECTS = 20;

    private AspectList aspectList = new AspectList();

    public InfusedFarmlandBlockEntity(BlockPos pos, BlockState state) {
        super(CropsFeature.INFUSED_FARMLAND_ENTITY.get(), pos, state);
    }

    public void reduceSaturatedAspects(RandomSource random) {
        int sum = aspectList.visSize();
        int toRemove = sum - MAX_ASPECTS;
        while (toRemove > 0) {
            List<Aspect> aspects = aspectList.getAspects();
            if (aspects.isEmpty()) {
                break;
            }
            Aspect target = aspects.get(random.nextInt(aspects.size()));
            aspectList.remove(target, 1);
            toRemove--;
        }
    }

    @Override
    public AspectList getAspects() {
        return aspectList;
    }

    @Override
    public void setAspects(AspectList aspects) {
        aspectList = aspects.copy();
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspectList = input.read("aspect_list", AspectList.CODEC).orElseGet(AspectList::new).copy();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("aspect_list", AspectList.CODEC, aspectList);
    }
}
