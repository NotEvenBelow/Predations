package dev.foltz.predations.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final File FILE = new File("config/predations.json");
    private static Config DATA;

    public static final class Tuning {
        public Boolean enabled;
        public Double runSpeed;
        public Double runNearPlayerBlock;
        public Boolean requireLineOfSightToRun;
        public Double shiftingReduceDetectRangeByPercent;
        public Double SafeDistance;
        public Double nearPlayerSpeedMultiplier;
        public Integer continueToRunOutsideOfSafeDistanceInTicks;
        public Boolean allowLure;
        public Boolean leashingStopTheRunning;
    }

    public static final class Defaults {
        public boolean enabled = false;
        public double runSpeed = 2.6;
        public double runNearPlayerBlock = 10.0;
        public boolean requireLineOfSightToRun = true;
        public double shiftingReduceDetectRangeByPercent = 0.4;
        public double SafeDistance = 16.0;
        public double nearPlayerSpeedMultiplier = 1.25;
        public int continueToRunOutsideOfSafeDistanceInTicks = 120;
        public boolean allowLure = false;
        public boolean leashingStopTheRunning = true;
    }

    public static final class Config {
        public Defaults defaults = new Defaults();
        public Map<String, Tuning> entities = new LinkedHashMap<>();
        public Set<String> enabledEntities;
        public Map<String, List<String>> aggressionTargets = new LinkedHashMap<>();
        public Map<String, List<String>> runawayFromEntities = new LinkedHashMap<>();
        public ConsumptionDefaults consumeDefaults = new ConsumptionDefaults();
        public Map<String, ConsumptionTuning> consume = new LinkedHashMap<>();
        public Map<String, List<String>> forAllowLuresinEntities = new LinkedHashMap<>();
    }

    public static final class ConsumptionDefaults {
        public boolean enabled = false;
        public float healHearts = 1.5f;
        public int delayTicks = 50;
        public double radius = 2.0;
        public List<ConsumptionEffectEntry> getFullHealthBuffs = new ArrayList<>();
    }

    public static final class ConsumptionTuning {
        public Boolean enabled;
        public Float healHearts;
        public Integer delayTicks;
        public Double radius;
        public Set<String> items = new LinkedHashSet<>();
    }

    public static final class ConsumptionEffectEntry {
        public String effectId;
        public int durationSeconds;
        public int amplifier;
    }

    private ConfigManager() {}

    public static void load() {
        if (FILE.exists()) {
            try (FileReader r = new FileReader(FILE)) {
                JsonReader reader = new JsonReader(r);
                reader.setLenient(true);
                DATA = GSON.fromJson(reader, Config.class);
                if (DATA == null) DATA = fresh();
            } catch (IOException e) {
                DATA = fresh();
                save();
            }
        } else {
            DATA = fresh();
            save();
        }

        if (DATA.enabledEntities != null && !DATA.enabledEntities.isEmpty()) {
            for (String key : DATA.enabledEntities) {
                DATA.entities.computeIfAbsent(key, k -> {
                    Tuning t = new Tuning();
                    t.enabled = true;
                    t.allowLure = false;
                    return t;
                });
            }
            DATA.enabledEntities = null;
            save();
        }


        if (DATA.defaults == null) DATA.defaults = new Defaults();
        if (DATA.consumeDefaults == null) DATA.consumeDefaults = new ConsumptionDefaults();
        if (DATA.consume == null) DATA.consume = new LinkedHashMap<>();
        if (DATA.aggressionTargets == null) DATA.aggressionTargets = new LinkedHashMap<>();
        if (DATA.runawayFromEntities == null) DATA.runawayFromEntities = new LinkedHashMap<>();
        if (DATA.entities == null) DATA.entities = new LinkedHashMap<>();
        if (DATA.forAllowLuresinEntities == null) DATA.forAllowLuresinEntities = new LinkedHashMap<>();
    }
    public static Config get() {
        if (DATA == null) load();
        return DATA;
    }

    public static void save() {
        try {
            FILE.getParentFile().mkdirs();

            String json = GSON.toJson(DATA);

            json = insertHeader(json, "defaults", "DEFAULT EXAMPLE DO NOT MODIFY");
            json = insertHeader(json, "entities", "ENTITY TUNING");
            json = insertHeader(json, "aggressionTargets", "AGGRESSION LIST");
            json = insertHeader(json, "runawayFromEntities", "RUNAWAY LIST");
            json = insertHeader(json, "consumeDefaults", "EATING DEFAULTS");
            json = insertHeader(json, "consume", "EATING ENTITIES");
            json = insertHeader(json, "forAllowLuresinEntities", "LURE ITEMS");

            try (FileWriter w = new FileWriter(FILE)) {
                w.write(json);
            }
        } catch (IOException ignored) {}
    }

    private static String insertHeader(String json, String key, String title) {
        String header =
                "\n" +
                        "  /* |---------------------------------------------------------------------| */\n" +
                        "  /* |=======================[ " + String.format("%-18s", title) + "]=======================| */\n" +
                        "  /* |---------------------------------------------------------------------| */\n" +
                        "  \"" + key + "\":";
        return json.replace("\"" + key + "\":", header);
    }

    public static boolean isEnabled(Entity e) {
        Tuning t = pickByEntity(e);
        return t != null && t.enabled != null ? t.enabled : DATA.defaults.enabled;
    }

    public static boolean allowLure(Entity e) {
        Tuning t = pickByEntity(e);
        return t != null && t.allowLure != null ? t.allowLure : DATA.defaults.allowLure;
    }

    public static boolean leashingStopTheRunning(Entity e) {
        Tuning t = pickByEntity(e);
        return t != null && t.leashingStopTheRunning != null ? t.leashingStopTheRunning : DATA.defaults.leashingStopTheRunning;
    }

    public static double runSpeed(Entity e) {
        Tuning t = pickByEntity(e);
        return t != null && t.runSpeed != null ? t.runSpeed : DATA.defaults.runSpeed;
    }

    public static double runNearPlayerBlock(Entity e) {
        Tuning t = pickByEntity(e);
        return t != null && t.runNearPlayerBlock != null ? t.runNearPlayerBlock : DATA.defaults.runNearPlayerBlock;
    }

    public static double safeDistance(Entity e) {
        Tuning t = pickByEntity(e);
        return t != null && t.SafeDistance != null ? t.SafeDistance : DATA.defaults.SafeDistance;
    }

    public static boolean requireLineOfSightToRun(Entity e) {
        Tuning t = pickByEntity(e);
        return t != null && t.requireLineOfSightToRun != null ? t.requireLineOfSightToRun : DATA.defaults.requireLineOfSightToRun;
    }

    public static double nearPlayerSpeedMultiplier(Entity e) {
        Tuning t = pickByEntity(e);
        return t != null && t.nearPlayerSpeedMultiplier != null ? t.nearPlayerSpeedMultiplier : DATA.defaults.nearPlayerSpeedMultiplier;
    }

    public static int getLingerTicks(Entity e) {
        Tuning t = pickByEntity(e);
        int ticks = t != null && t.continueToRunOutsideOfSafeDistanceInTicks != null
                ? t.continueToRunOutsideOfSafeDistanceInTicks
                : DATA.defaults.continueToRunOutsideOfSafeDistanceInTicks;
        return Math.max(0, ticks);
    }

    public static double shiftingReduceDetectRangeByPercent(Entity e) {
        Tuning t = pickByEntity(e);
        return t != null && t.shiftingReduceDetectRangeByPercent != null
                ? t.shiftingReduceDetectRangeByPercent : DATA.defaults.shiftingReduceDetectRangeByPercent;
    }

    public static List<Item> standardLureItems(EntityType<?> type) {
        Identifier id = Registries.ENTITY_TYPE.getId(type);
        if (id == null) return List.of();
        String path = id.getPath();
        List<String> items = DATA.forAllowLuresinEntities.getOrDefault(path, List.of());
        return items.stream()
                .map(Identifier::tryParse)
                .filter(Objects::nonNull)
                .map(Registries.ITEM::get)
                .filter(i -> i != null && i != Items.AIR)
                .collect(Collectors.toList());
    }

    public static Set<String> aggressionTargetsFor(Entity e) {
        if (DATA == null || DATA.aggressionTargets == null) return Set.of();
        Identifier id = Registries.ENTITY_TYPE.getId(e.getType());
        if (id == null) return Set.of();
        List<String> list = DATA.aggressionTargets.getOrDefault(id.toString().toLowerCase(Locale.ROOT),
                DATA.aggressionTargets.get(id.getPath().toLowerCase(Locale.ROOT)));
        if (list == null) return Set.of();
        return list.stream().filter(Objects::nonNull).map(s -> s.trim().toLowerCase(Locale.ROOT)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<String> getRunawayTargets(Entity e) {
        if (DATA == null || DATA.runawayFromEntities == null) return Set.of();
        Identifier id = Registries.ENTITY_TYPE.getId(e.getType());
        if (id == null) return Set.of();
        List<String> list = DATA.runawayFromEntities.getOrDefault(id.toString().toLowerCase(Locale.ROOT),
                DATA.runawayFromEntities.get(id.getPath().toLowerCase(Locale.ROOT)));
        if (list == null) return Set.of();
        return list.stream().filter(Objects::nonNull).map(s -> s.trim().toLowerCase(Locale.ROOT)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static boolean consumeEnabled(Entity e) {
        ConsumptionTuning t = pickConsumeByEntity(e);
        return t != null && t.enabled != null ? t.enabled : DATA.consumeDefaults.enabled;
    }

    public static float consumeHealHearts(Entity e) {
        ConsumptionTuning t = pickConsumeByEntity(e);
        return t != null && t.healHearts != null ? t.healHearts : DATA.consumeDefaults.healHearts;
    }

    public static int consumeDelayTicks(Entity e) {
        ConsumptionTuning t = pickConsumeByEntity(e);
        int v = t != null && t.delayTicks != null ? t.delayTicks : DATA.consumeDefaults.delayTicks;
        return Math.max(1, v);
    }

    public static double consumeRadius(Entity e) {
        ConsumptionTuning t = pickConsumeByEntity(e);
        double v = t != null && t.radius != null ? t.radius : DATA.consumeDefaults.radius;
        return Math.max(0.5, v);
    }

    public static List<ConsumptionEffectEntry> getFullHealthBuffs(Entity e) {
        ConsumptionTuning t = pickConsumeByEntity(e);
        return DATA.consumeDefaults.getFullHealthBuffs;
    }

    public static boolean isConsumableItem(Entity e, Identifier id) {
        if (id == null) return false;
        ConsumptionTuning t = pickConsumeByEntity(e);
        if (t == null || t.items == null || t.items.isEmpty()) return false;
        String full = id.toString().toLowerCase(Locale.ROOT);
        String path = id.getPath().toLowerCase(Locale.ROOT);
        return t.items.contains(full) || t.items.contains(path);
    }

    private static Tuning pickByEntity(Entity e) {
        Identifier id = Registries.ENTITY_TYPE.getId(e.getType());
        if (id == null) return null;
        String full = id.toString().toLowerCase(Locale.ROOT);
        String path = id.getPath().toLowerCase(Locale.ROOT);
        return DATA == null || DATA.entities == null ? null : DATA.entities.getOrDefault(full, DATA.entities.get(path));
    }

    private static ConsumptionTuning pickConsumeByEntity(Entity e) {
        Identifier id = Registries.ENTITY_TYPE.getId(e.getType());
        if (id == null) return null;
        String full = id.toString().toLowerCase(Locale.ROOT);
        String path = id.getPath().toLowerCase(Locale.ROOT);
        return DATA == null || DATA.consume == null ? null : DATA.consume.getOrDefault(full, DATA.consume.get(path));
    }

    private static Config fresh() {
        Config c = new Config();

        double shiftReduce = c.defaults.shiftingReduceDetectRangeByPercent;
        put(c, "chicken", true, 2.8, 10.0, 16.0, 1.25, 120, true, shiftReduce, false, true);
        put(c, "horse",   true, 3.0, 10.0, 16.0, 1.25, 120, true, shiftReduce, false, true);
        put(c, "pig",     true, 2.8, 10.0, 16.0, 1.25, 120, true, shiftReduce, false, true);
        put(c, "sheep",   true, 2.8, 10.0, 16.0, 1.25, 120, true, shiftReduce, false, true);
        put(c, "trader_llama", true, 2.8, 10.0, 16.0, 1.25, 120, true, shiftReduce, false, true);

        c.aggressionTargets.put("wolf", List.of("sheep", "rabbit"));
        c.aggressionTargets.put("zombie", List.of("sheep", "pig"));
        c.aggressionTargets.put("spider", List.of("chicken", "rabbit", "pig"));
        c.aggressionTargets.put("drowned", List.of("cod", "salmon", "tropical_fish"));
        c.aggressionTargets.put("dolphin", List.of("cod", "salmon", "tropical_fish", "pufferfish"));

        c.runawayFromEntities.put("sheep", List.of("wolf"));
        c.runawayFromEntities.put("rabbit", List.of("wolf", "fox"));
        c.runawayFromEntities.put("chicken", List.of("spider", "fox"));


        ConsumptionEffectEntry strength = new ConsumptionEffectEntry();
        strength.effectId = "minecraft:strength";
        strength.durationSeconds = 40;
        strength.amplifier = 0;

        ConsumptionEffectEntry speed = new ConsumptionEffectEntry();
        speed.effectId = "minecraft:speed";
        speed.durationSeconds = 40;
        speed.amplifier = 0;

        ConsumptionEffectEntry regeneration = new ConsumptionEffectEntry();
        regeneration.effectId = "minecraft:regeneration";
        regeneration.durationSeconds = 40;
        regeneration.amplifier = 0;

        c.consumeDefaults.getFullHealthBuffs.add(strength);
        c.consumeDefaults.getFullHealthBuffs.add(speed);
        c.consumeDefaults.getFullHealthBuffs.add(regeneration);

        ConsumptionTuning zomEat = new ConsumptionTuning();
        zomEat.enabled = true;
        zomEat.items.add("chicken");
        zomEat.items.add("beef");
        zomEat.items.add("porkchop");
        zomEat.items.add("mutton");
        zomEat.items.add("predations:villager_meat");
        zomEat.items.add("predations:cooked_villager_meat");
        zomEat.items.add("cod");
        zomEat.items.add("salmon");
        zomEat.items.add("tropical_fish");
        zomEat.items.add("pufferfish");
        zomEat.items.add("cooked_chicken");
        zomEat.items.add("steak");
        zomEat.items.add("cooked_porkchop");
        zomEat.items.add("cooked_mutton");
        zomEat.items.add("cooked_cod");
        zomEat.items.add("cooked_salmon");

        ConsumptionTuning spiderEat = new ConsumptionTuning();
        spiderEat.enabled = true;
        spiderEat.items.add("chicken");
        spiderEat.items.add("porkchop");
        spiderEat.items.add("rabbit");
        spiderEat.items.add("mutton");
        spiderEat.items.add("predations:villager_meat");
        spiderEat.items.add("predations:cooked_villager_meat");
        spiderEat.items.add("cod");
        spiderEat.items.add("salmon");
        spiderEat.items.add("tropical_fish");
        spiderEat.items.add("pufferfish");
        spiderEat.items.add("cooked_chicken");
        spiderEat.items.add("steak");
        spiderEat.items.add("cooked_porkchop");
        spiderEat.items.add("cooked_mutton");
        spiderEat.items.add("cooked_rabbit");
        spiderEat.items.add("cooked_cod");
        spiderEat.items.add("cooked_salmon");

        ConsumptionTuning wolfEat = new ConsumptionTuning();
        wolfEat.enabled = true;
        wolfEat.items.add("chicken");
        wolfEat.items.add("porkchop");
        wolfEat.items.add("rabbit");
        wolfEat.items.add("mutton");
        wolfEat.items.add("rotten_flesh");
        wolfEat.items.add("predations:villager_meat");
        wolfEat.items.add("predations:cooked_villager_meat");
        wolfEat.items.add("cod");
        wolfEat.items.add("salmon");
        wolfEat.items.add("tropical_fish");
        wolfEat.items.add("pufferfish");
        wolfEat.items.add("cooked_chicken");
        wolfEat.items.add("steak");
        wolfEat.items.add("cooked_porkchop");
        wolfEat.items.add("cooked_mutton");
        wolfEat.items.add("cooked_rabbit");
        wolfEat.items.add("cooked_cod");
        wolfEat.items.add("cooked_salmon");

        ConsumptionTuning drownedEat = new ConsumptionTuning();
        drownedEat.enabled = true;
        drownedEat.items.add("cod");
        drownedEat.items.add("salmon");
        drownedEat.items.add("tropical_fish");
        drownedEat.items.add("pufferfish");
        drownedEat.items.add("cooked_cod");
        drownedEat.items.add("cooked_salmon");

        ConsumptionTuning dolphinEat = new ConsumptionTuning();
        dolphinEat.enabled = true;
        dolphinEat.items.add("cod");
        dolphinEat.items.add("salmon");
        dolphinEat.items.add("tropical_fish");
        dolphinEat.items.add("pufferfish");
        dolphinEat.items.add("cooked_cod");
        dolphinEat.items.add("cooked_salmon");

        c.consume.put("zombie", zomEat);
        c.consume.put("spider", spiderEat);
        c.consume.put("wolf", wolfEat);
        c.consume.put("drowned", drownedEat);
        c.consume.put("dolphin", dolphinEat);

        Map<String, List<String>> lureMap = c.forAllowLuresinEntities;
        lureMap.put("chicken", List.of("minecraft:wheat_seeds", "minecraft:melon_seeds", "minecraft:pumpkin_seeds",
                "minecraft:beetroot_seeds", "minecraft:torchflower_seeds", "minecraft:pitcher_pod"));
        List<String> wheat = List.of("minecraft:wheat");
        lureMap.put("cow", wheat);
        lureMap.put("goat", wheat);
        lureMap.put("mooshroom", wheat);
        lureMap.put("sheep", wheat);
        lureMap.put("camel", wheat);
        List<String> pigItems = List.of("minecraft:carrot", "minecraft:potato", "minecraft:beetroot");
        lureMap.put("pig", pigItems);
        lureMap.put("hoglin", pigItems);
        List<String> horseItems = List.of("minecraft:wheat", "minecraft:sugar", "minecraft:hay_block",
                "minecraft:apple", "minecraft:golden_carrot", "minecraft:golden_apple",
                "minecraft:enchanted_golden_apple");
        lureMap.put("horse", horseItems);
        lureMap.put("donkey", horseItems);
        lureMap.put("mule", horseItems);
        lureMap.put("rabbit", List.of("minecraft:carrot", "minecraft:golden_carrot", "minecraft:dandelion"));
        lureMap.put("llama", List.of("minecraft:hay_block", "minecraft:wheat"));
        lureMap.put("trader_llama", List.of("minecraft:hay_block", "minecraft:wheat"));
        lureMap.put("cat", List.of("minecraft:cod", "minecraft:salmon"));
        lureMap.put("ocelot", List.of("minecraft:cod", "minecraft:salmon"));
        lureMap.put("wolf", List.of("minecraft:beef", "minecraft:porkchop", "minecraft:mutton",
                "minecraft:chicken", "minecraft:rabbit", "minecraft:rotten_flesh"));

        return c;
    }

    private static void put(Config c, String key, boolean enabled, double runSpeed, double runNearPlayerBlock, double safeDistance,
                            double nearPlayerSpeedMultiplier, int continueTicks, boolean allowLure, double shiftingReduce, boolean requireLineOfSight,
                            boolean leashingStopTheRunning) {
        Tuning t = new Tuning();
        t.enabled = enabled;
        t.runSpeed = runSpeed;
        t.runNearPlayerBlock = runNearPlayerBlock;
        t.SafeDistance = safeDistance;
        t.nearPlayerSpeedMultiplier = nearPlayerSpeedMultiplier;
        t.continueToRunOutsideOfSafeDistanceInTicks = continueTicks;
        t.allowLure = allowLure;
        t.shiftingReduceDetectRangeByPercent = shiftingReduce;
        t.requireLineOfSightToRun = requireLineOfSight;
        t.leashingStopTheRunning = leashingStopTheRunning;
        c.entities.put(key, t);
    }
}