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

    public void saveImage(String outputPath, String format) throws IOException {
        File outputFile = new File(outputPath);
        ImageIO.write(this.image, format, outputFile);
    }

}