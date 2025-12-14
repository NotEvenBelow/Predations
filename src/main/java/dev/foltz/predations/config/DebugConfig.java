package dev.foltz.predations.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class DebugConfig {
    private static final File FILE = new File("config/predations-debug.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static DebugConfig INSTANCE;

    public boolean orbitEnabled = false;

    public static DebugConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        try {
            if (FILE.exists()) {
                INSTANCE = GSON.fromJson(new FileReader(FILE), DebugConfig.class);
                if (INSTANCE == null) INSTANCE = new DebugConfig();
            } else {
                INSTANCE = new DebugConfig();
            }
            save();
        } catch (Exception e) {
            e.printStackTrace();
            INSTANCE = new DebugConfig();
        }
    }

    public static void save() {
        try (FileWriter w = new FileWriter(FILE)) {
            GSON.toJson(INSTANCE, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}