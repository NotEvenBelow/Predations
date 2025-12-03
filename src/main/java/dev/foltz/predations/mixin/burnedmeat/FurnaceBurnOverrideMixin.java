// Helper in item/BurnedMeatHelper for extraconfig idk why the fuck i put in it in item lmao
package dev.foltz.predations.mixin.burnedmeat;

import dev.foltz.predations.item.BurnedMeatHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(net.minecraft.recipe.RecipeManager.class)
public abstract class FurnaceBurnOverrideMixin {

    @Shadow @Final @Mutable
    private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("TAIL"))
    private void predations$replaceRecipes(Map<Identifier, Recipe<?>> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {

        boolean configEnabled = BurnedMeatHelper.onlySmokerGivesCookedMeat();

        if (!configEnabled) {
            return;
        }

        var burnedItem = Registries.ITEM.get(new Identifier("predations", "burned_meat"));
        if (burnedItem == Items.AIR) {
            return;
        }

        Map<Identifier, Recipe<?>> newBurnedRecipes = new HashMap<>();
        int found = 0;

        Map<Identifier, Recipe<?>> originalSmeltingMap = this.recipes.getOrDefault(RecipeType.SMELTING, Map.of());

        for (Recipe<?> recipe : originalSmeltingMap.values()) {
            if (!(recipe instanceof SmeltingRecipe smelt)) {
                continue;
            }

            DefaultedList<Ingredient> ingredients = smelt.getIngredients();
            if (ingredients.isEmpty()) continue;

            Ingredient inputIngredient = ingredients.get(0);
            boolean shouldBurn = false;

            for (ItemStack stack : inputIngredient.getMatchingStacks()) {
                Item item = stack.getItem();

                if (BurnedMeatHelper.isBurnInsteadItem(item)) {
                    shouldBurn = true;
                    break;
                }
            }

            if (shouldBurn) {
                found++;

                Identifier id = smelt.getId();
                SmeltingRecipe burned = new SmeltingRecipe(
                        id,
                        smelt.getGroup(),
                        smelt.getCategory(),
                        inputIngredient,
                        new ItemStack(burnedItem),
                        smelt.getExperience(),
                        smelt.getCookTime()
                );
                newBurnedRecipes.put(id, burned);
            }
        }

        if (found > 0) {
            try {
                Map<RecipeType<?>, Map<Identifier, Recipe<?>>> newMasterMap = new HashMap<>(this.recipes);
                Map<Identifier, Recipe<?>> newSmeltingMap = new HashMap<>(originalSmeltingMap);

                newSmeltingMap.putAll(newBurnedRecipes);
                newMasterMap.put(RecipeType.SMELTING, newSmeltingMap);

                this.recipes = newMasterMap;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}