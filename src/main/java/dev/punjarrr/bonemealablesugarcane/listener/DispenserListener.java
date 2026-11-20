package dev.punjarrr.bonemealablesugarcane.listener;

import dev.punjarrr.bonemealablesugarcane.config.Settings;
import dev.punjarrr.bonemealablesugarcane.growth.CaneGrower;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

/** Dispensers firing bone meal at sugar cane. */
public class DispenserListener implements Listener {

    private final Supplier<Settings> settings;
    private final CaneGrower grower;

    public DispenserListener(Supplier<Settings> settings, CaneGrower grower) {
        this.settings = settings;
        this.grower = grower;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        if (!settings.get().dispenserSupport()) return;
        ItemStack dispensed = event.getItem();
        if (dispensed.getType() != Material.BONE_MEAL) return;

        Block dispenser = event.getBlock();
        if (dispenser.getType() != Material.DISPENSER) return;
        if (!(dispenser.getBlockData() instanceof Directional directional)) return;
        Block target = dispenser.getRelative(directional.getFacing());
        if (target.getType() != Material.SUGAR_CANE) return;

        // If nothing grew, don't cancel: vanilla then fails as it always does on sugar cane
        // (fail click, smoke, bone meal kept).
        if (grower.grow(target, null) != CaneGrower.Result.GROWN) return;

        // Cancelled, the server keeps the bone meal and plays the click and smoke itself,
        // so we only use up one bone meal here.
        event.setCancelled(true);
        consumeOne(dispenser, dispensed);
    }

    /**
     * Takes one item matching {@code dispensed} out of the dispenser. The amount is lowered on
     * the live stack instead of clearing the slot: after a cancelled dispense the server writes
     * that same stack back into its slot, which would undo a cleared slot and give the last
     * bone meal back.
     */
    private static void consumeOne(Block dispenser, ItemStack dispensed) {
        if (!(dispenser.getState() instanceof Dispenser state)) return;
        Inventory inventory = state.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack != null && stack.isSimilar(dispensed)) {
                stack.setAmount(stack.getAmount() - 1);
                return;
            }
        }
    }
}
