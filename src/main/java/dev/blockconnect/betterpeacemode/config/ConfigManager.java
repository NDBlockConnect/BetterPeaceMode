package dev.blockconnect.betterpeacemode.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.blockconnect.betterpeacemode.platform.PlatformPaths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and persists the mod configuration from {@code config/betterpeacemode.json}.
 *
 * <p>Failures never propagate into the game: an unreadable or malformed file falls back to
 * defaults and is rewritten only when the caller explicitly saves.
 */
public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static BetterPeaceModeConfig config = new BetterPeaceModeConfig();
    private static Path configPath;

    private ConfigManager() {
    }

    public static BetterPeaceModeConfig get() {
        return config;
    }

    public static void load() {
        configPath = PlatformPaths.configDir().resolve("betterpeacemode.json");
        if (!Files.exists(configPath)) {
            config = new BetterPeaceModeConfig();
            config.normalize();
            save();
            return;
        }
        try {
            String json = Files.readString(configPath, StandardCharsets.UTF_8);
            BetterPeaceModeConfig loaded = GSON.fromJson(json, BetterPeaceModeConfig.class);
            config = loaded != null ? loaded : new BetterPeaceModeConfig();
        } catch (IOException | RuntimeException ex) {
            config = new BetterPeaceModeConfig();
        }
        config.normalize();
    }

    public static void save() {
        if (configPath == null) {
            configPath = PlatformPaths.configDir().resolve("betterpeacemode.json");
        }
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(config), StandardCharsets.UTF_8);
//G  i  tHu b @NDBl  o  ckCon  nect | Bl  o ckConnect@Star  sai l  s C lover
        } catch (IOException ex) {
            // Non-fatal: the in-memory config stays authoritative for this session.
        }
    }
}
