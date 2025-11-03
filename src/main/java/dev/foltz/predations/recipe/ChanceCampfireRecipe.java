package dev.foltz.predations.recipe;

import dev.foltz.predations.item.ModItems;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

/**
 * Campfire recipe that rolls per craft: cooked vs burned.
 * Constructor signature matches 1.20.1:
 * (id, category, ingredient, output, xp, cookTime, burnChance)
 */
public final class ChanceCampfireRecipe extends CampfireCookingRecipe {
    private final float burnChance;
    private final ItemStack cookedOut;

    public ChanceCampfireRecipe(
            Identifier id,
            CookingRecipeCategory category,
            Ingredient input,
            ItemStack cookedOut,
            float experience,
            int cookTime,
            float burnChance
    ) {

        super(id, "", category, input, cookedOut, experience, cookTime);
        this.cookedOut = cookedOut.copy();
        this.burnChance = Math.max(0f, Math.min(1f, burnChance));
    }

    private ItemStack rollOnce() {
        return (Random.create().nextFloat() < burnChance)
                ? new ItemStack(ModItems.BURNED_MEAT)
                : cookedOut.copy();
    }

    @Override
    public ItemStack craft(Inventory inv, DynamicRegistryManager registries) {
        return rollOnce();
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registries) {
        return rollOnce();
    }
}
