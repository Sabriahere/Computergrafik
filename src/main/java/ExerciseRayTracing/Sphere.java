package ExerciseRayTracing;

import JavaVectors.Vector3;

/**
 * @author u244353 (Sabria Karim)
 * @since 9/22/2025
 */

public class Sphere {

    Vector3 center;
    float radius;
    Color diffuse;
    Color emission;
    Color specular;

    public Sphere(Vector3 center, float radius, Color diffuse, Color emission, Color specular) {
        this.center = center;
        this.radius = radius;
        this.diffuse = diffuse;
        this.emission = emission;
        this.specular = specular;
    }

}