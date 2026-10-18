package thaumictinkerer.kami;

import java.util.Collection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import thaumictinkerer.Config;
import thaumictinkerer.ThaumicTinkerer;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID)
public final class DimensionalShardDrops {
    public static final double ENDER_CHANCE = 1.0 / 32.0;
    public static final double NETHER_CHANCE = 1.0 / 16.0;

    private DimensionalShardDrops() {
    }

    @SubscribeEvent
    static void onDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        apply(entity, entity.level().dimension(), event.getSource(), event.getDrops(), entity.getRandom());
    }

    public static void apply(LivingEntity entity, ResourceKey<Level> dimension, DamageSource source, Collection<ItemEntity> drops, RandomSource random) {
        if (!Config.kamiEnabled() || entity.level().isClientSide() || !(source.getEntity() instanceof Player)) {
            return;
        }
        if (entity instanceof EnderMan && dimension == Level.END && random.nextDouble() <= ENDER_CHANCE) {
            drops.add(drop(entity, KamiFeature.ENDER_SHARD.get().getDefaultInstance()));
        }
        if (entity instanceof ZombifiedPiglin && dimension == Level.NETHER && random.nextDouble() <= NETHER_CHANCE) {
            drops.add(drop(entity, KamiFeature.NETHER_SHARD.get().getDefaultInstance()));
        }
    }

    private static ItemEntity drop(LivingEntity entity, ItemStack stack) {
        return new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
    }
}
