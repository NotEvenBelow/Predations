package dev.foltz.predations.kuru;

import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.item.TalismanImmunityTracker;
import dev.foltz.predations.kuru_status.KuruStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class VillagerMeatItem extends Item {
    private final boolean isCooked;

    public VillagerMeatItem(Item.Settings settings, boolean isCooked) {
        super(settings);
        this.isCooked = isCooked;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof PlayerEntity player) {

            if (!user.hasStatusEffect(KuruStatusEffects.KURU)) {

                double chanceToGetKuru;
                var config = ExtraConfig.get();

                if (TalismanImmunityTracker.isImmune(player)) {
                    chanceToGetKuru = 1.0 - config.foxItems.TalismanChanceToNotGetKuruWhenEatingVillagerMeat;
                } else {
                    chanceToGetKuru = config.kuru.kuruChanceforVillagerMeatNoTalisman;
                }

                if (world.random.nextFloat() < chanceToGetKuru) {
                    user.addStatusEffect(new StatusEffectInstance(KuruStatusEffects.KURU, Integer.MAX_VALUE, 0, false, false, true));
                }
            }
        }

        return super.finishUsing(stack, world, user);
    }
}