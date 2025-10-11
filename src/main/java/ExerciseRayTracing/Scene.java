package ExerciseRayTracing;

import JavaVectors.Vector3;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author u244353 (Sabria Karim)
 * @since 9/22/2025
 */
public class Scene {

    ArrayList<Sphere> sphereList;
    Sphere a;
    Sphere b;
    Sphere c;
    Sphere d;
    Sphere e;
    Sphere f;
    Sphere g;

    public Scene() {
        a = new Sphere(new Vector3(-1001, 0, 0), 1000, Color.RED, Color.BLACK, Color.BLACK);
        b = new Sphere(new Vector3(1001, 0, 0), 1000, Color.BLUE, Color.BLACK, Color.BLACK);
        c = new Sphere(new Vector3(0, 0, 1001), 1000, Color.GRAY, Color.BLACK, Color.BLACK);
        d = new Sphere(new Vector3(0, -1001, 0), 1000, Color.GRAY, Color.BLACK, Color.BLACK);
        e = new Sphere(new Vector3(0, 1001, 0), 1000, Color.WHITE, Color.WHITE2, Color.BLACK);
        // so it doesnt reflect all of its color and look unnatural
        f = new Sphere(new Vector3(-0.6, -0.7, -0.6), 0.3F, Color.YELLOW.multiply(0.7f).add(Color.WHITE.multiply(0.3f)), Color.BLACK, Color.WHITE);
        g = new Sphere(new Vector3(0.3, -0.4, 0.3), 0.6F, Color.LIGHTCYAN.multiply(0.7f).add(Color.WHITE.multiply(0.3f)), Color.BLACK, Color.WHITE);
        sphereList = new ArrayList<>(Arrays.asList(a, b, c, d, e, f, g));
    }

    public ArrayList<Sphere> getSphereList() {
        return sphereList;
    }
}