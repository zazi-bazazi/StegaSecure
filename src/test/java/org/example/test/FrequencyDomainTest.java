package org.example.test;

import org.example.model.image.FrequencyDomain;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FrequencyDomainTest {

    @Test
    public void testSetAndGetCoefficient() {
        FrequencyDomain matrix = new FrequencyDomain(16, 16); // 2x2 blocks

        matrix.setCoefficient(0, 5, 42.0);
        matrix.setCoefficient(0, 10, -7.5);

        assertEquals(42.0, matrix.getCoefficient(0, 5), 0.001);
        assertEquals(-7.5, matrix.getCoefficient(0, 10), 0.001);
        // Non-existent coefficient should return 0
        assertEquals(0.0, matrix.getCoefficient(0, 3), 0.001);

        System.out.println("SUCCESS: Set/Get coefficient works correctly.");
    }

    @Test
    public void testSetCoefficientOverwrite() {
        FrequencyDomain matrix = new FrequencyDomain(8, 8); // 1 block

        matrix.setCoefficient(0, 5, 10.0);
        assertEquals(10.0, matrix.getCoefficient(0, 5), 0.001);

        // Overwrite the same position
        matrix.setCoefficient(0, 5, 99.0);
        assertEquals(99.0, matrix.getCoefficient(0, 5), 0.001);

        // Should still be only 1 node for this coefficient, not 2
        assertEquals(1, matrix.getNonZeroCoefficientsForBlock(0).size());

        System.out.println("SUCCESS: Coefficient overwrite works correctly.");
    }

    @Test
    public void testZeroCoefficientNotStored() {
        FrequencyDomain matrix = new FrequencyDomain(8, 8);

        matrix.setCoefficient(0, 5, 0.0);
        assertEquals(0, matrix.getNonZeroCoefficientsForBlock(0).size(),
                "Zero coefficients should not be stored in the sparse matrix.");

        System.out.println("SUCCESS: Zero coefficients are not stored.");
    }

    @Test
    public void testCopyConstructorDeepCopy() {
        FrequencyDomain original = new FrequencyDomain(16, 16);
        original.setCoefficient(0, 1, 50.0);
        original.setCoefficient(3, 7, -20.0);

        FrequencyDomain copy = new FrequencyDomain(original);

        // Copy should have the same values
        assertEquals(50.0, copy.getCoefficient(0, 1), 0.001);
        assertEquals(-20.0, copy.getCoefficient(3, 7), 0.001);

        // Mutating the copy should NOT affect the original
        copy.setCoefficient(0, 1, 999.0);
        assertEquals(50.0, original.getCoefficient(0, 1), 0.001,
                "FATAL: Deep copy is broken — modifying the copy changed the original!");

        System.out.println("SUCCESS: Copy constructor creates an independent deep copy.");
    }

    @Test
    public void testSpatialAdjacency4Connected() {
        // 3x3 grid of blocks = 24x24 image
        FrequencyDomain matrix = new FrequencyDomain(24, 24);

        // Block layout (blocksX=3, blocksY=3):
        // index = bx * blocksY + by
        // 0 1 2
        // 3 4 5
        // 6 7 8

        // Center block (4) should have 4 neighbors: 1, 3, 5, 7
        List<Integer> neighbors4 = matrix.getNeighborBlocks(4);
        assertEquals(4, neighbors4.size(), "Center block should have exactly 4 neighbors.");
        assertTrue(neighbors4.contains(1), "Block 4 should neighbor block 1 (up).");
        assertTrue(neighbors4.contains(3), "Block 4 should neighbor block 3 (left).");
        assertTrue(neighbors4.contains(5), "Block 4 should neighbor block 5 (right).");
        assertTrue(neighbors4.contains(7), "Block 4 should neighbor block 7 (down).");

        // Corner block (0) should have 2 neighbors: 1 (right-ish), 3 (down-ish)
        List<Integer> neighbors0 = matrix.getNeighborBlocks(0);
        assertEquals(2, neighbors0.size(), "Corner block should have exactly 2 neighbors.");

        // Edge block (1) should have 3 neighbors
        List<Integer> neighbors1 = matrix.getNeighborBlocks(1);
        assertEquals(3, neighbors1.size(), "Edge block should have exactly 3 neighbors.");

        System.out.println("SUCCESS: 4-connected spatial adjacency is correct.");
    }

    @Test
    public void testSingleBlockNoNeighbors() {
        // 1x1 grid = single 8x8 block
        FrequencyDomain matrix = new FrequencyDomain(8, 8);

        List<Integer> neighbors = matrix.getNeighborBlocks(0);
        assertEquals(0, neighbors.size(), "Single block should have no neighbors.");

        System.out.println("SUCCESS: Single block has no neighbors.");
    }

    @Test
    public void testDimensions() {
        FrequencyDomain matrix = new FrequencyDomain(100, 200);

        assertEquals(100, matrix.getWidth());
        assertEquals(200, matrix.getHeight());
        assertEquals(13, matrix.getBlocksX()); // ceil(100/8) = 13
        assertEquals(25, matrix.getBlocksY()); // ceil(200/8) = 25
        assertEquals(325, matrix.getTotalBlocks()); // 13 * 25

        System.out.println("SUCCESS: Dimensions calculated correctly.");
    }
}
