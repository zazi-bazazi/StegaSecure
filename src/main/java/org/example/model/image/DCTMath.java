package org.example.model.image;

public class DCTMath {

    private DCTMath() {
    }

    private static final int BLOCK_SIZE = 8;
    private static final int PIXEL_MAX_VALUE = 255;
    private static final int PIXEL_MIN_VALUE = 0;
    //
    public static final int[][] LUMA_QUANTIZATION = {
            { 16, 11, 10, 16, 24, 40, 51, 61 },
            { 12, 12, 14, 19, 26, 58, 60, 55 },
            { 14, 13, 16, 24, 40, 57, 69, 56 },
            { 14, 17, 22, 29, 51, 87, 80, 62 },
            { 18, 22, 37, 56, 68, 109, 103, 77 },
            { 24, 35, 55, 64, 81, 104, 113, 92 },
            { 49, 64, 78, 87, 103, 121, 120, 101 },
            { 72, 92, 95, 98, 112, 100, 103, 99 }
    };


    /**
     * Applies the Forward Discrete Cosine Transform (FDCT) to an 8x8 spatial block.
     * <p>
     * Level shifting (subtracting 128) is applied to center the pixel values
     * around zero before transforming them into the frequency domain.
     * </p>
     *
     * @param spatialBlock An 8x8 array of spatial pixel values (0-255).
     * @return An 8x8 array of frequency domain coefficients.
     */
    public static double[][] calculateDCT(double[][] spatialBlock) {
        double[][] frequencyBlock = new double[8][8];

        for (int u = 0; u < BLOCK_SIZE; u++) {
            for (int v = 0; v < BLOCK_SIZE; v++) {
                double sum = 0.0;

                for (int x = 0; x < BLOCK_SIZE; x++) {
                    for (int y = 0; y < BLOCK_SIZE; y++) {
                        // LEVEL SHIFT: Subtract 128 before the math!
                        double shiftedPixel = spatialBlock[x][y] - 128.0;

                        double cosX = Math.cos(((2 * x + 1) * u * Math.PI) / 16.0);
                        double cosY = Math.cos(((2 * y + 1) * v * Math.PI) / 16.0);

                        sum += shiftedPixel * cosX * cosY;
                    }
                }

                double cu = (u == 0) ? (1.0 / Math.sqrt(2)) : 1.0;
                double cv = (v == 0) ? (1.0 / Math.sqrt(2)) : 1.0;

                frequencyBlock[u][v] = 0.25 * cu * cv * sum;
            }
        }
        return frequencyBlock;
    }

    /**
     * Applies the Inverse Discrete Cosine Transform (IDCT) to an 8x8 frequency block.
     * <p>
     * Reconstructs spatial pixels from frequency coefficients and reverses
     * the level shifting by adding 128. Output is clamped to the 0-255 range.
     * </p>
     *
     * @param frequencyBlock An 8x8 array of frequency domain coefficients.
     * @return An 8x8 array of reconstructed spatial pixel values.
     */
    public static double[][] calculateIDCT(double[][] frequencyBlock) {
        double[][] spatialBlock = new double[BLOCK_SIZE][BLOCK_SIZE];

        for (int x = 0; x < BLOCK_SIZE; x++) {
            for (int y = 0; y < BLOCK_SIZE; y++) {
                double sum = 0.0;

                for (int u = 0; u < BLOCK_SIZE; u++) {
                    for (int v = 0; v < BLOCK_SIZE; v++) {
                        double cu = (u == 0) ? (1.0 / Math.sqrt(2)) : 1.0;
                        double cv = (v == 0) ? (1.0 / Math.sqrt(2)) : 1.0;

                        double cosX = Math.cos(((2 * x + 1) * u * Math.PI) / 16.0);
                        double cosY = Math.cos(((2 * y + 1) * v * Math.PI) / 16.0);

                        sum += cu * cv * frequencyBlock[u][v] * cosX * cosY;
                    }
                }

                // REVERSE LEVEL SHIFT: Add the 128 back
                double pixelValue = (0.25 * sum) + 128.0;

                // fix the
                spatialBlock[x][y] = Math.max(PIXEL_MIN_VALUE, Math.min(PIXEL_MAX_VALUE, Math.round(pixelValue)));
            }
        }
        return spatialBlock;
    }

    /**
     * Quantizes an 8x8 block of frequency coefficients using the standard JPEG Luma table.
     * <p>
     * This step reduces precision in high-frequency areas, driving many coefficients
     * to zero to achieve compression/sparsity.
     * </p>
     *
     * @param freqBlock The 8x8 array of unquantized DCT coefficients.
     * @return An 8x8 array of quantized integer values (stored as doubles).
     */
    public static double[][] quantize(double[][] freqBlock) {
        double[][] quantized = new double[BLOCK_SIZE][BLOCK_SIZE];
        for (int u = 0; u < BLOCK_SIZE; u++)
            for (int v = 0; v < BLOCK_SIZE; v++)
                quantized[u][v] = Math.round(freqBlock[u][v] / LUMA_QUANTIZATION[u][v]);
        return quantized;
    }

    /**
     * Dequantizes an 8x8 block by multiplying it against the Luma table.
     *
     * @param quantizedBlock The 8x8 array of quantized coefficients.
     * @return The approximated 8x8 array of frequency coefficients.
     */
    public static double[][] dequantize(double[][] quantizedBlock) {
        double[][] dequantized = new double[BLOCK_SIZE][BLOCK_SIZE];
        for (int u = 0; u < BLOCK_SIZE; u++)
            for (int v = 0; v < BLOCK_SIZE; v++)
                dequantized[u][v] = quantizedBlock[u][v] * LUMA_QUANTIZATION[u][v];
        return dequantized;
    }

}
