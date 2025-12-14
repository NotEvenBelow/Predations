package dev.foltz.predations.kuru_status;

import dev.foltz.predations.PredationsMod;
import dev.foltz.predations.kuru.KuruEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class KuruStatusEffects {
    public static StatusEffect KURU;

    public static StatusEffect registerStatusEffect(String name, StatusEffect effect) {
        return Registry.register(Registries.STATUS_EFFECT, new Identifier(PredationsMod.MODID, name), effect);
    }

    public static void register() {
        KURU = registerStatusEffect("kuru", new KuruEffect());
    }
}