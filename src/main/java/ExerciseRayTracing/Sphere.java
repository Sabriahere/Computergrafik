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
    Color specular = Color.WHITE;
    public boolean mirror = false;            // reflective object?

    public Sphere(Vector3 center, float radius, Color color) {
        this.center = center;
        this.radius = radius;
        this.color = color;
        diffuse = null;
        emission = null;
        specular = null;
    }

    public Sphere(Vector3 center, float radius, Color diffuse, Color emission, Color specular) {
        this.center = center;
        this.radius = radius;
        this.diffuse = diffuse;
        this.emission = emission;
        this.specular = specular;
        color = null;
    }

}