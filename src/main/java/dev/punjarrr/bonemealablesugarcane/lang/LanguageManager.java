package dev.punjarrr.bonemealablesugarcane.lang;

import dev.punjarrr.bonemealablesugarcane.util.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class LanguageManager {

    private static final String TRANSLATIONS_DIR = "translations";
    private static final String FILE_EXT = ".properties";
    private static final Language FALLBACK = Language.EN_US;

    private static final Map<Language, Properties> cache = new EnumMap<>(Language.class);

    private static JavaPlugin plugin;
    private static Language activeLanguage = FALLBACK;

    private LanguageManager() {}

    public static void load(JavaPlugin instance) {
        plugin = instance;
        cache.clear();

        for (Language lang : Language.values()) {
            saveDefault(lang);
            loadLocale(lang);
        }

        String configured = plugin.getConfig().getString("settings.language", FALLBACK.getCode());
        Language resolved = Language.fromCode(configured);

        if (!resolved.getCode().equalsIgnoreCase(configured)) {
            Logger.warning("Unknown language '" + configured + "' in config.yml. Falling back to " + FALLBACK.getCode() + ".");
        }

        activeLanguage = resolved;
        Logger.info("Language loaded: " + activeLanguage.getDisplayName() + " (" + activeLanguage.getCode() + ").");
    }

    /** Returns the raw string for {@code key} with placeholders replaced, no color applied. */
    public static String getRaw(String key, String... replacements) {
        return replace(getRaw(key), replacements);
    }

    /** Returns the raw (uncolored) string for {@code key}. Falls back to en_US, then an error token. */
    public static String getRaw(String key) {
        String value = getValue(activeLanguage, key);
        if (value != null) return value;

        if (activeLanguage != FALLBACK) {
            value = getValue(FALLBACK, key);
            if (value != null) return value;
        }

        return "&c[missing key: " + key + "]";
    }

    /** Returns the colored string for {@code key} with placeholders replaced. */
    public static String get(String key, String... replacements) {
        return colorize(replace(getRaw(key), replacements));
    }

    /** Returns the colored string for {@code key}. */
    public static String get(String key) {
        return colorize(getRaw(key));
    }

    /**
     * Sends {@code key}'s message to {@code sender}. Splits on newlines so multi-line
     * translation values render as separate chat lines.
     */
    public static void send(CommandSender sender, String key, String... replacements) {
        String msg = get(key, replacements);
        for (String line : msg.split("\n")) {
            sender.sendMessage(line);
        }
    }

    public static Language getActiveLanguage() {
        return activeLanguage;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static String getValue(Language lang, String key) {
        Properties props = cache.get(lang);
        if (props == null) return null;
        return props.getProperty(key);
    }

    private static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static String replace(String template, String[] replacements) {
        if (replacements == null || replacements.length == 0) return template;
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "LanguageManager.get() requires an even number of replacement arguments (key, value pairs).");
        }
        String result = template;
        for (int i = 0; i < replacements.length; i += 2) {
            result = result.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        return result;
    }

    private static void saveDefault(Language lang) {
        String resourcePath = TRANSLATIONS_DIR + "/" + lang.getCode() + FILE_EXT;
        File target = new File(plugin.getDataFolder(), resourcePath);

        if (target.exists()) return;
        target.getParentFile().mkdirs();

        InputStream in = plugin.getResource(resourcePath);
        if (in == null) return;

        try {
            OutputStream out = new FileOutputStream(target);
            try {
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
                in.close();
            }
        } catch (IOException e) {
            Logger.warning("Could not save default translation file: " + resourcePath);
        }
    }

    private static void loadLocale(Language lang) {
        String resourcePath = TRANSLATIONS_DIR + "/" + lang.getCode() + FILE_EXT;
        File file = new File(plugin.getDataFolder(), resourcePath);

        if (!file.exists()) return;

        Properties props = new Properties();
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            try {
                props.load(reader);
                cache.put(lang, props);
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            Logger.warning("Failed to load translation file: " + resourcePath);
        }
    }
}
