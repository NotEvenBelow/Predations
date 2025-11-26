package dev.foltz.predations;

import dev.foltz.predations.ARFY.ARFYInstaller;
import dev.foltz.predations.consume.ConsumeHandler;
import dev.foltz.predations.fox.FoxRabiesInstaller;
import dev.foltz.predations.squid.FishFleeInstaller;
import dev.foltz.predations.stepup.StepUpHandler;
import dev.foltz.predations.config.ConfigManager;
import dev.foltz.predations.config.ExtraConfig;
import dev.foltz.predations.cow.CowInstaller;
import dev.foltz.predations.item.BurnedMeatHelper;
import dev.foltz.predations.item.ModItems;
import dev.foltz.predations.kuru.KuruHandler;
import dev.foltz.predations.loot_modifier.LootModifier;
import dev.foltz.predations.recipe.CampfireRecipePatch;
import dev.foltz.predations.squid.FishToSquidSwap;
import dev.foltz.predations.squid.PeakComedySquidSpawnHandler;
import dev.foltz.predations.squid.ModSquidEntities;
import dev.foltz.predations.kuru_status.KuruStatusEffects;
import net.fabricmc.api.ModInitializer;
import dev.foltz.predations.entity.FamishedCowEntity;
import dev.foltz.predations.entity.ModEntities;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import dev.foltz.predations.item.TalismanImmunityTracker;
import dev.foltz.predations.config.LootModifierConfig;
import dev.foltz.predations.runTargets.RunAwayFromTypesGoalInstaller;
import dev.foltz.predations.villager.VillagerZombificationInstaller;
import dev.foltz.predations.rirtualNetwork.ModPackets;
import dev.foltz.predations.rabiesEffect.ModEffects;
import dev.foltz.predations.wolf.WolfRabiesInstaller;

public final class PredationsMod implements ModInitializer {
    public static final String MODID = "predations";

    @Override
    public void onInitialize() {

        ConfigManager.load();
        ExtraConfig.load();
        LootModifierConfig.load();

        ModItems.register();
        KuruStatusEffects.register();
        CampfireRecipePatch.register();

        ARFYInstaller.register();
        CowInstaller.register();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            TalismanImmunityTracker.tick();
            KuruHandler.tick(server);
        });

        LootModifier.init();

        FishToSquidSwap.register();
        PeakComedySquidSpawnHandler.register();

        ModEntities.register();
        ModSquidEntities.register();

        ConsumeHandler.register();
        StepUpHandler.register();

        FishFleeInstaller.register();
        RunAwayFromTypesGoalInstaller.register();

        VillagerZombificationInstaller.register();
        ModPackets.registerServerPackets();

        ModEffects.register();
        WolfRabiesInstaller.register();
        FoxRabiesInstaller.register();


    }
}