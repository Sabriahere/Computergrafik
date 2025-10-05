import ExerciseRayTracing.Exercise2RayTracing;
import ExerciseRayTracing.Exercise3RayTracing;
import JavaVectors.Vector3;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello world!");

        Exercise1LinearRGB ex1 = new Exercise1LinearRGB();
        //ex1.createGradient();

        Exercise2RayTracing ex2_1 = new Exercise2RayTracing(new Vector3(0, 0, -4), new Vector3(0, 0, 6), 36);
        //ex2_1.generateEyeRays();

        Exercise2RayTracing ex2_2 = new Exercise2RayTracing(new Vector3(-0.9, -0.5, 0.9), new Vector3(0, 0, 0), 110);
        //ex2_2.generateEyeRays();

        Exercise3RayTracing ex3 = new Exercise3RayTracing(new Vector3(0, 0, -4), new Vector3(0, 0, 6), 36);
        ex3.generateEyeRays();

    }
}