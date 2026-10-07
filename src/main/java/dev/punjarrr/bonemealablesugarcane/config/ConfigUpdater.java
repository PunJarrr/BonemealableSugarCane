package dev.punjarrr.bonemealablesugarcane.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Brings the user's config.yml up to date with the bundled one without losing their settings.
 *
 * <p>The file is only rewritten when the config version changed or a key is missing. The
 * rewrite starts from the bundled text (so comments are kept) and fills in the user's values.
 * A file that fails to parse is never touched, a backup is made before every rewrite, and the
 * new file is checked before it replaces the old one.
 *
 * <p>Adding a new key needs no code here. Add an {@code isOlderThan} block in
 * {@link #migrate} only when an existing value has to be renamed or transformed.
 */
public final class ConfigUpdater {

    public enum Result {
        /** No config.yml existed, so the bundled one was written. */
        CREATED,
        /** Nothing to do. The file was not written. */
        UP_TO_DATE,
        /** The file was rewritten (a backup was made first). */
        UPDATED,
        /** The file has a YAML syntax error. It was not touched. */
        INVALID,
        /** The file is from a newer plugin version. It was not touched. */
        NEWER_VERSION,
        /** Reading or writing failed. The original file is still in place. */
        FAILED
    }

    static final String VERSION_PATH = "config.version";
    private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Path configFile;
    private final String template;
    private final Logger logger;

    /**
     * @param configFile the user's config.yml
     * @param template   the text of the bundled config.yml
     */
    public ConfigUpdater(Path configFile, String template, Logger logger) {
        this.configFile = configFile;
        this.template = template;
        this.logger = logger;
    }

    public Result update() {
        try {
            if (Files.notExists(configFile)) {
                Files.createDirectories(configFile.getParent());
                Files.writeString(configFile, template, StandardCharsets.UTF_8);
                return Result.CREATED;
            }

            YamlConfiguration user = new YamlConfiguration();
            try {
                user.loadFromString(Files.readString(configFile, StandardCharsets.UTF_8));
            } catch (InvalidConfigurationException e) {
                logger.severe("config.yml has a syntax error, so it was NOT updated and the defaults are used. "
                        + "Fix the error, then run /bsc reload. Details: " + e.getMessage());
                return Result.INVALID;
            }

            YamlConfiguration defaults = parse(template);
            String current = defaults.getString(VERSION_PATH, "0.0.0");
            String old = user.getString(VERSION_PATH, "1.0.0");

            if (isOlderThan(current, old)) {
                logger.warning("config.yml is from a newer version (" + old + ") than this plugin (" + current
                        + "). It was left unchanged. Settings this version doesn't know are ignored.");
                return Result.NEWER_VERSION;
            }

            Map<String, Object> values = leafValues(user);
            Map<String, Object> templateValues = leafValues(defaults);
            boolean versionChanged = isOlderThan(old, current);
            boolean keysMissing = !values.keySet().containsAll(templateValues.keySet());
            if (!versionChanged && !keysMissing) {
                return Result.UP_TO_DATE;
            }

            migrate(old, values);

            for (String key : values.keySet()) {
                if (!templateValues.containsKey(key)) {
                    logger.info("config.yml: '" + key + "' is no longer used and was removed (it's still in the backup).");
                }
            }

            String rendered = YamlTemplate.render(template, values);
            parse(rendered); // never write a file we couldn't read back

            Path backup = backup(old);
            writeAtomically(rendered);
            logger.info((versionChanged
                    ? "Updated config.yml from " + old + " to " + current
                    : "Added missing settings to config.yml")
                    + ". Your old file was saved as " + backup.getFileName() + ".");
            return Result.UPDATED;
        } catch (IOException | InvalidConfigurationException e) {
            logger.log(Level.SEVERE, "Couldn't update config.yml. The file was left as it was.", e);
            return Result.FAILED;
        }
    }

    /**
     * Value transformations between versions. {@code values} holds every leaf value the user
     * has set, keyed by dotted path (without {@code config.version}).
     */
    private void migrate(String oldVersion, Map<String, Object> values) {
        // No blocks yet.
    }

    // -------------------------------------------------------------------------

    private Path backup(String oldVersion) throws IOException {
        String base = "config-backup-" + oldVersion + "-" + LocalDateTime.now().format(BACKUP_TIME);
        Path backup = configFile.resolveSibling(base + ".yml");
        for (int i = 1; Files.exists(backup); i++) {
            backup = configFile.resolveSibling(base + "-" + i + ".yml");
        }
        Files.copy(configFile, backup);
        return backup;
    }

    private void writeAtomically(String text) throws IOException {
        Path tmp = configFile.resolveSibling(configFile.getFileName() + ".tmp");
        Files.writeString(tmp, text, StandardCharsets.UTF_8);
        try {
            Files.move(tmp, configFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, configFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static YamlConfiguration parse(String text) throws InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(text);
        return config;
    }

    /** Every non-section value, keyed by dotted path, except {@code config.version}. */
    private static Map<String, Object> leafValues(ConfigurationSection config) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (String key : config.getKeys(true)) {
            if (key.equals(VERSION_PATH) || config.isConfigurationSection(key)) continue;
            values.put(key, config.get(key));
        }
        return values;
    }

    static boolean isOlderThan(String version, String target) {
        int[] v = parseVersion(version);
        int[] t = parseVersion(target);
        for (int i = 0; i < 3; i++) {
            if (v[i] != t[i]) return v[i] < t[i];
        }
        return false;
    }

    private static int[] parseVersion(String version) {
        List<String> parts = List.of(version.trim().split("\\."));
        int[] result = new int[3];
        for (int i = 0; i < 3 && i < parts.size(); i++) {
            try {
                result[i] = Integer.parseInt(parts.get(i));
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }
}
