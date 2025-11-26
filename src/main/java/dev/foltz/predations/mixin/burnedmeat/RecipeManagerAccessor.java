// Helper in item/BurnedMeatHelper for extraconfig idk why the fuck i put in it in item lmao
package dev.foltz.predations.mixin.burnedmeat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

import java.util.Map;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Accessor("recipes")
    Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipes();

    @Mutable
    @Accessor("recipes")
    void setRecipes(Map<RecipeType<?>, Map<Identifier, Recipe<?>>> value);
}
