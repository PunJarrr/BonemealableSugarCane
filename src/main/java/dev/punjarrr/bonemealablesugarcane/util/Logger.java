package dev.punjarrr.bonemealablesugarcane.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {

    private static final String PREFIX =
            ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "" + ChatColor.BOLD +
            "BonemealableSugarcane" + ChatColor.DARK_GRAY + "]" + ChatColor.RESET + " ";

    public static void info(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.WHITE + message);
    }

    public static void warning(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.YELLOW + message);
    }

    public static void error(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + message);
    }

    public static void success(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.GREEN + message);
    }
}
