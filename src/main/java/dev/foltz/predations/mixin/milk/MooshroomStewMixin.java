package dev.foltz.predations.mixin.milk;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.cow.AttackMemory;
import dev.foltz.predations.util.CowCooldownInterface;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MooshroomEntity.class)
public abstract class MooshroomStewMixin {

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void checkStewCooldown(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);

        MooshroomEntity self = (MooshroomEntity) (Object) this;

        if (stack.isOf(Items.BOWL) && !self.isBaby()) {
            ExtraConfig.MilkChangeConfig config = ExtraConfig.getMilkConfig();

            if (config != null && config.milkCowCooldownSeconds > 0) {
                long currentTick = self.getWorld().getTime();
                long cooldownTicks = config.milkCowCooldownSeconds * 20L;

                CowCooldownInterface tracker = (CowCooldownInterface) self;
                long lastMilked = tracker.predations$getLastMilked();

                if (lastMilked == -1 || (currentTick - lastMilked) >= cooldownTicks) {
                    tracker.predations$setLastMilked(currentTick);

                    ExtraConfig.AngryMob angryConfig = ExtraConfig.angryFor(self);
                    int angerDuration = (angryConfig != null && angryConfig.kickActiveWindowTicks != null)
                            ? angryConfig.kickActiveWindowTicks
                            : 120;

                    AttackMemory.mark(self, currentTick, angerDuration, player);

                } else {
                    if (!self.getWorld().isClient) {
                        long secondsLeft = (cooldownTicks - (currentTick - lastMilked)) / 20L;

                        player.sendMessage(Text.literal("Wait bro I don't have infinite food " + secondsLeft + "s."), true);

                        self.playSound(SoundEvents.ENTITY_COW_AMBIENT, 1.0f, 0.5f);
                    }

                    cir.setReturnValue(ActionResult.FAIL);
                }
            }
        }
    }
}