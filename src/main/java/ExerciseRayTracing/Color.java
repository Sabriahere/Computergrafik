package ExerciseRayTracing;

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


    public int toARGB() {
        int a = 0xFF;
        int rr = (int) (Math.min(1f, r) * 255);
        int gg = (int) (Math.min(1f, g) * 255);
        int bb = (int) (Math.min(1f, b) * 255);
        return (a << 24) | (rr << 16) | (gg << 8) | bb;
    }

}
