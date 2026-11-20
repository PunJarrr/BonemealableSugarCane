package dev.punjarrr.bonemealablesugarcane.smoketest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

/**
 * Test-only helper for scripts/smoke-test.sh. Never shipped: it lives in its own jar in build/smoke-plugin/.
 *
 * Builds and reads the test site through the Bukkit API instead of vanilla commands, because Folia's
 * console can't run block commands like /execute if block or /fill. Every action runs on the thread
 * that owns the test site (Folia's region scheduler, or the main thread elsewhere) and logs its result
 * as "BSCTEST <id> <result>", which the script reads from the server log.
 *
 *   bsctest <id> site         stone floor, sand next to water, a dispenser facing the cane spot
 *   bsctest <id> cane         a single sugar cane block
 *   bsctest <id> fill <n>     n bone meal in the dispenser (0 empties it)
 *   bsctest <id> power on|off a redstone block next to the dispenser, or air
 *   bsctest <id> state        height=<sugar cane height> bonemeal=<bone meal in the dispenser>
 */
public final class SmokeTestPlugin extends JavaPlugin {

    private static final int X = 5, Y = 101, Z = 5;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) return false;
        String id = args[0];
        World world = Bukkit.getWorlds().get(0);
        Location site = new Location(world, X, Y, Z);
        runAt(site, () -> {
            String result;
            try {
                result = run(world, args);
            } catch (RuntimeException e) {
                result = "error " + e;
            }
            getLogger().info("BSCTEST " + id + " " + result);
        });
        return true;
    }

    private String run(World world, String[] args) {
        switch (args[1]) {
            case "site" -> {
                // The script force-loads chunk 0,0 with /forceload: Folia only allows that on its global thread.
                fill(world, 2, 99, 2, 9, 100, 9, Material.STONE);
                fill(world, 2, 101, 2, 9, 110, 9, Material.AIR);
                world.getBlockAt(X, Y - 1, Z).setType(Material.SAND);
                world.getBlockAt(X, Y - 1, Z + 1).setType(Material.WATER);
                world.getBlockAt(X + 1, Y, Z).setBlockData(Bukkit.createBlockData("minecraft:dispenser[facing=west]"));
                return "ok";
            }
            case "cane" -> {
                fill(world, X, Y + 1, Z, X, 110, Z, Material.AIR);
                world.getBlockAt(X, Y, Z).setType(Material.SUGAR_CANE);
                return "ok";
            }
            case "fill" -> {
                Inventory inventory = dispenser(world);
                inventory.clear();
                int amount = Integer.parseInt(args[2]);
                if (amount > 0) inventory.setItem(0, new ItemStack(Material.BONE_MEAL, amount));
                return "ok";
            }
            case "power" -> {
                world.getBlockAt(X + 2, Y, Z).setType(args[2].equals("on") ? Material.REDSTONE_BLOCK : Material.AIR);
                return "ok";
            }
            case "state" -> {
                int height = 0;
                while (height < 10 && world.getBlockAt(X, Y + height, Z).getType() == Material.SUGAR_CANE) height++;
                int boneMeal = 0;
                for (ItemStack stack : dispenser(world).getContents()) {
                    if (stack != null && stack.getType() == Material.BONE_MEAL) boneMeal += stack.getAmount();
                }
                return "height=" + height + " bonemeal=" + boneMeal;
            }
            default -> {
                return "error unknown action " + args[1];
            }
        }
    }

    private static Inventory dispenser(World world) {
        return ((Container) world.getBlockAt(X + 1, Y, Z).getState()).getInventory();
    }

    private static void fill(World world, int x1, int y1, int z1, int x2, int y2, int z2, Material material) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != material) block.setType(material, false);
                }
            }
        }
    }

    /** On Folia, runs on the region that owns the location; elsewhere, on the main thread. */
    private void runAt(Location location, Runnable task) {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
        } catch (ClassNotFoundException notFolia) {
            if (Bukkit.isPrimaryThread()) task.run();
            else Bukkit.getScheduler().runTask(this, task);
            return;
        }
        try {
            Object scheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            Method execute = Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler")
                    .getMethod("execute", Plugin.class, Location.class, Runnable.class);
            execute.invoke(scheduler, this, location, task);
        } catch (ReflectiveOperationException e) {
            getLogger().severe("BSCTEST could not schedule on Folia: " + e);
        }
    }
}
