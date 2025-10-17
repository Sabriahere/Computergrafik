package ExerciseRayTracing;

import java.util.ArrayList;
import java.util.Arrays;

public class Color {

    float r, g, b;

    public Color(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static final Color RED = new Color(1f, 0f, 0f);
    public static final Color BLUE = new Color(0f, 0f, 1f);
    public static final Color YELLOW = new Color(1f, 1f, 0f);
    public static final Color WHITE = new Color(1f, 1f, 1f);
    public static final Color WHITE2 = new Color(2f, 2f, 2f);
    public static final Color GRAY = new Color(0.5f, 0.5f, 0.5f);
    public static final Color LIGHTCYAN = new Color(0f, 1f, 1f);
    public static final Color BLACK = new Color(0f, 0f, 0f);
    public static final ArrayList<Color> colorList = new ArrayList<>(Arrays.asList(RED, BLUE, YELLOW, GRAY, LIGHTCYAN)); // used to try out spheres


    public int toARGB() {
        float gamma = 1.0f / 2.2f;
        int a = 0xFF;

        // min-max because of odd brightness at some spots
        int rr = (int) (Math.pow(Math.max(0f, Math.min(1f, r)), gamma) * 255);
        int gg = (int) (Math.pow(Math.max(0f, Math.min(1f, g)), gamma) * 255);
        int bb = (int) (Math.pow(Math.max(0f, Math.min(1f, b)), gamma) * 255);

        return (a << 24) | (rr << 16) | (gg << 8) | bb;
    }


    public Color multiply(float number) {
        return new Color(r * number, g * number, b * number);
    }

    public Color multiply(double number) {
        return new Color((float) (r * number), (float) (g * number), (float) (b * number));
    }

    public Color multiply(Color other) {
        return new Color(r * other.r, g * other.g, b * other.b);
    }


    public Color add(Color other) {
        return new Color(r + other.r, g + other.g, b + other.b);
    }

    public Color add(float number) {
        return new Color(r + number, g + number, b + number);
    }

}
