package dev.foltz.predations.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Blocks;
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


public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/predations.json");

    private static Config DATA;

    public static final class Tuning {
        public Float farSpeed;
        public Float nearSpeed;
        public Integer distance;
        public Float ratio;
        public Boolean enabled;
        public Boolean allowLure;
    }

    public static final class Defaults {
        public float farSpeed = 1.2f;
        public float nearSpeed = 2.4f;
        public int distance = 16;
        public float ratio = 0.0f;
        public boolean allowLure = false;
    }

    // ---------- dropped-item consumption ----------
    public static final class ConsumptionDefaults {
        public boolean enabled = false;
        public float healHearts = 1.5f;   // 3 health
        public int    delayTicks = 50;    // cooldown
        public double radius = 2.0;       // search radius
    }
    public static final class ConsumptionTuning {
        public Boolean enabled;           // null -> inherit defaults
        public Float   healHearts;        // null -> inherit defaults
        public Integer delayTicks;        // null -> inherit defaults
        public Double  radius;            // null -> inherit defaults
        /** ids or paths, e.g. "minecraft:wheat", "wheat" */
        public Set<String> items = new LinkedHashSet<>();
    }

    // ---------- root ----------
    public static final class Config {
        public Defaults defaults = new Defaults();
        public Map<String, Tuning> entities = new LinkedHashMap<>();
        /** legacy convenience list (now folded into entities on load) */
        public Set<String> enabledEntities;

        /** attacker → prey list (lower-cased ids or paths) */
        public Map<String, List<String>> aggressionTargets = new LinkedHashMap<>();

        /** NEW: dropped-item consumption */
        public ConsumptionDefaults consumeDefaults = new ConsumptionDefaults();
        public Map<String, ConsumptionTuning> consume = new LinkedHashMap<>();
    }

    private ConfigManager() {}

    // ---------- IO ----------
    public static void load() {
        if (FILE.exists()) {
            try (FileReader r = new FileReader(FILE)) {
                DATA = GSON.fromJson(r, Config.class);
                if (DATA == null) DATA = fresh();
            } catch (IOException e) {
                DATA = fresh();
                save();
            }
        } else {
            DATA = fresh();
            save();
        }

        // migrate legacy enabledEntities -> entities
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

        // normalize blocks
        if (DATA.consumeDefaults == null) DATA.consumeDefaults = new ConsumptionDefaults();
        if (DATA.consume == null) DATA.consume = new LinkedHashMap<>();
        if (DATA.aggressionTargets == null) DATA.aggressionTargets = new LinkedHashMap<>();
        if (DATA.entities == null) DATA.entities = new LinkedHashMap<>();
    }

    public static void save() {
        try {
            FILE.getParentFile().mkdirs();
            try (FileWriter w = new FileWriter(FILE)) {
                GSON.toJson(DATA, w);
            }
        } catch (IOException ignored) {}
    }

    // ---------- queries ----------
    public static boolean isEnabled(Entity entity) {
        Identifier id = Registries.ENTITY_TYPE.getId(entity.getType());
        if (id == null) return false;

        Tuning t = pick(id.toString().toLowerCase(Locale.ROOT), id.getPath().toLowerCase(Locale.ROOT));
        if (t == null) return false;
        if (t.enabled != null) return t.enabled;

        return false;
    }

    public static boolean allowLure(Entity entity) {
        Identifier id = Registries.ENTITY_TYPE.getId(entity.getType());
        if (id == null) return DATA.defaults.allowLure;
        Tuning t = pick(id.toString().toLowerCase(Locale.ROOT), id.getPath().toLowerCase(Locale.ROOT));
        if (t == null || t.allowLure == null) return DATA.defaults.allowLure;
        return t.allowLure;
    }

    public static float farSpeed(Entity e)  { Tuning t = pickByEntity(e); return t != null && t.farSpeed  != null ? t.farSpeed  : DATA.defaults.farSpeed; }
    public static float nearSpeed(Entity e) { Tuning t = pickByEntity(e); return t != null && t.nearSpeed != null ? t.nearSpeed : DATA.defaults.nearSpeed; }
    public static int   distance(Entity e)  { Tuning t = pickByEntity(e); return t != null && t.distance  != null ? t.distance  : DATA.defaults.distance; }
    public static float ratio(Entity e)     { Tuning t = pickByEntity(e); return t != null && t.ratio     != null ? t.ratio     : DATA.defaults.ratio; }

    public static List<Item> standardLureItems(EntityType<?> type) {
        Identifier id = Registries.ENTITY_TYPE.getId(type);
        if (id == null) return List.of();
        String path = id.getPath();
        return switch (path) {
            case "chicken" -> List.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD);
            case "cow", "goat", "mooshroom", "sheep", "camel" -> List.of(Items.WHEAT);
            case "pig", "hoglin" -> List.of(Items.CARROT, Items.POTATO, Items.BEETROOT);
            case "horse", "donkey", "mule" -> List.of(Items.WHEAT, Items.SUGAR, Blocks.HAY_BLOCK.asItem(), Items.APPLE, Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
            case "rabbit" -> List.of(Items.CARROT, Items.GOLDEN_CARROT, Items.DANDELION);
            case "llama", "trader_llama" -> List.of(Blocks.HAY_BLOCK.asItem(), Items.WHEAT);
            case "cat", "ocelot" -> List.of(Items.COD, Items.SALMON);
            case "wolf" -> List.of(Items.BEEF, Items.PORKCHOP, Items.MUTTON, Items.CHICKEN, Items.RABBIT, Items.ROTTEN_FLESH);
            default -> List.of();
        };
    }

    public static Set<String> listedEntityKeysLower() {
        Set<String> out = new LinkedHashSet<>();
        for (var k : DATA.entities.keySet()) {
            String lower = k.toLowerCase(Locale.ROOT);
            out.add(lower);
            int i = lower.indexOf(':');
            if (i >= 0 && i + 1 < lower.length()) out.add(lower.substring(i + 1));
        }
        return out;
    }

    public static Set<String> aggressionTargetsFor(Entity attacker) {
        if (DATA == null || DATA.aggressionTargets == null) return Set.of();
        Identifier id = Registries.ENTITY_TYPE.getId(attacker.getType());
        if (id == null) return Set.of();
        String full = id.toString().toLowerCase(Locale.ROOT);
        String path = id.getPath().toLowerCase(Locale.ROOT);

        List<String> list = DATA.aggressionTargets.get(full);
        if (list == null) list = DATA.aggressionTargets.get(path);
        if (list == null || list.isEmpty()) return Set.of();

        LinkedHashSet<String> out = new LinkedHashSet<>();
        // FIX: Ensure values are trimmed before lowercasing for robustness against JSON spacing issues
        for (String s : list) {
            if (s != null) {
                out.add(s.trim().toLowerCase(Locale.ROOT));
            }
        }
        return out;
    }

    // ---------- consumption helpers ----------
    public static boolean consumeEnabled(Entity e) {
        ConsumptionTuning t = pickConsumeByEntity(e);
        if (t != null && t.enabled != null) return t.enabled;
        return DATA.consumeDefaults.enabled;
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
    public static boolean isConsumableItem(Entity e, Identifier itemId) {
        if (itemId == null) return false;
        ConsumptionTuning t = pickConsumeByEntity(e);
        if (t == null || t.items == null || t.items.isEmpty()) return false;
        String full = itemId.toString().toLowerCase(Locale.ROOT);
        String path = itemId.getPath().toLowerCase(Locale.ROOT);
        return t.items.contains(full) || t.items.contains(path);
    }

    // ---------- internals ----------
    private static Tuning pickByEntity(Entity e) {
        Identifier id = Registries.ENTITY_TYPE.getId(e.getType());
        if (id == null) return null;
        return pick(id.toString().toLowerCase(Locale.ROOT), id.getPath().toLowerCase(Locale.ROOT));
    }
    private static Tuning pick(String full, String path) {
        Tuning t = DATA.entities.get(full);
        if (t != null) return t;
        return DATA.entities.get(path);
    }
    private static ConsumptionTuning pickConsumeByEntity(Entity e) {
        Identifier id = Registries.ENTITY_TYPE.getId(e.getType());
        if (id == null) return null;
        String full = id.toString().toLowerCase(Locale.ROOT);
        String path = id.getPath().toLowerCase(Locale.ROOT);
        ConsumptionTuning c = DATA.consume.get(full);
        if (c == null) c = DATA.consume.get(path);
        return c;
    }

    // ---------- defaults ----------
    private static Config fresh() {
        Config c = new Config();

        c.defaults.farSpeed = 1.2f;
        c.defaults.nearSpeed = 2.4f;
        c.defaults.distance = 16;
        c.defaults.ratio = 0.0f;
        c.defaults.allowLure = false;

        put(c, "chicken", 1.8f, 2.8f, 16, 0.0f, true, false);
        put(c, "horse",   1.8f, 3.0f, 18, 0.0f, true, false);
        put(c, "pig",     1.4f, 2.8f, 16, 0.0f, true, false);
        put(c, "sheep",   1.6f, 2.8f, 16, 0.0f, true, false);
        put(c, "trader_llama", 1.6f, 2.8f, 16, 0.0f, true, false);

        // example aggression mapping
        c.aggressionTargets.put("wolf", List.of("sheep", "rabbit"));

        // --- ONLY example requested: ZOMBIE eats RAW BEEF ---
        // results in JSON:
        //  "consume": { "zombie": { "enabled": true, "items": ["beef"] } }
        ConsumptionTuning zomEat = new ConsumptionTuning();
        zomEat.enabled = true;
        zomEat.items.add("beef"); // raw beef (minecraft:beef)
        c.consume.put("zombie", zomEat);

        return c;
    }

    private static void put(Config c, String key, float far, float near, int dist, float ratio, boolean enabled, boolean allowLure) {
        Tuning t = new Tuning();
        t.farSpeed = far; t.nearSpeed = near; t.distance = dist; t.ratio = ratio;
        t.enabled = enabled; t.allowLure = allowLure;
        c.entities.put(key, t);
    }
}
