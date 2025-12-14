package dev.foltz.predations.mixin.sniffer;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class TorchflowerLightMixin {

    @Shadow public abstract Block getBlock();

    @Inject(method = "getLuminance", at = @At("HEAD"), cancellable = true)
    private void overrideTorchflowerLight(CallbackInfoReturnable<Integer> cir) {
        ExtraConfig.BetterSnifferRelatedFeaturesConfig config = ExtraConfig.getSnifferConfig();

        if (!config.enabled) return;

        if (this.getBlock() == Blocks.TORCHFLOWER) {
            cir.setReturnValue(config.torchflowerEmitsLightLevel);
        }
    }
}