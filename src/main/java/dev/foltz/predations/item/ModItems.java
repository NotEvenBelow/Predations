package dev.foltz.predations.item;

import dev.foltz.predations.PredationsMod;
import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.diddy.DiddyMilkItem;
import dev.foltz.predations.guano.GuanoFertilizerItem;
import dev.foltz.predations.kuru.VillagerMeatItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    private ModItems() {}

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

    public static final Item FOX_FEATHER = new Item(
            new Item.Settings().maxCount(64)
    );

    public static final Item FOX_TALISMAN = new FoxTalismanItem(
            new Item.Settings().maxCount(1)
    );

    public static final FoodComponent COOKED_VILLAGER_MEAT_FOOD = new FoodComponent.Builder()
            .hunger(8)
            .saturationModifier(0.8f)
            .meat()
            .statusEffect(new StatusEffectInstance(StatusEffects.SPEED, 30 * 20, 1), 1.0f)
            .statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 30 * 20, 0), 1.0f)
            .statusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 30 * 20, 0), 1.0f)
            .statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 30 * 20, 1), 1.0f)
            .build();

    public static final FoodComponent VILLAGER_MEAT_FOOD = new FoodComponent.Builder()
            .hunger(3)
            .saturationModifier(0.3f)
            .meat()
            .build();

    public static final Item VILLAGER_MEAT = new VillagerMeatItem(new FabricItemSettings().food(VILLAGER_MEAT_FOOD), false);
    public static final Item COOKED_VILLAGER_MEAT = new VillagerMeatItem(new FabricItemSettings().food(COOKED_VILLAGER_MEAT_FOOD), true);

    public static final Item DIDDY_MILK = new DiddyMilkItem(new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1));

    public static final Item SPLASH_POTION_OF_CURING = new SplashPotionOfCuringItem(new Item.Settings().maxCount(16));

    public static final Item LIFE_RITUAL = new LifeRitualItem(new Item.Settings().maxCount(1));

    public static final Item GUANO = new Item(new Item.Settings());

    // FIX: Removed maxDamage() to allow stacking.
    public static final Item GUANO_FERTILIZER = new GuanoFertilizerItem(new Item.Settings());

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(PredationsMod.MODID, name), item);
    }

    public static void register() {
        registerItem("burned_meat", BURNED_MEAT);
        registerItem("fox_feather", FOX_FEATHER);
        registerItem("fox_talisman", FOX_TALISMAN);
        registerItem("villager_meat", VILLAGER_MEAT);
        registerItem("cooked_villager_meat", COOKED_VILLAGER_MEAT);
        registerItem("diddy_milk", DIDDY_MILK);
        registerItem("splash_potion_of_curing", SPLASH_POTION_OF_CURING);
        registerItem("life_ritual", LIFE_RITUAL);

        registerItem("guano", GUANO);
        registerItem("guano_fertilizer", GUANO_FERTILIZER);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(BURNED_MEAT);
            entries.add(VILLAGER_MEAT);
            entries.add(COOKED_VILLAGER_MEAT);
            entries.add(DIDDY_MILK);
            entries.add(SPLASH_POTION_OF_CURING);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(FOX_FEATHER);
            entries.add(GUANO);
            entries.add(GUANO_FERTILIZER);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(FOX_TALISMAN);
            entries.add(LIFE_RITUAL);
        });
    }
}