package ExerciseSoftwareRendering;

/**
 * @author u244353 (Sabria Karim)
 * @since 10/13/2025
 */

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import Mesh.Vector3;

public class ImageTexture {

    public final int WIDTH;
    public final int HEIGHT;
    private final Vector3[] data;

    public ImageTexture(String filename) {
        try {
            BufferedImage img = ImageIO.read(new File(filename));
            WIDTH = img.getWidth();
            HEIGHT = img.getHeight();
            data = new Vector3[WIDTH * HEIGHT];

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {

                    int rgb = img.getRGB(x, y);

                    float sr = ((rgb >> 16) & 255) / 255f;
                    float sg = ((rgb >> 8) & 255) / 255f;
                    float sb = (rgb & 255) / 255f;

                    // gamma decode: sRGB -> linear
                    float r = (float) Math.pow(sr, 2.2);
                    float g = (float) Math.pow(sg, 2.2);
                    float b = (float) Math.pow(sb, 2.2);

                    data[y * WIDTH + x] = new Vector3(r, g, b);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load texture");
        }
    }

    public Vector3 sampleNearest(float u, float v) {
        int x = (int) (u * (WIDTH - 1));
        int y = (int) (v * (HEIGHT - 1));
        return data[y * WIDTH + x];
    }

    public Vector3 sampleBilinear(float u, float v) {
        u = u - (float) Math.floor(u);
        v = v - (float) Math.floor(v);

        float x = u * (WIDTH - 1);
        float y = v * (HEIGHT - 1);

        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        int x1 = Math.min(x0 + 1, WIDTH - 1);
        int y1 = Math.min(y0 + 1, HEIGHT - 1);

        float tx = x - x0;
        float ty = y - y0;

        Vector3 c00 = data[y0 * WIDTH + x0];
        Vector3 c10 = data[y0 * WIDTH + x1];
        Vector3 c01 = data[y1 * WIDTH + x0];
        Vector3 c11 = data[y1 * WIDTH + x1];

        Vector3 c0 = c00.multiply(1 - tx).add(c10.multiply(tx));
        Vector3 c1 = c01.multiply(1 - tx).add(c11.multiply(tx));

        return c0.multiply(1 - ty).add(c1.multiply(ty));
    }
}

