// Helper in item/BurnedMeatHelper for extraconfig
package dev.foltz.predations.mixin.burnedmeat;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceAccessor {
    @Accessor("inventory")
    DefaultedList<ItemStack> getInventory();
}
