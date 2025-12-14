package dev.foltz.predations.mixin.milk;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.cow.AttackMemory;
import dev.foltz.predations.util.CowCooldownInterface;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CowEntity.class)
public abstract class CowMilkingMixin extends AnimalEntity implements CowCooldownInterface {

    @Unique
    private long lastMilkedTick = -1;

    protected CowMilkingMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override public long predations$getLastMilked() { return lastMilkedTick; }
    @Override public void predations$setLastMilked(long tick) { this.lastMilkedTick = tick; }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void checkMilkingCooldown(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.isOf(Items.BUCKET) && !this.isBaby()) {
            ExtraConfig.MilkChangeConfig config = ExtraConfig.getMilkConfig();

            if (config != null && config.milkCowCooldownSeconds > 0) {
                long currentTick = this.getWorld().getTime();
                long cooldownTicks = config.milkCowCooldownSeconds * 20L;

                if (lastMilkedTick == -1 || (currentTick - lastMilkedTick) >= cooldownTicks) {
                    lastMilkedTick = currentTick;

                    ExtraConfig.AngryMob angryConfig = ExtraConfig.angryFor(this);
                    int angerDuration = (angryConfig != null && angryConfig.kickActiveWindowTicks != null)
                            ? angryConfig.kickActiveWindowTicks
                            : 120;

                    AttackMemory.mark(this, currentTick, angerDuration, player);

                } else {
                    if (!this.getWorld().isClient) {
                        long secondsLeft = (cooldownTicks - (currentTick - lastMilkedTick)) / 20L;
                        player.sendMessage(Text.literal("Dad is not home yet, dad will be home in " + secondsLeft + "s."), true);
                        this.playSound(SoundEvents.ENTITY_COW_AMBIENT, 1.0f, 0.5f);
                    }
                    cir.setReturnValue(ActionResult.FAIL);
                }
            }
        }
    }
}