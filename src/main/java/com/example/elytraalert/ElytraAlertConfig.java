package com.example.elytraalert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class ElytraAlertConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("elytraalert.json");
    public static ElytraAlertConfig INSTANCE = new ElytraAlertConfig();

    public boolean enabled = true;
    public int sound = 0;
    public int durationSeconds = 5;
    public boolean continuous = false;
    public boolean colorPlayers = true;
    public boolean colorUnder500Only = false;
    public boolean includeSelf = false;
    public boolean onlyWhenGliding = false;
    public int volume = 100; // percent, 0..1000 (NEW)

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                ElytraAlertConfig c = GSON.fromJson(Files.readString(FILE), ElytraAlertConfig.class);
                if (c != null) INSTANCE = c;
            }
        } catch (Exception e) {
            System.err.println("[ElytraAlert] Could not read config: " + e);
        }
        INSTANCE.sound = Math.max(0, Math.min(AlertSounds.NAMES.length - 1, INSTANCE.sound));
        INSTANCE.durationSeconds = Math.max(1, Math.min(60, INSTANCE.durationSeconds));
        INSTANCE.volume = Math.max(0, Math.min(1000, INSTANCE.volume));
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(INSTANCE));
        } catch (Exception e) {
            System.err.println("[ElytraAlert] Could not save config: " + e);
        }
    }
}
