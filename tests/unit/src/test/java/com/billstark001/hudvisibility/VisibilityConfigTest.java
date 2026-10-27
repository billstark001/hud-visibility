package com.billstark001.hudvisibility;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class VisibilityConfigTest {
    @TempDir Path directory;

    @Test void defaultsHideOnlyWithF1() {
        VisibilityConfig config = new VisibilityConfig();
        for (Feature feature : Feature.values()) {
            assertTrue(config.isVisible(feature, false), feature.key());
            assertFalse(config.isVisible(feature, true), feature.key());
        }
    }

    @Test void featuresAndHudStatesAreIndependent() {
        for (Feature selected : Feature.values()) {
            for (boolean withHud : new boolean[] {false, true}) {
                for (boolean withoutHud : new boolean[] {false, true}) {
                    VisibilityConfig config = new VisibilityConfig();
                    config.set(selected, new VisibilityRule(withHud, withoutHud));
                    assertEquals(withHud, config.isVisible(selected, false));
                    assertEquals(withoutHud, config.isVisible(selected, true));
                    for (Feature other : Feature.values()) {
                        if (other != selected) assertEquals(VisibilityRule.DEFAULT, config.rule(other));
                    }
                }
            }
        }
    }

    @Test void cancellingAnEditDoesNotChangeTheActiveConfiguration() {
        VisibilityConfig active = new VisibilityConfig();
        VisibilityConfig edited = active.copy();
        edited.set(Feature.HANDS, new VisibilityRule(false, true));
        assertEquals(VisibilityRule.DEFAULT, active.rule(Feature.HANDS));
    }

    @Test void allRulesSurviveSavingAndReplacingAConfiguration() throws IOException {
        Path file = directory.resolve("nested/hud_visibility.properties");
        VisibilityConfig config = new VisibilityConfig();
        for (Feature feature : Feature.values()) {
            config.set(feature, new VisibilityRule(feature.ordinal() % 2 == 0, feature.ordinal() % 3 == 0));
        }
        config.save(file);
        VisibilityConfig loaded = VisibilityConfig.load(file);
        for (Feature feature : Feature.values()) assertEquals(config.rule(feature), loaded.rule(feature));
        loaded.set(Feature.HANDS, new VisibilityRule(false, true));
        loaded.save(file);
        assertEquals(new VisibilityRule(false, true), VisibilityConfig.load(file).rule(Feature.HANDS));
        try (var files = Files.list(file.getParent())) {
            assertEquals(1, files.count(), "Temporary files must be cleaned up");
        }
    }

    @Test void partialConfigurationKeepsDefaultsForMissingFeatures() throws IOException {
        Path file = directory.resolve("config.properties");
        Files.writeString(file, "# 中文\nhands.without_hud = TRUE\nunknown = ignored\n");
        VisibilityConfig config = VisibilityConfig.load(file);
        assertTrue(config.rule(Feature.HANDS).withoutHud());
        assertTrue(config.rule(Feature.HANDS).withHud());
        assertEquals(VisibilityRule.DEFAULT, config.rule(Feature.HITBOXES));
    }

    @Test void invalidConfigurationIsReportedAndPreserved() throws IOException {
        Path file = directory.resolve("config.properties");
        String original = "hands.without_hud=typo\n";
        Files.writeString(file, original);
        assertThrows(IOException.class, () -> VisibilityConfig.load(file));
        assertEquals(original, Files.readString(file));
    }

    @Test void missingConfigurationDoesNotCreateFilesDuringRead() throws IOException {
        Path file = directory.resolve("missing.properties");
        assertEquals(VisibilityRule.DEFAULT, VisibilityConfig.load(file).rule(Feature.HANDS));
        assertFalse(Files.exists(file));
    }
}
