package thaumictinkerer.necromancy;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.Villager;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.entity.monster.BrainyZombie;
import thaumcraft.entity.monster.FireBat;
import thaumcraft.entity.monster.Wisp;
import thaumcraft.registry.ModEntities;

public record MobAspect(String name, Class<? extends Entity> type, Supplier<EntityType<?>> entityType, List<Aspect> aspects) {
    public static final List<MobAspect> ALL = List.of(
        entry("SnowMan", SnowGolem.class, () -> EntityTypes.SNOW_GOLEM, Aspect.WATER, Aspect.WATER, Aspect.MAN),
        entry("Bat", Bat.class, () -> EntityTypes.BAT, Aspect.AIR, Aspect.AIR, Aspect.FLIGHT),
        entry("Blaze", Blaze.class, () -> EntityTypes.BLAZE, Aspect.FIRE, Aspect.FIRE, Aspect.FIRE),
        entry("BrainyZombie", BrainyZombie.class, ModEntities.BRAINY_ZOMBIE::get, Aspect.MAGIC, Aspect.UNDEAD, Aspect.FLESH),
        entry("Firebat", FireBat.class, ModEntities.FIREBAT::get, Aspect.FLIGHT, Aspect.FIRE, Aspect.MAGIC),
        entry("CaveSpider", CaveSpider.class, () -> EntityTypes.CAVE_SPIDER, Aspect.BEAST, Aspect.POISON, Aspect.POISON),
        entry("Chicken", Chicken.class, () -> EntityTypes.CHICKEN, Aspect.CROP, Aspect.FLIGHT, Aspect.BEAST),
        entry("Cow", AbstractCow.class, () -> EntityTypes.COW, Aspect.BEAST, Aspect.EARTH, Aspect.BEAST),
        entry("Creeper", Creeper.class, () -> EntityTypes.CREEPER, Aspect.MAGIC, Aspect.BEAST, Aspect.ELDRITCH),
        entry("Enderman", Enderman.class, () -> EntityTypes.ENDERMAN, Aspect.ELDRITCH, Aspect.ELDRITCH, Aspect.MAN),
        entry("Ghast", Ghast.class, () -> EntityTypes.GHAST, Aspect.FIRE, Aspect.FLIGHT, Aspect.FLIGHT),
        entry("EntityHorse", AbstractHorse.class, () -> EntityTypes.HORSE, Aspect.BEAST, Aspect.BEAST, Aspect.TRAVEL),
        entry("VillagerGolem", IronGolem.class, () -> EntityTypes.IRON_GOLEM, Aspect.METAL, Aspect.METAL, Aspect.MAN),
        entry("LavaSlime", MagmaCube.class, () -> EntityTypes.MAGMA_CUBE, Aspect.FIRE, Aspect.SLIME, Aspect.SLIME),
        entry("MushroomCow", MushroomCow.class, () -> EntityTypes.MOOSHROOM, Aspect.BEAST, Aspect.EARTH, Aspect.CROP),
        entry("Ozelot", Ocelot.class, () -> EntityTypes.OCELOT, Aspect.BEAST, Aspect.EARTH, Aspect.ELDRITCH),
        entry("Pig", Pig.class, () -> EntityTypes.PIG, Aspect.BEAST, Aspect.EARTH, Aspect.TRAVEL),
        entry("PigZombie", ZombifiedPiglin.class, () -> EntityTypes.ZOMBIFIED_PIGLIN, Aspect.UNDEAD, Aspect.FLESH, Aspect.FIRE),
        entry("Sheep", Sheep.class, () -> EntityTypes.SHEEP, Aspect.EARTH, Aspect.CLOTH, Aspect.BEAST),
        entry("Silverfish", Silverfish.class, () -> EntityTypes.SILVERFISH, Aspect.METAL, Aspect.METAL, Aspect.EARTH),
        entry("Skeleton", AbstractSkeleton.class, () -> EntityTypes.SKELETON, Aspect.UNDEAD, Aspect.MAN, Aspect.UNDEAD),
        entry("Slime", Slime.class, () -> EntityTypes.SLIME, Aspect.SLIME, Aspect.SLIME, Aspect.BEAST),
        entry("Spider", Spider.class, () -> EntityTypes.SPIDER, Aspect.BEAST, Aspect.UNDEAD, Aspect.UNDEAD),
        entry("Squid", Squid.class, () -> EntityTypes.SQUID, Aspect.WATER, Aspect.WATER, Aspect.WATER),
        entry("Villager", Villager.class, () -> EntityTypes.VILLAGER, Aspect.MAN, Aspect.MAN, Aspect.MAN),
        entry("Wisp", Wisp.class, ModEntities.WISP::get, Aspect.AIR, Aspect.MAGIC, Aspect.MAGIC),
        entry("Witch", Witch.class, () -> EntityTypes.WITCH, Aspect.MAGIC, Aspect.UNDEAD, Aspect.ELDRITCH),
        entry("Wolf", Wolf.class, () -> EntityTypes.WOLF, Aspect.BEAST, Aspect.BEAST, Aspect.BEAST),
        entry("Zombie", Zombie.class, () -> EntityTypes.ZOMBIE, Aspect.FLESH, Aspect.FLESH, Aspect.UNDEAD)
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
