package dev.foltz.predations.mixin.llama;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.passive.LlamaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LlamaEntity.class)
public class LlamaAttributeMixin {
    @Inject(method = "createLlamaAttributes", at = @At("RETURN"), cancellable = true)
    private static void predations$addAttackAttribute(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
    }
}