package thaumictinkerer.gear;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumictinkerer.necromancy.MobAspect;
import thaumictinkerer.necromancy.NecromancyFeature;

public final class SoulDrops {
    private SoulDrops() {
    }

    public static @Nullable Aspect[] aspectsFor(Entity entity) {
        MobAspect mob = MobAspect.forEntity(entity);
        return mob == null ? null : mob.aspects().toArray(Aspect[]::new);
    }

    public static ItemStack soul(Aspect aspect) {
        return NecromancyFeature.soulStack(aspect);
    }

    public static @Nullable List<ItemStack> soulsFor(Entity entity) {
        Aspect[] aspects = aspectsFor(entity);
        if (aspects == null) {
            return null;
        }
        List<ItemStack> souls = new ArrayList<>();
        for (Aspect aspect : aspects) {
            souls.add(soul(aspect));
        }
        return souls;
    }
}
