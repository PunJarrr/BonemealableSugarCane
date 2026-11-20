package dev.punjarrr.bonemealablesugarcane.config;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/** The player-facing messages from the {@code messages} section, with & color codes applied. */
public final class Messages {

    public static final String NO_PERMISSION = "no-permission";
    public static final String UNKNOWN_SUBCOMMAND = "unknown-subcommand";
    public static final String RELOAD_SUCCESS = "reload-success";
    public static final String RELOAD_FAILED = "reload-failed";

    private final Map<String, String> messages;

    private Messages(Map<String, String> messages) {
        this.messages = messages;
    }

    /**
     * Reads every message the defaults define. A missing message falls back to the default;
     * a non-text value logs a warning and falls back too.
     */
    static Messages from(Configuration config, Logger logger) {
        Map<String, String> messages = new HashMap<>();
        Configuration defaults = config.getDefaults();
        if (defaults != null && defaults.isConfigurationSection("messages")) {
            for (String key : defaults.getConfigurationSection("messages").getKeys(false)) {
                String path = "messages." + key;
                Object value = config.get(path);
                if (!(value instanceof String)) {
                    logger.warning("config.yml: '" + path + "' should be text in quotes, but is '" + value
                            + "'. Using the default.");
                    value = defaults.getString(path);
                }
                messages.put(key, ChatColor.translateAlternateColorCodes('&', (String) value));
            }
        }
        return new Messages(messages);
    }

    /**
     * Sends a message, replacing {@code {name}} placeholders given as name/value pairs.
     * Empty messages aren't sent. A {@code \n} in the text starts a new chat line.
     */
    public void send(CommandSender sender, String key, String... placeholders) {
        String text = messages.getOrDefault(key, "");
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            text = text.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        if (text.isEmpty()) return;
        for (String line : text.split("\n")) {
            sender.sendMessage(line);
        }
    }
}
