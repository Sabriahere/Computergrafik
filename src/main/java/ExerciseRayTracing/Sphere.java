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
    public ImageTexture texture;


    public Sphere(Vector3 center, float radius, Color diffuse, Color emission, Color specular) {
        this.center = center;
        this.radius = radius;
        this.diffuse = diffuse;
        this.emission = emission;
        this.specular = specular;
        texture = null;
    }

    public Sphere(Vector3 center, float radius, Color diffuse, Color emission, Color specular, ImageTexture texture) {
        this.center = center;
        this.radius = radius;
        this.diffuse = diffuse;
        this.emission = emission;
        this.specular = specular;
        this.texture = texture;
    }

    public void changeColor(Color color) {
        this.diffuse = color;
    }

}