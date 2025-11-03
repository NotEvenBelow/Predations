/** Might be needed in the future**/


/**package dev.foltz.predations.mixin;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.squid.ModSquidEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class SquidSpawnReplaceMixin {
    /**
     * Replace vanilla squid/glow squid spawns with predatory versions.
     */
    /**@Inject(
            method = "spawnEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void predation$replaceSquid(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!ExtraConfig.get().predatorySquid.enabled) return;

        EntityType<?> type = entity.getType();
        ServerWorld world = (ServerWorld) (Object) this;

        if (type == EntityType.SQUID) {
            Entity replacement = ModSquidEntities.PREDATORY_SQUID.create(world);
            if (replacement != null) {
                replacement.refreshPositionAndAngles(entity.getBlockPos(), world.random.nextFloat() * 360.0F, 0.0F);
                world.spawnEntity(replacement);
                cir.setReturnValue(false); // cancel original
            }
        }
        else if (type == EntityType.GLOW_SQUID) {
            Entity replacement = ModSquidEntities.PREDATORY_GLOW_SQUID.create(world);
            if (replacement != null) {
                replacement.refreshPositionAndAngles(entity.getBlockPos(), world.random.nextFloat() * 360.0F, 0.0F);
                world.spawnEntity(replacement);
                cir.setReturnValue(false); // cancel original
            }
        }
    }
}**/
