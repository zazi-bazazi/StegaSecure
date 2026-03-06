package org.example.model.image;

public class DCTMath {

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
}
