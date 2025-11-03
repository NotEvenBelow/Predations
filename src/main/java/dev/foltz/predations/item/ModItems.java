package dev.foltz.predations.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    private ModItems() {}

    // 🔥 Burned Meat
    public static final Item BURNED_MEAT = new Item(new Item.Settings().food(
            new FoodComponent.Builder()
                    .hunger(1)
                    .saturationModifier(0.0f)
                    .meat()
                    .statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 15 * 20), 1.0f)
                    .statusEffect(new StatusEffectInstance(StatusEffects.UNLUCK, 10 * 20), 1.0f)
                    .statusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 10 * 20, 1), 1.0f)
                    .statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 6 * 20), 1.0f)
                    .build()
    ));

    // 🦊 Fox Feather
    public static final Item FOX_FEATHER = new Item(
            new Item.Settings().maxCount(64)
    );

    // 🦊 Fox Talisman (durability comes from config)
    public static final Item FOX_TALISMAN = new FoxTalismanItem(
            new Item.Settings().maxCount(1)
    );

    public static void register() {
        Registry.register(Registries.ITEM, new Identifier("predations", "burned_meat"), BURNED_MEAT);
        Registry.register(Registries.ITEM, new Identifier("predations", "fox_feather"), FOX_FEATHER);
        Registry.register(Registries.ITEM, new Identifier("predations", "fox_talisman"), FOX_TALISMAN);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> entries.add(BURNED_MEAT));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(FOX_FEATHER));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(FOX_TALISMAN));
    }
}
