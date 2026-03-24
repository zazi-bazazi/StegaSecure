package org.example.model.stego;

import org.example.model.image.SparseDCTMatrix;
import org.example.model.image.SpatialMatrix;

public class ImageMetrics {

    private static final double MAX_PIXEL_VALUE = 255.0;
    private static final double MINIMAL_QUALITY = 35.0;
    private static final double PENALTY = 2.0;

    private ImageMetrics() {
    }

    @Deprecated
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

    public static double calculatePSNR(SparseDCTMatrix frequencyDomain, String secretBits, int bitIndex, double sse) {
        int skipped = secretBits.length() - bitIndex;
        int totalPixels = frequencyDomain.getWidth() * frequencyDomain.getHeight();
        double mse = sse / totalPixels;

        double psnrScore;
        if (mse == 0.0 && skipped == 0) {
            psnrScore = Double.MAX_VALUE;
        } else if (mse == 0.0) {
            psnrScore = Math.max(0, MINIMAL_QUALITY - (skipped * PENALTY));
        } else {
            psnrScore = 10.0 * Math.log10(255.0 * 255.0 / mse);
            if (psnrScore < MINIMAL_QUALITY || skipped > 0)
                psnrScore /= PENALTY;
        }
        return psnrScore;
    }

}
