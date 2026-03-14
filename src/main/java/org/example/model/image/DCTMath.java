package org.example.model.image;

public class DCTMath {

    private DCTMath() {
    }

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

    // FORWARD DCT
    public static double[][] calculateDCT(double[][] spatialBlock) {
        double[][] frequencyBlock = new double[8][8];

        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {
                double sum = 0.0;

                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
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

    // INVERSE DCT
    public static double[][] calculateIDCT(double[][] frequencyBlock) {
        double[][] spatialBlock = new double[8][8];

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                double sum = 0.0;

                for (int u = 0; u < 8; u++) {
                    for (int v = 0; v < 8; v++) {
                        double cu = (u == 0) ? (1.0 / Math.sqrt(2)) : 1.0;
                        double cv = (v == 0) ? (1.0 / Math.sqrt(2)) : 1.0;

                        double cosX = Math.cos(((2 * x + 1) * u * Math.PI) / 16.0);
                        double cosY = Math.cos(((2 * y + 1) * v * Math.PI) / 16.0);

                        sum += cu * cv * frequencyBlock[u][v] * cosX * cosY;
                    }
                }

                // REVERSE LEVEL SHIFT: Add the 128 back!
                double pixelValue = (0.25 * sum) + 128.0;

                // Clamp it just in case floating point math gets weird
                spatialBlock[x][y] = Math.max(0, Math.min(255, Math.round(pixelValue)));
            }
        }
        return spatialBlock;
    }

    public static double[][] quantize(double[][] freqBlock) {
        double[][] quantized = new double[8][8];
        for (int u = 0; u < 8; u++)
            for (int v = 0; v < 8; v++)
                quantized[u][v] = Math.round(freqBlock[u][v] / LUMA_QUANTIZATION[u][v]);
        return quantized;
    }

    public static double[][] dequantize(double[][] quantizedBlock) {
        double[][] dequantized = new double[8][8];
        for (int u = 0; u < 8; u++)
            for (int v = 0; v < 8; v++)
                dequantized[u][v] = quantizedBlock[u][v] * LUMA_QUANTIZATION[u][v];
        return dequantized;
    }

}
