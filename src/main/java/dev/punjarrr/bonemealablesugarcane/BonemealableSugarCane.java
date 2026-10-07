package dev.punjarrr.bonemealablesugarcane;

import dev.punjarrr.bonemealablesugarcane.command.BonemealableCommand;
import dev.punjarrr.bonemealablesugarcane.config.ConfigUpdater;
import dev.punjarrr.bonemealablesugarcane.lang.LanguageManager;
import dev.punjarrr.bonemealablesugarcane.listener.DispenserListener;
import dev.punjarrr.bonemealablesugarcane.listener.SugarCaneListener;
import dev.punjarrr.bonemealablesugarcane.util.Logger;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class BonemealableSugarCane extends JavaPlugin {

    private static BonemealableSugarCane instance;
    private SugarCaneListener sugarcaneListener;
    private DispenserListener dispenserListener;

    @Override
    public void onEnable() {
        instance = this;
        updateConfigFile();
        reloadConfig();
        LanguageManager.load(this);
        registerListeners();

        BonemealableCommand executor = new BonemealableCommand(this);
        Objects.requireNonNull(getCommand("bonemealablesugarcane")).setExecutor(executor);
        Objects.requireNonNull(getCommand("bonemealablesugarcane")).setTabCompleter(executor);

        Logger.success(LanguageManager.get("plugin.enabled"));
    }

    @Override
    public void onDisable() {
        Logger.info(LanguageManager.get("plugin.disabled"));
    }

    /** Called by {@code /bsc reload} — unregisters listeners, reloads config + lang, then re-registers. */
    public void reload() {
        if (sugarcaneListener != null) {
            HandlerList.unregisterAll(sugarcaneListener);
        }
        if (dispenserListener != null) {
            HandlerList.unregisterAll(dispenserListener);
        }
        reloadConfig();
        LanguageManager.load(this);
        registerListeners();
    }

    /** Creates or updates config.yml on disk. Never overwrites a file that fails to parse. */
    private void updateConfigFile() {
        try (InputStream in = Objects.requireNonNull(getResource("config.yml"), "bundled config.yml")) {
            String template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            new ConfigUpdater(getDataFolder().toPath().resolve("config.yml"), template, getLogger()).update();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read the bundled config.yml", e);
        }
    }

    private void registerListeners() {
        sugarcaneListener = new SugarCaneListener(this);
        getServer().getPluginManager().registerEvents(sugarcaneListener, this);
        dispenserListener = new DispenserListener(this);
        getServer().getPluginManager().registerEvents(dispenserListener, this);
    }

    public static BonemealableSugarCane getInstance() {
        return instance;
    }
}
