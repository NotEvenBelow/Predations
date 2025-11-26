package dev.foltz.predations.mixin.milk;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.kuru_status.KuruStatusEffects;
import dev.foltz.predations.rabiesEffect.ModEffects;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public class MilkChangeMixin {

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    public void finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        ExtraConfig.MilkChangeConfig milkConfig = ExtraConfig.getMilkConfig();

        if (milkConfig == null || !milkConfig.milkRestoreHungerInsteadRemovingEffect) {
            return;
        }


        if (!world.isClient) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 1200, 1));

            if (ExtraConfig.getKuruConfig().milkCanCureKuru) {
                user.removeStatusEffect(KuruStatusEffects.KURU);
            }
            if (ExtraConfig.getRabiesConfig().milkRemoveRabies) {
                user.removeStatusEffect(ModEffects.RABIES);
            }
        }

        if (user instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat((Item)(Object)this));
        }

        if (user instanceof PlayerEntity player) {
            player.getHungerManager().add((int) milkConfig.milkHungerRestore, milkConfig.milkSaturation);

            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        if (stack.isEmpty()) {
            cir.setReturnValue(new ItemStack(Items.BUCKET));
        } else {
            cir.setReturnValue(stack);
        }
    }
}