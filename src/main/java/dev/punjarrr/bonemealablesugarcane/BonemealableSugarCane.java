package dev.punjarrr.bonemealablesugarcane;

import dev.punjarrr.bonemealablesugarcane.command.BonemealableCommand;
import dev.punjarrr.bonemealablesugarcane.config.ConfigUpdater;
import dev.punjarrr.bonemealablesugarcane.config.Settings;
import dev.punjarrr.bonemealablesugarcane.growth.CaneGrower;
import dev.punjarrr.bonemealablesugarcane.listener.DispenserListener;
import dev.punjarrr.bonemealablesugarcane.listener.SugarCaneListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;

public final class BonemealableSugarCane extends JavaPlugin {

    private String bundledConfig;
    private volatile Settings settings;

    @Override
    public void onEnable() {
        bundledConfig = readBundledConfig();
        Settings loaded = loadSettings();
        if (loaded == null) {
            YamlConfiguration empty = new YamlConfiguration();
            empty.setDefaults(defaultsConfig());
            loaded = Settings.from(empty, getLogger());
        }
        settings = loaded;

        PluginManager pluginManager = getServer().getPluginManager();
        CaneGrower grower = new CaneGrower(this::settings, pluginManager);
        pluginManager.registerEvents(new SugarCaneListener(grower), this);
        pluginManager.registerEvents(new DispenserListener(this::settings, grower), this);

        BonemealableCommand executor = new BonemealableCommand(this);
        PluginCommand command = Objects.requireNonNull(getCommand("bonemealablesugarcane"));
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    public Settings settings() {
        return settings;
    }

    /**
     * Re-reads config.yml for {@code /bsc reload}. Returns false, and keeps the current
     * settings, if the file can't be read.
     */
    public boolean reload() {
        Settings loaded = loadSettings();
        if (loaded == null) return false;
        settings = loaded;
        return true;
    }

    /** Updates config.yml on disk if needed, then reads it. Returns null if it can't be parsed. */
    private Settings loadSettings() {
        Path configFile = getDataFolder().toPath().resolve("config.yml");
        String fallback = settings == null
                ? "Using the default settings until it's fixed and /bsc reload is run."
                : "Keeping the previous settings.";
        if (new ConfigUpdater(configFile, bundledConfig, getLogger()).update() == ConfigUpdater.Result.INVALID) {
            getLogger().severe(fallback);
            return null;
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(Files.readString(configFile, StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, "Couldn't read config.yml. " + fallback, e);
            return null;
        }
        config.setDefaults(defaultsConfig());
        return Settings.from(config, getLogger());
    }

    private YamlConfiguration defaultsConfig() {
        try {
            YamlConfiguration defaults = new YamlConfiguration();
            defaults.loadFromString(bundledConfig);
            return defaults;
        } catch (InvalidConfigurationException e) {
            throw new IllegalStateException("The bundled config.yml is invalid", e);
        }
    }

    private String readBundledConfig() {
        try (InputStream in = Objects.requireNonNull(getResource("config.yml"), "bundled config.yml")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read the bundled config.yml", e);
        }
    }
}
