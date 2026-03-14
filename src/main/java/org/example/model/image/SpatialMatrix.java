package org.example.model.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Represents an image in the YCbCr color space.
 * RGB is converted to YCbCr once on load; all processing happens on the Y channel.
 * YCbCr is converted back to RGB once on save.
 */
public class SpatialMatrix {
    private final int width;
    private final int height;

    private final double[][] yChannel;
    private final double[][] cbChannel;
    private final double[][] crChannel;

    // Constructor 1: Load an existing image from disk and convert RGB → YCbCr once
    public SpatialMatrix(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedImage image = ImageIO.read(file);
        this.width = image.getWidth();
        this.height = image.getHeight();

        this.yChannel = new double[width][height];
        this.cbChannel = new double[width][height];
        this.crChannel = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                yChannel[x][y] = 0.299 * r + 0.587 * g + 0.114 * b;
                cbChannel[x][y] = -0.168736 * r - 0.331264 * g + 0.5 * b + 128.0;
                crChannel[x][y] = 0.5 * r - 0.418688 * g - 0.081312 * b + 128.0;
            }
        }
    }

    // Constructor 2: Create a blank image (all channels zeroed)
    public SpatialMatrix(int width, int height) {
        this.width = width;
        this.height = height;
        this.yChannel = new double[width][height];
        this.cbChannel = new double[width][height];
        this.crChannel = new double[width][height];

        // Default Cb and Cr to 128 (neutral chroma) so pure-grayscale reconstruction works
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cbChannel[x][y] = 128.0;
                crChannel[x][y] = 128.0;
            }
        }
    }

    // Constructor 3: Deep copy
    public SpatialMatrix(SpatialMatrix original) {
        this.width = original.width;
        this.height = original.height;
        this.yChannel = new double[width][height];
        this.cbChannel = new double[width][height];
        this.crChannel = new double[width][height];

        for (int x = 0; x < width; x++) {
            System.arraycopy(original.yChannel[x], 0, this.yChannel[x], 0, height);
            System.arraycopy(original.cbChannel[x], 0, this.cbChannel[x], 0, height);
            System.arraycopy(original.crChannel[x], 0, this.crChannel[x], 0, height);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // ---- Y channel access (used by DCT / steganography) ----

    public double getGrayscalePixel(int x, int y) {
        return yChannel[x][y];
    }

    public void setYChannel(int x, int y, double value) {
        yChannel[x][y] = Math.max(0, Math.min(255, value));
    }

    // ---- Conversion back to RGB and saving ----

    /**
     * Converts the YCbCr channels to a BufferedImage (single conversion on save).
     */
    private BufferedImage toBufferedImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double yVal = yChannel[x][y];
                double cb = cbChannel[x][y];
                double cr = crChannel[x][y];

                int r = clamp((int) Math.round(yVal + 1.40200 * (cr - 128.0)));
                int g = clamp((int) Math.round(yVal - 0.344136 * (cb - 128.0) - 0.714136 * (cr - 128.0)));
                int b = clamp((int) Math.round(yVal + 1.77200 * (cb - 128.0)));

                image.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return image;
    }

    public File saveImage(String directoryPath, String format) throws IOException {
        File directory = new File(directoryPath);

        // 1. If the folder path doesn't exist yet, create it automatically
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Could not create output directory: " + directoryPath);
            }
        }

        // 2. Auto-generate the file name with a timestamp so runs never collide
        String fileName = "stego_result_" + System.currentTimeMillis() + "." + format;

        // 3. Combine the directory and the file name safely
        File outputFile = new File(directory, fileName);

        // 4. Convert YCbCr → RGB once, then write to disk
        BufferedImage image = toBufferedImage();
        ImageIO.write(image, format, outputFile);

        System.out.println(">>> Image physically created at: " + outputFile.getAbsolutePath());
        return outputFile;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}