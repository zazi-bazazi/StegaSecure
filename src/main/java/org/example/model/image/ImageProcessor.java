package org.example.model.image;

public class ImageProcessor {

    // Converts the full image into a Sparse Matrix
    public SparseDCTMatrix convertToFrequencyDomain(SpatialMatrix image) {
        int blocksX = (int) Math.ceil(image.getWidth() / 8.0);
        int blocksY = (int) Math.ceil(image.getHeight() / 8.0);
        int totalBlocks = blocksX * blocksY;

        SparseDCTMatrix sparseMatrix = new SparseDCTMatrix(totalBlocks);
        int blockIndex = 0;

        // Loop through the blocks
        for (int blockX = 0; blockX < blocksX; blockX++) {
            for (int blockY = 0; blockY < blocksY; blockY++) {

                // Calculate the actual starting pixel coordinates for this block (jump by 8)
                int startPixelX = blockX * 8;
                int startPixelY = blockY * 8;

                // 1. Extract a single 8x8 block from the spatial image
                double[][] spatialBlock = extractBlock(image, startPixelX, startPixelY);

                // 2. Do the math
                double[][] frequencyBlock = DCTMath.calculateDCT(spatialBlock);

                // 3. Save the results into your SparseMatrix
                saveBlockToSparseMatrix(sparseMatrix, frequencyBlock, blockIndex);

                blockIndex++;
            }
        }
        return sparseMatrix;
    }

    // Helper to push the 8x8 array into your custom Sparse structure
    private void saveBlockToSparseMatrix(SparseDCTMatrix sparseMatrix, double[][] freqBlock, int blockIndex) {
        int coeffIndex = 0;
        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {
                // The SparseDCTMatrix class handles dropping the zeros internally
                sparseMatrix.addCoefficient(blockIndex, coeffIndex, freqBlock[u][v]);
                coeffIndex++;
            }
        }
    }

    private double[][] extractBlock(SpatialMatrix image, int startX, int startY) {
        double[][] block = new double[8][8];

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {

                // Check boundaries so we don't get an OutOfBounds exception on the edges
                if ((startX + x) < image.getWidth() && (startY + y) < image.getHeight()) {

                    // Assuming you updated SpatialMatrix to handle the grayscale math under the hood!
                    block[x][y] = image.getGrayscalePixel(startX + x, startY + y);

                } else {
                    // Pad with 0 if we hit the edge of the image
                    block[x][y] = 0.0;
                }
            }
        }
        return block;
    }
}