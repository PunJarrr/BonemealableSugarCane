package dev.punjarrr.bonemealablesugarcane.listener;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DispenserListener implements Listener {

    private static final List<Material> AIR_TYPES = Arrays.asList(
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR
    );

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public DispenserListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        if (event.getItem().getType() != Material.BONE_MEAL) return;
        if (!plugin.getConfig().getBoolean("settings.dispenser-support", true)) return;
        if (event.getBlock().getType() != Material.DISPENSER) return;

        Block dispenser = event.getBlock();
        BlockFace facing = ((Directional) dispenser.getBlockData()).getFacing();
        Block target = dispenser.getRelative(facing);

        if (target.getType() != Material.SUGAR_CANE) return;

        // Always cancel to prevent the bone meal being ejected as an item entity
        event.setCancelled(true);

        // Walk down to the base of the sugar cane column
        Block base = target;
        while (base.getRelative(BlockFace.DOWN).getType() == Material.SUGAR_CANE) {
            base = base.getRelative(BlockFace.DOWN);
        }

        // Count current height
        int length = 1;
        Block top = base;
        for (int i = 0; i < 3; i++) {
            Block above = top.getRelative(BlockFace.UP);
            if (above.getType() != Material.SUGAR_CANE) break;
            top = above;
            length++;
        }

        if (length >= 3) return;

        boolean growByOne = plugin.getConfig().getBoolean("settings.grow-by-one-block", false);
        int blocksToGrow = growByOne ? 1 : (3 - length);

        boolean grew = false;
        for (int i = 0; i < blocksToGrow; i++) {
            Block next = top.getRelative(BlockFace.UP);
            if (!AIR_TYPES.contains(next.getType())) break;
            next.setType(Material.SUGAR_CANE);
            top = next;
            grew = true;
        }

        if (!grew) return;

        // Consume one bone meal from the dispenser inventory
        org.bukkit.block.Dispenser dispenserState = (org.bukkit.block.Dispenser) dispenser.getState();
        Inventory inv = dispenserState.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && stack.getType() == Material.BONE_MEAL) {
                if (stack.getAmount() > 1) {
                    stack.setAmount(stack.getAmount() - 1);
                } else {
                    inv.setItem(i, null);
                }
                break;
            }
        }

        World world = target.getWorld();

        if (plugin.getConfig().getBoolean("settings.spawn-particles", true)) {
            double bx = target.getX();
            double by = target.getY();
            double bz = target.getZ();
            for (int i = 0; i < 15; i++) {
                world.spawnParticle(Particle.HAPPY_VILLAGER,
                        bx + random.nextDouble(),
                        by + random.nextDouble(),
                        bz + random.nextDouble(),
                        1);
            }
        }

        if (plugin.getConfig().getBoolean("settings.play-sound", true)) {
            world.playSound(target.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 1.0f);
        }
    }
}
