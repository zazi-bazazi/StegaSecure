package org.example.model.image;

import java.util.ArrayList;

/**
 * 
 */
public class ImageProcessor {

    private static final int BLOCK_SIZE = 8;

    private ImageProcessor() {
    }

    // Packing LUT: Maps [u][v] to a 1D ZigZag Index (0-63)
    private static final int[][] ZIGZAG_INDEX = {
            { 0, 1, 5, 6, 14, 15, 27, 28 },
            { 2, 4, 7, 13, 16, 26, 29, 42 },
            { 3, 8, 12, 17, 25, 30, 41, 43 },
            { 9, 11, 18, 24, 31, 40, 44, 53 },
            { 10, 19, 23, 32, 39, 45, 52, 54 },
            { 20, 22, 33, 38, 46, 51, 55, 60 },
            { 21, 34, 37, 47, 50, 56, 59, 61 },
            { 35, 36, 48, 49, 57, 58, 62, 63 }
    };

    // Unpacking LUTs: Maps a 1D ZigZag Index (0-63) back to u and v coordinates
    public static final int[] ZIGZAG_U = {
            0, 0, 1, 2, 1, 0, 0, 1, 2, 3, 4, 3, 2, 1, 0, 0,
            1, 2, 3, 4, 5, 6, 5, 4, 3, 2, 1, 0, 0, 1, 2, 3,
            4, 5, 6, 7, 7, 6, 5, 4, 3, 2, 1, 2, 3, 4, 5, 6,
            7, 7, 6, 5, 4, 3, 4, 5, 6, 7, 7, 6, 5, 6, 7, 7
    };

    public static final int[] ZIGZAG_V = {
            0, 1, 0, 0, 1, 2, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5,
            4, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5, 6, 7, 6, 5, 4,
            3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 6, 5, 4, 3,
            2, 3, 4, 5, 6, 7, 7, 6, 5, 4, 5, 6, 7, 7, 6, 7
    };

    private static class Holder {
        private static final ImageProcessor INSTANCE = new ImageProcessor();
    }

    public static ImageProcessor getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Converts a spatial image into a sparse frequency domain representation.
     * <p>
     * Slices the image into 8x8 blocks, applies the DCT and quantization,
     * and packs the non-zero coefficients into a graph-based SparseDCTMatrix
     * using a zig-zag scanning sequence.
     * </p>
     *
     * @param image The spatial domain matrix of the image.
     * @return A SparseDCTMatrix containing only the non-zero quantized
     *         coefficients.
     */
    public FrequencyDomain convertToFrequencyDomain(SpatialDomain image) {
        int blocksX = (int) Math.ceil((double) image.getWidth() / BLOCK_SIZE);
        int blocksY = (int) Math.ceil((double) image.getHeight() / BLOCK_SIZE);
        int totalBlocks = blocksX * blocksY;

        FrequencyDomain sparseMatrix = new FrequencyDomain(image.getWidth(), image.getHeight());
        int blockIndex = 0;

        for (int X = 0; X < blocksX; X++) {
            for (int Y = 0; Y < blocksY; Y++) {

                int startPixelX = X * BLOCK_SIZE;
                int startPixelY = Y * BLOCK_SIZE;

                // Extract a single 8x8 block from the spatial image
                double[][] spatialBlock = extractBlock(image, startPixelX, startPixelY);

                // System.out.println("SpatialBlock: " + Arrays.deepToString(spatialBlock));

                double[][] frequencyBlock = DCTMath.calculateDCT(spatialBlock);

                // System.out.println("FrequencyBlock: " + Arrays.deepToString(frequencyBlock));

                double[][] quantizedBlock = DCTMath.quantize(frequencyBlock);

                // System.out.println("QuantizedBlock: " + Arrays.deepToString(quantizedBlock));

                // Save the results into your SparseMatrix
                saveBlockToSparseMatrix(sparseMatrix, quantizedBlock, blockIndex);

                blockIndex++;
            }
        }
        return sparseMatrix;
    }

    /**
     * Converts a sparse frequency domain matrix back into a spatial image.
     * <p>
     * Unpacks the zigzag coefficients, dequantizes them, applies the Inverse DCT,
     * and stitches the 8x8 blocks back into a full image grid.
     * </p>
     *
     * @param frequencyMatrix The sparse matrix containing quantized DCT
     *                        coefficients.
     * @return A reconstructed SpatialMatrix representing the image pixels.
     */
    public SpatialDomain convertToSpatialDomain(FrequencyDomain frequencyMatrix) {

        SpatialDomain stegoImage = new SpatialDomain(frequencyMatrix.getWidth(), frequencyMatrix.getHeight());

        int blocksX = (int) Math.ceil((double) frequencyMatrix.getWidth() / BLOCK_SIZE);
        int blocksY = (int) Math.ceil((double) frequencyMatrix.getHeight() / BLOCK_SIZE);
        int blockIndex = 0;

        for (int blockX = 0; blockX < blocksX; blockX++) {
            for (int blockY = 0; blockY < blocksY; blockY++) {
                double[][] quantizedBlock = unpackBlock(frequencyMatrix, blockIndex);
                double[][] frequencyBlock = DCTMath.dequantize(quantizedBlock);
                double[][] spatialBlock = DCTMath.calculateIDCT(frequencyBlock);

                writeBlockToImage(stegoImage, spatialBlock, blockX * BLOCK_SIZE, blockY * BLOCK_SIZE);

                blockIndex++;
            }
        }

        return stegoImage;
    }

    /**
     * Saves a 2D block of frequency coefficients into the sparse matrix structure.
     * <p>
     * Maps the 2D (u,v) coordinates to a 1D index using a zig-zag Look-Up Table
     * (LUT).
     * </p>
     *
     * @param sparseMatrix The target sparse matrix.
     * @param freqBlock    The 8x8 block of quantized coefficients to save.
     * @param blockIndex   The linear index of the current 8x8 block.
     */
    private void saveBlockToSparseMatrix(FrequencyDomain sparseMatrix, double[][] freqBlock, int blockIndex) {
        for (int u = 0; u < BLOCK_SIZE; u++) {
            for (int v = 0; v < BLOCK_SIZE; v++) {

                // zig-zag index from the LUT
                int coeffIndex = ZIGZAG_INDEX[u][v];

                sparseMatrix.setCoefficient(blockIndex, coeffIndex, freqBlock[u][v]);
            }
        }
    }

    /**
     * Extracts an 8x8 block of pixels from the main image matrix.
     * <p>
     * If the block exceeds the image boundaries, the out-of-bounds pixels
     * are padded with 0.0.
     * </p>
     *
     * @param image  The spatial image matrix.
     * @param startX The starting X coordinate (column) in the image.
     * @param startY The starting Y coordinate (row) in the image.
     * @return An 8x8 array representing the extracted spatial block.
     */
    private double[][] extractBlock(SpatialDomain image, int startX, int startY) {
        double[][] block = new double[BLOCK_SIZE][BLOCK_SIZE];

        for (int x = 0; x < BLOCK_SIZE; x++) {
            for (int y = 0; y < BLOCK_SIZE; y++) {

                if ((startX + x) < image.getWidth() && (startY + y) < image.getHeight()) {

                    block[x][y] = image.getGrayscalePixel(startX + x, startY + y);

                } else {
                    block[x][y] = 0.0;
                }
            }
        }
        return block;
    }

    /**
     * Writes an 8x8 block of reconstructed spatial pixels back into the main image
     * matrix.
     * <p>
     * This method maps the localized 8x8 block coordinates back to the global
     * image coordinates. It includes boundary checks to ensure that blocks on the
     * right or bottom edges of an image (whose dimensions aren't perfect multiples
     * of 8) do not throw out-of-bounds exceptions.
     * </p>
     *
     * @param image  The target spatial image matrix where the pixels will be
     *               written.
     * @param block  The 8x8 array of spatial pixel values to write.
     * @param startX The global X coordinate (column) in the image corresponding to
     *               the top-left of this block.
     * @param startY The global Y coordinate (row) in the image corresponding to the
     *               top-left of this block.
     */
    private void writeBlockToImage(SpatialDomain image, double[][] block, int startX, int startY) {
        for (int x = 0; x < BLOCK_SIZE; x++) {
            for (int y = 0; y < BLOCK_SIZE; y++) {

                if ((startX + x) < image.getWidth() && (startY + y) < image.getHeight()) {
                    image.setYChannel(startX + x, startY + y, block[x][y]);
                }
            }
        }
    }

    /**
     * Reconstructs a full 8x8 2D array from the 1D sparse representation of a
     * block.
     * <p>
     * This method iterates through the non-zero coefficients stored in the sparse
     * matrix
     * for a given block. It uses the inverse zig-zag Look-Up Tables (LUTs) to
     * instantly
     * translate the 1D coefficient index back to its correct 2D (u, v) position in
     * the 8x8 grid.
     * Unspecified positions will naturally default to 0.0.
     * </p>
     *
     * @param matrix The sparse frequency matrix containing the stored non-zero
     *               coefficients.
     * @param index  The linear index of the 8x8 block to unpack.
     * @return A standard 8x8 2D array of the block's frequency coefficients.
     */
    private double[][] unpackBlock(FrequencyDomain matrix, int index) {
        double[][] block = new double[BLOCK_SIZE][BLOCK_SIZE];

        ArrayList<DCTNode> coefficientsBlock = matrix.getNonZeroCoefficientsForBlock(index);

        for (DCTNode node : coefficientsBlock) {
            int coeffIndex = node.getCoefficientIndex();

            // Use the LUTs to instantly translate the 1D index back to 2D
            int u = ZIGZAG_U[coeffIndex];
            int v = ZIGZAG_V[coeffIndex];

            block[u][v] = node.getValue();
        }
        return block;
    }

}