package org.example.model.stego;

import org.example.model.image.SpatialMatrix;

public class ImageMetrics {

    private static final double MAX_PIXEL_VALUE = 255.0;

    private ImageMetrics() {
    }

    public static double calculatePSNR(SpatialMatrix original, SpatialMatrix stego) {
        double mse = calculateMSE(original, stego);
        if (mse == 0.0)
            return Double.MAX_VALUE;
        return 20.0 * Math.log10(MAX_PIXEL_VALUE / Math.sqrt(mse));
    }

    private static double calculateMSE(SpatialMatrix original, SpatialMatrix stego) {
        double sum_sq = 0;

        for (int i = 0; i < original.getWidth(); i++) {
            for (int j = 0; j < original.getHeight(); j++) {
                double p1 = original.getGrayscalePixel(i, j);
                double p2 = stego.getGrayscalePixel(i, j);
                double err = p2 - p1;

                sum_sq += (err * err);
            }
        }
        return sum_sq / (original.getWidth() * original.getHeight());
    }

}
