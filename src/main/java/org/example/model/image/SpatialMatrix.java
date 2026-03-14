package org.example.model.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SpatialMatrix {
    private final BufferedImage image;

    // Constructor 1: Load an existing image from the hard drive
    public SpatialMatrix(String filePath) throws IOException {
        File file = new File(filePath);
        this.image = ImageIO.read(file);
    }

    // Constructor 2: Create a blank image
    public SpatialMatrix(int width, int height) {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public SpatialMatrix(SpatialMatrix original) {
        this.image = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.image.getGraphics().drawImage(original.image, 0, 0, null);
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    // Returns standard Grayscale Luminance directly
    public double getGrayscalePixel(int x, int y) {
        int rgb = image.getRGB(x, y);

        // Bit-shifting to extract the colors
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        return (red * 0.299) + (green * 0.587) + (blue * 0.114);
    }

    public void setPixel(int x, int y, int red, int green, int blue) {
        // Re-pack the colors into a single 32-bit integer
        int rgb = (red << 16) | (green << 8) | blue;
        image.setRGB(x, y, rgb);
    }

    public void setYChannel(int x, int y, double newy) {
        int rgb = image.getRGB(x, y);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        double cb = -0.168736 * red - 0.331264 * green + 0.5 * blue + 128.0;
        double cr = 0.5 * red - 0.418688 * green - 0.081312 * blue + 128.0;

        double newY_clamped = Math.max(0, Math.min(255, newy));
        int newR = (int) Math.round(Math.max(0, Math.min(255, newY_clamped + 1.40200 * (cr - 128))));
        int newG = (int) Math
                .round(Math.max(0, Math.min(255, newY_clamped - 0.344136 * (cb - 128) - 0.714136 * (cr - 128))));
        int newB = (int) Math.round(Math.max(0, Math.min(255, newY_clamped + 1.77200 * (cb - 128))));

        this.setPixel(x, y, newR, newG, newB);
    }

    public double getBluePixel(int x, int y) {
        int rgb = image.getRGB(x, y);
        return (double) (rgb & 0xFF);
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

        // 4. Write the image to disk
        ImageIO.write(this.image, format, outputFile);

        System.out.println(">>> Image physically created at: " + outputFile.getAbsolutePath());
        return outputFile;
    }
}