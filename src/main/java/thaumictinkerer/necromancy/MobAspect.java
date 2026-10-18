package thaumictinkerer.necromancy;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.entity.monster.BrainyZombie;
import thaumcraft.entity.monster.FireBat;
import thaumcraft.entity.monster.Wisp;
import thaumcraft.registry.ModEntities;

public record MobAspect(String name, Class<? extends Entity> type, Supplier<EntityType<?>> entityType, List<Aspect> aspects) {
    public static final List<MobAspect> ALL = List.of(
        entry("SnowMan", SnowGolem.class, () -> EntityType.SNOW_GOLEM, Aspect.WATER, Aspect.WATER, Aspect.MAN),
        entry("Bat", Bat.class, () -> EntityType.BAT, Aspect.AIR, Aspect.AIR, Aspect.FLIGHT),
        entry("Blaze", Blaze.class, () -> EntityType.BLAZE, Aspect.FIRE, Aspect.FIRE, Aspect.FIRE),
        entry("BrainyZombie", BrainyZombie.class, ModEntities.BRAINY_ZOMBIE::get, Aspect.MAGIC, Aspect.UNDEAD, Aspect.FLESH),
        entry("Firebat", FireBat.class, ModEntities.FIREBAT::get, Aspect.FLIGHT, Aspect.FIRE, Aspect.MAGIC),
        entry("CaveSpider", CaveSpider.class, () -> EntityType.CAVE_SPIDER, Aspect.BEAST, Aspect.POISON, Aspect.POISON),
        entry("Chicken", Chicken.class, () -> EntityType.CHICKEN, Aspect.CROP, Aspect.FLIGHT, Aspect.BEAST),
        entry("Cow", Cow.class, () -> EntityType.COW, Aspect.BEAST, Aspect.EARTH, Aspect.BEAST),
        entry("Creeper", Creeper.class, () -> EntityType.CREEPER, Aspect.MAGIC, Aspect.BEAST, Aspect.ELDRITCH),
        entry("EnderMan", EnderMan.class, () -> EntityType.ENDERMAN, Aspect.ELDRITCH, Aspect.ELDRITCH, Aspect.MAN),
        entry("Ghast", Ghast.class, () -> EntityType.GHAST, Aspect.FIRE, Aspect.FLIGHT, Aspect.FLIGHT),
        entry("EntityHorse", AbstractHorse.class, () -> EntityType.HORSE, Aspect.BEAST, Aspect.BEAST, Aspect.TRAVEL),
        entry("VillagerGolem", IronGolem.class, () -> EntityType.IRON_GOLEM, Aspect.METAL, Aspect.METAL, Aspect.MAN),
        entry("LavaSlime", MagmaCube.class, () -> EntityType.MAGMA_CUBE, Aspect.FIRE, Aspect.SLIME, Aspect.SLIME),
        entry("MushroomCow", MushroomCow.class, () -> EntityType.MOOSHROOM, Aspect.BEAST, Aspect.EARTH, Aspect.CROP),
        entry("Ozelot", Ocelot.class, () -> EntityType.OCELOT, Aspect.BEAST, Aspect.EARTH, Aspect.ELDRITCH),
        entry("Pig", Pig.class, () -> EntityType.PIG, Aspect.BEAST, Aspect.EARTH, Aspect.TRAVEL),
        entry("PigZombie", ZombifiedPiglin.class, () -> EntityType.ZOMBIFIED_PIGLIN, Aspect.UNDEAD, Aspect.FLESH, Aspect.FIRE),
        entry("Sheep", Sheep.class, () -> EntityType.SHEEP, Aspect.EARTH, Aspect.CLOTH, Aspect.BEAST),
        entry("Silverfish", Silverfish.class, () -> EntityType.SILVERFISH, Aspect.METAL, Aspect.METAL, Aspect.EARTH),
        entry("Skeleton", AbstractSkeleton.class, () -> EntityType.SKELETON, Aspect.UNDEAD, Aspect.MAN, Aspect.UNDEAD),
        entry("Slime", Slime.class, () -> EntityType.SLIME, Aspect.SLIME, Aspect.SLIME, Aspect.BEAST),
        entry("Spider", Spider.class, () -> EntityType.SPIDER, Aspect.BEAST, Aspect.UNDEAD, Aspect.UNDEAD),
        entry("Squid", Squid.class, () -> EntityType.SQUID, Aspect.WATER, Aspect.WATER, Aspect.WATER),
        entry("Villager", Villager.class, () -> EntityType.VILLAGER, Aspect.MAN, Aspect.MAN, Aspect.MAN),
        entry("Wisp", Wisp.class, ModEntities.WISP::get, Aspect.AIR, Aspect.MAGIC, Aspect.MAGIC),
        entry("Witch", Witch.class, () -> EntityType.WITCH, Aspect.MAGIC, Aspect.UNDEAD, Aspect.ELDRITCH),
        entry("Wolf", Wolf.class, () -> EntityType.WOLF, Aspect.BEAST, Aspect.BEAST, Aspect.BEAST),
        entry("Zombie", Zombie.class, () -> EntityType.ZOMBIE, Aspect.FLESH, Aspect.FLESH, Aspect.UNDEAD)
    );

    private static MobAspect entry(String name, Class<? extends Entity> type, Supplier<EntityType<?>> entityType, Aspect... aspects) {
        return new MobAspect(name, type, entityType, List.of(aspects));
    }

    public static @Nullable MobAspect forEntity(Entity entity) {
        for (MobAspect mob : ALL) {
            if (mob.type().isInstance(entity)) {
                return mob;
            }
        }
        return null;
    }

    public static @Nullable MobAspect forAspects(List<Aspect> aspects) {
        for (MobAspect mob : ALL) {
            if (mob.aspects().containsAll(aspects) && aspects.containsAll(mob.aspects())) {
                return mob;
            }
        }
        return null;
    }
}
