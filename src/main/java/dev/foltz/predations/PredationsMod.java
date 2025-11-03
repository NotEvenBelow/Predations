package dev.foltz.predations;

import dev.foltz.predations.ARFY.ARFYInstaller;
import dev.foltz.predations.stepup.StepUpHandler;
import dev.foltz.predations.config.ConfigManager;
import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.cow.CowInstaller;
import dev.foltz.predations.item.BurnedMeatHelper;
import dev.foltz.predations.item.ModItems;
import dev.foltz.predations.loot_modifier.LootModifier;
import dev.foltz.predations.recipe.CampfireRecipePatch;
import dev.foltz.predations.squid.FishToSquidSwap;
import dev.foltz.predations.squid.PeakComedySquidSpawnHandler;
import dev.foltz.predations.squid.ModSquidEntities;
import net.fabricmc.api.ModInitializer;
import dev.foltz.predations.entity.FamishedCowEntity;
import dev.foltz.predations.entity.ModEntities;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import dev.foltz.predations.item.TalismanImmunityTracker;
import dev.foltz.predations.config.LootModifierConfig;

public final class PredationsMod implements ModInitializer {
    public static final String MODID = "predations";

    @Override
    public void onInitialize() {
        // load configs
        ConfigManager.load();
        ExtraConfig.load();
        LootModifierConfig.load();

        // register items & recipes
        ModItems.register();
        CampfireRecipePatch.register();

        // register ARFY logic
        ARFYInstaller.register();
        CowInstaller.register();


        // tick-based helpers
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            TalismanImmunityTracker.tick();  // talisman immunity decay
                    // burned meat rolling system
        });

        // loot modifications
        LootModifier.init();

        FishToSquidSwap.register();
        PeakComedySquidSpawnHandler.register();


        ModEntities.register();
        ModSquidEntities.register();

        dev.foltz.predations.consume.ConsumeHandler.register();
        StepUpHandler.register();
    }
}
