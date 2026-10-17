package thaumictinkerer.necromancy;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.fx.Fx;
import thaumcraft.infusion.PedestalBlockEntity;

public class NecromancyTabletBlockEntity extends TcBlockEntity {
    public static final int INTERVAL = 300;
    public static final int INFUSED_INTERVAL = 1200;

    public NecromancyTabletBlockEntity(BlockPos pos, BlockState state) {
        super(NecromancyFeature.TABLET_ENTITY.get(), pos, state);
    }

    public void tick() {
        if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % INTERVAL == 0) {
            attempt(serverLevel, serverLevel.getGameTime());
        }
    }

    public boolean attempt(ServerLevel serverLevel, long time) {
        if (serverLevel.hasNeighborSignal(worldPosition)) {
            return false;
        }
        for (int radius = 1; radius < 6; radius++) {
            List<PedestalBlockEntity> pedestals = new ArrayList<>();
            for (int x = worldPosition.getX() - radius; x < worldPosition.getX() + radius; x++) {
                for (int z = worldPosition.getZ() - radius; z < worldPosition.getZ() + radius; z++) {
                    if (serverLevel.getBlockEntity(new BlockPos(x, worldPosition.getY(), z)) instanceof PedestalBlockEntity pedestal && SoulAspectItem.isSoul(pedestal.getStack())) {
                        pedestals.add(pedestal);
                    }
                }
            }
            for (PedestalBlockEntity first : pedestals) {
                for (PedestalBlockEntity second : pedestals) {
                    for (PedestalBlockEntity third : pedestals) {
                        if (first == second || second == third || first == third) {
                            continue;
                        }
                        List<Aspect> aspects = List.of(
                            SoulAspectItem.aspectOf(first.getStack()),
                            SoulAspectItem.aspectOf(second.getStack()),
                            SoulAspectItem.aspectOf(third.getStack())
                        );
                        MobAspect recipe = MobAspect.forAspects(aspects);
                        if (recipe == null) {
                            continue;
                        }
                        List<PedestalBlockEntity> used = List.of(first, second, third);
                        boolean infused = SoulAspectItem.isInfused(first.getStack()) && SoulAspectItem.isInfused(second.getStack()) && SoulAspectItem.isInfused(third.getStack());
                        if (infused && time % INFUSED_INTERVAL != 0) {
                            return false;
                        }
                        if (!infused) {
                            effects(serverLevel, used, aspects);
                            for (PedestalBlockEntity pedestal : used) {
                                pedestal.removeStack();
                            }
                            spawn(serverLevel, recipe);
                        } else {
                            if (matches(first.getStack()) && matches(second.getStack()) && matches(third.getStack())) {
                                effects(serverLevel, used, aspects);
                                spawn(serverLevel, recipe);
                            }
                            for (PedestalBlockEntity pedestal : used) {
                                ItemStack marked = pedestal.getStack().copy();
                                NecromancyFeature.LAST_TABLET.set(marked, worldPosition);
                                pedestal.setStack(marked);
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean matches(ItemStack stack) {
        BlockPos last = NecromancyFeature.LAST_TABLET.get(stack);
        return last == null || last.equals(worldPosition);
    }

    private void spawn(ServerLevel serverLevel, MobAspect recipe) {
        EntityType<?> type = recipe.entityType().get();
        if (type == EntityTypes.SKELETON && serverLevel.dimension() == Level.NETHER) {
            type = EntityTypes.WITHER_SKELETON;
        }
        Entity entity = type.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
        if (entity == null) {
            return;
        }
        entity.snapTo(worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, 0.0F, 0.0F);
        if (entity instanceof Mob mob) {
            EventHooks.finalizeMobSpawn(mob, serverLevel, serverLevel.getCurrentDifficultyAt(worldPosition), EntitySpawnReason.MOB_SUMMONED, null);
        }
        serverLevel.addFreshEntity(entity);
        if (entity instanceof Mob mob) {
            mob.playAmbientSound();
        }
    }

    private void effects(ServerLevel serverLevel, List<PedestalBlockEntity> pedestals, List<Aspect> aspects) {
        for (int index = 0; index < pedestals.size(); index++) {
            BlockPos offset = pedestals.get(index).getBlockPos().subtract(worldPosition);
            int aspectIndex = NecromancyFeature.ASPECTS.indexOf(aspects.get(index));
            serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), aspectIndex, ((offset.getX() + 8) << 4) | (offset.getZ() + 8));
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id >= 0 && id < NecromancyFeature.ASPECT_COUNT) {
            if (level != null && level.isClientSide()) {
                BlockPos from = worldPosition.offset((type >> 4) - 8, 0, (type & 15) - 8);
                Fx.get().essentiaTrail(level, from, worldPosition, 20, NecromancyFeature.ASPECTS.get(id).getColor(), 20.0F);
            }
            return true;
        }
        return super.triggerEvent(id, type);
    }
}
