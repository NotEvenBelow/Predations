package dev.foltz.predations.mixin.pufferfish;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.pufferfish.PufferfishJackpotAdvancement;
import dev.foltz.predations.pufferfish.PufferfishJackpotTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public class PufferfishItemMixin {

    @Inject(method = "finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;",
            at = @At("HEAD"))
    private void onFinishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!stack.isOf(Items.PUFFERFISH)) return;
        if (world.isClient) return;
        if (!(user instanceof ServerPlayerEntity player)) return;

        ExtraConfig.PufferfishConfig config = ExtraConfig.getPufferfishConfig();
        boolean jackpot = world.getRandom().nextDouble() < config.jackpotChance;

        PufferfishJackpotTracker.setJackpot(player, jackpot);

        if (jackpot) {
            for (ExtraConfig.PufferfishEffectEntry effectConfig : config.jackpotEffects) {
                Registries.STATUS_EFFECT.getOrEmpty(new Identifier(effectConfig.effectId))
                        .ifPresent(effect -> player.addStatusEffect(
                                new StatusEffectInstance(effect,
                                        effectConfig.durationSeconds * 20,
                                        effectConfig.amplifier,
                                        false, false, true)
                        ));
            }

            PufferfishJackpotAdvancement.grant(player);

            player.removeStatusEffect(StatusEffects.POISON);
            player.removeStatusEffect(StatusEffects.NAUSEA);
            player.removeStatusEffect(StatusEffects.HUNGER);
        }
    }
}

