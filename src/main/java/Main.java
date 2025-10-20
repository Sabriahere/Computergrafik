import ExerciseRayTracing.*;
import JavaVectors.Vector3;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;


public class Main {

    public static void main(String[] args) {
        System.out.println("Hello world!");

        Scene s = createNewSceneExercise4();
        Instant start = Instant.now();

        // Exercise 1
        Exercise1LinearRGB ex1 = new Exercise1LinearRGB();
        //ex1.createGradient();

        // Exercise 2 is in the other Project folder!

        // Exercise 3
        Exercise3RayTracing ex3 = new Exercise3RayTracing(new Vector3(0, 0, -4), new Vector3(0, 0, 6), 36);
        //ex3.generateEyeRays();

        // Exercise 4
        Exercise4RayTracing ex4 = new Exercise4RayTracing(s);
        //ex4.generateEyeRays();

        // Exercise 5
        Exercise5 ex5 = new Exercise5();
        //ex5.render2DTriangles();
        //ex5.exerciseInClass();
        ex5.render2DTriangles();

        Instant end = Instant.now();
        Duration elapsed = Duration.between(start, end);

        long minutes = elapsed.toMinutes();
        long secondsPart = elapsed.minusMinutes(minutes).getSeconds();
        double secondsExact = elapsed.toNanos() / 1e9;

        System.out.printf("Render time: %d min %d s  (%.3f s exact)%n", minutes, secondsPart, secondsExact);

    }

    // cool scene
    private static Scene createNewSceneExercise4() {

        ArrayList<Sphere> spheres = new ArrayList<>();

        // light source
        spheres.add(new Sphere(new Vector3(500, -800, 500), 800, Color.WHITE, Color.WHITE2, Color.BLACK));
        spheres.add(new Sphere(new Vector3(-1400, 200, 600), 700F, Color.WHITE, Color.WHITE2, Color.BLACK));

        // spheres
        Random random = new Random();
        for (int i = 0; i < 4; i++) {

            for (int j = 0; j < 4; j++) {
                Sphere sphere = new Sphere(new Vector3(-1.5 + i, -0.5, 0.5 + j), 0.4F, Color.WHITE, Color.BLACK, Color.WHITE.multiply(0.25 + 0.25 * j));
                if (i % 4 == 0) {
                    sphere.texture = new ImageTexture("src/main/resources/ExerciseRayTracing/chessboard.png");
                } else if (i % 4 == 1) {
                    sphere.texture = new ImageTexture("src/main/resources/ExerciseRayTracing/stripes.png");
                } else if (i % 4 == 2) {
                    sphere.texture = new ImageTexture("src/main/resources/ExerciseRayTracing/water.png");
                } else {
                    sphere.changeColor(Color.RED.multiply(0.9f).add(Color.WHITE.multiply(0.1f)));
                }
                spheres.add(sphere);
            }
        }
        return new Scene(spheres);
    }

}