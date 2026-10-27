package io.github.billstark001.hudvisibility;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.Properties;

/** UTF-8 persistence independent of either loader; Cloth Config edits a staged copy. */
public final class VisibilityConfig {
    private final EnumMap<Feature, VisibilityRule> rules = new EnumMap<>(Feature.class);

    public VisibilityConfig() {
        for (Feature feature : Feature.values()) rules.put(feature, VisibilityRule.DEFAULT);
    }

    public VisibilityRule rule(Feature feature) { return rules.get(feature); }
    public void set(Feature feature, VisibilityRule rule) { rules.put(feature, java.util.Objects.requireNonNull(rule)); }
    public boolean isVisible(Feature feature, boolean hudHidden) { return rule(feature).isVisible(hudHidden); }
    public VisibilityConfig copy() {
        VisibilityConfig copy = new VisibilityConfig();
        copy.rules.putAll(rules);
        return copy;
    }

    public static VisibilityConfig load(Path file) throws IOException {
        VisibilityConfig config = new VisibilityConfig();
        if (!Files.exists(file)) return config;
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid configuration: " + file, e);
        }
        for (Feature feature : Feature.values()) {
            config.set(feature, new VisibilityRule(
                readBoolean(properties, feature.key() + ".with_hud", true),
                readBoolean(properties, feature.key() + ".without_hud", false)));
        }
        return config;
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) throws IOException {
        String value = properties.getProperty(key);
        if (value == null) return fallback;
        if (value.trim().equalsIgnoreCase("true")) return true;
        if (value.trim().equalsIgnoreCase("false")) return false;
        throw new IOException("Expected true or false for " + key);
    }

    public void save(Path file) throws IOException {
        Path absolute = file.toAbsolutePath();
        Files.createDirectories(absolute.getParent());
        Properties properties = new Properties();
        properties.setProperty("schema_version", "1");
        for (Feature feature : Feature.values()) {
            properties.setProperty(feature.key() + ".with_hud", Boolean.toString(rule(feature).withHud()));
            properties.setProperty(feature.key() + ".without_hud", Boolean.toString(rule(feature).withoutHud()));
        }
        Path temporary = Files.createTempFile(absolute.getParent(), "hud_visibility-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                properties.store(writer, "HUD Visibility: true = allow eligible content; false = hide. F3 toggles are preserved.");
            }
            try {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
