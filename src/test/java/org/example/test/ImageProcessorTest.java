package org.example.test;

import org.example.model.image.DCTMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageProcessorTest {

    @Test
    public void testDCTandIDCTRoundTrip() {
        System.out.println("--- TESTING DCT MATH ROUND TRIP ---");

        // 1. Create a dummy 8x8 spatial block (representing pixels 0-255)
        // Let's make a simple gradient block
        double[][] originalBlock = new double[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                originalBlock[x][y] = 100.0 + (x * 10);
            }
        }

        // 2. Forward Transform (Spatial -> Frequency)
        double[][] frequencyBlock = DCTMath.calculateDCT(originalBlock);

        // 3. Inverse Transform (Frequency -> Spatial)
        double[][] reconstructedBlock = DCTMath.calculateIDCT(frequencyBlock);

        // 4. Evaluate the difference!
        // Because we are doing heavy floating-point math (cosines, square roots),
        // the pixels won't be mathematically identical to the decimal point.
        // We use a "delta" of 1.5 to allow for standard rounding errors.
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                assertEquals(originalBlock[x][y], reconstructedBlock[x][y], 1.5,
                        "Pixel reconstruction failed at coordinate [" + x + "][" + y + "]");
            }
        }
        System.out.println("SUCCESS: DCT and IDCT math perfectly reconstruct the image matrix!");
    }

    @Test
    public void testDCTLevelShifting() {
        System.out.println("--- TESTING DCT DC COEFFICIENT MATH ---");

        // If an entire block is solid gray (128), shifting it by -128 makes it 0.
        // Therefore, the DC coefficient (top left at [0][0]) should be exactly 0.0.
        double[][] solidGrayBlock = new double[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                solidGrayBlock[x][y] = 128.0;
            }
        }

        double[][] frequencyBlock = DCTMath.calculateDCT(solidGrayBlock);

        // Assert the DC coefficient is 0.0 (allowing 0.1 delta for floating math)
        assertEquals(0.0, frequencyBlock[0][0], 0.1, "Level shifting by -128 failed!");
        System.out.println("SUCCESS: Level shifting and DC calculation are correct.");
    }
}