package dev.punjarrr.bonemealablesugarcane.command;

import dev.punjarrr.bonemealablesugarcane.BonemealableSugarCane;
import dev.punjarrr.bonemealablesugarcane.lang.LanguageManager;
import dev.punjarrr.bonemealablesugarcane.util.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BonemealableCommand implements CommandExecutor, TabCompleter {

    private final BonemealableSugarCane plugin;

    public BonemealableCommand(BonemealableSugarCane plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (args.length == 0) {
            LanguageManager.send(sender, "command.help", "label", label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "version":
                handleVersion(sender);
                break;
            case "info":
                handleInfo(sender);
                break;
            default:
                LanguageManager.send(sender, "command.unknown-subcommand",
                        "subcommand", args[0], "label", label);
                LanguageManager.send(sender, "command.help", "label", label);
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("bonemealablesugarcane.reload")) {
            LanguageManager.send(sender, "command.no-permission");
            return;
        }
        try {
            plugin.reload();
            LanguageManager.send(sender, "command.reload.success");
            Logger.success("Config reloaded by " + sender.getName() + ".");
        } catch (Exception e) {
            LanguageManager.send(sender, "command.reload.failed");
            Logger.error("Reload failed: " + e.getMessage());
        }
    }

    private void handleVersion(CommandSender sender) {
        String version = LanguageManager.getRaw("meta.version");
        LanguageManager.send(sender, "command.version", "version", version);
    }

    private void handleInfo(CommandSender sender) {
        String version = LanguageManager.getRaw("meta.version");
        String name = LanguageManager.getRaw("plugin.name");
        String author = LanguageManager.getRaw("plugin.author");

        LanguageManager.send(sender, "command.info.line");
        LanguageManager.send(sender, "command.info.title", "name", name, "version", version);
        LanguageManager.send(sender, "command.info.author", "author", author);
        LanguageManager.send(sender, "command.info.line");
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("reload", "version", "info"));
            List<String> result = new ArrayList<>();
            for (String s : completions) {
                if (s.startsWith(args[0].toLowerCase())) result.add(s);
            }
            return result;
        }
        return Collections.emptyList();
    }
}
