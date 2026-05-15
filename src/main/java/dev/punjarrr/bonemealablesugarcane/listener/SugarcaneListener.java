package dev.punjarrr.bonemealablesugarcane.listener;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SugarcaneListener implements Listener {

    private static final List<Material> AIR_TYPES = Arrays.asList(
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR
    );

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public SugarcaneListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getPlayer().isSneaking()) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        if (item.getType() != Material.BONE_MEAL) return;
        if (block.getType() != Material.SUGAR_CANE) return;

        Player player = event.getPlayer();

        // Walk down to the base of this sugar cane column
        Block base = block;
        while (base.getRelative(BlockFace.DOWN).getType() == Material.SUGAR_CANE) {
            base = base.getRelative(BlockFace.DOWN);
        }

        // Count the current height (vanilla max is 3)
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

        if (player.getGameMode() != GameMode.CREATIVE) {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        }

        World world = block.getWorld();

        if (plugin.getConfig().getBoolean("settings.spawn-particles", true)) {
            double bx = block.getX();
            double by = block.getY();
            double bz = block.getZ();
            for (int i = 0; i < 15; i++) {
                world.spawnParticle(Particle.HAPPY_VILLAGER,
                        bx + random.nextDouble(),
                        by + random.nextDouble(),
                        bz + random.nextDouble(),
                        1);
            }
        }

        if (plugin.getConfig().getBoolean("settings.play-sound", true)) {
            world.playSound(block.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 1.0f);
        }
    }
}
