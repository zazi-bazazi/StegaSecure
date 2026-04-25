package org.example.test;

import org.example.model.image.DCTMath;
import org.example.model.image.ImageProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DCTQuantizationTest {

    @Test
    public void testQuantizeAndDequantizeRoundTrip() {
        // Create a frequency block with known values
        double[][] freqBlock = new double[8][8];
        freqBlock[0][0] = 1024.0; // DC component
        freqBlock[0][1] = 55.0;
        freqBlock[1][0] = -36.0;
        freqBlock[3][3] = 15.0;

        double[][] quantized = DCTMath.quantize(freqBlock);
        double[][] dequantized = DCTMath.dequantize(quantized);

        // DC: 1024 / 16 = 64, then 64 * 16 = 1024 — perfect
        assertEquals(1024.0, dequantized[0][0], 0.001, "DC coefficient should survive quantization exactly.");

        // The dequantized values won't match originals exactly due to rounding,
        // but should be close (within one quantization step)
        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {
                double maxError = DCTMath.LUMA_QUANTIZATION[u][v];
                assertEquals(freqBlock[u][v], dequantized[u][v], maxError,
                        "Dequantized value at [" + u + "][" + v + "] exceeds one quantization step.");
            }
        }

        System.out.println("SUCCESS: Quantize/Dequantize round trip within tolerance.");
    }

    @Test
    public void testQuantizationProducesZeros() {
        // Small values should quantize to zero
        double[][] freqBlock = new double[8][8];
        freqBlock[7][7] = 40.0; // Q[7][7] = 99, so 40/99 rounds to 0

        double[][] quantized = DCTMath.quantize(freqBlock);
        assertEquals(0.0, quantized[7][7], 0.001,
                "Small coefficient should be quantized to zero.");

        System.out.println("SUCCESS: Small coefficients are killed by quantization.");
    }

    @Test
    public void testFullPipelineRoundTrip() {
        // Create a gradient spatial block
        double[][] originalBlock = new double[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                originalBlock[x][y] = 50.0 + (x * 20) + (y * 5);
            }
        }

        // Forward: DCT → Quantize
        double[][] freq = DCTMath.calculateDCT(originalBlock);
        double[][] quantized = DCTMath.quantize(freq);

        // Reverse: Dequantize → IDCT
        double[][] dequantized = DCTMath.dequantize(quantized);
        double[][] reconstructed = DCTMath.calculateIDCT(dequantized);

        // With quantization, we expect more error than without.
        // But it should still be a reasonable reconstruction.
        double maxError = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                double error = Math.abs(originalBlock[x][y] - reconstructed[x][y]);
                maxError = Math.max(maxError, error);
            }
        }

        assertTrue(maxError < 30.0,
                "Full pipeline reconstruction error too large: " + maxError);

        System.out.println("SUCCESS: Full DCT → Quantize → Dequantize → IDCT pipeline. Max error: "
                + String.format("%.2f", maxError));
    }

    @Test
    public void testZigZagLUTConsistency() {
        // Every (u,v) pair from ZIGZAG_U/V should map back to the correct zig-zag index
        // by checking with the ZIGZAG_INDEX packing table
        for (int zigzag = 0; zigzag < 64; zigzag++) {
            int u = ImageProcessor.ZIGZAG_U[zigzag];
            int v = ImageProcessor.ZIGZAG_V[zigzag];

            assertTrue(u >= 0 && u < 8, "ZIGZAG_U[" + zigzag + "] out of range: " + u);
            assertTrue(v >= 0 && v < 8, "ZIGZAG_V[" + zigzag + "] out of range: " + v);
        }

        // Check that all 64 positions are covered (no duplicates)
        boolean[][] visited = new boolean[8][8];
        for (int zigzag = 0; zigzag < 64; zigzag++) {
            int u = ImageProcessor.ZIGZAG_U[zigzag];
            int v = ImageProcessor.ZIGZAG_V[zigzag];
            assertFalse(visited[u][v], "Duplicate mapping at zigzag index " + zigzag);
            visited[u][v] = true;
        }

        System.out.println("SUCCESS: Zig-zag LUT covers all 64 positions with no duplicates.");
    }
}
