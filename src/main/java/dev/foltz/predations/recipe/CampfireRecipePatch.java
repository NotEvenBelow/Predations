package dev.foltz.predations.recipe;

import dev.foltz.predations.item.BurnedMeatHelper;
import dev.foltz.predations.mixin.burnedmeat.RecipeManagerAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;


public final class CampfireRecipePatch {
    private CampfireRecipePatch() { }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(CampfireRecipePatch::apply);
    }

    private static void apply(MinecraftServer server) {
        if (!BurnedMeatHelper.onlySmokerGivesCookedMeat()) return;

        RecipeManager manager = server.getRecipeManager();
        RecipeManagerAccessor acc = (RecipeManagerAccessor) manager;

        Map<RecipeType<?>, Map<Identifier, Recipe<?>>> src = acc.getRecipes();
        Map<RecipeType<?>, Map<Identifier, Recipe<?>>> mutable = new HashMap<>(src.size());
        for (var e : src.entrySet()) {
            mutable.put(e.getKey(), new HashMap<>(e.getValue()));
        }

        Map<Identifier, Recipe<?>> campfireOrig = src.get(RecipeType.CAMPFIRE_COOKING);
        if (campfireOrig == null || campfireOrig.isEmpty()) {
            acc.setRecipes(mutable);
            return;
        }

        Map<Identifier, Recipe<?>> campfireMut = mutable.get(RecipeType.CAMPFIRE_COOKING);

        for (var entry : campfireOrig.entrySet()) {
            Identifier id = entry.getKey();
            Recipe<?> r = entry.getValue();
            if (!(r instanceof CampfireCookingRecipe original)) continue;
            if (original.getIngredients().isEmpty()) continue;

            Ingredient in = original.getIngredients().get(0);
            ItemStack[] matches = in.getMatchingStacks();
            if (matches.length == 0) continue;
            if (!BurnedMeatHelper.isBurnInsteadItem(matches[0].getItem())) continue;

            ItemStack cookedOut = original.getOutput(server.getRegistryManager());

            int cookTime = original.getCookTime();
            float xp     = original.getExperience();

            ChanceCampfireRecipe replacement = new ChanceCampfireRecipe(
                    id,
                    CookingRecipeCategory.FOOD,
                    in,
                    cookedOut.copy(),
                    xp,
                    cookTime,
                    BurnedMeatHelper.campfireBurnChance()
            );

            campfireMut.put(id, replacement);
        }

        acc.setRecipes(mutable);
    }
}
