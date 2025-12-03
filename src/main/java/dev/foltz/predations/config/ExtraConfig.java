package dev.foltz.predations.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public final class ExtraConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final File FILE = new File("config/predations-extras.json");

    // ==========================================
    //              CONFIG OBJECTS
    // ==========================================

    public static class StepPerEntity { public Boolean enabled; public Float height; }

    public static final class StepUpConfig {
        public float defaultHeight = 1.0f;
        public Map<String, StepPerEntity> entities = new LinkedHashMap<>();
    }

    public static final class AngryMob {
        public boolean enabled = true;
        public Double maxHearts = 9.0;
        public Double kickingRange = 3.5;
        public Float kickDamageHeartsEasy = 1.0f;
        public Float kickDamageHeartsNormal = 1.5f;
        public Float kickDamageHeartsHard = 2.0f;
        public Integer kickCooldownTicks = 30;
        public Double runSpeed = 1.5;
        public Double kickHorizontalVelocity = 0.28;
        public Double kickVerticalVelocity = 1.2;
        public Integer kickActiveWindowTicks = 120;
    }

    public static final class AngryMobsConfig {
        public boolean enabled = true;
        public int defaultKickCooldownTicks = 20;
        public Map<String, AngryMob> entities = new LinkedHashMap<>();
    }

    public static final class MilkChangeConfig {
        public boolean milkRestoreHungerInsteadRemovingEffect = true;
        public double milkHungerRestore = 2.5;
        public float milkSaturation = 2.0f;
        public int milkCowCooldownSeconds = 300;
    }

    public static final class RareVariantsConfig {
        public boolean famishedCowEnabled = false;
        public double famishedCowChance = 0.006;
        public Float famishedCowBaseSpeed = null;
        public float famishedCowSpeedMultiplier = 0.4f;
    }

    public static final class SecretConfig {
        public boolean noKnockbackAll = false;
        public Set<String> knockbackWhitelist = new LinkedHashSet<>();
    }

    public static final class PredatorySquidConfig {
        public boolean enabled = true;
        public int lowLightLevel = 7;
        public double aiWorkRange = 48;
        public String _note_squidFloatAtNight = "Squid floating mechanic (WIP)";
        public boolean squidFloatAtNight = false;
        public boolean nudgingSquidwhenStuckonLand = false;
        public String _note_nudgingRange = "Default: 3x3 all directions";
        public double nudgingRange = 3;
        public double nudgingJumpHeightVelocity = 0.4;
        public int nudgingInterval = 35;
        public boolean nudgePathFindThroughWall = false;
        public double replaceChance = 0.05;
        public float glowSquidChance = 0.25f;
        public float dmgHeartsEasy = 0.5f;
        public float dmgHeartsNormal = 1.0f;
        public float dmgHeartsHard = 1.5f;
        public int squidMaxHealth = 10;
        public int glowSquidMaxHealth = 20;
        public int attackTickInterval = 40;
        public float healPct = 0.4f;
        public double extraThornDamage = 0.5;
        public double downforceDrag = 0.5;
        public boolean squidAndGlowGiveBlindness = true;
        public int squidAndGlowSlownessLevel = 6;
        public int glowHungerLevel = 10;
        public int squidHungerLevel = 2;
        public int glowWeaknessLevel = 1;
        public int glowMiningFatigueLevel = 1;
        public int glowStrengthLevel = 1;
        public boolean breakBoats = true;
        public boolean breakMinecarts = true;
        public String _note_psychic = "Squid Long Range Attack";
        public int psychicGrabIntervalTicks = 200;
        public double psychicGrabRange = 12.5;
        public double psychicGrabChance = 0.5;
        public String _note_latchRangeBlocks = "Squid Close Range Attack";
        public double latchRangeBlocks = 3.0;
        public int requiredTicksInRange = 1;
        public Set<String> excludedTargets = new HashSet<>(Set.of("creeper", "squid", "glow_squid", "predations:predatory_squid", "predations:predatory_glow_squid"));
        public boolean fishFleeSquidLikePlayer = true;
        public int fishFleeDistance = 8;
        public String _note_attachDistance = "Squid attach distance to Head";
        public double attachDistance = 1.25;
        public boolean blockedBySolids = true;
        public float lineBreakSpeed = 1.6f;
        public boolean tongueEnabled = true;
        public double catchSuccessChance = 0.85;
        public int regrabCooldownTicks = 60;
        public double peakComedySquidChance = 0.0001;
        public double peakComedyGlowSquidChance = 0.000001;
    }

    public static final class FoxItemsConfig {
        public boolean FoxTalismanFunctionEnabled = true;
        public double FoxFeatherDropChance = 20;
        public int FoxFeatherDropRollTickInterval = 6000;
        public int TalismanUseTime = 3;
        public double TalismanChanceToNotGetKuruWhenEatingVillagerMeat = 0.15;
        public int TalismanCooldowninSecond = 30;
        public int TalismanSquidImmunityTimeInSecond = 60;
        public List<TalismanEffectEntry> TalismanEffects = List.of(
                new TalismanEffectEntry("minecraft:jump_boost", 4, 2),
                new TalismanEffectEntry("minecraft:speed", 6, 1),
                new TalismanEffectEntry("minecraft:weakness", 10, 0),
                new TalismanEffectEntry("minecraft:resistance", 10, 0)
        );
    }

    public static final class TalismanEffectEntry {
        public String effectId;
        public int durationSeconds;
        public int amplifier;
        public TalismanEffectEntry() {}
        public TalismanEffectEntry(String effectId, int durationSeconds, int amplifier) {
            this.effectId = effectId;
            this.durationSeconds = durationSeconds;
            this.amplifier = amplifier;
        }
    }

    public static final class BurnedMeatEntry {
        public int min = 1;
        public int max = 1;
        public BurnedMeatEntry() {}
        public BurnedMeatEntry(int min, int max) { this.min = min; this.max = max; }
        public int nextCount(net.minecraft.util.math.random.Random rng) {
            if (max <= min) return min;
            return rng.nextBetween(min, max);
        }
    }

    public static final class BurnedMeatConfig {
        public boolean enabled = true;
        public Map<String, BurnedMeatEntry> entities = new LinkedHashMap<>();
        public boolean onlySmokerGivesCookedMeat = false;
        public Set<String> burnInsteadItems = new LinkedHashSet<>();
        public float campfireBurnChance = 0.8f;
    }

    public static final class PufferfishEffectEntry {
        public String effectId;
        public int durationSeconds;
        public int amplifier;
        public PufferfishEffectEntry() {}
        public PufferfishEffectEntry(String effectId, int durationSeconds, int amplifier) {
            this.effectId = effectId;
            this.durationSeconds = durationSeconds;
            this.amplifier = amplifier;
        }
    }

    public static final class PufferfishConfig {
        public double jackpotChance = 0.01;
        public List<PufferfishEffectEntry> jackpotEffects = List.of(
                new PufferfishEffectEntry("minecraft:strength", 30, 1),
                new PufferfishEffectEntry("minecraft:resistance", 60, 0),
                new PufferfishEffectEntry("minecraft:speed", 60, 0)
        );
    }

    public static final class VillagerConfig {
        public String _note_zombificationChance = "Chance (0.0 to 1.0) for natural villagers to spawn as zombie villagers.";
        public double zombificationChance = 1.0;
        public boolean LifeRitualFunctionEnabled = true;
        public String _note_disableCuring = "If true, players cannot cure zombie villagers with a golden apple.";
        public boolean disableCuring = true;;
        public String _note_customCureSplashRange = "Splash radius for custom Curing Potion.";
        public double customCureSplashRange = 3.0;
        public boolean ironGolemChangeEnabled = true;
        public double ironGolemReachRangeVertically= 4.5;
        public boolean ironGolemCannotSuffocate = true;
    }

    public static final class KuruStageEffect {
        public String effectId;
        public int durationSeconds;
        public int amplifier;
        public KuruStageEffect() {}
        public KuruStageEffect(String effectId, int durationSeconds, int amplifier) {
            this.effectId = effectId;
            this.durationSeconds = durationSeconds;
            this.amplifier = amplifier;
        }
    }

    public static final class KuruStage {
        public int startTick;
        public List<KuruStageEffect> effects;
        public KuruStage(int startTick, List<KuruStageEffect> effects) {
            this.startTick = startTick;
            this.effects = effects;
        }
    }

    public static final class KuruConfig {
        public boolean enabled = true;
        public boolean chorusFruitRemovesKuru = true;
        public boolean goldenAppleRemovesKuru = false;
        public boolean enchantedGoldenAppleRemovesKuru = true;
        public double kuruChanceforVillagerMeatNoTalisman = 0.95;
        public boolean milkCanCureKuru = false;
        public boolean unableToEatAnyMeatsAtStage2 = true;
        public boolean zombifiedIfDieWithKuru = true;
        public boolean stage4Kill = true;
        public int stage3SurvivedKillSecond = 300;
        public Map<String, KuruStage> stages = new LinkedHashMap<>();
    }

    public static final class RabiesConfig {
        public boolean enabled = true;
        public boolean milkRemoveRabies = false;
        public boolean chorusFruitRemovesRabies = true;
        public boolean goldenAppleRemovesRabies = false;
        public boolean enchantedGoldenAppleRemovesRabies = true;
        public int timeSurviveWithRabiesEffectTillKilledInTick = 72000;
        public double naturalAggressiveWolfSpawnChance = 0.01;
        public double nonNaturalAggressiveWolfRabiesBitChance = 0.001;
        public double naturalAggressiveWolfRabiesBitChance = 0.98;
        public double naturalAggressiveFoxSpawnChance = 0.01;
        public double naturalAggressiveFoxRabiesBitChance = 0.98;
        public double batWhenFeedingRabiesBitChance = 0.1;
        public double batRabiesChanceWhenFinallyBit = 0.9;
        public double batGlowOtherEntitiesRange = 8;
    }

    public static final class BatGuanoAndBoneMealConfig {
        public boolean enabled = true;
        public boolean vanillaBoneMealSpeedUpCropGrowthInsteadofInstantGrowth = false;
        public String _note_guanoGrowth = "If false, guano speeds up growth (x3 bone meal) if setting above is true.";
        public boolean guanoFertilizerInstantFinishCropGrowth = true;
        public double vanillaBoneMealSpeedUpCropGrowthInsteadofInstantGrowthMultiplier = 2.0;
        public double guanoFertilizerCropGrowthMultiplier = 3.0;
        public double guanoFertilizerConsumeChance = 0.33;
        public double batGuanoDropChance = 0.15;
        public double phantomGuanoDropChance = 0.4;
        public int batGuanoRollIntervalInTick = 1500;
        public int phantomGuanoRollIntervalInTick = 1500;
        public boolean batCanLeash = true;
        public boolean phantomCanLeash = false;
    }

    public static final class BetterSnifferRelatedFeaturesConfig {
        public boolean enabled = true;
        public boolean snifferBoostCropGrowth = true;
        public double snifferCropGrowthMultiplier = 1.5;
        public int snifferCropGrowthRadius = 4;
        public int snifferCropGrowthDurationInTicks = 200;
        public int snifferCropGrowthInterval = 400;
        public int torchflowerEmitsLightLevel = 7;
        public boolean beeMoreEffectiveWhenPollenateWithTorchflower = true;
        public double beeMoreEffectiveWhenPollenateWithTorchflowerMultiplier = 2.0;
        public int pitcherEffectGiveInLightLevel = 10;
        public int pitcherEffectGiveInRadius = 0;
        public int pitcherEffectCancelWhenDamagedInTicks = 400;
        public Map<String, Integer> pitcherEffects = new LinkedHashMap<>();
    }

    // ==========================================
    //       MAIN MODEL
    // ==========================================

    public static final class Model {

        public StepUpConfig stepUp = new StepUpConfig();
        public AngryMobsConfig angryMobs = new AngryMobsConfig();
        public MilkChangeConfig milkChange = new MilkChangeConfig();
        public RareVariantsConfig rareVariants = new RareVariantsConfig();
        public PredatorySquidConfig predSquid = new PredatorySquidConfig();
        public FoxItemsConfig foxItems = new FoxItemsConfig();
        public BurnedMeatConfig burnedMeat = new BurnedMeatConfig();
        public PufferfishConfig pufferfish = new PufferfishConfig();
        public KuruConfig kuru = new KuruConfig();
        public VillagerConfig villager = new VillagerConfig();
        public RabiesConfig rabies = new RabiesConfig();
        public BatGuanoAndBoneMealConfig batGuanoAndBoneMeal = new BatGuanoAndBoneMealConfig();
        public BetterSnifferRelatedFeaturesConfig betterSniffer = new BetterSnifferRelatedFeaturesConfig();
        public SecretConfig secret = new SecretConfig();
    }

    // ==========================================
    //       LOAD / SAVE LOGIC
    // ==========================================

    private static Model DATA;
    private ExtraConfig() {}
    static { try { if (DATA == null) load(); } catch (Throwable ignored) {} }

    public static synchronized void load() {
        if (FILE.exists()) {
            try (FileReader r = new FileReader(FILE)) {
                JsonReader reader = new JsonReader(r);
                reader.setLenient(true);

                Model m = GSON.fromJson(reader, Model.class);
                if (m == null) m = new Model();
                normalize(m);
                DATA = m;
            } catch (Exception ignored) {
                if (DATA == null) { DATA = new Model(); normalize(DATA); }
            }
            return;
        }
        DATA = new Model();
        normalize(DATA);
        save();
    }

    public static synchronized void save() {
        try {
            File parentDir = FILE.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) return;

            String json = GSON.toJson(DATA);

            json = insertHeader(json, "stepUp", "STEP UP");
            json = insertHeader(json, "angryMobs", "ANGRY MOBS");
            json = insertHeader(json, "milkChange", "MILK CHANGE");
            json = insertHeader(json, "rareVariants", "RARE VARIANTS");
            json = insertHeader(json, "predSquid", "PREDATORY SQUID");
            json = insertHeader(json, "foxItems", "FOX ITEMS");
            json = insertHeader(json, "burnedMeat", "BURNED MEAT");
            json = insertHeader(json, "pufferfish", "PUFFERFISH");
            json = insertHeader(json, "kuru", "KURU DISEASE");
            json = insertHeader(json, "villager", "VILLAGERS");
            json = insertHeader(json, "rabies", "RABIES");
            json = insertHeader(json, "batGuanoAndBoneMeal", "GUANO & BONEMEAL");
            json = insertHeader(json, "betterSniffer", "SNIFFER");
            json = insertHeader(json, "secret", "SECRET");

            try (FileWriter w = new FileWriter(FILE)) {
                w.write(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String insertHeader(String json, String key, String title) {
        String header =
                "\n" +
                        "  // |---------------------------------------------------------------------|\n" +
                        "  // |=======================[ " + String.format("%-18s", title) + "]=======================|\n" +
                        "  // |---------------------------------------------------------------------|\n" +
                        "  \"" + key + "\":";

        return json.replace("\"" + key + "\":", header);
    }

    private static void normalize(Model m) {
        if (m.stepUp == null) m.stepUp = new StepUpConfig();
        if (m.stepUp.entities == null) m.stepUp.entities = new LinkedHashMap<>();
        if (!m.stepUp.entities.containsKey("minecraft:horse")) {
            StepPerEntity step = new StepPerEntity();
            step.enabled = true;
            step.height = 1.0f;
            m.stepUp.entities.put("minecraft:horse", step);
        }

        if (m.angryMobs == null) m.angryMobs = new AngryMobsConfig();
        if (m.angryMobs.entities == null) m.angryMobs.entities = new LinkedHashMap<>();

        if (m.milkChange == null) m.milkChange = new MilkChangeConfig();

        if (m.rareVariants == null) m.rareVariants = new RareVariantsConfig();
        if (m.secret == null) m.secret = new SecretConfig();
        if (m.secret.knockbackWhitelist == null) m.secret.knockbackWhitelist = new LinkedHashSet<>();
        if (m.predSquid == null) m.predSquid = new PredatorySquidConfig();
        if (m.predSquid.excludedTargets == null) m.predSquid.excludedTargets = new HashSet<>();
        if (m.foxItems == null) m.foxItems = new FoxItemsConfig();
        if (m.burnedMeat == null) m.burnedMeat = new BurnedMeatConfig();
        if (m.burnedMeat.entities == null) m.burnedMeat.entities = new LinkedHashMap<>();
        if (m.burnedMeat.burnInsteadItems == null) m.burnedMeat.burnInsteadItems = new LinkedHashSet<>();
        m.angryMobs.entities.putIfAbsent("cow", new AngryMob());
        m.angryMobs.entities.putIfAbsent("mooshroom", new AngryMob());
        if (m.burnedMeat.entities.isEmpty()) {
            m.burnedMeat.entities.put("cow", new BurnedMeatEntry(1, 2));
            m.burnedMeat.entities.put("pig", new BurnedMeatEntry(1, 2));
            m.burnedMeat.entities.put("sheep", new BurnedMeatEntry(1, 2));
            m.burnedMeat.entities.put("chicken", new BurnedMeatEntry(1, 2));
        }
        if (m.burnedMeat.burnInsteadItems.isEmpty()) {
            m.burnedMeat.burnInsteadItems.addAll(List.of("beef","porkchop","mutton","chicken","rabbit","cod","salmon"));
        }
        if (m.pufferfish == null) m.pufferfish = new PufferfishConfig();
        if (m.pufferfish.jackpotEffects == null) {
            m.pufferfish.jackpotEffects = List.of(
                    new PufferfishEffectEntry("minecraft:strength", 30, 1),
                    new PufferfishEffectEntry("minecraft:resistance", 60, 0),
                    new PufferfishEffectEntry("minecraft:speed", 60, 0)
            );
        }
        if (m.kuru == null) m.kuru = new KuruConfig();
        if (m.kuru.stages == null) m.kuru.stages = new LinkedHashMap<>();
        if (m.kuru.stages.isEmpty()) {
            m.kuru.stages.put("1", new KuruStage(6000, List.of(
                    new KuruStageEffect("minecraft:slowness", 300, 1),
                    new KuruStageEffect("minecraft:mining_fatigue", 300, 1)
            )));
            m.kuru.stages.put("2", new KuruStage(19200, List.of(
                    new KuruStageEffect("minecraft:slowness", 300, 3),
                    new KuruStageEffect("minecraft:nausea", 300, 0)
            )));
            m.kuru.stages.put("3", new KuruStage(26400, List.of(
                    new KuruStageEffect("minecraft:wither", 300, 0),
                    new KuruStageEffect("minecraft:weakness", 300, 4)
            )));
        }
        if (m.villager == null) m.villager = new VillagerConfig();
        if (m.rabies == null) m.rabies = new RabiesConfig();
        if (m.batGuanoAndBoneMeal == null) m.batGuanoAndBoneMeal = new BatGuanoAndBoneMealConfig();

        if (m.betterSniffer == null) m.betterSniffer = new BetterSnifferRelatedFeaturesConfig();
        if (m.betterSniffer.pitcherEffects == null) m.betterSniffer.pitcherEffects = new LinkedHashMap<>();
        if (m.betterSniffer.pitcherEffects.isEmpty()) {
            m.betterSniffer.pitcherEffects.put("minecraft:speed", 0);
            m.betterSniffer.pitcherEffects.put("minecraft:resistance", 0);
        }
    }

    public static Model get() {
        if (DATA == null) load();
        return DATA;
    }

    public static boolean angryEnabled() { return get().angryMobs.enabled; }
    public static AngryMob angryFor(Entity e) {
        if (e == null) return null;
        var id = Registries.ENTITY_TYPE.getId(e.getType());
        if (id == null) return null;
        return get().angryMobs.entities.get(id.getPath().toLowerCase());
    }
    public static int angryDefaultKickCooldown() { return get().angryMobs.defaultKickCooldownTicks; }
    public static PufferfishConfig getPufferfishConfig() { return get().pufferfish; }
    public static KuruConfig getKuruConfig() { return get().kuru; }
    public static VillagerConfig getVillagerConfig() { return get().villager; }
    public static RabiesConfig getRabiesConfig() { return get().rabies; }
    public static BatGuanoAndBoneMealConfig getBatGuanoConfig() { return get().batGuanoAndBoneMeal; }
    public static BetterSnifferRelatedFeaturesConfig getSnifferConfig() { return get().betterSniffer; }
    public static MilkChangeConfig getMilkConfig() { return get().milkChange; }
}