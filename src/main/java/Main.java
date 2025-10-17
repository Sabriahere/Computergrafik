import ExerciseRayTracing.Color;
import ExerciseRayTracing.Exercise4RayTracing;
import ExerciseRayTracing.ImageTexture;
import ExerciseRayTracing.Scene;
import ExerciseRayTracing.Sphere;
import JavaVectors.Vector3;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;


public class Main {

    public static void main(String[] args) {
        System.out.println("Hello world!");

        Scene s = createNewScene();

        Exercise1LinearRGB ex1 = new Exercise1LinearRGB();
        ex1.createGradient();

        Instant start = Instant.now();
        Exercise4RayTracing ex4 = new Exercise4RayTracing(s);
        ex4.generateEyeRays();
        Instant end = Instant.now();
        Duration elapsed = Duration.between(start, end);

        long minutes = elapsed.toMinutes();
        long secondsPart = elapsed.minusMinutes(minutes).getSeconds();
        double secondsExact = elapsed.toNanos() / 1e9;

        System.out.printf("Render time: %d min %d s  (%.3f s exact)%n", minutes, secondsPart, secondsExact);

    }

    // cool scene
    private static Scene createNewScene() {

        ArrayList<Sphere> spheres = new ArrayList<>();

        // light source
        //TODO: find better light placement
        spheres.add(new Sphere(new Vector3(500, -800, 500), 800, Color.WHITE, Color.WHITE2, Color.BLACK));
        spheres.add(new Sphere(new Vector3(-1400, 200, 600), 700F, Color.WHITE, Color.WHITE2, Color.BLACK));

        // spheres
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            Color color = Color.colorList.get(random.nextInt(0, Color.colorList.size()));

            for (int j = 0; j < 4; j++) {
                Sphere sphere = new Sphere(new Vector3(-1.5 + i, -0.5, 0.5 + j), 0.4F, color.multiply(0.7f).add(Color.WHITE.multiply(0.3f)), Color.BLACK, Color.WHITE.multiply(0.25 + 0.25 * j));
                if (i % 2 == 0) {
                    sphere.texture = new ImageTexture("src/main/resources/ExerciseRayTracing/chestboard.png");
                } else {
                    sphere.texture = new ImageTexture("src/main/resources/ExerciseRayTracing/pumpkin.png");
                }
                spheres.add(sphere);
            }
        }
        return new Scene(spheres);
    }

}