import ExerciseRayTracing.Exercise2RayTracing;
import ExerciseRayTracing.Exercise3RayTracing;
import JavaVectors.Vector3;

import java.time.Duration;
import java.time.Instant;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello world!");

        Exercise1LinearRGB ex1 = new Exercise1LinearRGB();
        //ex1.createGradient();

        Exercise2RayTracing ex2_1 = new Exercise2RayTracing(new Vector3(0, 0, -4), new Vector3(0, 0, 6), 36);
        //ex2_1.generateEyeRays();

        Exercise2RayTracing ex2_2 = new Exercise2RayTracing(new Vector3(-0.9, -0.5, 0.9), new Vector3(0, 0, 0), 110);
        //ex2_2.generateEyeRays();

        Instant start = Instant.now();
        Exercise3RayTracing ex3 = new Exercise3RayTracing(new Vector3(0, 0, -4), new Vector3(0, 0, 6), 36);
        ex3.generateEyeRays();
        Instant end = Instant.now();
        Duration elapsed = Duration.between(start, end);

        long minutes = elapsed.toMinutes();
        long secondsPart = elapsed.minusMinutes(minutes).getSeconds();
        double secondsExact = elapsed.toNanos() / 1e9;

        System.out.printf("Render time: %d min %d s  (%.3f s exact)%n",
                minutes, secondsPart, secondsExact);

    }
}