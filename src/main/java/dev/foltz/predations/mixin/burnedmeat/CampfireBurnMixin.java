// Helper in item/BurnedMeatHelper for extraconfig idk why the fuck i put in it in item lmao
package dev.foltz.predations.mixin.burnedmeat;

import dev.foltz.predations.item.BurnedMeatHelper;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBurnMixin {
    private static final Random RNG = Random.create();
    private static Item PREDATIONS_BURNED_MEAT = null;

    private Item getBurnedMeatItem() {
        if (PREDATIONS_BURNED_MEAT == null) {
            PREDATIONS_BURNED_MEAT = Registries.ITEM.get(new Identifier("predations", "burned_meat"));
        }
        return PREDATIONS_BURNED_MEAT;
    }

    @Inject(method = "getRecipeFor", at = @At("HEAD"), cancellable = true)
    private void predations$burnInstead(ItemStack stack, CallbackInfoReturnable<Optional<CampfireCookingRecipe>> cir) {
        if (!BurnedMeatHelper.onlySmokerGivesCookedMeat()) return;
        if (!BurnedMeatHelper.isBurnInsteadItem(stack.getItem())) return;
        if (RNG.nextFloat() >= BurnedMeatHelper.campfireBurnChance()) return;

        Item burnedMeat = getBurnedMeatItem();
        if (burnedMeat == null || burnedMeat == Items.AIR) {
            return;
        }

        Item item = stack.getItem();
        Identifier itemId = Registries.ITEM.getId(item);
        Identifier id = new Identifier("predations", "burned_campfire_" + itemId.getPath());

        CampfireCookingRecipe fake = new CampfireCookingRecipe(
                id,
                "",
                CookingRecipeCategory.FOOD,
                Ingredient.ofItems(item),
                new ItemStack(burnedMeat),
                0.0f,
                600
        );
        cir.setReturnValue(Optional.of(fake));
    }
}