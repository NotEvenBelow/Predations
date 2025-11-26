package dev.foltz.predations.mixin.client.rabies;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.rabiesEffect.BatEntityAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class ClientEntityGlowMixin {

    @Shadow public abstract World getWorld();
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();

    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
    private void checkBatSonarGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (!this.getWorld().isClient || cir.getReturnValue()) {
            return;
        }


        if (!ExtraConfig.getRabiesConfig().enabled) {
            return;
        }

        double range = ExtraConfig.getRabiesConfig().batGlowOtherEntitiesRange;
        if (range <= 0) return;

        Box searchBox = new Box(
                this.getX() - range, this.getY() - range, this.getZ() - range,
                this.getX() + range, this.getY() + range, this.getZ() + range
        );

        List<BatEntity> nearbyBats = this.getWorld().getEntitiesByClass(BatEntity.class, searchBox, bat -> {
            return bat instanceof BatEntityAccess access && access.predations$isSonarActive();
        });

        if (!nearbyBats.isEmpty()) {
            cir.setReturnValue(true);
        }
    }
}