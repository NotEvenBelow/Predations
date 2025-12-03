
package dev.foltz.predations.client;

import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.foltz.predations.config.*;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PredationsModMenuIntegration implements ModMenuApi {

    private static String lastCategory = "Entity Tuning";

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.of("Predations Config"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            ConfigManager.Config mainConfig = ConfigManager.get();
            ExtraConfig.Model extraConfig = ExtraConfig.get();
            LootModifierConfig lootConfig = LootModifierConfig.load();
            DebugConfig debugConfig = DebugConfig.get();

            // ====================================================================
            // 1. ENTITY TUNING
            // ====================================================================
            ConfigCategory entityCat = builder.getOrCreateCategory(Text.of("Entity Tuning"));

            buildMapCategory(builder, entityCat, null, "Entity Tuning", mainConfig.entities, parent,
                    () -> {
                        ConfigManager.Tuning t = new ConfigManager.Tuning();
                        t.enabled = mainConfig.defaults.enabled;
                        return t;
                    },
                    (sub, t) -> {
                        if (t.enabled != null) sub.add(entryBuilder.startBooleanToggle(Text.of("Enabled"), t.enabled).setDefaultValue(mainConfig.defaults.enabled).setSaveConsumer(v -> t.enabled = v).build());
                        if (t.runSpeed != null) sub.add(entryBuilder.startDoubleField(Text.of("Run Speed"), t.runSpeed).setDefaultValue(mainConfig.defaults.runSpeed).setSaveConsumer(v -> t.runSpeed = v).build());
                        if (t.runNearPlayerBlock != null) sub.add(entryBuilder.startDoubleField(Text.of("Run Near Player Block"), t.runNearPlayerBlock).setDefaultValue(mainConfig.defaults.runNearPlayerBlock).setSaveConsumer(v -> t.runNearPlayerBlock = v).build());
                        if (t.requireLineOfSightToRun != null) sub.add(entryBuilder.startBooleanToggle(Text.of("Req Line of Sight"), t.requireLineOfSightToRun).setDefaultValue(mainConfig.defaults.requireLineOfSightToRun).setSaveConsumer(v -> t.requireLineOfSightToRun = v).build());
                        if (t.shiftingReduceDetectRangeByPercent != null) sub.add(entryBuilder.startDoubleField(Text.of("Shift Reduce %"), t.shiftingReduceDetectRangeByPercent).setDefaultValue(mainConfig.defaults.shiftingReduceDetectRangeByPercent).setSaveConsumer(v -> t.shiftingReduceDetectRangeByPercent = v).build());
                        if (t.SafeDistance != null) sub.add(entryBuilder.startDoubleField(Text.of("Safe Distance"), t.SafeDistance).setDefaultValue(mainConfig.defaults.SafeDistance).setSaveConsumer(v -> t.SafeDistance = v).build());
                        if (t.nearPlayerSpeedMultiplier != null) sub.add(entryBuilder.startDoubleField(Text.of("Near Player Speed Mult"), t.nearPlayerSpeedMultiplier).setDefaultValue(mainConfig.defaults.nearPlayerSpeedMultiplier).setSaveConsumer(v -> t.nearPlayerSpeedMultiplier = v).build());
                        if (t.continueToRunOutsideOfSafeDistanceInTicks != null) sub.add(entryBuilder.startIntField(Text.of("Linger Ticks"), t.continueToRunOutsideOfSafeDistanceInTicks).setDefaultValue(mainConfig.defaults.continueToRunOutsideOfSafeDistanceInTicks).setSaveConsumer(v -> t.continueToRunOutsideOfSafeDistanceInTicks = v).build());
                        if (t.allowLure != null) sub.add(entryBuilder.startBooleanToggle(Text.of("Allow Lure"), t.allowLure).setDefaultValue(mainConfig.defaults.allowLure).setSaveConsumer(v -> t.allowLure = v).build());
                        if (t.leashingStopTheRunning != null) sub.add(entryBuilder.startBooleanToggle(Text.of("Leash Stops Run"), t.leashingStopTheRunning).setDefaultValue(mainConfig.defaults.leashingStopTheRunning).setSaveConsumer(v -> t.leashingStopTheRunning = v).build());
                    }
            );

            // ====================================================================
            // 2. RELATIONS (AI)
            // ====================================================================
            ConfigCategory relationsCat = builder.getOrCreateCategory(Text.of("Relations (AI)"));

            // Aggression
            SubCategoryBuilder aggressionSub = entryBuilder.startSubCategory(Text.of("Aggression Targets"));
            buildMapCategory(builder, null, aggressionSub, "Attacker", mainConfig.aggressionTargets, parent, ArrayList::new,
                    (sub, list) -> sub.add(entryBuilder.startStrList(Text.of("Targets"), list).setSaveConsumer(v -> { list.clear(); list.addAll(v); }).build()));
            relationsCat.addEntry(aggressionSub.build());

            // Runaway
            SubCategoryBuilder runawaySub = entryBuilder.startSubCategory(Text.of("Runaway From"));
            buildMapCategory(builder, null, runawaySub, "Fleeing Entity", mainConfig.runawayFromEntities, parent, ArrayList::new,
                    (sub, list) -> sub.add(entryBuilder.startStrList(Text.of("Flees from"), list).setSaveConsumer(v -> { list.clear(); list.addAll(v); }).build()));
            relationsCat.addEntry(runawaySub.build());

            // Lures
            SubCategoryBuilder luresSub = entryBuilder.startSubCategory(Text.of("Lure Items"));
            buildMapCategory(builder, null, luresSub, "Lured Entity", mainConfig.forAllowLuresinEntities, parent, ArrayList::new,
                    (sub, list) -> sub.add(entryBuilder.startStrList(Text.of("Tempted by"), list).setSaveConsumer(v -> { list.clear(); list.addAll(v); }).build()));
            relationsCat.addEntry(luresSub.build());

            // ====================================================================
            // 3. CONSUMPTION
            // ====================================================================
            ConfigCategory consumeCat = builder.getOrCreateCategory(Text.of("Consumption"));
            ConfigManager.ConsumptionDefaults cDef = mainConfig.consumeDefaults;

            consumeCat.addEntry(entryBuilder.startBooleanToggle(Text.of("Defaults Enabled"), cDef.enabled).setDefaultValue(false).setSaveConsumer(v -> cDef.enabled = v).build());
            consumeCat.addEntry(entryBuilder.startFloatField(Text.of("Def. Heal Hearts"), cDef.healHearts).setDefaultValue(1.5f).setSaveConsumer(v -> cDef.healHearts = v).build());
            consumeCat.addEntry(entryBuilder.startIntField(Text.of("Def. Delay Ticks"), cDef.delayTicks).setDefaultValue(50).setSaveConsumer(v -> cDef.delayTicks = v).build());
            consumeCat.addEntry(entryBuilder.startDoubleField(Text.of("Def. Radius"), cDef.radius).setDefaultValue(2.0).setSaveConsumer(v -> cDef.radius = v).build());

            // Buffs
            SubCategoryBuilder buffSub = entryBuilder.startSubCategory(Text.of("Full Health Buffs"));
            for (int i = 0; i < cDef.getFullHealthBuffs.size(); i++) {
                ConfigManager.ConsumptionEffectEntry eff = cDef.getFullHealthBuffs.get(i);
                SubCategoryBuilder eSub = entryBuilder.startSubCategory(Text.of("Buff " + i));
                eSub.add(entryBuilder.startStrField(Text.of("Effect ID"), eff.effectId).setSaveConsumer(v -> eff.effectId = v).build());
                eSub.add(entryBuilder.startIntField(Text.of("Duration (s)"), eff.durationSeconds).setSaveConsumer(v -> eff.durationSeconds = v).build());
                eSub.add(entryBuilder.startIntField(Text.of("Amplifier"), eff.amplifier).setSaveConsumer(v -> eff.amplifier = v).build());
                buffSub.add(eSub.build());
            }
            consumeCat.addEntry(buffSub.build());

            // Entity Settings
            SubCategoryBuilder consumeEntsSub = entryBuilder.startSubCategory(Text.of("Entity Settings"));
            buildMapCategory(builder, null, consumeEntsSub, "Entity", mainConfig.consume, parent,
                    () -> { ConfigManager.ConsumptionTuning t = new ConfigManager.ConsumptionTuning(); t.enabled = cDef.enabled; return t; },
                    (sub, t) -> {
                        if (t.enabled != null) sub.add(entryBuilder.startBooleanToggle(Text.of("Enabled"), t.enabled).setDefaultValue(cDef.enabled).setSaveConsumer(v -> t.enabled = v).build());
                        if (t.healHearts != null) sub.add(entryBuilder.startFloatField(Text.of("Heal Hearts"), t.healHearts).setDefaultValue(cDef.healHearts).setSaveConsumer(v -> t.healHearts = v).build());
                        if (t.delayTicks != null) sub.add(entryBuilder.startIntField(Text.of("Delay Ticks"), t.delayTicks).setDefaultValue(cDef.delayTicks).setSaveConsumer(v -> t.delayTicks = v).build());
                        if (t.radius != null) sub.add(entryBuilder.startDoubleField(Text.of("Radius"), t.radius).setDefaultValue(cDef.radius).setSaveConsumer(v -> t.radius = v).build());
                        sub.add(entryBuilder.startStrList(Text.of("Items"), new ArrayList<>(t.items)).setSaveConsumer(l -> { t.items.clear(); t.items.addAll(l); }).build());
                    }
            );
            consumeCat.addEntry(consumeEntsSub.build());

            // ====================================================================
            // 4. MECHANICS
            // ====================================================================
            ConfigCategory mechCat = builder.getOrCreateCategory(Text.of("Mechanics"));

            // Step Up
            SubCategoryBuilder stepSub = entryBuilder.startSubCategory(Text.of("Step Up"));
            stepSub.add(entryBuilder.startFloatField(Text.of("Default Height"), extraConfig.stepUp.defaultHeight).setDefaultValue(1.0f).setSaveConsumer(v -> extraConfig.stepUp.defaultHeight = v).build());
            buildMapCategory(builder, null, stepSub, "Entity", extraConfig.stepUp.entities, parent,
                    () -> { ExtraConfig.StepPerEntity s = new ExtraConfig.StepPerEntity(); s.enabled = true; s.height = extraConfig.stepUp.defaultHeight; return s; },
                    (sub, s) -> {
                        sub.add(entryBuilder.startBooleanToggle(Text.of("Enabled"), s.enabled).setSaveConsumer(v -> s.enabled = v).build());
                        sub.add(entryBuilder.startFloatField(Text.of("Height"), s.height).setSaveConsumer(v -> s.height = v).build());
                    }
            );
            mechCat.addEntry(stepSub.build());

            // Angry Mobs
            SubCategoryBuilder angrySub = entryBuilder.startSubCategory(Text.of("Angry Mobs"));
            angrySub.add(entryBuilder.startBooleanToggle(Text.of("Enabled"), extraConfig.angryMobs.enabled).setDefaultValue(true).setSaveConsumer(v -> extraConfig.angryMobs.enabled = v).build());
            angrySub.add(entryBuilder.startIntField(Text.of("Default Kick Cooldown"), extraConfig.angryMobs.defaultKickCooldownTicks).setDefaultValue(20).setSaveConsumer(v -> extraConfig.angryMobs.defaultKickCooldownTicks = v).build());
            buildMapCategory(builder, null, angrySub, "Entity", extraConfig.angryMobs.entities, parent,
                    ExtraConfig.AngryMob::new,
                    (sub, m) -> {
                        sub.add(entryBuilder.startBooleanToggle(Text.of("Enabled"), m.enabled).setSaveConsumer(v -> m.enabled = v).build());
                        if (m.maxHearts != null) sub.add(entryBuilder.startDoubleField(Text.of("Max Hearts"), m.maxHearts).setSaveConsumer(v -> m.maxHearts = v).build());
                        if (m.kickingRange != null) sub.add(entryBuilder.startDoubleField(Text.of("Kick Range"), m.kickingRange).setSaveConsumer(v -> m.kickingRange = v).build());
                        if (m.kickDamageHeartsEasy != null) sub.add(entryBuilder.startFloatField(Text.of("Dmg Easy"), m.kickDamageHeartsEasy).setSaveConsumer(v -> m.kickDamageHeartsEasy = v).build());
                        if (m.kickDamageHeartsNormal != null) sub.add(entryBuilder.startFloatField(Text.of("Dmg Normal"), m.kickDamageHeartsNormal).setSaveConsumer(v -> m.kickDamageHeartsNormal = v).build());
                        if (m.kickDamageHeartsHard != null) sub.add(entryBuilder.startFloatField(Text.of("Dmg Hard"), m.kickDamageHeartsHard).setSaveConsumer(v -> m.kickDamageHeartsHard = v).build());
                        if (m.kickCooldownTicks != null) sub.add(entryBuilder.startIntField(Text.of("Kick Cooldown"), m.kickCooldownTicks).setSaveConsumer(v -> m.kickCooldownTicks = v).build());
                        if (m.runSpeed != null) sub.add(entryBuilder.startDoubleField(Text.of("Run Speed"), m.runSpeed).setSaveConsumer(v -> m.runSpeed = v).build());
                        if (m.kickHorizontalVelocity != null) sub.add(entryBuilder.startDoubleField(Text.of("Kick Horiz Vel"), m.kickHorizontalVelocity).setSaveConsumer(v -> m.kickHorizontalVelocity = v).build());
                        if (m.kickVerticalVelocity != null) sub.add(entryBuilder.startDoubleField(Text.of("Kick Vert Vel"), m.kickVerticalVelocity).setSaveConsumer(v -> m.kickVerticalVelocity = v).build());
                        if (m.kickActiveWindowTicks != null) sub.add(entryBuilder.startIntField(Text.of("Active Window"), m.kickActiveWindowTicks).setSaveConsumer(v -> m.kickActiveWindowTicks = v).build());
                    }
            );
            mechCat.addEntry(angrySub.build());

            // Milk Change
            SubCategoryBuilder milkSub = entryBuilder.startSubCategory(Text.of("Milk Change"));
            milkSub.add(entryBuilder.startBooleanToggle(Text.of("Restore Hunger"), extraConfig.milkChange.milkRestoreHungerInsteadRemovingEffect).setDefaultValue(true).setSaveConsumer(v -> extraConfig.milkChange.milkRestoreHungerInsteadRemovingEffect = v).build());
            milkSub.add(entryBuilder.startDoubleField(Text.of("Hunger Amount"), extraConfig.milkChange.milkHungerRestore).setDefaultValue(2.5).setSaveConsumer(v -> extraConfig.milkChange.milkHungerRestore = v).build());
            milkSub.add(entryBuilder.startFloatField(Text.of("Saturation"), extraConfig.milkChange.milkSaturation).setDefaultValue(2.0f).setSaveConsumer(v -> extraConfig.milkChange.milkSaturation = v).build());
            milkSub.add(entryBuilder.startIntField(Text.of("Cow Cooldown (s)"), extraConfig.milkChange.milkCowCooldownSeconds).setDefaultValue(300).setSaveConsumer(v -> extraConfig.milkChange.milkCowCooldownSeconds = v).build());
            mechCat.addEntry(milkSub.build());

            // Rare Variants
            SubCategoryBuilder rareSub = entryBuilder.startSubCategory(Text.of("Rare Variants"));
            rareSub.add(entryBuilder.startBooleanToggle(Text.of("Famished Cow"), extraConfig.rareVariants.famishedCowEnabled).setDefaultValue(false).setSaveConsumer(v -> extraConfig.rareVariants.famishedCowEnabled = v).build());
            rareSub.add(entryBuilder.startDoubleField(Text.of("Famished Chance"), extraConfig.rareVariants.famishedCowChance).setDefaultValue(0.006).setSaveConsumer(v -> extraConfig.rareVariants.famishedCowChance = v).build());
            if (extraConfig.rareVariants.famishedCowBaseSpeed != null) rareSub.add(entryBuilder.startFloatField(Text.of("Base Speed"), extraConfig.rareVariants.famishedCowBaseSpeed).setSaveConsumer(v -> extraConfig.rareVariants.famishedCowBaseSpeed = v).build());
            rareSub.add(entryBuilder.startFloatField(Text.of("Speed Mult"), extraConfig.rareVariants.famishedCowSpeedMultiplier).setDefaultValue(0.4f).setSaveConsumer(v -> extraConfig.rareVariants.famishedCowSpeedMultiplier = v).build());
            mechCat.addEntry(rareSub.build());

            // Secret
            SubCategoryBuilder secretSub = entryBuilder.startSubCategory(Text.of("Secret"));
            secretSub.add(entryBuilder.startBooleanToggle(Text.of("No Knockback All"), extraConfig.secret.noKnockbackAll).setDefaultValue(false).setSaveConsumer(v -> extraConfig.secret.noKnockbackAll = v).build());
            secretSub.add(entryBuilder.startStrList(Text.of("Knockback Whitelist"), new ArrayList<>(extraConfig.secret.knockbackWhitelist)).setSaveConsumer(l -> { extraConfig.secret.knockbackWhitelist.clear(); extraConfig.secret.knockbackWhitelist.addAll(l); }).build());
            mechCat.addEntry(secretSub.build());

            // ====================================================================
            // 5. PREDATORS
            // ====================================================================
            ConfigCategory predCat = builder.getOrCreateCategory(Text.of("Predators"));

            // Predatory Squid
            ExtraConfig.PredatorySquidConfig sq = extraConfig.predSquid;
            SubCategoryBuilder squidSub = entryBuilder.startSubCategory(Text.of("Predatory Squid"));
            squidSub.add(entryBuilder.startBooleanToggle(Text.of("Enabled"), sq.enabled).setSaveConsumer(v -> sq.enabled = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Low Light Level"), sq.lowLightLevel).setSaveConsumer(v -> sq.lowLightLevel = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("AI Work Range"), sq.aiWorkRange).setSaveConsumer(v -> sq.aiWorkRange = v).build());
            squidSub.add(entryBuilder.startBooleanToggle(Text.of("Float At Night"), sq.squidFloatAtNight).setSaveConsumer(v -> sq.squidFloatAtNight = v).build());
            squidSub.add(entryBuilder.startBooleanToggle(Text.of("Nudging Stuck"), sq.nudgingSquidwhenStuckonLand).setSaveConsumer(v -> sq.nudgingSquidwhenStuckonLand = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("Nudging Range"), sq.nudgingRange).setSaveConsumer(v -> sq.nudgingRange = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("Nudge Jump Vel"), sq.nudgingJumpHeightVelocity).setSaveConsumer(v -> sq.nudgingJumpHeightVelocity = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Nudge Interval"), sq.nudgingInterval).setSaveConsumer(v -> sq.nudgingInterval = v).build());
            squidSub.add(entryBuilder.startBooleanToggle(Text.of("Nudge Thru Wall"), sq.nudgePathFindThroughWall).setSaveConsumer(v -> sq.nudgePathFindThroughWall = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("Replace Chance"), sq.replaceChance).setSaveConsumer(v -> sq.replaceChance = v).build());
            squidSub.add(entryBuilder.startFloatField(Text.of("Glow Chance"), sq.glowSquidChance).setSaveConsumer(v -> sq.glowSquidChance = v).build());
            squidSub.add(entryBuilder.startFloatField(Text.of("Dmg Easy"), sq.dmgHeartsEasy).setSaveConsumer(v -> sq.dmgHeartsEasy = v).build());
            squidSub.add(entryBuilder.startFloatField(Text.of("Dmg Normal"), sq.dmgHeartsNormal).setSaveConsumer(v -> sq.dmgHeartsNormal = v).build());
            squidSub.add(entryBuilder.startFloatField(Text.of("Dmg Hard"), sq.dmgHeartsHard).setSaveConsumer(v -> sq.dmgHeartsHard = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Squid MaxHealth"), sq.squidMaxHealth).setSaveConsumer(v -> sq.squidMaxHealth = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Glow MaxHealth"), sq.glowSquidMaxHealth).setSaveConsumer(v -> sq.glowSquidMaxHealth = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Atk Tick Interval"), sq.attackTickInterval).setSaveConsumer(v -> sq.attackTickInterval = v).build());
            squidSub.add(entryBuilder.startFloatField(Text.of("Heal %"), sq.healPct).setSaveConsumer(v -> sq.healPct = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("Thorn Dmg"), sq.extraThornDamage).setSaveConsumer(v -> sq.extraThornDamage = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("Downforce Drag"), sq.downforceDrag).setSaveConsumer(v -> sq.downforceDrag = v).build());
            squidSub.add(entryBuilder.startBooleanToggle(Text.of("Give Blindness"), sq.squidAndGlowGiveBlindness).setSaveConsumer(v -> sq.squidAndGlowGiveBlindness = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Slowness Lvl"), sq.squidAndGlowSlownessLevel).setSaveConsumer(v -> sq.squidAndGlowSlownessLevel = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Glow Hunger Lvl"), sq.glowHungerLevel).setSaveConsumer(v -> sq.glowHungerLevel = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Squid Hunger Lvl"), sq.squidHungerLevel).setSaveConsumer(v -> sq.squidHungerLevel = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Glow Weakness Lvl"), sq.glowWeaknessLevel).setSaveConsumer(v -> sq.glowWeaknessLevel = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Glow MiningFatigue"), sq.glowMiningFatigueLevel).setSaveConsumer(v -> sq.glowMiningFatigueLevel = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Glow Strength"), sq.glowStrengthLevel).setSaveConsumer(v -> sq.glowStrengthLevel = v).build());
            squidSub.add(entryBuilder.startBooleanToggle(Text.of("Break Boats"), sq.breakBoats).setSaveConsumer(v -> sq.breakBoats = v).build());
            squidSub.add(entryBuilder.startBooleanToggle(Text.of("Break Minecarts"), sq.breakMinecarts).setSaveConsumer(v -> sq.breakMinecarts = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Psychic Interval"), sq.psychicGrabIntervalTicks).setSaveConsumer(v -> sq.psychicGrabIntervalTicks = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("Psychic Range"), sq.psychicGrabRange).setSaveConsumer(v -> sq.psychicGrabRange = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("Psychic Chance"), sq.psychicGrabChance).setSaveConsumer(v -> sq.psychicGrabChance = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("Latch Range"), sq.latchRangeBlocks).setSaveConsumer(v -> sq.latchRangeBlocks = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Ticks In Range"), sq.requiredTicksInRange).setSaveConsumer(v -> sq.requiredTicksInRange = v).build());
            squidSub.add(entryBuilder.startStrList(Text.of("Excluded Targets"), new ArrayList<>(sq.excludedTargets))
                    .setSaveConsumer(l -> { sq.excludedTargets.clear(); sq.excludedTargets.addAll(l); }).build());
            squidSub.add(entryBuilder.startBooleanToggle(Text.of("Fish Flee"), sq.fishFleeSquidLikePlayer).setSaveConsumer(v -> sq.fishFleeSquidLikePlayer = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Flee Dist"), sq.fishFleeDistance).setSaveConsumer(v -> sq.fishFleeDistance = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("Attach Dist"), sq.attachDistance).setSaveConsumer(v -> sq.attachDistance = v).build());
            squidSub.add(entryBuilder.startBooleanToggle(Text.of("Blocked by Solids"), sq.blockedBySolids).setSaveConsumer(v -> sq.blockedBySolids = v).build());
            squidSub.add(entryBuilder.startFloatField(Text.of("Line Break Spd"), sq.lineBreakSpeed).setSaveConsumer(v -> sq.lineBreakSpeed = v).build());
            squidSub.add(entryBuilder.startBooleanToggle(Text.of("Tongue Enabled"), sq.tongueEnabled).setSaveConsumer(v -> sq.tongueEnabled = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("Catch Chance"), sq.catchSuccessChance).setSaveConsumer(v -> sq.catchSuccessChance = v).build());
            squidSub.add(entryBuilder.startIntField(Text.of("Regrab Cooldown"), sq.regrabCooldownTicks).setSaveConsumer(v -> sq.regrabCooldownTicks = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("Comedy Squid %"), sq.peakComedySquidChance).setSaveConsumer(v -> sq.peakComedySquidChance = v).build());
            squidSub.add(entryBuilder.startDoubleField(Text.of("Comedy Glow %"), sq.peakComedyGlowSquidChance).setSaveConsumer(v -> sq.peakComedyGlowSquidChance = v).build());
            predCat.addEntry(squidSub.build());

            // Rabies
            ExtraConfig.RabiesConfig rabies = extraConfig.rabies;
            SubCategoryBuilder rabiesSub = entryBuilder.startSubCategory(Text.of("Rabies"));
            rabiesSub.add(entryBuilder.startBooleanToggle(Text.of("Enabled"), rabies.enabled).setSaveConsumer(v -> rabies.enabled = v).build());
            rabiesSub.add(entryBuilder.startBooleanToggle(Text.of("Milk Cures"), rabies.milkRemoveRabies).setSaveConsumer(v -> rabies.milkRemoveRabies = v).build());
            rabiesSub.add(entryBuilder.startBooleanToggle(Text.of("Chorus Cures"), rabies.chorusFruitRemovesRabies).setSaveConsumer(v -> rabies.chorusFruitRemovesRabies = v).build());
            rabiesSub.add(entryBuilder.startBooleanToggle(Text.of("G. Apple Cures"), rabies.goldenAppleRemovesRabies).setSaveConsumer(v -> rabies.goldenAppleRemovesRabies = v).build());
            rabiesSub.add(entryBuilder.startBooleanToggle(Text.of("Ench. G. Apple Cures"), rabies.enchantedGoldenAppleRemovesRabies).setSaveConsumer(v -> rabies.enchantedGoldenAppleRemovesRabies = v).build());
            rabiesSub.add(entryBuilder.startIntField(Text.of("Survival Time (Ticks)"), rabies.timeSurviveWithRabiesEffectTillKilledInTick).setSaveConsumer(v -> rabies.timeSurviveWithRabiesEffectTillKilledInTick = v).build());
            rabiesSub.add(entryBuilder.startDoubleField(Text.of("Natural Wolf Spawn %"), rabies.naturalAggressiveWolfSpawnChance).setSaveConsumer(v -> rabies.naturalAggressiveWolfSpawnChance = v).build());
            rabiesSub.add(entryBuilder.startDoubleField(Text.of("Non-Nat Wolf Bite %"), rabies.nonNaturalAggressiveWolfRabiesBitChance).setSaveConsumer(v -> rabies.nonNaturalAggressiveWolfRabiesBitChance = v).build());
            rabiesSub.add(entryBuilder.startDoubleField(Text.of("Nat Wolf Bite %"), rabies.naturalAggressiveWolfRabiesBitChance).setSaveConsumer(v -> rabies.naturalAggressiveWolfRabiesBitChance = v).build());
            rabiesSub.add(entryBuilder.startDoubleField(Text.of("Nat Fox Spawn %"), rabies.naturalAggressiveFoxSpawnChance).setSaveConsumer(v -> rabies.naturalAggressiveFoxSpawnChance = v).build());
            rabiesSub.add(entryBuilder.startDoubleField(Text.of("Nat Fox Bite %"), rabies.naturalAggressiveFoxRabiesBitChance).setSaveConsumer(v -> rabies.naturalAggressiveFoxRabiesBitChance = v).build());
            rabiesSub.add(entryBuilder.startDoubleField(Text.of("Bat Feed Bite %"), rabies.batWhenFeedingRabiesBitChance).setSaveConsumer(v -> rabies.batWhenFeedingRabiesBitChance = v).build());
            rabiesSub.add(entryBuilder.startDoubleField(Text.of("Bat Final Bite %"), rabies.batRabiesChanceWhenFinallyBit).setSaveConsumer(v -> rabies.batRabiesChanceWhenFinallyBit = v).build());
            rabiesSub.add(entryBuilder.startDoubleField(Text.of("Bat Glow Range"), rabies.batGlowOtherEntitiesRange).setSaveConsumer(v -> rabies.batGlowOtherEntitiesRange = v).build());
            predCat.addEntry(rabiesSub.build());

            // Kuru
            ExtraConfig.KuruConfig kuru = extraConfig.kuru;
            SubCategoryBuilder kuruSub = entryBuilder.startSubCategory(Text.of("Kuru Disease"));
            kuruSub.add(entryBuilder.startBooleanToggle(Text.of("Enabled"), kuru.enabled).setSaveConsumer(v -> kuru.enabled = v).build());
            kuruSub.add(entryBuilder.startBooleanToggle(Text.of("Chorus Cures"), kuru.chorusFruitRemovesKuru).setSaveConsumer(v -> kuru.chorusFruitRemovesKuru = v).build());
            kuruSub.add(entryBuilder.startBooleanToggle(Text.of("G. Apple Cures"), kuru.goldenAppleRemovesKuru).setSaveConsumer(v -> kuru.goldenAppleRemovesKuru = v).build());
            kuruSub.add(entryBuilder.startBooleanToggle(Text.of("Ench. G. Apple Cures"), kuru.enchantedGoldenAppleRemovesKuru).setSaveConsumer(v -> kuru.enchantedGoldenAppleRemovesKuru = v).build());
            kuruSub.add(entryBuilder.startDoubleField(Text.of("Meat Chance"), kuru.kuruChanceforVillagerMeatNoTalisman).setSaveConsumer(v -> kuru.kuruChanceforVillagerMeatNoTalisman = v).build());
            kuruSub.add(entryBuilder.startBooleanToggle(Text.of("Milk Cures"), kuru.milkCanCureKuru).setSaveConsumer(v -> kuru.milkCanCureKuru = v).build());
            kuruSub.add(entryBuilder.startBooleanToggle(Text.of("No Eat at Stage 2"), kuru.unableToEatAnyMeatsAtStage2).setSaveConsumer(v -> kuru.unableToEatAnyMeatsAtStage2 = v).build());
            kuruSub.add(entryBuilder.startBooleanToggle(Text.of("Zombie on Death"), kuru.zombifiedIfDieWithKuru).setSaveConsumer(v -> kuru.zombifiedIfDieWithKuru = v).build());
            kuruSub.add(entryBuilder.startBooleanToggle(Text.of("Stage 4 Kill"), kuru.stage4Kill).setSaveConsumer(v -> kuru.stage4Kill = v).build());
            kuruSub.add(entryBuilder.startIntField(Text.of("S3 Survive Time (s)"), kuru.stage3SurvivedKillSecond).setSaveConsumer(v -> kuru.stage3SurvivedKillSecond = v).build());

            SubCategoryBuilder stagesSub = entryBuilder.startSubCategory(Text.of("Stages"));
            for (Map.Entry<String, ExtraConfig.KuruStage> en : kuru.stages.entrySet()) {
                SubCategoryBuilder stSub = entryBuilder.startSubCategory(Text.of("Stage " + en.getKey()));
                stSub.add(entryBuilder.startIntField(Text.of("Start Tick"), en.getValue().startTick).setSaveConsumer(v -> en.getValue().startTick = v).build());
                for (int i=0; i<en.getValue().effects.size(); i++) {
                    ExtraConfig.KuruStageEffect ef = en.getValue().effects.get(i);
                    SubCategoryBuilder efSub = entryBuilder.startSubCategory(Text.of("Effect " + i));
                    efSub.add(entryBuilder.startStrField(Text.of("ID"), ef.effectId).setSaveConsumer(v -> ef.effectId = v).build());
                    efSub.add(entryBuilder.startIntField(Text.of("Duration"), ef.durationSeconds).setSaveConsumer(v -> ef.durationSeconds = v).build());
                    efSub.add(entryBuilder.startIntField(Text.of("Amplifier"), ef.amplifier).setSaveConsumer(v -> ef.amplifier = v).build());
                    stSub.add(efSub.build());
                }
                stagesSub.add(stSub.build());
            }
            kuruSub.add(stagesSub.build());
            predCat.addEntry(kuruSub.build());

            // ====================================================================
            // 6. ITEMS & MISC
            // ====================================================================
            ConfigCategory itemsCat = builder.getOrCreateCategory(Text.of("Items & Misc"));

            // Fox Items
            ExtraConfig.FoxItemsConfig fox = extraConfig.foxItems;
            SubCategoryBuilder foxSub = entryBuilder.startSubCategory(Text.of("Fox Items"));
            foxSub.add(entryBuilder.startBooleanToggle(Text.of("Talisman Enabled"), fox.FoxTalismanFunctionEnabled).setSaveConsumer(v -> fox.FoxTalismanFunctionEnabled = v).build());
            foxSub.add(entryBuilder.startDoubleField(Text.of("Feather Drop %"), fox.FoxFeatherDropChance).setSaveConsumer(v -> fox.FoxFeatherDropChance = v).build());
            foxSub.add(entryBuilder.startIntField(Text.of("Drop Interval"), fox.FoxFeatherDropRollTickInterval).setSaveConsumer(v -> fox.FoxFeatherDropRollTickInterval = v).build());
            foxSub.add(entryBuilder.startIntField(Text.of("Use Time"), fox.TalismanUseTime).setSaveConsumer(v -> fox.TalismanUseTime = v).build());
            foxSub.add(entryBuilder.startDoubleField(Text.of("Immunity Chance"), fox.TalismanChanceToNotGetKuruWhenEatingVillagerMeat).setSaveConsumer(v -> fox.TalismanChanceToNotGetKuruWhenEatingVillagerMeat = v).build());
            foxSub.add(entryBuilder.startIntField(Text.of("Cooldown (s)"), fox.TalismanCooldowninSecond).setSaveConsumer(v -> fox.TalismanCooldowninSecond = v).build());
            foxSub.add(entryBuilder.startIntField(Text.of("Squid Immunity (s)"), fox.TalismanSquidImmunityTimeInSecond).setSaveConsumer(v -> fox.TalismanSquidImmunityTimeInSecond = v).build());
            SubCategoryBuilder foxEff = entryBuilder.startSubCategory(Text.of("Talisman Effects"));
            for(int i=0; i<fox.TalismanEffects.size(); i++) {
                ExtraConfig.TalismanEffectEntry te = fox.TalismanEffects.get(i);
                SubCategoryBuilder teSub = entryBuilder.startSubCategory(Text.of("Effect " + i));
                teSub.add(entryBuilder.startStrField(Text.of("ID"), te.effectId).setSaveConsumer(v -> te.effectId = v).build());
                teSub.add(entryBuilder.startIntField(Text.of("Duration"), te.durationSeconds).setSaveConsumer(v -> te.durationSeconds = v).build());
                teSub.add(entryBuilder.startIntField(Text.of("Amp"), te.amplifier).setSaveConsumer(v -> te.amplifier = v).build());
                foxEff.add(teSub.build());
            }
            foxSub.add(foxEff.build());
            itemsCat.addEntry(foxSub.build());

            // Burned Meat
            ExtraConfig.BurnedMeatConfig burn = extraConfig.burnedMeat;
            SubCategoryBuilder burnSub = entryBuilder.startSubCategory(Text.of("Burned Meat"));
            burnSub.add(entryBuilder.startBooleanToggle(Text.of("Enabled"), burn.enabled).setSaveConsumer(v -> burn.enabled = v).build());
            burnSub.add(entryBuilder.startBooleanToggle(Text.of("Only Smoker"), burn.onlySmokerGivesCookedMeat).setSaveConsumer(v -> burn.onlySmokerGivesCookedMeat = v).build());
            burnSub.add(entryBuilder.startFloatField(Text.of("Campfire Chance"), burn.campfireBurnChance).setSaveConsumer(v -> burn.campfireBurnChance = v).build());
            burnSub.add(entryBuilder.startStrList(Text.of("Burn Instead Items"), new ArrayList<>(burn.burnInsteadItems)).setSaveConsumer(l -> { burn.burnInsteadItems.clear(); burn.burnInsteadItems.addAll(l); }).build());
            SubCategoryBuilder burnEnts = entryBuilder.startSubCategory(Text.of("Entities"));
            buildMapCategory(builder, null, burnEnts, "Entity", burn.entities, parent,
                    () -> new ExtraConfig.BurnedMeatEntry(1, 1),
                    (sub, val) -> {
                        sub.add(entryBuilder.startIntField(Text.of("Min"), val.min).setSaveConsumer(v -> val.min = v).build());
                        sub.add(entryBuilder.startIntField(Text.of("Max"), val.max).setSaveConsumer(v -> val.max = v).build());
                    }
            );
            burnSub.add(burnEnts.build());
            itemsCat.addEntry(burnSub.build());

            // Pufferfish
            ExtraConfig.PufferfishConfig puff = extraConfig.pufferfish;
            SubCategoryBuilder puffSub = entryBuilder.startSubCategory(Text.of("Pufferfish"));
            puffSub.add(entryBuilder.startDoubleField(Text.of("Jackpot %"), puff.jackpotChance).setSaveConsumer(v -> puff.jackpotChance = v).build());
            SubCategoryBuilder puffEff = entryBuilder.startSubCategory(Text.of("Jackpot Effects"));
            for(int i=0; i<puff.jackpotEffects.size(); i++) {
                ExtraConfig.PufferfishEffectEntry pe = puff.jackpotEffects.get(i);
                SubCategoryBuilder peSub = entryBuilder.startSubCategory(Text.of("Effect " + i));
                peSub.add(entryBuilder.startStrField(Text.of("ID"), pe.effectId).setSaveConsumer(v -> pe.effectId = v).build());
                peSub.add(entryBuilder.startIntField(Text.of("Duration"), pe.durationSeconds).setSaveConsumer(v -> pe.durationSeconds = v).build());
                peSub.add(entryBuilder.startIntField(Text.of("Amp"), pe.amplifier).setSaveConsumer(v -> pe.amplifier = v).build());
                puffEff.add(peSub.build());
            }
            puffSub.add(puffEff.build());
            itemsCat.addEntry(puffSub.build());

            // Villager
            ExtraConfig.VillagerConfig vil = extraConfig.villager;
            SubCategoryBuilder vilSub = entryBuilder.startSubCategory(Text.of("Villagers"));
            vilSub.add(entryBuilder.startDoubleField(Text.of("Zombification %"), vil.zombificationChance).setSaveConsumer(v -> vil.zombificationChance = v).build());
            vilSub.add(entryBuilder.startBooleanToggle(Text.of("Life Ritual"), vil.LifeRitualFunctionEnabled).setSaveConsumer(v -> vil.LifeRitualFunctionEnabled = v).build());
            vilSub.add(entryBuilder.startBooleanToggle(Text.of("Disable Curing"), vil.disableCuring).setSaveConsumer(v -> vil.disableCuring = v).build());
            vilSub.add(entryBuilder.startDoubleField(Text.of("Custom Cure Splash"), vil.customCureSplashRange).setSaveConsumer(v -> vil.customCureSplashRange = v).build());
            vilSub.add(entryBuilder.startBooleanToggle(Text.of("Iron Golem Change"), vil.ironGolemChangeEnabled).setSaveConsumer(v -> vil.ironGolemChangeEnabled = v).build());
            vilSub.add(entryBuilder.startDoubleField(Text.of("IG Vert Reach"), vil.ironGolemReachRangeVertically).setSaveConsumer(v -> vil.ironGolemReachRangeVertically = v).build());
            vilSub.add(entryBuilder.startBooleanToggle(Text.of("IG No Suffocate"), vil.ironGolemCannotSuffocate).setSaveConsumer(v -> vil.ironGolemCannotSuffocate = v).build());
            itemsCat.addEntry(vilSub.build());

            // Bat Guano
            ExtraConfig.BatGuanoAndBoneMealConfig guano = extraConfig.batGuanoAndBoneMeal;
            SubCategoryBuilder guanoSub = entryBuilder.startSubCategory(Text.of("Guano & Bonemeal"));
            guanoSub.add(entryBuilder.startBooleanToggle(Text.of("Enabled"), guano.enabled).setSaveConsumer(v -> guano.enabled = v).build());
            guanoSub.add(entryBuilder.startBooleanToggle(Text.of("Vanilla SpeedUp"), guano.vanillaBoneMealSpeedUpCropGrowthInsteadofInstantGrowth).setSaveConsumer(v -> guano.vanillaBoneMealSpeedUpCropGrowthInsteadofInstantGrowth = v).build());
            guanoSub.add(entryBuilder.startBooleanToggle(Text.of("Guano Instant"), guano.guanoFertilizerInstantFinishCropGrowth).setSaveConsumer(v -> guano.guanoFertilizerInstantFinishCropGrowth = v).build());
            guanoSub.add(entryBuilder.startDoubleField(Text.of("Vanilla Mult"), guano.vanillaBoneMealSpeedUpCropGrowthInsteadofInstantGrowthMultiplier).setSaveConsumer(v -> guano.vanillaBoneMealSpeedUpCropGrowthInsteadofInstantGrowthMultiplier = v).build());
            guanoSub.add(entryBuilder.startDoubleField(Text.of("Guano Mult"), guano.guanoFertilizerCropGrowthMultiplier).setSaveConsumer(v -> guano.guanoFertilizerCropGrowthMultiplier = v).build());
            guanoSub.add(entryBuilder.startDoubleField(Text.of("Consume %"), guano.guanoFertilizerConsumeChance).setSaveConsumer(v -> guano.guanoFertilizerConsumeChance = v).build());
            guanoSub.add(entryBuilder.startDoubleField(Text.of("Bat Drop %"), guano.batGuanoDropChance).setSaveConsumer(v -> guano.batGuanoDropChance = v).build());
            guanoSub.add(entryBuilder.startDoubleField(Text.of("Phantom Drop %"), guano.phantomGuanoDropChance).setSaveConsumer(v -> guano.phantomGuanoDropChance = v).build());
            guanoSub.add(entryBuilder.startIntField(Text.of("Bat Interval"), guano.batGuanoRollIntervalInTick).setSaveConsumer(v -> guano.batGuanoRollIntervalInTick = v).build());
            guanoSub.add(entryBuilder.startIntField(Text.of("Phantom Interval"), guano.phantomGuanoRollIntervalInTick).setSaveConsumer(v -> guano.phantomGuanoRollIntervalInTick = v).build());
            guanoSub.add(entryBuilder.startBooleanToggle(Text.of("Bat Leash"), guano.batCanLeash).setSaveConsumer(v -> guano.batCanLeash = v).build());
            guanoSub.add(entryBuilder.startBooleanToggle(Text.of("Phantom Leash"), guano.phantomCanLeash).setSaveConsumer(v -> guano.phantomCanLeash = v).build());
            itemsCat.addEntry(guanoSub.build());

            // Sniffer
            ExtraConfig.BetterSnifferRelatedFeaturesConfig sniff = extraConfig.betterSniffer;
            SubCategoryBuilder sniffSub = entryBuilder.startSubCategory(Text.of("Better Sniffer Features"));
            sniffSub.add(entryBuilder.startBooleanToggle(Text.of("Better Sniffer Features Enabled"), sniff.enabled).setDefaultValue(true).setSaveConsumer(v -> sniff.enabled = v).build());
            sniffSub.add(entryBuilder.startBooleanToggle(Text.of("Sniffer Boosts Crop Growth"), sniff.snifferBoostCropGrowth).setDefaultValue(true).setSaveConsumer(v -> sniff.snifferBoostCropGrowth = v).build());
            sniffSub.add(entryBuilder.startDoubleField(Text.of("Sniffer Crop Growth Speed Multiplier"), sniff.snifferCropGrowthMultiplier).setDefaultValue(1.5).setSaveConsumer(v -> sniff.snifferCropGrowthMultiplier = v).build());
            sniffSub.add(entryBuilder.startIntField(Text.of("Sniffer Crop Growth Effect Radius"), sniff.snifferCropGrowthRadius).setDefaultValue(4).setSaveConsumer(v -> sniff.snifferCropGrowthRadius = v).build());
            sniffSub.add(entryBuilder.startIntField(Text.of("Sniffer Crop Growth Effect Duration (Ticks)"), sniff.snifferCropGrowthDurationInTicks).setDefaultValue(200).setSaveConsumer(v -> sniff.snifferCropGrowthDurationInTicks = v).build());
            sniffSub.add(entryBuilder.startIntField(Text.of("Sniffer Crop Growth Effect Interval (Ticks)"), sniff.snifferCropGrowthInterval).setDefaultValue(400).setSaveConsumer(v -> sniff.snifferCropGrowthInterval = v).build());
            sniffSub.add(entryBuilder.startIntField(Text.of("Torchflower Light Emission Level"), sniff.torchflowerEmitsLightLevel).setDefaultValue(7).setSaveConsumer(v -> sniff.torchflowerEmitsLightLevel = v).build());
            sniffSub.add(entryBuilder.startBooleanToggle(Text.of("Bees Pollinate Faster With Torchflower"), sniff.beeMoreEffectiveWhenPollenateWithTorchflower).setDefaultValue(true).setSaveConsumer(v -> sniff.beeMoreEffectiveWhenPollenateWithTorchflower = v).build());
            sniffSub.add(entryBuilder.startDoubleField(Text.of("Bee Pollination Speed Multiplier"), sniff.beeMoreEffectiveWhenPollenateWithTorchflowerMultiplier).setDefaultValue(2.0).setSaveConsumer(v -> sniff.beeMoreEffectiveWhenPollenateWithTorchflowerMultiplier = v).build());
            sniffSub.add(entryBuilder.startIntField(Text.of("Pitcher Plant Effect Minimum Light Level"), sniff.pitcherEffectGiveInLightLevel).setDefaultValue(10).setSaveConsumer(v -> sniff.pitcherEffectGiveInLightLevel = v).build());
            sniffSub.add(entryBuilder.startIntField(Text.of("Pitcher Plant Effect Radius"), sniff.pitcherEffectGiveInRadius).setDefaultValue(0).setSaveConsumer(v -> sniff.pitcherEffectGiveInRadius = v).build());
            sniffSub.add(entryBuilder.startIntField(Text.of("Pitcher Plant Effect Disable Duration On Damage (Ticks)"), sniff.pitcherEffectCancelWhenDamagedInTicks).setDefaultValue(400).setSaveConsumer(v -> sniff.pitcherEffectCancelWhenDamagedInTicks = v).build());

            SubCategoryBuilder pEffects = entryBuilder.startSubCategory(Text.of("Pitcher Plant Status Effects"));
            for(Map.Entry<String, Integer> en : sniff.pitcherEffects.entrySet()) {
                pEffects.add(entryBuilder.startIntField(Text.of(en.getKey()), en.getValue()).setSaveConsumer(v -> sniff.pitcherEffects.put(en.getKey(), v)).build());
            }
            sniffSub.add(pEffects.build());
            itemsCat.addEntry(sniffSub.build());

            // ====================================================================
            // 7. LOOT MODIFIERS
            // ====================================================================
            ConfigCategory lootCat = builder.getOrCreateCategory(Text.of("Loot Modifiers"));
            buildMapCategory(builder, lootCat, null, "Entity", lootConfig.entities, parent,
                    LootModifierConfig.Rule::new,
                    (sub, rule) -> {
                        sub.add(entryBuilder.startBooleanToggle(Text.of("Enabled"), rule.enabled).setSaveConsumer(v -> rule.enabled = v).build());

                        for(int i=0; i<rule.drops.size(); i++) {
                            LootModifierConfig.Drop drop = rule.drops.get(i);
                            SubCategoryBuilder dropSub = entryBuilder.startSubCategory(Text.of("Drop " + i + ": " + drop.item));
                            dropSub.add(entryBuilder.startStrField(Text.of("Item"), drop.item).setSaveConsumer(v -> drop.item = v).build());
                            if (drop.itemDroppedOnFire != null) dropSub.add(entryBuilder.startStrField(Text.of("Fire Item"), drop.itemDroppedOnFire).setSaveConsumer(v -> drop.itemDroppedOnFire = v).build());
                            dropSub.add(entryBuilder.startFloatField(Text.of("Chance"), drop.chance).setSaveConsumer(v -> drop.chance = v).build());
                            dropSub.add(entryBuilder.startFloatField(Text.of("Min"), drop.min).setSaveConsumer(v -> drop.min = v).build());
                            dropSub.add(entryBuilder.startFloatField(Text.of("Max"), drop.max).setSaveConsumer(v -> drop.max = v).build());

                            // Delete Drop Button
                            dropSub.add(buildButtonEntry(Text.literal("❌ Remove Drop").formatted(Formatting.RED), () -> {
                                rule.drops.remove(drop);
                                lastCategory = "Loot Modifiers";
                                MinecraftClient.getInstance().setScreen(getModConfigScreenFactory().create(parent));
                            }));

                            sub.add(dropSub.build());
                        }

                        // Add Drop Button (Uses "Add Drop" specialized logic for List, not Map)
                        sub.add(buildButtonEntry(Text.of("✚ Add New Drop"), () -> {
                            rule.drops.add(new LootModifierConfig.Drop());
                            lastCategory = "Loot Modifiers";
                            MinecraftClient.getInstance().setScreen(getModConfigScreenFactory().create(parent));
                        }));
                    }
            );

            // ====================================================================
            // 8. DEBUG
            // ====================================================================
            ConfigCategory debugCat = builder.getOrCreateCategory(Text.of("Debug"));
            debugCat.addEntry(entryBuilder.startBooleanToggle(Text.of("Orbit Enabled"), debugConfig.orbitEnabled)
                    .setDefaultValue(false).setSaveConsumer(v -> debugConfig.orbitEnabled = v).build());

            // ====================================================================
            // SAVE & CLEANUP
            // ====================================================================
            builder.setSavingRunnable(() -> {
                ConfigManager.save();
                ExtraConfig.save();
                DebugConfig.save();
                try (FileWriter w = new FileWriter(LootModifierConfig.PATH.toFile())) {
                    new GsonBuilder().setPrettyPrinting().create().toJson(lootConfig, w);
                } catch (IOException e) { e.printStackTrace(); }
            });

            // Smart Category Restore
            if (lastCategory != null) {
                if(lastCategory.equals("Relations (AI)")) builder.setFallbackCategory(relationsCat);
                else if(lastCategory.equals("Consumption")) builder.setFallbackCategory(consumeCat);
                else if(lastCategory.equals("Mechanics")) builder.setFallbackCategory(mechCat);
                else if(lastCategory.equals("Predators")) builder.setFallbackCategory(predCat);
                else if(lastCategory.equals("Items & Misc")) builder.setFallbackCategory(itemsCat);
                else if(lastCategory.equals("Loot Modifiers")) builder.setFallbackCategory(lootCat);
                else if(lastCategory.equals("Debug")) builder.setFallbackCategory(debugCat);
                else builder.setFallbackCategory(entityCat);
            }

            return builder.build();
        };
    }

    // ====================================================================
    // HELPERS
    // ====================================================================

    private <T> void buildMapCategory(
            ConfigBuilder builder,
            ConfigCategory rootCategory,
            SubCategoryBuilder parentSub,
            String entityName,
            Map<String, T> map,
            net.minecraft.client.gui.screen.Screen parentScreen,
            Supplier<T> defaultFactory,
            BiConsumer<SubCategoryBuilder, T> contentBuilder
    ) {
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        List<String> suggestions = new ArrayList<>();
        try {
            if (Registries.ENTITY_TYPE != null) {
                for (Identifier id : Registries.ENTITY_TYPE.getIds()) suggestions.add(id.toString());
                Collections.sort(suggestions);
            }
        } catch (Exception e) { suggestions.add("minecraft:pig"); }

        final String[] selectedId = { "" };
        var dropdown = entryBuilder.startStringDropdownMenu(Text.of("Select ID to Add:"), "")
                .setDefaultValue("").setSelections(suggestions).setSaveConsumer(v -> selectedId[0] = v).build();

        var addButton = buildButtonEntry(Text.of("✚ Add New " + entityName), () -> {
            dropdown.save();
            String newId = selectedId[0];
            if (newId != null && !newId.isBlank() && !map.containsKey(newId)) {
                map.put(newId, defaultFactory.get());
                if (rootCategory != null) lastCategory = rootCategory.getCategoryKey().getString();
                MinecraftClient.getInstance().setScreen(getModConfigScreenFactory().create(parentScreen));
            }
        });

        if (parentSub != null) { parentSub.add(dropdown); parentSub.add(addButton); }
        else if (rootCategory != null) { rootCategory.addEntry(dropdown); rootCategory.addEntry(addButton); }

        for (Map.Entry<String, T> entry : map.entrySet()) {
            String key = entry.getKey();
            SubCategoryBuilder sub = entryBuilder.startSubCategory(Text.of(key));
            sub.add(buildButtonEntry(Text.literal("❌ Delete ").append(key).formatted(Formatting.RED), () -> {
                map.remove(key);
                if (rootCategory != null) lastCategory = rootCategory.getCategoryKey().getString();
                MinecraftClient.getInstance().setScreen(getModConfigScreenFactory().create(parentScreen));
            }));
            contentBuilder.accept(sub, entry.getValue());
            if (parentSub != null) parentSub.add(sub.build());
            else if (rootCategory != null) rootCategory.addEntry(sub.build());
        }
    }

    private AbstractConfigListEntry<Object> buildButtonEntry(Text text, Runnable onClick) {
        return new AbstractConfigListEntry<Object>(Text.empty(), false) {
            private final ButtonWidget widget = ButtonWidget.builder(text, button -> onClick.run()).dimensions(0, 0, 200, 20).build();
            @Override public void render(DrawContext context, int index, int y, int x, int w, int h, int mx, int my, boolean hov, float tick) {
                widget.setX(x + (w / 2) - (widget.getWidth() / 2)); widget.setY(y); widget.render(context, mx, my, tick);
            }
            @Override public java.util.List<? extends Element> children() { return Collections.singletonList(widget); }
            @Override public java.util.List<? extends Selectable> narratables() { return Collections.singletonList(widget); }
            @Override public Object getValue() { return null; }
            @Override public Optional<Object> getDefaultValue() { return Optional.empty(); }
            @Override public void save() {}
        };
    }
}