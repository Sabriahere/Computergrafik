package ExerciseRayTracing;

import JavaVectors.Vector3;

/**
 * @author u244353 (Sabria Karim)
 * @since 9/22/2025
 */

public class Sphere {

    Vector3 center;
    float radius;
    Color color;
    Color diffuse;
    Color emission;

    public Sphere(Vector3 center, float radius, Color color) {
        this.center = center;
        this.radius = radius;
        this.color = color;
        diffuse = null;
        emission = null;
    }

    public Sphere(Vector3 center, float radius, Color diffuse, Color emission) {
        this.center = center;
        this.radius = radius;
        this.diffuse = diffuse;
        this.emission = emission;
        color = null;
    }

}