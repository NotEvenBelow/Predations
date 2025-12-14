package dev.foltz.predations.secret;

import dev.foltz.predations.config.ExtraConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;


public final class KnockbackHelper {
    private KnockbackHelper() {
    }

    public static boolean noKnockbackAll() {
        return ExtraConfig.get().secret.noKnockbackAll;
    }


    public static boolean isWhitelistedKB(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return ExtraConfig.get().secret.knockbackWhitelist.contains(id.toString());
    }
}
