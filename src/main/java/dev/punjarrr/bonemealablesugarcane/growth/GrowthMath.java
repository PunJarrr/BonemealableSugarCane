package dev.punjarrr.bonemealablesugarcane.growth;

/** The height math behind one bone meal, kept free of Bukkit types so it can be unit-tested. */
public final class GrowthMath {

    private GrowthMath() {}

    /**
     * How many sugar cane blocks one bone meal adds.
     *
     * @param height     the column's current height (at least 1)
     * @param maxHeight  the configured max height (at least 1)
     * @param growByOne  grow one block at a time instead of straight to the max
     * @param freeAbove  how many air blocks are above the column before something
     *                   (a block or the world's height limit) is in the way
     */
    public static int blocksToGrow(int height, int maxHeight, boolean growByOne, int freeAbove) {
        if (height >= maxHeight) return 0;
        int wanted = growByOne ? 1 : maxHeight - height;
        return Math.max(0, Math.min(wanted, freeAbove));
    }
}
