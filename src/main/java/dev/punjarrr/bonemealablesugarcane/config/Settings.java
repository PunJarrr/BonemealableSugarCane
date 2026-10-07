package dev.punjarrr.bonemealablesugarcane.config;

import org.bukkit.configuration.Configuration;

import java.util.logging.Logger;

/**
 * A validated, immutable snapshot of config.yml. Listeners read the current snapshot on
 * every event; {@code /bsc reload} swaps in a new one.
 */
public record Settings(int maxHeight, boolean growByOneBlock, boolean effects, boolean dispenserSupport,
                       Messages messages) {

    /**
     * Reads the settings from {@code config}, whose defaults must be the bundled config.yml.
     * Invalid values log a warning naming the key and fall back to the default.
     */
    public static Settings from(Configuration config, Logger logger) {
        int maxHeight = readInt(config, "settings.max-height", logger);
        if (maxHeight < 1) {
            int fallback = config.getDefaults().getInt("settings.max-height");
            logger.warning("config.yml: 'settings.max-height' must be 1 or higher, but is " + maxHeight
                    + ". Using " + fallback + ".");
            maxHeight = fallback;
        }
        return new Settings(
                maxHeight,
                readBoolean(config, "settings.grow-by-one-block", logger),
                readBoolean(config, "settings.effects", logger),
                readBoolean(config, "settings.dispenser-support", logger),
                Messages.from(config, logger));
    }

    private static int readInt(Configuration config, String path, Logger logger) {
        Object value = config.get(path);
        if (value instanceof Integer i) return i;
        int fallback = config.getDefaults().getInt(path);
        logger.warning("config.yml: '" + path + "' should be a whole number, but is '" + value
                + "'. Using " + fallback + ".");
        return fallback;
    }

    private static boolean readBoolean(Configuration config, String path, Logger logger) {
        Object value = config.get(path);
        if (value instanceof Boolean b) return b;
        boolean fallback = config.getDefaults().getBoolean(path);
        logger.warning("config.yml: '" + path + "' should be true or false, but is '" + value
                + "'. Using " + fallback + ".");
        return fallback;
    }
}
