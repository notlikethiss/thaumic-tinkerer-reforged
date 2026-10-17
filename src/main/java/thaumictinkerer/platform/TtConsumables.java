package thaumictinkerer.platform;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumables;

public final class TtConsumables {
    private TtConsumables() {
    }

    public static Item.Properties drinkable(Item.Properties properties, int color, List<MobEffectInstance> effects) {
        return properties
            .stacksTo(1)
            .component(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(color), effects, Optional.empty()))
            .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK)
            .usingConvertsTo(Items.GLASS_BOTTLE);
    }
}
