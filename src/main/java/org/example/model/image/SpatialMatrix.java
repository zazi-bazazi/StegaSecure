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

    private static final int PIXEL_MAX_VALUE = 255;
    private static final int PIXEL_MIN_VALUE = 0;
    private static final double GRAY_SCALE_VALUE = 128.0;

    /**
     * Constructs a SpatialMatrix by loading an image file from disk.
     * @param file file The source image file
     * @throws IOException If the file cannot be read or is not a valid image.
     */
    public SpatialMatrix(File file) throws IOException {

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
                cbChannel[x][y] = -0.168736 * r - 0.331264 * g + 0.5 * b + GRAY_SCALE_VALUE;
                crChannel[x][y] = 0.5 * r - 0.418688 * g - 0.081312 * b + GRAY_SCALE_VALUE;
            }
        }
    }

    /**
     * Initializes a blank SpatialMatrix with specific dimensions.
     * <p>
     * The Chrominance channels (Cb and Cr) are initialized to 128.0
     * (neutral gray) to ensure valid color reconstruction.
     * @param width The width of the image in pixels.
     * @param height The height of the image in pixels.
     */
    public SpatialMatrix(int width, int height) {
        this.width = width;
        this.height = height;
        this.yChannel = new double[width][height];
        this.cbChannel = new double[width][height];
        this.crChannel = new double[width][height];

        // Default Cb and Cr to 128 (neutral chroma) so pure-grayscale reconstruction works
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cbChannel[x][y] = GRAY_SCALE_VALUE;
                crChannel[x][y] = GRAY_SCALE_VALUE;
            }
        }
    }

    /**
     * Creates a deep copy of an existing SpatialMatrix.
     * @param original The Image to copy
     */
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

    /**
     * Retrieves the Luminance (Y) value of a specific pixel.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return  The grayscale-equivalent value (0.0 to 255.0).
     */
    public double getGrayscalePixel(int x, int y) {
        return yChannel[x][y];
    }

    /**
     * Updates the Luminance (Y) value of a specific pixel.
     * Values are automatically clamped to the [0, 255] range.
     *
     * @param x     The x-coordinate.
     * @param y     The y-coordinate.
     * @param value The new luminance value.
     */
    public void setYChannel(int x, int y, double value) {
        yChannel[x][y] = Math.max(PIXEL_MIN_VALUE, Math.min(PIXEL_MAX_VALUE, value));
    }

    /**
     * Converts the YCbCr channels to a BufferedImage (single conversion on save).
     * @return {@link BufferedImage}
     */
    public BufferedImage saveImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double yVal = yChannel[x][y];
                double cb = cbChannel[x][y];
                double cr = crChannel[x][y];

                int r = clamp((int) Math.round(yVal + 1.40200 * (cr - GRAY_SCALE_VALUE)));
                int g = clamp((int) Math.round(yVal - 0.344136 * (cb - GRAY_SCALE_VALUE) - 0.714136 * (cr - GRAY_SCALE_VALUE)));
                int b = clamp((int) Math.round(yVal + 1.77200 * (cb - GRAY_SCALE_VALUE)));

                image.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return image;
    }

    @Deprecated
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
        BufferedImage image = saveImage();
        ImageIO.write(image, format, outputFile);

        System.out.println(">>> Image physically created at: " + outputFile.getAbsolutePath());
        return outputFile;
    }

    /**
     * Restores the color channels from a source matrix.
     * <p>
     * This is crucial after steganographic reconstruction, as the Genetic Algorithm
     * only processes the Y channel. This method re-attaches the original color.
     * </p>
     *
     * @param original The source matrix containing the original Cb and Cr data.
     */
    public void copyChromaFrom(SpatialMatrix original) {
        int w = Math.min(this.width, original.width);
        int h = Math.min(this.height, original.height);
        for (int x = 0; x < w; x++) {
            System.arraycopy(original.cbChannel[x], 0, this.cbChannel[x], 0, h);
            System.arraycopy(original.crChannel[x], 0, this.crChannel[x], 0, h);
        }
    }

    /**
     * Clamps an integer value to the valid range of an unsigned 8-bit byte.
     * @param value The value to be restricted.
     * @return      The value constrained between 0 and 255 inclusive.
     */
    private static int clamp(int value) {
        return Math.max(PIXEL_MIN_VALUE, Math.min(PIXEL_MAX_VALUE, value));
    }
}