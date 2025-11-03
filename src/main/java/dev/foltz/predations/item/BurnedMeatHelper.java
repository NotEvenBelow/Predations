package dev.foltz.predations.item;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;

/**
 * Central access for burned meat config and logic.
 */
public final class BurnedMeatHelper {
    private BurnedMeatHelper() {}

    /** Global toggle: fire/lava deaths produce burned meat instead of vanilla loot. */
    public static boolean isEnabled() {
        return ExtraConfig.get().burnedMeat.enabled;
    }

    /** Per-entity entry: lookup by path-only ("cow") or full id ("minecraft:cow"). */
    public static ExtraConfig.BurnedMeatEntry getEntry(LivingEntity entity) {
        var cfg = ExtraConfig.get().burnedMeat.entities;

        // path-only
        String path = Registries.ENTITY_TYPE.getId(entity.getType()).getPath();
        ExtraConfig.BurnedMeatEntry entry = cfg.get(path);
        if (entry != null) return entry;

        // full id
        String id = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
        return cfg.get(id);
    }

    /** Roll a random count between min and max, inclusive. */
    public static int rollCount(ExtraConfig.BurnedMeatEntry entry, Random rng) {
        return entry != null ? entry.nextCount(rng) : 0;
    }

    /** Config toggle: only smoker gives cooked meat. */
    public static boolean onlySmokerGivesCookedMeat() {
        return ExtraConfig.get().burnedMeat.onlySmokerGivesCookedMeat;
    }

    /** Check if an item should burn instead of cooking. */
    public static boolean isBurnInsteadItem(Item item) {
        var burnSet = ExtraConfig.get().burnedMeat.burnInsteadItems;
        String id = Registries.ITEM.getId(item).toString();
        String path = Registries.ITEM.getId(item).getPath();
        return burnSet.contains(id) || burnSet.contains(path);
    }

    /** Config value: chance [0..1] for campfire cooking to burn instead. */
    public static float campfireBurnChance() {
        return ExtraConfig.get().burnedMeat.campfireBurnChance;
    }
}
