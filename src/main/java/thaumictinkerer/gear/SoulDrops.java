package thaumictinkerer.gear;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.AbstractCow;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.entity.monster.BrainyZombie;
import thaumcraft.entity.monster.FireBat;
import thaumcraft.entity.monster.Wisp;
import thaumictinkerer.ThaumicTinkerer;

public final class SoulDrops {
    private record Entry(Class<? extends Entity> type, Aspect... aspects) {
    }

    private static final List<Entry> ENTRIES = List.of(
        new Entry(SnowGolem.class, Aspect.WATER, Aspect.WATER, Aspect.MAN),
        new Entry(Bat.class, Aspect.AIR, Aspect.AIR, Aspect.FLIGHT),
        new Entry(Blaze.class, Aspect.FIRE, Aspect.FIRE, Aspect.FIRE),
        new Entry(BrainyZombie.class, Aspect.MAGIC, Aspect.UNDEAD, Aspect.FLESH),
        new Entry(FireBat.class, Aspect.FLIGHT, Aspect.FIRE, Aspect.MAGIC),
        new Entry(CaveSpider.class, Aspect.BEAST, Aspect.POISON, Aspect.POISON),
        new Entry(Chicken.class, Aspect.CROP, Aspect.FLIGHT, Aspect.BEAST),
        new Entry(AbstractCow.class, Aspect.BEAST, Aspect.EARTH, Aspect.BEAST),
        new Entry(Creeper.class, Aspect.MAGIC, Aspect.BEAST, Aspect.ELDRITCH),
        new Entry(Enderman.class, Aspect.ELDRITCH, Aspect.ELDRITCH, Aspect.MAN),
        new Entry(Ghast.class, Aspect.FIRE, Aspect.FLIGHT, Aspect.FLIGHT),
        new Entry(AbstractHorse.class, Aspect.BEAST, Aspect.BEAST, Aspect.TRAVEL),
        new Entry(IronGolem.class, Aspect.METAL, Aspect.METAL, Aspect.MAN),
        new Entry(MagmaCube.class, Aspect.FIRE, Aspect.SLIME, Aspect.SLIME),
        new Entry(Ocelot.class, Aspect.BEAST, Aspect.EARTH, Aspect.ELDRITCH),
        new Entry(Pig.class, Aspect.BEAST, Aspect.EARTH, Aspect.TRAVEL),
        new Entry(ZombifiedPiglin.class, Aspect.UNDEAD, Aspect.FLESH, Aspect.FIRE),
        new Entry(Sheep.class, Aspect.EARTH, Aspect.CLOTH, Aspect.BEAST),
        new Entry(Silverfish.class, Aspect.METAL, Aspect.METAL, Aspect.EARTH),
        new Entry(AbstractSkeleton.class, Aspect.UNDEAD, Aspect.MAN, Aspect.UNDEAD),
        new Entry(Slime.class, Aspect.SLIME, Aspect.SLIME, Aspect.BEAST),
        new Entry(Spider.class, Aspect.BEAST, Aspect.UNDEAD, Aspect.UNDEAD),
        new Entry(Squid.class, Aspect.WATER, Aspect.WATER, Aspect.WATER),
        new Entry(Villager.class, Aspect.MAN, Aspect.MAN, Aspect.MAN),
        new Entry(Wisp.class, Aspect.AIR, Aspect.MAGIC, Aspect.MAGIC),
        new Entry(Witch.class, Aspect.MAGIC, Aspect.UNDEAD, Aspect.ELDRITCH),
        new Entry(Wolf.class, Aspect.BEAST, Aspect.BEAST, Aspect.BEAST),
        new Entry(Zombie.class, Aspect.FLESH, Aspect.FLESH, Aspect.UNDEAD)
    );

    private SoulDrops() {
    }

    public static Aspect @Nullable [] aspectsFor(Entity entity) {
        for (Entry entry : ENTRIES) {
            if (entry.type().isInstance(entity)) {
                return entry.aspects();
            }
        }
        return null;
    }

    public static ItemStack soul(Aspect aspect) {
        Item item = BuiltInRegistries.ITEM.getValue(ThaumicTinkerer.id(aspect.getTag() + "_soul_aspect"));
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public static @Nullable List<ItemStack> soulsFor(Entity entity) {
        Aspect[] aspects = aspectsFor(entity);
        if (aspects == null) {
            return null;
        }
        List<ItemStack> souls = new ArrayList<>();
        for (Aspect aspect : aspects) {
            ItemStack stack = soul(aspect);
            if (stack.isEmpty()) {
                return null;
            }
            souls.add(stack);
        }
        return souls;
    }
}
