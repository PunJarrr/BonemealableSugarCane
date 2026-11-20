package dev.punjarrr.bonemealablesugarcane.command;

import dev.punjarrr.bonemealablesugarcane.BonemealableSugarCane;
import dev.punjarrr.bonemealablesugarcane.config.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BonemealableCommand implements CommandExecutor, TabCompleter {

    private static final String RELOAD_PERMISSION = "bonemealablesugarcane.reload";

    private final BonemealableSugarCane plugin;

    public BonemealableCommand(BonemealableSugarCane plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help" -> sendHelp(sender, label);
            case "reload" -> handleReload(sender);
            case "version" -> sender.sendMessage(color("&6&l" + description().getName()
                    + " &7v&f" + description().getVersion()));
            case "info" -> sendInfo(sender);
            default -> plugin.settings().messages().send(sender, Messages.UNKNOWN_SUBCOMMAND,
                    "subcommand", args[0], "label", label);
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(color("&6&l" + description().getName() + " &7v&f" + description().getVersion()));
        sender.sendMessage(color("&7  /&f" + label + " help    &8- &7Show this list"));
        if (sender.hasPermission(RELOAD_PERMISSION)) {
            sender.sendMessage(color("&7  /&f" + label + " reload  &8- &7Reload config.yml"));
        }
        sender.sendMessage(color("&7  /&f" + label + " version &8- &7Show the plugin version"));
        sender.sendMessage(color("&7  /&f" + label + " info    &8- &7Show plugin information"));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            plugin.settings().messages().send(sender, Messages.NO_PERMISSION);
            return;
        }
        if (plugin.reload()) {
            plugin.settings().messages().send(sender, Messages.RELOAD_SUCCESS);
            plugin.getLogger().info("Config reloaded by " + sender.getName() + ".");
        } else {
            plugin.settings().messages().send(sender, Messages.RELOAD_FAILED);
        }
    }

    private void sendInfo(CommandSender sender) {
        PluginDescriptionFile description = description();
        String line = color("&8-----------------------------------------------------");
        sender.sendMessage(line);
        sender.sendMessage(color("&6&l" + description.getName() + " &7v&f" + description.getVersion()));
        sender.sendMessage(color("&7Author: &f" + String.join(", ", description.getAuthors())));
        if (description.getWebsite() != null) {
            sender.sendMessage(color("&7Website: &f" + description.getWebsite()));
        }
        sender.sendMessage(line);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length != 1) return result;
        String prefix = args[0].toLowerCase(Locale.ROOT);
        for (String sub : List.of("help", "reload", "version", "info")) {
            if (sub.equals("reload") && !sender.hasPermission(RELOAD_PERMISSION)) continue;
            if (sub.startsWith(prefix)) result.add(sub);
        }
        return result;
    }

    private PluginDescriptionFile description() {
        return plugin.getDescription();
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
