package dev.foltz.predations.mixin.villager;

import dev.foltz.predations.villager.VillagerZombificationInstaller;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieVillagerEntity.class)
public class ZombieVillagerCureMixin {

    @Inject(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/ZombieVillagerEntity;convertTo(Lnet/minecraft/entity/EntityType;Z)Lnet/minecraft/entity/mob/MobEntity;"))
    private void beforeConvert(ServerWorld world, CallbackInfo ci) {
        VillagerZombificationInstaller.setIsCuring(true);
    }

    @Inject(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/ZombieVillagerEntity;convertTo(Lnet/minecraft/entity/EntityType;Z)Lnet/minecraft/entity/mob/MobEntity;", shift = At.Shift.AFTER))
    private void afterConvert(ServerWorld world, CallbackInfo ci) {
        VillagerZombificationInstaller.setIsCuring(false);
    }
}