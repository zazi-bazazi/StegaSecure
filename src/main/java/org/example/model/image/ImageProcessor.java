package org.example.model.image;

import java.util.ArrayList;

public class ImageProcessor {

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

    // Converts the full image into a Sparse Matrix
    public SparseDCTMatrix convertToFrequencyDomain(SpatialMatrix image) {
        int blocksX = (int) Math.ceil(image.getWidth() / 8.0);
        int blocksY = (int) Math.ceil(image.getHeight() / 8.0);
        int totalBlocks = blocksX * blocksY;

        SparseDCTMatrix sparseMatrix = new SparseDCTMatrix(image.getWidth(), image.getHeight());
        int blockIndex = 0;

        // Loop through the blocks
        for (int X = 0; X < blocksX; X++) {
            for (int Y = 0; Y < blocksY; Y++) {

                // Calculate the actual starting pixel coordinates for this block (jump by 8)
                int startPixelX = X * 8;
                int startPixelY = Y * 8;

                // 1. Extract a single 8x8 block from the spatial image
                double[][] spatialBlock = extractBlock(image, startPixelX, startPixelY);

                // 2. Do the math
                double[][] frequencyBlock = DCTMath.calculateDCT(spatialBlock);

                double[][] quantizedBlock = DCTMath.quantize(frequencyBlock);

                // 3. Save the results into your SparseMatrix
                saveBlockToSparseMatrix(sparseMatrix, quantizedBlock, blockIndex);

                blockIndex++;
            }
        }
        return sparseMatrix;
    }

    public SpatialMatrix convertToSpatialDomain(SparseDCTMatrix frequencyMatrix) {

        SpatialMatrix stegoImage = new SpatialMatrix(frequencyMatrix.getWidth(), frequencyMatrix.getHeight());

        int blocksX = (int) Math.ceil(frequencyMatrix.getWidth() / 8.0);
        int blocksY = (int) Math.ceil(frequencyMatrix.getHeight() / 8.0);
        int blockIndex = 0;

        for (int blockX = 0; blockX < blocksX; blockX++) {
            for (int blockY = 0; blockY < blocksY; blockY++) {
                double[][] quantizedBlock = unpackBlock(frequencyMatrix, blockIndex);
                double[][] frequencyBlock = DCTMath.dequantize(quantizedBlock);
                double[][] spatialBlock = DCTMath.calculateIDCT(frequencyBlock);

                writeBlockToImage(stegoImage, spatialBlock, blockX * 8, blockY * 8);

                blockIndex++;
            }
        }

        return stegoImage;
    }

    private void saveBlockToSparseMatrix(SparseDCTMatrix sparseMatrix, double[][] freqBlock, int blockIndex) {
        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {
                // Instantly grab the correct zig-zag index from the LUT
                int coeffIndex = ZIGZAG_INDEX[u][v];

                sparseMatrix.setCoefficient(blockIndex, coeffIndex, freqBlock[u][v]);
            }
        }
    }

    private double[][] extractBlock(SpatialMatrix image, int startX, int startY) {
        double[][] block = new double[8][8];

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {

                // Check boundaries so we don't get an OutOfBounds exception on the edges
                if ((startX + x) < image.getWidth() && (startY + y) < image.getHeight()) {

                    // Assuming you updated SpatialMatrix to handle the grayscale math under the
                    // hood!
                    block[x][y] = image.getGrayscalePixel(startX + x, startY + y);

                } else {
                    // Pad with 0 if we hit the edge of the image
                    block[x][y] = 0.0;
                }
            }
        }
        return block;
    }

    private void writeBlockToImage(SpatialMatrix image, double[][] block, int startX, int startY) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                // Check boundaries so we don't get an OutOfBounds exception on the edges
                if ((startX + x) < image.getWidth() && (startY + y) < image.getHeight()) {
                    image.setYChannel(startX + x, startY + y, block[x][y]);
                }
            }
        }
    }

    private double[][] unpackBlock(SparseDCTMatrix matrix, int index) {
        double[][] block = new double[8][8];

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