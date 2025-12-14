package dev.foltz.predations.item;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class LifeRitualItem extends Item {
    public LifeRitualItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ExtraConfig.VillagerConfig config = ExtraConfig.getVillagerConfig();
        if (config != null && !config.LifeRitualFunctionEnabled) {
            if (!world.isClient) {
                user.sendMessage(Text.literal("Life Ritual is disabled in the configuration."), true);
            }
            return TypedActionResult.fail(user.getStackInHand(hand));
        }


        return TypedActionResult.success(user.getStackInHand(hand));
    }
}