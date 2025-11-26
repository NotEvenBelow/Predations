package dev.foltz.predations.diddy;

import dev.foltz.predations.kuru_status.KuruStatusEffects;
import dev.foltz.predations.rabiesEffect.ModEffects; // <-- ADDED IMPORT
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class DiddyMilkItem extends Item {
    public DiddyMilkItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            user.removeStatusEffect(KuruStatusEffects.KURU);
            user.removeStatusEffect(ModEffects.RABIES);
        }

        if (user instanceof PlayerEntity player) {
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        if (stack.isEmpty()) {
            return new ItemStack(Items.BUCKET);
        } else {
            if (user instanceof PlayerEntity player && !player.getAbilities().creativeMode) {
                player.getInventory().offerOrDrop(new ItemStack(Items.BUCKET));
            }
            return stack;
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }
}