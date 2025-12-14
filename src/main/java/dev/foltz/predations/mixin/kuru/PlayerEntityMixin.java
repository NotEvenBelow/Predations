package dev.foltz.predations.mixin.kuru;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.kuru.KuruHandler;
import dev.foltz.predations.kuru_status.KuruStatusEffects;
import dev.foltz.predations.util.IEntityDataSaver;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.nbt.NbtCompound;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    private static final String KURU_ZOMBIE_SPAWNED_KEY = "PredationsKuruZombieSpawned";

    @Inject(method = "tick", at = @At("TAIL"))
    private void spawnZombieIfHealthZero(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        World world = player.getWorld();

        if (world.isClient) return;

        boolean hasKuru = player.hasStatusEffect(KuruStatusEffects.KURU);
        boolean shouldZombify = ExtraConfig.getKuruConfig().zombifiedIfDieWithKuru;

        IEntityDataSaver dataSaver = (IEntityDataSaver) player;
        NbtCompound data = dataSaver.getPersistentData();
        boolean alreadySpawned = data.getBoolean(KURU_ZOMBIE_SPAWNED_KEY);

        if (hasKuru && shouldZombify && player.getHealth() <= 0 && !alreadySpawned) {
            try {
                ZombieEntity zombie = new ZombieEntity(EntityType.ZOMBIE, world);
                zombie.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());

                if (player.getName() != null)
                    zombie.setCustomName(Text.of(player.getName().getString()));

                zombie.setCanPickUpLoot(true);
                world.spawnEntity(zombie);

                KuruHandler.clearKuru(player);
                data.putBoolean(KURU_ZOMBIE_SPAWNED_KEY, true);

            } catch (Exception e) {
                System.err.println("[Predations] Failed to spawn Kuru zombie: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (player.getHealth() > 0) {
            data.putBoolean(KURU_ZOMBIE_SPAWNED_KEY, false);
        }
    }
}
