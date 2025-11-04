package dev.foltz.predations.loot_modifier;

import dev.foltz.predations.config.LootModifierConfig;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityFlagsPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public final class LootModifier {
    private static LootModifierConfig CFG;
    private static final Map<Identifier, LootModifierConfig.Rule> BY_LOOT_TABLE = new HashMap<>();

    public static void init() {
        CFG = LootModifierConfig.load();
        BY_LOOT_TABLE.clear();

        for (var e : CFG.entities.entrySet()) {
            try {
                Identifier entId = new Identifier(e.getKey());
                EntityType<?> type = Registries.ENTITY_TYPE.get(entId);
                if (type != null) {
                    BY_LOOT_TABLE.put(type.getLootTableId(), e.getValue());
                }
            } catch (Exception ignore) {
            }
        }

        LootTableEvents.REPLACE.register((resourceManager, lootManager, id, original, source) -> {
            LootModifierConfig.Rule rule = BY_LOOT_TABLE.get(id);
            if (rule == null || !rule.enabled) return null;

            LootTable.Builder table = LootTable.builder();

            if (rule.drops == null || rule.drops.isEmpty()) {
                return table.build();
            }

            for (LootModifierConfig.Drop d : rule.drops) {
                if (d == null || d.item == null) continue;
                Optional<Item> maybeItem = Registries.ITEM.getOrEmpty(new Identifier(d.item));
                if (maybeItem.isEmpty()) continue;

                ItemEntry.Builder<?> entry = ItemEntry.builder(maybeItem.get());

                
                if (d.min == d.max) {
                    if (d.min != 1.0f) {
                        entry.apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(d.min)));
                    }
                } else {
                    float min = Math.min(d.min, d.max);
                    float max = Math.max(d.min, d.max);
                    entry.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max)));
                }

                
                if (d.itemDroppedOnFire != null && !d.itemDroppedOnFire.isEmpty()) {
                    Optional<Item> cooked = Registries.ITEM.getOrEmpty(new Identifier(d.itemDroppedOnFire));
                    if (cooked.isPresent()) {
                        var onFire = EntityPropertiesLootCondition.builder(
                                LootContext.EntityTarget.THIS,
                                EntityPredicate.Builder.create().flags(
                                        EntityFlagsPredicate.Builder.create().onFire(true).build()
                                )
                        );
                        entry.alternatively(ItemEntry.builder(cooked.get()).conditionally(onFire));
                    }
                }

                
                float p = Math.max(0f, Math.min(1f, d.chance));
                LootPool.Builder pool = LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f));
                if (p < 1f) {
                    entry.conditionally(RandomChanceLootCondition.builder(p));
                }
                pool.with(entry);

                table.pool(pool);
            }

            return table.build();
        });
    }
}
