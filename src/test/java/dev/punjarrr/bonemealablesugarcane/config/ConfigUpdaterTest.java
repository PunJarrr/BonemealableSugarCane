package dev.punjarrr.bonemealablesugarcane.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigUpdaterTest {

    @TempDir
    Path dir;

    private Path configFile;
    private String template;
    private final List<LogRecord> logs = new ArrayList<>();
    private Logger logger;

    @BeforeEach
    void setUp() throws IOException {
        configFile = dir.resolve("config.yml");
        try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
            template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override public void publish(LogRecord record) { logs.add(record); }
            @Override public void flush() {}
            @Override public void close() {}
        });
    }

    private ConfigUpdater.Result update() {
        return new ConfigUpdater(configFile, template, logger).update();
    }

    private YamlConfiguration loadResult() {
        return YamlConfiguration.loadConfiguration(configFile.toFile());
    }

    private long backupCount() throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.getFileName().toString().startsWith("config-backup-")).count();
        }
    }

    private String currentVersion() {
        return YamlConfiguration.loadConfiguration(new java.io.StringReader(template)).getString("config.version");
    }

    @Test
    void createsFileWhenMissing() throws IOException {
        assertEquals(ConfigUpdater.Result.CREATED, update());
        assertEquals(template, Files.readString(configFile));
    }

    @Test
    void doesNotRewriteCurrentConfig() throws IOException {
        String custom = template.replace("max-height: 3", "max-height: 7") + "\n# my own comment\n";
        Files.writeString(configFile, custom);

        assertEquals(ConfigUpdater.Result.UP_TO_DATE, update());
        assertEquals(custom, Files.readString(configFile));
        assertEquals(0, backupCount());
    }

    @Test
    void neverTouchesBrokenFile() throws IOException {
        String broken = "settings:\n  max-height: 5\n    grow-by-one-block: [oops\n";
        Files.writeString(configFile, broken);

        assertEquals(ConfigUpdater.Result.INVALID, update());
        assertEquals(broken, Files.readString(configFile));
        assertEquals(0, backupCount());
        assertTrue(logs.stream().anyMatch(r -> r.getLevel() == Level.SEVERE));
    }

    @Test
    void leavesNewerConfigAlone() throws IOException {
        String newer = "settings:\n  max-height: 4\nconfig:\n  version: 99.0.0\n";
        Files.writeString(configFile, newer);

        assertEquals(ConfigUpdater.Result.NEWER_VERSION, update());
        assertEquals(newer, Files.readString(configFile));
    }

    @Test
    void oldConfigKeepsValuesAndGetsBackedUp() throws IOException {
        // A 1.0.0 config: no max-height, no dispenser-support, no config section.
        Files.writeString(configFile, """
                settings:
                  grow-by-one-block: true
                """);

        assertEquals(ConfigUpdater.Result.UPDATED, update());
        YamlConfiguration result = loadResult();
        assertTrue(result.getBoolean("settings.grow-by-one-block"));
        assertEquals(3, result.getInt("settings.max-height"));
        assertTrue(result.getBoolean("settings.dispenser-support"));
        assertEquals(currentVersion(), result.getString("config.version"));
        assertEquals(1, backupCount());
        assertFalse(Files.exists(dir.resolve("config.yml.tmp")));

        // Second run: nothing left to do.
        String afterFirst = Files.readString(configFile);
        assertEquals(ConfigUpdater.Result.UP_TO_DATE, update());
        assertEquals(afterFirst, Files.readString(configFile));
    }

    @Test
    void missingKeyIsAddedEvenWithoutVersionChange() throws IOException {
        Files.writeString(configFile, "settings:\n  max-height: 6\nconfig:\n  version: " + currentVersion() + "\n");

        assertEquals(ConfigUpdater.Result.UPDATED, update());
        YamlConfiguration result = loadResult();
        assertEquals(6, result.getInt("settings.max-height"));
        assertTrue(result.contains("settings.dispenser-support"));
    }

    @Test
    void migratesFrom102() throws IOException {
        Files.writeString(configFile, """
                settings:
                  language: "de_DE"
                  max-height: 5
                  grow-by-one-block: true
                  spawn-particles: false
                  play-sound: true
                  dispenser-support: false
                config:
                  version: 1.0.2
                """);
        Path translations = Files.createDirectory(dir.resolve("translations"));
        Path german = Files.writeString(translations.resolve("de_DE.properties"), "x=y");

        assertEquals(ConfigUpdater.Result.UPDATED, update());
        YamlConfiguration result = loadResult();
        assertEquals(5, result.getInt("settings.max-height"));
        assertTrue(result.getBoolean("settings.grow-by-one-block"));
        assertTrue(result.getBoolean("settings.effects"));
        assertFalse(result.getBoolean("settings.dispenser-support"));
        assertFalse(result.contains("settings.language"));
        assertFalse(result.contains("settings.spawn-particles"));
        assertFalse(result.contains("settings.play-sound"));
        assertTrue(result.getString("messages.no-permission").startsWith("&c"));
        assertEquals("1.1.0", result.getString("config.version"));

        assertTrue(Files.exists(german), "translation files must not be deleted");
        assertTrue(logs.stream().anyMatch(r -> r.getMessage().contains("translations folder")));
        assertTrue(logs.stream().anyMatch(r -> r.getMessage().contains("de_DE")));
    }

    @Test
    void effectsOffOnlyWhenParticlesAndSoundWereBothOff() throws IOException {
        Files.writeString(configFile, """
                settings:
                  spawn-particles: false
                  play-sound: false
                config:
                  version: 1.0.1
                """);
        update();
        assertFalse(loadResult().getBoolean("settings.effects"));
    }

    @Test
    void effectsOnWhenOldKeysWereMissing() throws IOException {
        Files.writeString(configFile, "settings:\n  grow-by-one-block: false\n");
        update();
        assertTrue(loadResult().getBoolean("settings.effects"));
        assertTrue(logs.stream().noneMatch(r -> r.getMessage().contains("translations folder")));
    }

    @Test
    void versionComparison() {
        assertTrue(ConfigUpdater.isOlderThan("1.0.2", "1.1.0"));
        assertTrue(ConfigUpdater.isOlderThan("1.0", "1.0.1"));
        assertFalse(ConfigUpdater.isOlderThan("1.1.0", "1.1.0"));
        assertFalse(ConfigUpdater.isOlderThan("1.10.0", "1.9.0"));
    }
}
