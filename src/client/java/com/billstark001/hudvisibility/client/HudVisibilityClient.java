package com.billstark001.hudvisibility.client;

import com.billstark001.hudvisibility.HudVisibility;
import com.billstark001.hudvisibility.VisibilityConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HudVisibilityClient {
    private static volatile VisibilityConfig config = new VisibilityConfig();
    private static Path configFile;
    private HudVisibilityClient() {}

    public static void initialize(Path configDirectory) {
        configFile = configDirectory.resolve("hud_visibility.properties");
        try {
            config = VisibilityConfig.load(configFile);
            if (!Files.exists(configFile)) config.save(configFile);
        } catch (IOException e) {
            HudVisibility.LOGGER.log(System.Logger.Level.ERROR, "Cannot load HUD Visibility configuration; using defaults. The existing file is preserved.", e);
        }
        HudVisibility.LOGGER.log(System.Logger.Level.INFO, "HUD Visibility client ready");
    }

    public static VisibilityConfig config() { return config; }
    public static void save(VisibilityConfig edited) {
        try {
            edited.save(configFile);
            config = edited.copy();
        } catch (IOException e) {
            HudVisibility.LOGGER.log(System.Logger.Level.ERROR, "Cannot save HUD Visibility configuration", e);
            throw new IllegalStateException("Could not save HUD Visibility configuration", e);
        }
    }
}
