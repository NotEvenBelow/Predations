package dev.foltz.predations.mixin.guanoAndBoneMeal;

import dev.foltz.predations.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class GuanoItemEntityMixin extends Entity {

    public GuanoItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract ItemStack getStack();
    @Shadow public abstract void setStack(ItemStack stack);

    @Inject(method = "tick", at = @At("TAIL"))
    private void transformGuanoInWater(CallbackInfo ci) {
        if (this.getWorld().isClient) return;


        ItemStack currentStack = this.getStack();
        if (currentStack.isOf(ModItems.GUANO)) {


            if (this.isSubmergedIn(FluidTags.WATER)) {


                if (this.getWorld().getFluidState(this.getBlockPos()).isStill()) {


                    ItemStack fertilizerStack = new ItemStack(ModItems.GUANO_FERTILIZER, currentStack.getCount());
                    this.setStack(fertilizerStack);


                    this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.NEUTRAL, 0.25f, 1.0f);
                }
            }
        }
    }
}