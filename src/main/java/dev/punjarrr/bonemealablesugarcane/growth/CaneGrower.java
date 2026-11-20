package dev.punjarrr.bonemealablesugarcane.growth;

import dev.punjarrr.bonemealablesugarcane.config.Settings;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/** Grows a sugar cane column with one bone meal. Shared by players and dispensers. */
public final class CaneGrower {

    public enum Result {
        /** The column grew. The caller should use up one bone meal. */
        GROWN,
        /** Already at max height, or blocked above. Nothing changed. */
        NOT_GROWABLE,
        /** Another plugin (e.g. claim protection) cancelled the BlockFertilizeEvent. Nothing changed. */
        CANCELLED
    }

    // Vanilla bone meal sends this many particles (BoneMealItem: levelEvent(1505, pos, 15)).
    private static final int PARTICLE_COUNT = 15;

    // Effect.BONE_MEAL_USE (vanilla level event 1505: particles and sound) is in the API from 1.19.4.
    // Older servers only have VILLAGER_PLANT_GROW (2005: the same particles, no sound), so the sound
    // is played separately there. Looked up by name because the plugin is compiled against 1.17.
    private static final Effect BONE_MEAL_EFFECT = effect("BONE_MEAL_USE");
    private static final Effect PARTICLES_ONLY_EFFECT = effect("VILLAGER_PLANT_GROW");
    private static final String BONE_MEAL_SOUND = "minecraft:item.bone_meal.use";

    private final Supplier<Settings> settings;
    private final PluginManager pluginManager;

    public CaneGrower(Supplier<Settings> settings, PluginManager pluginManager) {
        this.settings = settings;
        this.pluginManager = pluginManager;
    }

    /**
     * @param clicked the sugar cane block that was bone-mealed (any block of the column)
     * @param player  the player, or {@code null} for a dispenser
     */
    public Result grow(Block clicked, Player player) {
        Settings s = settings.get();

        Block base = clicked;
        while (base.getRelative(BlockFace.DOWN).getType() == Material.SUGAR_CANE) {
            base = base.getRelative(BlockFace.DOWN);
        }

        // Count up to max-height only; a taller column counts as full.
        Block top = base;
        int height = 1;
        while (height < s.maxHeight() && top.getRelative(BlockFace.UP).getType() == Material.SUGAR_CANE) {
            top = top.getRelative(BlockFace.UP);
            height++;
        }

        // Free space above the column, stopping at any block or the world's height limit.
        World world = clicked.getWorld();
        int freeAbove = 0;
        Block probe = top.getRelative(BlockFace.UP);
        while (height + freeAbove < s.maxHeight() && probe.getY() < world.getMaxHeight() && probe.getType().isAir()) {
            freeAbove++;
            probe = probe.getRelative(BlockFace.UP);
        }

        int count = GrowthMath.blocksToGrow(height, s.maxHeight(), s.growByOneBlock(), freeAbove);
        if (count == 0) return Result.NOT_GROWABLE;

        List<BlockState> states = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            BlockState state = top.getRelative(BlockFace.UP, i).getState();
            state.setType(Material.SUGAR_CANE);
            states.add(state);
        }

        BlockFertilizeEvent event = new BlockFertilizeEvent(clicked, player, states);
        pluginManager.callEvent(event);
        if (event.isCancelled()) return Result.CANCELLED;

        // Apply what's left of our column, bottom up, and stop at the first gap: if a plugin
        // removed a middle block, cane above the gap would pop off as a free item.
        List<BlockState> remaining = new ArrayList<>(event.getBlocks());
        remaining.sort(Comparator.comparingInt(BlockState::getY));
        int expectedY = top.getY() + 1;
        boolean grew = false;
        for (BlockState state : remaining) {
            if (state.getX() != top.getX() || state.getZ() != top.getZ() || !world.equals(state.getWorld())) continue;
            if (state.getY() != expectedY) break;
            state.update(true);
            grew = true;
            expectedY++;
        }
        if (!grew) return Result.CANCELLED;

        if (s.effects()) {
            // The vanilla bone meal level event: the client draws the particles over the block's
            // shape and plays the sound on the Blocks volume slider.
            playBoneMealEffect(world, clicked.getLocation());
        }
        return Result.GROWN;
    }

    private static void playBoneMealEffect(World world, Location location) {
        if (BONE_MEAL_EFFECT != null) {
            world.playEffect(location, BONE_MEAL_EFFECT, PARTICLE_COUNT);
        } else if (PARTICLES_ONLY_EFFECT != null) {
            world.playEffect(location, PARTICLES_ONLY_EFFECT, PARTICLE_COUNT);
            world.playSound(location.clone().add(0.5, 0.5, 0.5), BONE_MEAL_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    private static Effect effect(String name) {
        try {
            return Effect.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
