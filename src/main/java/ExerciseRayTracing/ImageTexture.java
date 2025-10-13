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
    private final double GAMMA = 2.2;

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

        return new Color(r, g, b);
    }

    //TODO: implement gammaCorrection
    private Vector3 gammaCorrection(Vector3 point) {
        double r = Math.pow(point.x(), 1 / GAMMA);
        double g = Math.pow(point.y(), 1 / GAMMA);
        double b = Math.pow(point.z(), 1 / GAMMA);
        return new Vector3(r, g, b);
    }

}
