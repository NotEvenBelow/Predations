package dev.foltz.predations.mixin.rabies;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.rabiesEffect.BatEntityAccess;
import dev.foltz.predations.rabiesEffect.ModEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BatEntity.class)
public abstract class BatEntityMixin extends AmbientEntity implements BatEntityAccess {

    @Unique
    private static final TrackedData<Boolean> PREDATIONS_SONAR_ACTIVE = DataTracker.registerData(BatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Unique
    private int predations$sonarTimer = 0;

    protected BatEntityMixin(EntityType<? extends AmbientEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initSonarData(CallbackInfo ci) {
        this.dataTracker.startTracking(PREDATIONS_SONAR_ACTIVE, false);
    }

    @Override
    public boolean predations$isSonarActive() {
        return this.dataTracker.get(PREDATIONS_SONAR_ACTIVE);
    }

    @Override
    public void predations$setSonarActive(boolean active) {
        this.dataTracker.set(PREDATIONS_SONAR_ACTIVE, active);
    }

    @Inject(method = "mobTick", at = @At("HEAD"), cancellable = true)
    private void tickSonarTimer(CallbackInfo ci) {
        // 1. Handle Sonar Timer
        if (ExtraConfig.getRabiesConfig().enabled) {
            if (!this.getWorld().isClient) {
                if (this.predations$sonarTimer > 0) {
                    this.predations$sonarTimer--;
                    if (this.predations$sonarTimer == 0) {
                        this.predations$setSonarActive(false);
                    }
                }
            }
        }

        Entity holder = this.getHoldingEntity();
        if (ExtraConfig.getBatGuanoConfig().batCanLeash && holder != null) {
            ci.cancel();

            double dist = this.distanceTo(holder);
            if (dist > 3.5) {
                Vec3d pull = holder.getPos().subtract(this.getPos()).normalize().multiply(0.3);
                Vec3d currentVel = this.getVelocity();
                this.setVelocity(currentVel.add(pull).multiply(0.9));
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeSonarNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("PredationsSonarTimer", this.predations$sonarTimer);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readSonarNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("PredationsSonarTimer")) {
            this.predations$sonarTimer = nbt.getInt("PredationsSonarTimer");
            this.predations$setSonarActive(this.predations$sonarTimer > 0);
        }
    }

    @Override
    public boolean canBeLeashedBy(PlayerEntity player) {
        ExtraConfig.BatGuanoAndBoneMealConfig config = ExtraConfig.getBatGuanoConfig();
        if (!config.enabled) {
            return super.canBeLeashedBy(player);
        }
        return config.batCanLeash;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!ExtraConfig.getRabiesConfig().enabled) {
            return super.interactMob(player, hand);
        }

        ItemStack stack = player.getStackInHand(hand);
        boolean isSweetBerry = stack.isOf(Items.SWEET_BERRIES);
        boolean isGlowBerry = stack.isOf(Items.GLOW_BERRIES);

        if (isSweetBerry || isGlowBerry) {
            if (!this.getWorld().isClient) {
                ExtraConfig.RabiesConfig config = ExtraConfig.getRabiesConfig();

                if (this.random.nextDouble() < config.batWhenFeedingRabiesBitChance) {
                    if (this.random.nextDouble() < config.batRabiesChanceWhenFinallyBit) {
                        player.damage(this.getDamageSources().mobAttack(this), 1.0f);
                        player.addStatusEffect(new StatusEffectInstance(ModEffects.RABIES, 999999, 0, false, false));
                        this.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    }
                }

                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }

                if (isGlowBerry) {
                    this.predations$setSonarActive(true);
                    this.predations$sonarTimer = 160;
                    this.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0f, 1.5f);
                } else {
                    this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
                }
            }
            return ActionResult.SUCCESS;
        }

        return super.interactMob(player, hand);
    }
}