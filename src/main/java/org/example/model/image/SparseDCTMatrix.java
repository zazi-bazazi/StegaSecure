package org.example.model.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Graph-structured representation of quantized DCT coefficients.
 * <p>
 * Each DCT block is a NODE that stores its non-zero coefficients.
 * EDGES connect spatially adjacent blocks (4-connected grid: up, down, left,
 * right).
 * <p>
 * This dual structure supports both:
 * - Coefficient access: getCoefficient(block, index) / setCoefficient(...)
 * - Spatial traversal: getNeighborBlocks(block) for graph-aware algorithms
 */
public class SparseDCTMatrix {

    /**
     * Node data: A list for each block containing only its non-zero coefficients.
     */
    private final ArrayList<ArrayList<DCTNode>> blockCoefficients;

    /**
     * Adjacency list representing spatial neighbor edges between 8 &times; 8
     * blocks.
     */
    private final ArrayList<List<Integer>> adjacencyList;

    private final int totalBlocks;
    private final int blocksX;
    private final int blocksY;
    private final int imageWidth;
    private final int imageHeight;

    /**
     * Constructs a new SparseDCTMatrix based on image dimensions.
     * 
     * @param width  The width of the original image in pixels.
     * @param height The height of the original image in pixels.
     */
    public SparseDCTMatrix(int width, int height) {
        this.imageHeight = height;
        this.imageWidth = width;
        this.blocksX = (int) Math.ceil(width / 8.0);
        this.blocksY = (int) Math.ceil(height / 8.0);
        this.totalBlocks = blocksX * blocksY;

        // Initialize node data (coefficients per block)
        this.blockCoefficients = new ArrayList<>(totalBlocks);
        for (int i = 0; i < totalBlocks; i++) {
            blockCoefficients.add(new ArrayList<>());
        }

        // Build spatial adjacency edges (4-connected grid)
        this.adjacencyList = new ArrayList<>(totalBlocks);
        for (int i = 0; i < totalBlocks; i++) {
            adjacencyList.add(new ArrayList<>());
        }

        for (int bx = 0; bx < blocksX; bx++) {
            for (int by = 0; by < blocksY; by++) {
                int current = bx * blocksY + by;

                // Right neighbor
                if (bx + 1 < blocksX) {
                    int right = (bx + 1) * blocksY + by;
                    adjacencyList.get(current).add(right);
                    adjacencyList.get(right).add(current);
                }
                // Down neighbor
                if (by + 1 < blocksY) {
                    int down = bx * blocksY + (by + 1);
                    adjacencyList.get(current).add(down);
                    adjacencyList.get(down).add(current);
                }
            }
        }
    }

    /**
     * Deep-copy constructor.
     * <p>
     * Creates new instances of the coefficient data while sharing the immutable
     * spatial adjacency structure to save memory.
     * </p>
     * 
     * @param original The source SparseDCTMatrix to copy.
     */
    public SparseDCTMatrix(SparseDCTMatrix original) {
        this.imageWidth = original.imageWidth;
        this.imageHeight = original.imageHeight;
        this.blocksX = original.blocksX;
        this.blocksY = original.blocksY;
        this.totalBlocks = original.totalBlocks;

        // Deep copy coefficient data
        this.blockCoefficients = new ArrayList<>(this.totalBlocks);
        for (int i = 0; i < this.totalBlocks; i++) {
            ArrayList<DCTNode> newBlockList = new ArrayList<>();
            ArrayList<DCTNode> originalBlockList = original.getNonZeroCoefficientsForBlock(i);
            for (DCTNode oldNode : originalBlockList) {
                newBlockList.add(new DCTNode(oldNode.getCoefficientIndex(), oldNode.getValue()));
            }
            this.blockCoefficients.add(newBlockList);
        }

        // Share the same adjacency structure (it's immutable after construction)
        this.adjacencyList = original.adjacencyList;
    }

    /**
     * Updates a coefficient's value.
     * If the value is 0.0, the node is removed from the sparse list.
     * 
     * @param blockIndex The index of the 8 &times; 8 block.
     * @param coeffIndex The zig-zag index of the coefficient (0-63).
     * @param value      The new value to set.
     */
    public void setCoefficient(int blockIndex, int coeffIndex, double value) {
        ArrayList<DCTNode> blockList = blockCoefficients.get(blockIndex);

        for (int i = 0; i < blockList.size(); i++) {
            if (blockList.get(i).getCoefficientIndex() == coeffIndex) {
                if (value == 0.0) {
                    // Remove the node — sparse means "only store non-zero"
                    blockList.remove(i);
                } else {
                    blockList.get(i).setValue(value);
                }
                return;
            }
        }

        // Not found — add only if non-zero
        if (value != 0.0) {
            blockList.add(new DCTNode(coeffIndex, value));
        }
    }

    /**
     * Retrieves a coefficient's value.
     * Returns 0.0 if the coefficient is not explicitly stored.
     * 
     * @param blockIndex The index of the 8 &times; 8 block.
     * @param coeffIndex The zig-zag index of the coefficient (0-63).
     * @return The value of the coefficient.
     */
    public double getCoefficient(int blockIndex, int coeffIndex) {
        ArrayList<DCTNode> blockList = blockCoefficients.get(blockIndex);
        for (DCTNode node : blockList) {
            if (node.getCoefficientIndex() == coeffIndex) {
                return node.getValue();
            }
        }
        return 0.0;
    }

    /**
     * Retrieves all stored (non-zero) coefficients for a specific block.
     * 
     * @param blockIndex The index of the block.
     * @return An ArrayList of {@link DCTNode} objects.
     */
    public ArrayList<DCTNode> getNonZeroCoefficientsForBlock(int blockIndex) {
        return blockCoefficients.get(blockIndex);
    }

    /**
     * Returns the block indices that are spatially adjacent to the given block.
     * 
     * @param blockIndex The center block index.
     * @return An unmodifiable list of neighbor indices.
     */
    public List<Integer> getNeighborBlocks(int blockIndex) {
        return Collections.unmodifiableList(adjacencyList.get(blockIndex));
    }

    public int getWidth() {
        return this.imageWidth;
    }

    public int getHeight() {
        return this.imageHeight;
    }

    public int getBlocksX() {
        return this.blocksX;
    }

    public int getBlocksY() {
        return this.blocksY;
    }

    public int getTotalBlocks() {
        return this.totalBlocks;
    }
}