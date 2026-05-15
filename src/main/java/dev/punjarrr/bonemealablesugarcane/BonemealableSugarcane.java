package dev.punjarrr.bonemealablesugarcane;

import dev.punjarrr.bonemealablesugarcane.command.BonemealableCommand;
import dev.punjarrr.bonemealablesugarcane.lang.LanguageManager;
import dev.punjarrr.bonemealablesugarcane.listener.SugarcaneListener;
import dev.punjarrr.bonemealablesugarcane.util.Logger;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class BonemealableSugarcane extends JavaPlugin {

    private static BonemealableSugarcane instance;
    private SugarcaneListener sugarcaneListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
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
        reloadConfig();
        LanguageManager.load(this);
        registerListeners();
    }

    private void registerListeners() {
        sugarcaneListener = new SugarcaneListener(this);
        getServer().getPluginManager().registerEvents(sugarcaneListener, this);
    }

    public static BonemealableSugarcane getInstance() {
        return instance;
    }
}
