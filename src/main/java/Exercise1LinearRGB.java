
import JavaVectors.Vector3;

import java.awt.*;
import java.awt.image.MemoryImageSource;

public class Exercise1LinearRGB {
    int width = 600;
    int height = 400;
    double gamma = 2.2;


    public void createGradient() {

        // calculate each color vector
        int[] pixels = new int[width * height];
        Vector3 red = new Vector3(1, 0, 0);
        Vector3 green = new Vector3(0, 1, 0);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                // Color Interpolation
                float lambda = (float) x / (width - 1);
                Vector3 point = Vector3.lerp(red, green, lambda);

                // Linear RGB + Gamma Correction -> sRGB
                pixels[y * width + x] = rgb(gammaCorrection(point));
            }
        }

        // create & show image to display
        Image img = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(width, height, pixels, 0, width));
        Frame f = new Frame() {
            public void paint(Graphics g) {
                g.drawImage(img, 0, 0, this);
            }
        };
        f.setSize(width, height);
        f.setVisible(true);
    }

    private int rgb(Vector3 v) {
        int r = (int) (v.x() * 255);
        int g = (int) (v.y() * 255);
        int b = (int) (v.z() * 255);
        return 0xFF000000 | (r << 16) | (g << 8) | b; // alpha=255
    }

    private Vector3 gammaCorrection(Vector3 point) {
        double r = Math.pow(point.x(), 1 / gamma);
        double g = Math.pow(point.y(), 1 / gamma);
        double b = Math.pow(point.z(), 1 / gamma);
        return new Vector3(r, g, b);
    }

}
