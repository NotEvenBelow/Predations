package dev.foltz.predations.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public final class ExtraConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/predations-extras.json");

    // ---------- step up ----------
    public static class StepPerEntity { public Boolean enabled; public Float height; }
    public static final class StepUpConfig {
        public float defaultHeight = 1.0f;
        public Map<String, StepPerEntity> entities = new LinkedHashMap<>();
    }

    // ---------- flee on attack ----------
    public static final class FleeOnAttackConfig {
        public boolean enabled = true;
        public int windowTicks = 200;
        public float safeDistanceMultiplier = 1.5f;
        public int repathCooldownTicks = 10;
    }

    // ---------- flee from player ----------
    public static final class FleeFromPlayerConfig {
        public boolean enabled = true;
        public int lingerTicks = 80;
        public float safeDistanceMultiplier = 1.5f;
        public int repathCooldownTicks = 10;
    }

    // ---------- angry mobs ----------
    public static final class AngryMob {
        public boolean enabled = true;
        public float maxHearts = 9f;
        public double kickingRange = 3.5;
        public float kickDamageHeartsEasy = 1.0f;
        public float kickDamageHeartsNormal = 1.5f;
        public float kickDamageHeartsHard = 2.0f;
        public Integer kickCooldownTicks = 30;
        public Integer panicDistance = 15;
        public Float panicFarSpeed = 1.5f;
        public Float panicNearSpeed = 2.8f;
        public Float panicRatio = 0.5f;
    }
    public static final class AngryMobsConfig {
        public boolean enabled = true;
        public int defaultKickCooldownTicks = 20;
        public Map<String, AngryMob> entities = new LinkedHashMap<>();
    }

    // ---------- rare variants ----------
    public static final class RareVariantsConfig {
        public boolean famishedCowEnabled = false;
        public double famishedCowChance = 0.006;
        public Float famishedCowBaseSpeed = null;
        public float famishedCowSpeedMultiplier = 0.4f;
    }

    // ---------- secret ----------
    public static final class SecretConfig {
        public boolean noKnockbackAll = false;
        public Set<String> knockbackWhitelist = new LinkedHashSet<>();
    }

    // ---------- predatory squid (FULL restored) ----------
    public static final class PredatorySquidConfig {
        public boolean enabled = true;
        public int lowLightLevel = 7;
        public String note_squidFloatAtNight = "[UNUSED] Future Development, Squid will float at night";
        public boolean squidFloatAtNight = false;
        public boolean nudgingSquidwhenStuckonLand = false;
        public String note_nudgingRange = "by Default, 3x3 all directions";
        public double nudgingRange = 3;
        public double nudgingJumpHeightVelocity = 0.4;
        public int nudgingInterval = 35;
        public boolean nudgePathFindThroughWall = false;

        public double replaceChance = 0.05;
        public float glowSquidChance = 0.25f;

        public float dmgHeartsEasy = 0.5f, dmgHeartsNormal = 1.0f, dmgHeartsHard = 1.5f;
        public int squidMaxHealth = 10;
        public int glowSquidMaxHealth = 20;
        public int tickInterval = 40;     // drain every N ticks when latched
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

        public String note_psychic = "Squid Long Range Attack";
        public int psychicGrabIntervalTicks = 200;
        public double psychicGrabRange = 12.5;
        public double psychicGrabChance = 0.5;
        public String note_latchRangeBlocks = "Squid Close Range Attack";
        public double latchRangeBlocks = 3.0;
        public int requiredTicksInRange = 1;
        public Set<String> excludedTargets =
                new HashSet<>(Set.of("creeper", "squid", "glow_squid",
                        "predations:predatory_squid", "predations:predatory_glow_squid"));

        public boolean fishFleeSquidLikePlayer = true;
        public int fishFleeDistance = 8;

        public String note_attachDistance = "[UNUSED] Squid stuck/attach distance to Entities Head";
        public double attachDistance = 1.25;
        public boolean blockedBySolids = true;
        public float lineBreakSpeed = 1.6f;

        public String note_tongueEnabled = "[UNUSED] Lips when squid successfully do psychic grab";
        public boolean tongueEnabled = true;
        public double catchSuccessChance = 0.85;
        public int regrabCooldownTicks = 60;
        public double peakComedySquidChance = 0.0001;
        public double peakComedyGlowSquidChance = 0.000001;
    }

    // ---------- fox ----------
    public static final class FoxItemsConfig {
        public boolean FoxTalismanFunctionEnabled = true;
        public double FoxFeatherDropChance = 20;
        public int FoxFeatherDropRollTickInterval = 6000;
        public int TalismanUseTime = 3;
        public int TalismanCooldowninSecond = 30;
        public int TalismanSquidImmunityTimeInSecond = 60;

        public List<TalismanEffectEntry> TalismanEffects =
                List.of(
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
        public TalismanEffectEntry(String effectId, int durationSeconds, int amplifier) {
            this.effectId = effectId;
            this.durationSeconds = durationSeconds;
            this.amplifier = amplifier;
        }
    }

    // ---------- burned meat ----------
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

    // ---------- ROOT ----------
    public static final class Model {
        public StepUpConfig stepUp = new StepUpConfig();
        public FleeOnAttackConfig fleeOnAttack = new FleeOnAttackConfig();
        public FleeFromPlayerConfig fleeFromPlayer = new FleeFromPlayerConfig();
        public AngryMobsConfig angryMobs = new AngryMobsConfig();
        public RareVariantsConfig rareVariants = new RareVariantsConfig();
        public SecretConfig secret = new SecretConfig();
        public PredatorySquidConfig predSquid = new PredatorySquidConfig();
        public FoxItemsConfig foxItems = new FoxItemsConfig();
        public BurnedMeatConfig burnedMeat = new BurnedMeatConfig();
        public PredatorySquidConfig predatorySquid = new PredatorySquidConfig();
    }

    private static Model DATA;
    private ExtraConfig() {}
    static { try { if (DATA == null) load(); } catch (Throwable ignored) {} }

    // ---------- IO ----------
    public static synchronized void load() {
        if (FILE.exists()) {
            try (FileReader r = new FileReader(FILE)) {
                Model m = GSON.fromJson(r, Model.class);
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
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                System.err.println("[Predations] FAILED to create config directory: " + parentDir.getAbsolutePath());
                return;
            }
            try (FileWriter w = new FileWriter(FILE)) {
                GSON.toJson(DATA, w);
            }
        } catch (Exception e) {
            System.err.println("[Predations] FAILED to save config file: " + FILE.getAbsolutePath());
            e.printStackTrace();
        }
    }

    private static void normalize(Model m) {
        if (m.stepUp == null) m.stepUp = new StepUpConfig();
        if (m.stepUp.entities == null) m.stepUp.entities = new LinkedHashMap<>();

        // ✅ only add horse example
        if (!m.stepUp.entities.containsKey("minecraft:horse")) {
            StepPerEntity step = new StepPerEntity();
            step.enabled = true;
            step.height = 1.0f;
            m.stepUp.entities.put("minecraft:horse", step);
        }

        if (m.fleeOnAttack == null) m.fleeOnAttack = new FleeOnAttackConfig();
        if (m.fleeFromPlayer == null) m.fleeFromPlayer = new FleeFromPlayerConfig();

        if (m.angryMobs == null) m.angryMobs = new AngryMobsConfig();
        if (m.angryMobs.entities == null) m.angryMobs.entities = new LinkedHashMap<>();

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
    }

    public static Model get() {
        if (DATA == null) load();
        return DATA;
    }

    public static boolean fleeOnAttackEnabled() { return get().fleeOnAttack.enabled; }
    public static int fleeWindowTicks() { return get().fleeOnAttack.windowTicks; }
    public static boolean fleeFromPlayerEnabled() { return get().fleeFromPlayer.enabled; }
    public static float fleeFromPlayerSafeMult() { return get().fleeFromPlayer.safeDistanceMultiplier; }
    public static int fleeFromPlayerRepathCd() { return get().fleeFromPlayer.repathCooldownTicks; }
    public static int fleeFromPlayerLinger() { return get().fleeFromPlayer.lingerTicks; }
    public static boolean angryEnabled() { return get().angryMobs.enabled; }

    public static AngryMob angryFor(Entity e) {
        if (e == null) return null;
        var id = Registries.ENTITY_TYPE.getId(e.getType());
        if (id == null) return null;
        return get().angryMobs.entities.get(id.getPath().toLowerCase());
    }

    public static int angryDefaultKickCooldown() { return get().angryMobs.defaultKickCooldownTicks; }
}
