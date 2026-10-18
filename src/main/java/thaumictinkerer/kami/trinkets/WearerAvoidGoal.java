package thaumictinkerer.kami.trinkets;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;

public class WearerAvoidGoal extends AvoidEntityGoal<Player> {
    public WearerAvoidGoal(Creeper creeper) {
        super(creeper, Player.class, CatAmuletItem::isWearing, 6.0F, 1.0D, 1.2D, entity -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));
    }
}
