package dev.punjarrr.bonemealablesugarcane.growth;

import org.junit.jupiter.api.Test;

import static dev.punjarrr.bonemealablesugarcane.growth.GrowthMath.blocksToGrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GrowthMathTest {

    private static final int OPEN_SKY = 100;

    @Test
    void growsStraightToMaxHeight() {
        assertEquals(2, blocksToGrow(1, 3, false, OPEN_SKY));
        assertEquals(1, blocksToGrow(2, 3, false, OPEN_SKY));
        assertEquals(4, blocksToGrow(1, 5, false, OPEN_SKY));
    }

    @Test
    void growsOneBlockAtATime() {
        assertEquals(1, blocksToGrow(1, 3, true, OPEN_SKY));
        assertEquals(1, blocksToGrow(2, 3, true, OPEN_SKY));
    }

    @Test
    void nothingAtOrAboveMaxHeight() {
        assertEquals(0, blocksToGrow(3, 3, false, OPEN_SKY));
        assertEquals(0, blocksToGrow(3, 3, true, OPEN_SKY));
        assertEquals(0, blocksToGrow(5, 3, false, OPEN_SKY));
    }

    @Test
    void maxHeightOneNeverGrows() {
        assertEquals(0, blocksToGrow(1, 1, false, OPEN_SKY));
        assertEquals(0, blocksToGrow(1, 1, true, OPEN_SKY));
    }

    @Test
    void blockedAboveLimitsGrowth() {
        assertEquals(1, blocksToGrow(1, 3, false, 1));
        assertEquals(0, blocksToGrow(1, 3, false, 0));
        assertEquals(0, blocksToGrow(1, 3, true, 0));
    }
}
