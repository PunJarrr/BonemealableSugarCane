package dev.punjarrr.bonemealablesugarcane.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigUpdater {

    private static final String VERSION_PATH = "config.version";

    // Matches a top-level section header, e.g. "settings:"
    private static final Pattern SECTION_PATTERN = Pattern.compile("^([a-zA-Z][\\w-]*):\\s*$");
    // Matches an indented key-value line, e.g. "  language: en_US"
    private static final Pattern KV_PATTERN = Pattern.compile("^([ \\t]+)([\\w-]+):([ \\t].*|)$");

    private ConfigUpdater() {}

    /**
     * Rewrites config.yml on disk to the bundled default, then splices the user's
     * existing values back in. Comments, blank lines, and ASCII art are preserved
     * because we write raw text rather than going through Bukkit's YAML serialiser.
     *
     * Migration steps only need to handle value transformations (renaming a key,
     * changing a value's meaning, etc.) — adding new keys is automatic because the
     * fresh default already contains them, and missing user values stay at their
     * new default.
     */
    public static void migrate(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        String oldVersion = config.getString(VERSION_PATH, "1.0.0");

        // Snapshot what the user has explicitly set (version key excluded — the
        // fresh default always carries the correct new version).
        Map<String, Object> userValues = snapshotUserValues(config);

        // --- Value-transformation migrations ---
        // Mutate userValues in-place for any key renames / type changes.
        // Adding a new key requires no code here — just add it to config.yml.

        if (isOlderThan(oldVersion, "1.0.1")) {
            // No value transforms for 1.0.1 — dispenser-support is a new key,
            // so it simply keeps the default from the fresh config.
            Logger.info("Config migrated to 1.0.1.");
        }

        // Template for future migrations:
        // if (isOlderThan(oldVersion, "1.0.2")) {
        //     Object val = userValues.remove("settings.old-key");
        //     if (val != null) userValues.put("settings.new-key", val);
        //     Logger.info("Config migrated to 1.0.2.");
        // }

        // Overwrite config.yml with the fresh default, user values spliced in.
        writeRefreshedConfig(plugin, userValues);
        plugin.reloadConfig();
    }

    // -------------------------------------------------------------------------

    /** Builds a map of every key the user has explicitly written to their config. */
    private static Map<String, Object> snapshotUserValues(FileConfiguration config) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (String key : config.getKeys(true)) {
            if (config.isConfigurationSection(key)) continue;
            if (key.equals(VERSION_PATH)) continue;
            if (config.isSet(key)) {
                values.put(key, config.get(key));
            }
        }
        return values;
    }

    /**
     * Reads the bundled config.yml, substitutes any keys present in {@code userValues},
     * then writes the result straight to disk — preserving every comment and blank line.
     */
    private static void writeRefreshedConfig(JavaPlugin plugin, Map<String, Object> userValues) {
        InputStream in = plugin.getResource("config.yml");
        if (in == null) return;

        try {
            String rawText = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            String updated = applyValuesToText(rawText, userValues);
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            Files.writeString(configFile.toPath(), updated);
        } catch (IOException e) {
            Logger.info("Failed to refresh config.yml: " + e.getMessage());
        }
    }

    /**
     * Line-by-line substitution: for each key in {@code values} whose dotted path
     * matches a line in the YAML text, replaces only the value portion.
     * All other lines (comments, blank lines, section headers) are left untouched.
     *
     * Note: only handles one level of nesting (top-level section → key).
     * Sufficient for this plugin's flat config structure.
     */
    private static String applyValuesToText(String text, Map<String, Object> values) {
        StringBuilder result = new StringBuilder();
        String[] lines = text.split("\n", -1);
        String currentSection = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            Matcher sectionMatch = SECTION_PATTERN.matcher(line);
            if (sectionMatch.matches()) {
                currentSection = sectionMatch.group(1);
            } else if (currentSection != null) {
                Matcher kvMatch = KV_PATTERN.matcher(line);
                if (kvMatch.matches()) {
                    String indent = kvMatch.group(1);
                    String key   = kvMatch.group(2);
                    String fullKey = currentSection + "." + key;
                    if (values.containsKey(fullKey)) {
                        line = indent + key + ": " + toYamlValue(values.get(fullKey));
                    }
                }
            }

            result.append(line);
            if (i < lines.length - 1) result.append("\n");
        }
        return result.toString();
    }

    /** Serialises a config value to an inline YAML scalar. */
    private static String toYamlValue(Object val) {
        if (val instanceof Boolean || val instanceof Number) return val.toString();
        String s = val.toString();
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    // -------------------------------------------------------------------------

    private static boolean isOlderThan(String version, String target) {
        int[] v = parse(version);
        int[] t = parse(target);
        for (int i = 0; i < 3; i++) {
            if (v[i] != t[i]) return v[i] < t[i];
        }
        return false;
    }

    private static int[] parse(String version) {
        String[] parts = version.trim().split("\\.");
        int[] result = new int[3];
        for (int i = 0; i < 3; i++) {
            try {
                result[i] = i < parts.length ? Integer.parseInt(parts[i]) : 0;
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }
}
