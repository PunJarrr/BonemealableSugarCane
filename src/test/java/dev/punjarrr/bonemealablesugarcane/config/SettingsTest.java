package dev.punjarrr.bonemealablesugarcane.config;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsTest {

    private YamlConfiguration defaults;
    private final List<String> warnings = new ArrayList<>();
    private Logger logger;

    @BeforeEach
    void setUp() throws IOException, InvalidConfigurationException {
        try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
            defaults = new YamlConfiguration();
            defaults.loadFromString(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override public void publish(LogRecord record) { warnings.add(record.getMessage()); }
            @Override public void flush() {}
            @Override public void close() {}
        });
    }

    private Settings load(String yaml) throws InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(yaml);
        config.setDefaults(defaults);
        return Settings.from(config, logger);
    }

    @Test
    void bundledDefaults() throws InvalidConfigurationException {
        Settings settings = load("");
        assertEquals(3, settings.maxHeight());
        assertFalse(settings.growByOneBlock());
        assertTrue(settings.effects());
        assertTrue(settings.dispenserSupport());
        assertTrue(warnings.isEmpty(), warnings::toString);
    }

    @Test
    void readsUserValues() throws InvalidConfigurationException {
        Settings settings = load("settings:\n  max-height: 6\n  grow-by-one-block: true\n  effects: false\n");
        assertEquals(6, settings.maxHeight());
        assertTrue(settings.growByOneBlock());
        assertFalse(settings.effects());
    }

    @Test
    void maxHeightBelowOneFallsBackWithWarning() throws InvalidConfigurationException {
        assertEquals(3, load("settings:\n  max-height: 0\n").maxHeight());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("max-height") && w.contains("0")));
    }

    @Test
    void wrongTypesFallBackWithWarning() throws InvalidConfigurationException {
        Settings settings = load("settings:\n  max-height: tall\n  effects: maybe\n");
        assertEquals(3, settings.maxHeight());
        assertTrue(settings.effects());
        assertEquals(2, warnings.size(), warnings::toString);
    }

    @Test
    void messagesAreColoredAndFilled() throws InvalidConfigurationException {
        Settings settings = load("messages:\n  unknown-subcommand: \"&cNo {subcommand} for /{label}\"\n"
                + "  reload-success: \"\"\n");
        List<String> sent = new ArrayList<>();
        CommandSender sender = (CommandSender) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{CommandSender.class}, (proxy, method, args) -> {
                    if (method.getName().equals("sendMessage") && args[0] instanceof String s) sent.add(s);
                    return null;
                });

        settings.messages().send(sender, Messages.UNKNOWN_SUBCOMMAND, "subcommand", "foo", "label", "bsc");
        settings.messages().send(sender, Messages.RELOAD_SUCCESS);
        settings.messages().send(sender, Messages.NO_PERMISSION);

        assertEquals(List.of("§cNo foo for /bsc", "§cYou don't have permission to do that."), sent);
    }
}
