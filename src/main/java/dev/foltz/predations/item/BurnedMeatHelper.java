package dev.foltz.predations.item;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;


public final class BurnedMeatHelper {
    private BurnedMeatHelper() {}


    public static boolean isEnabled() {
        return ExtraConfig.get().burnedMeat.enabled;
    }


    public static ExtraConfig.BurnedMeatEntry getEntry(LivingEntity entity) {
        var cfg = ExtraConfig.get().burnedMeat.entities;


        String path = Registries.ENTITY_TYPE.getId(entity.getType()).getPath();
        ExtraConfig.BurnedMeatEntry entry = cfg.get(path);
        if (entry != null) return entry;


        String id = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
        return cfg.get(id);
    }

    public static int rollCount(ExtraConfig.BurnedMeatEntry entry, Random rng) {
        return entry != null ? entry.nextCount(rng) : 0;
    }


    public static boolean onlySmokerGivesCookedMeat() {
        return ExtraConfig.get().burnedMeat.onlySmokerGivesCookedMeat;
    }


    public static boolean isBurnInsteadItem(Item item) {
        var burnSet = ExtraConfig.get().burnedMeat.burnInsteadItems;
        String id = Registries.ITEM.getId(item).toString();
        String path = Registries.ITEM.getId(item).getPath();
        return burnSet.contains(id) || burnSet.contains(path);
    }


    public static float campfireBurnChance() {
        return ExtraConfig.get().burnedMeat.campfireBurnChance;
    }
}
