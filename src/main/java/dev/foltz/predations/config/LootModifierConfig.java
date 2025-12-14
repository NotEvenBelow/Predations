package dev.foltz.predations.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class LootModifierConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("predations_loot_modifier.json");

    public Map<String, Rule> entities = new LinkedHashMap<>();

    public static final class Rule {
        public boolean enabled = true;
        public List<Drop> drops = new ArrayList<>();
    }

    public static final class Drop {
        public String item;
        public String itemDroppedOnFire;
        public float chance = 1.0f;
        public float min = 1.0f;
        public float max = 1.0f;
    }

    public static LootModifierConfig load() {
        LootModifierConfig cfg = null;
        try {
            if (Files.exists(PATH)) {
                try (Reader r = Files.newBufferedReader(PATH)) {
                    cfg = GSON.fromJson(r, LootModifierConfig.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LootModifierConfig defaults = defaults();

        if (cfg == null) {
            cfg = defaults;
        } else {
            if (cfg.entities == null) {
                cfg.entities = new LinkedHashMap<>();
            }

            for (Map.Entry<String, Rule> entry : defaults.entities.entrySet()) {
                cfg.entities.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        try (Writer w = Files.newBufferedWriter(PATH)) {
            GSON.toJson(cfg, w);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cfg;
    }

    private static LootModifierConfig defaults() {
        LootModifierConfig c = new LootModifierConfig();

        // ---- Cod ----
        Rule cod = new Rule();
        cod.enabled = true;
        Drop codRaw = new Drop();
        codRaw.item = "minecraft:cod";
        codRaw.itemDroppedOnFire = "minecraft:cooked_cod";
        codRaw.chance = 0.25f;
        codRaw.min = 1;
        codRaw.max = 1;
        Drop codBone = new Drop();
        codBone.item = "minecraft:bone_meal";
        codBone.chance = 0.35f;
        codBone.min = 1;
        codBone.max = 1;
        cod.drops.add(codRaw);
        cod.drops.add(codBone);
        c.entities.put("minecraft:cod", cod);

        // ---- Salmon ----
        Rule salmon = new Rule();
        salmon.enabled = true;
        Drop salmonRaw = new Drop();
        salmonRaw.item = "minecraft:salmon";
        salmonRaw.itemDroppedOnFire = "minecraft:cooked_salmon";
        salmonRaw.chance = 0.25f;
        salmonRaw.min = 1;
        salmonRaw.max = 1;
        Drop salmonBone = new Drop();
        salmonBone.item = "minecraft:bone_meal";
        salmonBone.chance = 0.35f;
        salmonBone.min = 1;
        salmonBone.max = 1;
        salmon.drops.add(salmonRaw);
        salmon.drops.add(salmonBone);
        c.entities.put("minecraft:salmon", salmon);

        // ---- Pufferfish ----
        Rule puffer = new Rule();
        puffer.enabled = true;
        Drop puffFish = new Drop();
        puffFish.item = "minecraft:pufferfish";
        puffFish.chance = 1.0f;
        puffFish.min = 1;
        puffFish.max = 1;
        Drop puffBone = new Drop();
        puffBone.item = "minecraft:bone_meal";
        puffBone.chance = 1.0f;
        puffBone.min = 1;
        puffBone.max = 1;
        Drop puffGrass = new Drop();
        puffGrass.item = "minecraft:grass_block";
        puffGrass.chance = 0.0001f;
        puffGrass.min = 1;
        puffGrass.max = 1;
        puffer.drops.add(puffFish);
        puffer.drops.add(puffBone);
        puffer.drops.add(puffGrass);
        c.entities.put("minecraft:pufferfish", puffer);

        // ---- Tropical Fish ----
        Rule tropical = new Rule();
        tropical.enabled = true;
        Drop tropFish = new Drop();
        tropFish.item = "minecraft:tropical_fish";
        tropFish.chance = 0.80f;
        tropFish.min = 1;
        tropFish.max = 1;
        Drop tropBone = new Drop();
        tropBone.item = "minecraft:bone_meal";
        tropBone.chance = 0.50f;
        tropBone.min = 1;
        tropBone.max = 1;
        tropical.drops.add(tropFish);
        tropical.drops.add(tropBone);
        c.entities.put("minecraft:tropical_fish", tropical);

        // ---- Squid ----
        Rule squid = new Rule();
        squid.enabled = true;
        Drop ink = new Drop();
        ink.item = "minecraft:ink_sac";
        ink.chance = 1.0f;
        ink.min = 1;
        ink.max = 3;
        Drop slime1 = new Drop();
        slime1.item = "minecraft:slime_ball";
        slime1.chance = 0.25f;
        slime1.min = 1;
        slime1.max = 1;
        Drop slime2 = new Drop();
        slime2.item = "minecraft:slime_ball";
        slime2.chance = 0.10f;
        slime2.min = 2;
        slime2.max = 2;
        Drop squidCod = new Drop();
        squidCod.item = "minecraft:cod";
        squidCod.chance = 0.5f;
        squidCod.min = 1;
        squidCod.max = 1;
        Drop squidSalmon = new Drop();
        squidSalmon.item = "minecraft:salmon";
        squidSalmon.chance = 0.5f;
        squidSalmon.min = 1;
        squidSalmon.max = 1;
        squid.drops.add(ink);
        squid.drops.add(slime1);
        squid.drops.add(slime2);
        squid.drops.add(squidCod);
        squid.drops.add(squidSalmon);
        c.entities.put("minecraft:squid", squid);

        // ---- Glow Squid ----
        Rule glowSquid = new Rule();
        glowSquid.enabled = true;
        Drop glowInk = new Drop();
        glowInk.item = "minecraft:glow_ink_sac";
        glowInk.chance = 1.0f;
        glowInk.min = 1;
        glowInk.max = 3;
        Drop glowSlime = new Drop();
        glowSlime.item = "minecraft:slime_ball";
        glowSlime.chance = 1.0f;
        glowSlime.min = 1;
        glowSlime.max = 3;
        Drop glowCod = new Drop();
        glowCod.item = "minecraft:cod";
        glowCod.chance = 0.5f;
        glowCod.min = 1;
        glowCod.max = 1;
        Drop glowSalmon = new Drop();
        glowSalmon.item = "minecraft:salmon";
        glowSalmon.chance = 0.5f;
        glowSalmon.min = 1;
        glowSalmon.max = 1;
        Drop glowDiamond = new Drop();
        glowDiamond.item = "minecraft:diamond";
        glowDiamond.chance = 0.0001f;
        glowDiamond.min = 1;
        glowDiamond.max = 1;
        glowSquid.drops.add(glowInk);
        glowSquid.drops.add(glowSlime);
        glowSquid.drops.add(glowCod);
        glowSquid.drops.add(glowSalmon);
        glowSquid.drops.add(glowDiamond);
        c.entities.put("minecraft:glow_squid", glowSquid);

        // ---- Villager  ----
        Rule villager = new Rule();
        villager.enabled = true;
        Drop villagerMeat = new Drop();
        villagerMeat.item = "predations:villager_meat";
        villagerMeat.itemDroppedOnFire = "predations:cooked_villager_meat";
        villagerMeat.chance = 1.0f;
        villagerMeat.min = 1.0f;
        villagerMeat.max = 2.0f;
        villager.drops.add(villagerMeat);
        c.entities.put("minecraft:villager", villager);

        return c;
    }

    private LootModifierConfig() {}
}