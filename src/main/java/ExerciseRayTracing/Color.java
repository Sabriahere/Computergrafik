package ExerciseRayTracing;

/**
 * @author u244353 (Sabria Karim)
 * @since 9/22/2025
 */
public enum Color {
    RED(0xFFFF0000),
    BLUE(0xFF0000FF),
    YELLOW(0xFFFFFF00),
    WHITE(0xFFFFFFFF),
    WHITE2(0xFFFFFFFF),
    GRAY(0xFF808080),
    LIGHTCYAN(0xFF00FFFF),
    BLACK(0xFF000000);

    private final int color;

    Color(int code) {
        this.color = code;
    }

    public int getColor() {
        return color;
    }
}

