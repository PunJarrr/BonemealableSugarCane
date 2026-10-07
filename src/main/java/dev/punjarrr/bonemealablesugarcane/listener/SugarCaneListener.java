package dev.punjarrr.bonemealablesugarcane.listener;

import dev.punjarrr.bonemealablesugarcane.growth.CaneGrower;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/** Players right-clicking sugar cane with bone meal, from either hand. */
public class SugarCaneListener implements Listener {

    private final CaneGrower grower;

    public SugarCaneListener(CaneGrower grower) {
        this.grower = grower;
    }

    // HIGH + ignoreCancelled: protection plugins at NORMAL or lower get to cancel first.
    // The event fires for the off hand only when the main hand did nothing, so the main
    // hand has priority, like vanilla. Sneaking doesn't matter for items, like vanilla.
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.useItemInHand() == Event.Result.DENY) return;

        EquipmentSlot hand = event.getHand();
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        if (hand == null || item == null || item.getType() != Material.BONE_MEAL) return;
        if (block == null || block.getType() != Material.SUGAR_CANE) return;

        // Vanilla doesn't let players use bone meal in Adventure mode.
        Player player = event.getPlayer();
        GameMode mode = player.getGameMode();
        if (mode == GameMode.ADVENTURE || mode == GameMode.SPECTATOR) return;

        if (grower.grow(block, player) != CaneGrower.Result.GROWN) return;

        // Stops the server from going on to use the other hand's item in this same click.
        event.setCancelled(true);

        if (hand == EquipmentSlot.HAND) {
            player.swingMainHand();
        } else {
            player.swingOffHand();
        }

        if (mode != GameMode.CREATIVE) {
            PlayerInventory inventory = player.getInventory();
            ItemStack inHand = inventory.getItem(hand);
            int left = inHand.getAmount() - 1;
            if (left > 0) {
                inHand.setAmount(left);
                inventory.setItem(hand, inHand);
            } else {
                inventory.setItem(hand, null);
            }
        }
    }
}
