package org.example.model.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SpatialMatrix {
    // We use the built-in, highly optimized library class to hold the data
    private final BufferedImage image;

    // Constructor 1: Load an existing image from the hard drive
    public SpatialMatrix(String filePath) throws IOException {
        File file = new File(filePath);
        this.image = ImageIO.read(file);
    }

    // Constructor 2: Create a blank image (useful for the IDCT when rebuilding)
    public SpatialMatrix(int width, int height) {
        // TYPE_INT_RGB is the standard 24-bit color matrix
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    // Abstract the AWT library away from the rest of your app
    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    // Returns standard Grayscale Luminance directly, saving us time in the ImageProcessor
    public double getGrayscalePixel(int x, int y) {
        int rgb = image.getRGB(x, y);

        // Bit-shifting to extract the colors (super fast O(1) operation)
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        return (red * 0.299) + (green * 0.587) + (blue * 0.114);
    }

    // Set a pixel (used when rebuilding the Stego-Object)
    public void setPixel(int x, int y, int red, int green, int blue) {
        // Re-pack the colors into a single 32-bit integer
        int rgb = (red << 16) | (green << 8) | blue;
        image.setRGB(x, y, rgb);
    }

    // Save the final image back to the hard drive
    public void saveImage(String outputPath, String format) throws IOException {
        File outputFile = new File(outputPath);
        ImageIO.write(this.image, format, outputFile);
    }
}