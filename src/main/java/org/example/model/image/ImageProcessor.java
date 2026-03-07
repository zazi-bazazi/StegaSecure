package org.example.model.image;

import java.util.ArrayList;

public class ImageProcessor {

    private static ImageProcessor instance = null;

    private ImageProcessor() {}

    public static ImageProcessor getInstance() {
        if (instance == null) {
            instance = new ImageProcessor();
        }
        return instance;
    }

    // Converts the full image into a Sparse Matrix
    public SparseDCTMatrix convertToFrequencyDomain(SpatialMatrix image) {
        int blocksX = (int) Math.ceil(image.getWidth() / 8.0);
        int blocksY = (int) Math.ceil(image.getHeight() / 8.0);
        int totalBlocks = blocksX * blocksY;

        SparseDCTMatrix sparseMatrix = new SparseDCTMatrix(image.getWidth() , image.getHeight());
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

    public SpatialMatrix convertToSpatialDomain(SparseDCTMatrix frequencyMatrix) {

        SpatialMatrix stegoImage = new SpatialMatrix(frequencyMatrix.getWidth(), frequencyMatrix.getHeight());

        int blocksX = (int) Math.ceil(frequencyMatrix.getWidth() / 8.0);
        int blocksY = (int) Math.ceil(frequencyMatrix.getHeight() / 8.0);
        int blockIndex = 0;

        for (int blockX = 0; blockX < blocksX; blockX++) {
            for(int blockY = 0; blockY < blocksY; blockY++) {
                double[][] frequencyBlock = unpackBlock(frequencyMatrix, blockIndex);
                double[][] spatialBlock = DCTMath.calculateIDCT(frequencyBlock);

                writeBlockToImage(stegoImage, spatialBlock, blockX * 8, blockY * 8);

                blockIndex++;
            }
        }

        return stegoImage;
    }


    private void saveBlockToSparseMatrix(SparseDCTMatrix sparseMatrix, double[][] freqBlock, int blockIndex) {
        int coeffIndex = 0;
        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {
                // The SparseDCTMatrix class handles dropping the zeros internally
                sparseMatrix.setCoefficient(blockIndex, coeffIndex, freqBlock[u][v]);
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

    private void writeBlockToImage(SpatialMatrix image, double[][] block, int startX, int startY) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                // Check boundaries so we don't get an OutOfBounds exception on the edges
                if ((startX + x) < image.getWidth() && (startY + y) < image.getHeight()) {
                    int grayValue = (int) Math.round(block[x][y]);
                    grayValue = Math.max(0, Math.min(255, grayValue));

                    image.setPixel(startX + x, startY + y, grayValue, grayValue, grayValue);
                }
            }
        }
    }

    private double[][] unpackBlock(SparseDCTMatrix matrix, int index) {
        double[][] block = new double[8][8];

        ArrayList<DCTNode> coefficientsBlock = matrix.getNonZeroCoefficientsForBlock(index);

        for (DCTNode node : coefficientsBlock) {
            int u = node.getCoefficientIndex() / 8;
            int v = node.getCoefficientIndex() % 8;
            block[u][v] = node.getValue();
        }
        return block;
    }


}