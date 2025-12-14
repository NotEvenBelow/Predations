package dev.foltz.predations.rabiesEffect;

import dev.foltz.predations.PredationsMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEffects {
    private ModEffects() {}

    public static final StatusEffect RABIES = new RabiesStatusEffect();

    public static void register() {
        Registry.register(Registries.STATUS_EFFECT, new Identifier(PredationsMod.MODID, "rabies"), RABIES);
        System.out.println("[Predations][Init] Rabies Effect registered");
    }
}