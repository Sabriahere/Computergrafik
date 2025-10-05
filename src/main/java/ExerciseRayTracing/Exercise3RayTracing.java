package ExerciseRayTracing;

import JavaVectors.Vector2;
import JavaVectors.Vector3;

import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;

/**
 * @author u244353 (Sabria Karim)
 * @since 9/22/2025
 */
public class Exercise3RayTracing {

    int width = 600;
    int height = 400;

    Scene s = new Scene(true);
    Vector3 up = new Vector3(0, 1, 0);

    Vector3 eye;
    Vector3 lookAt;
    float FOV;

    /*
    Vector3 eye = new Vector3(0, 0, -4);
    Vector3 lookAt = new Vector3(0, 0, 6);
    float FOV = 36;
     */

    public Exercise3RayTracing(Vector3 eye, Vector3 lookAt, float FOV) {
        this.eye = eye;
        this.lookAt = lookAt;
        this.FOV = FOV;
    }


    public void generateEyeRays() {
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                double nx = (2.0 * (x + 0.5) / width) - 1.0;   // left = -1, right = +1
                double ny = 1.0 - (2.0 * (y + 0.5) / height); // top = +1, bottom = -1

                // aspect ratio correction
                nx *= (double) width / height;

                ArrayList<Vector3> vectors = (createEyeRay(eye, lookAt, FOV, new Vector2((float) nx, (float) ny)));
                HitPoint hitPoint = findClosestHitPoint(s, vectors.getFirst(), vectors.getLast());
                pixels[y * width + x] = computeColor(s, vectors.getFirst(), vectors.getLast(), hitPoint).toARGB();
            }
        }

        // create & show image to display
        Image img = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(width, height, pixels, 0, width));
        Frame f = new Frame() {
            public void paint(Graphics g) {
                g.drawImage(img, 0, 0, this);
            }
        };
        f.setSize(width, height);
        f.setVisible(true);
    }

    ArrayList<Vector3> createEyeRay(Vector3 eye, Vector3 lookAt, float FOV, Vector2 pixel) {
        Vector3 f = Vector3.normalize(lookAt.subtract(eye));
        Vector3 r = Vector3.normalize(Vector3.cross(up, f));
        Vector3 u = Vector3.normalize(Vector3.cross(f, r));

        float s = (float) Math.tan(Math.toRadians(FOV * 0.5f));
        Vector3 d = Vector3.normalize(f.add(r.multiply(pixel.x() * s)).add(u.multiply(pixel.y() * s)));

        ArrayList<Vector3> ray = new ArrayList<>();
        ray.add(eye);
        ray.add(d);
        return ray;
    }

    HitPoint findClosestHitPoint(Scene s, Vector3 o, Vector3 d) {
        ArrayList<Sphere> sphereList = s.getSphereList();
        HitPoint closestHitPoint = new HitPoint(null, d, Float.MAX_VALUE, null);

        for (Sphere currentSphere : sphereList) {
            Vector3 oc = o.subtract(currentSphere.center);
            float a = Vector3.dot(d, d);
            float b = 2.0f * Vector3.dot(d, oc);
            float c = oc.length() * oc.length() - currentSphere.radius * currentSphere.radius;
            float discriminant = b * b - 4 * a * c;

            if (discriminant > 0) {
                float sqrt = (float) Math.sqrt(discriminant);

                float lambda1 = (-b - sqrt) / (2.0f * a);
                float lambda2 = (-b + sqrt) / (2.0f * a);
                float lambda = lambda1 > 0 ? lambda1 : (lambda2 > 0 ? lambda2 : Float.NaN);

                if (!Float.isNaN(lambda) && lambda < closestHitPoint.distance) {
                    closestHitPoint.coordinate = o.add(d.multiply(lambda));
                    closestHitPoint.distance = lambda;
                    closestHitPoint.sphere = currentSphere;
                }
            }
        }
        return closestHitPoint;
    }

    Color computeColor(Scene s, Vector3 o, Vector3 d, HitPoint hitPoint) {
        if (hitPoint == null) {
            return Color.GRAY;
        } else if (hitPoint.sphere == null) {
            return Color.BLACK;
        }

        if (hitPoint.sphere.color == null) {

            Vector3 n = Vector3.normalize(hitPoint.coordinate.subtract(hitPoint.sphere.center));
            Vector3 wr = generateRandomVector(n);

            //TODO implement brdf
            return hitPoint.sphere.emission.add(brdf(hitPoint, d, wr));
        }
        return hitPoint.sphere.color;
    }

    private Vector3 generateRandomVector(Vector3 n) {
        Vector3 random;
        float length;
        do {
            float x = (float) (2.0 * Math.random() - 1.0);
            float y = (float) (2.0 * Math.random() - 1.0);
            float z = (float) (2.0 * Math.random() - 1.0);

            random = new Vector3(x, y, z);
            length = random.length();
        } while (length > 1f || length < 1e-8f);

        random = Vector3.normalize(random);

        if (Vector3.dot(n, random) < 0f) {
            random = random.multiply(-1f);
        }
        return random;
    }

    private Color brdf(HitPoint hitPoint, Vector3 d, Vector3 wr) {
        d = Vector3.normalize(d);
        wr = Vector3.normalize(wr);

        Vector3 n = Vector3.normalize(hitPoint.coordinate.subtract(hitPoint.sphere.center));
        if (Vector3.dot(n, d) > 0) n = n.multiply(-1f); // to face n against d

        Vector3 m = n.multiply(Vector3.dot(d, n));
        Vector3 dr = Vector3.normalize(d.add(m.multiply(-2f)));

        Color diffuse = hitPoint.sphere.diffuse.multiply((float) (1.0f / Math.PI));

        if (Vector3.dot(wr, dr) > 1 - 0.01f) {
            return diffuse.add(hitPoint.sphere.diffuse.multiply(10f));
        }

        return diffuse;
    }
}