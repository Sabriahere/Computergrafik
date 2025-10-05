package ExerciseRayTracing;

import JavaVectors.Vector3;

public class HitPoint {

    Vector3 coordinate;
    Vector3 d;
    float distance;
    Sphere sphere;

    public HitPoint(Vector3 coordinate, Vector3 d, float distance, Sphere sphere) {
        this.coordinate = coordinate;
        this.d = d;
        this.distance = distance;
        this.sphere = sphere;
    }
}
