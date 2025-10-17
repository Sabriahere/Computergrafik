package ExerciseRayTracing;

/**
 * @author u244353 (Sabria Karim)
 * @since 10/13/2025
 */

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;

import JavaVectors.Vector3;

public class ImageTexture {

    private final int WIDTH;
    private final int HEIGHT;
    private final int[] PIXELS;

    public ImageTexture(String filename) {
        try {
            BufferedImage img = ImageIO.read(new File(filename));
            if (img == null) {
                throw new RuntimeException();
            }
            WIDTH = img.getWidth();
            HEIGHT = img.getHeight();
            PIXELS = img.getRGB(0, 0, WIDTH, HEIGHT, null, 0, WIDTH);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public Color get(double u, double v) {
        int x = (int) (u * (WIDTH - 1));
        int y = (int) (v * (HEIGHT - 1));

        int rgb = PIXELS[y * WIDTH + x];
        float r = ((rgb >> 16) & 255) / 255F;
        float g = ((rgb >> 8) & 255) / 255F;
        float b = ((rgb) & 255) / 255f;

        return gammaCorrection(r, g, b);
    }

    private Color gammaCorrection(float r, float g, float b) {
        double GAMMA = 2.2;
        r = (float) Math.pow(r, 1 / GAMMA);
        g = (float) Math.pow(g, 1 / GAMMA);
        b = (float) Math.pow(b, 1 / GAMMA);
        return new Color(r, g, b);
    }

}
