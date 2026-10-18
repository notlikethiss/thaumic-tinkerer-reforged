package thaumictinkerer.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class TravelEffect {
    private TravelEffect() {
    }

    public static void apply(Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (level.isClientSide()) {
            for (int index = 0; index < 5; index++) {
                level.addParticle(
                    new DustParticleOptions(new org.joml.Vector3f(0.0F, 128.0F / 255.0F, 0.0F), 1.0F),
                    pos.getX() + level.getRandom().nextFloat(),
                    pos.getY() + 1.0,
                    pos.getZ() + level.getRandom().nextFloat(),
                    0.0,
                    0.0,
                    0.0
                );
            }
        } else {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1));
            living.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 0));
        }
    }
}
