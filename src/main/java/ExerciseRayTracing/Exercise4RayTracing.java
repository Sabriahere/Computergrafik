package ExerciseRayTracing;

import JavaVectors.Vector2;
import JavaVectors.Vector3;

import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * @author u244353 (Sabria Karim)
 * @since 10/13/2025
 */
public class Exercise4RayTracing {

    int width = 1600;
    int height = 900;
    final double RATIO = (double) width / height;
    final float p = 0.05f;

    Scene s;
    Vector3 up = new Vector3(0, 1, 0);

    Vector3 eye = new Vector3(3, 5, -4);
    Vector3 lookAt = new Vector3(-1, -3, 6);
    ;
    float FOV = 36;

    // for better performance, my pc is dying :(
    private static final int MAX_DEPTH = 5;
    private static final int RAYS = 128;

    public Exercise4RayTracing(Scene s) {
        this.s = s;
    }

    public void generateEyeRays() {
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            System.out.println("at " + y + " of " + height);

            final int yy = y; // to use in the threads

            IntStream.range(0, width).parallel().unordered().forEach(x -> {
                Color acc = Color.BLACK;
                ThreadLocalRandom rng = ThreadLocalRandom.current();

                for (int repeat = 0; repeat < RAYS; repeat++) {
                    double jx = rng.nextDouble();
                    double jy = rng.nextDouble();

                    double nx = (2.0 * (x + jx) / width) - 1.0; // left=-1, right=+1
                    double ny = 1.0 - (2.0 * (yy + jy) / height); // top=+1, bottom=-1
                    nx *= RATIO;

                    ArrayList<Vector3> vectors =
                        createEyeRay(eye, lookAt, FOV, new Vector2((float) nx, (float) ny));
                    HitPoint hitPoint =
                        findClosestHitPoint(s, vectors.getFirst(), vectors.getLast());

                    acc = acc.add(computeColor(s, vectors.getFirst(), vectors.getLast(), hitPoint));
                }

                Color avg = acc.multiply(1f / RAYS); // average samples
                pixels[yy * width + x] = avg.toARGB();
            });
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
            float c = Vector3.dot(oc, oc) - currentSphere.radius * currentSphere.radius;
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
        return computeColor(s, o, d, hitPoint, 0);
    }

    Color computeColor(Scene s, Vector3 o, Vector3 d, HitPoint hitPoint, int depth) {
        if (hitPoint == null) {
            return Color.GRAY;
        } else if (hitPoint.sphere == null) {
            return Color.BLACK;
        }

        Color hpEmission = hitPoint.sphere.emission;

        // hard cutoff at max depth
        if (depth >= MAX_DEPTH) {
            return hpEmission;
        }

        Vector3 n = Vector3.normalize(hitPoint.coordinate.subtract(hitPoint.sphere.center));
        if (Vector3.dot(n, d) > 0) {
            n = n.multiply(-1f); // to face n against d
        }

        hpEmission = (hitPoint.sphere.emission != null) ? hitPoint.sphere.emission : Color.BLACK;
        if (ThreadLocalRandom.current().nextDouble() < p) {
            return hpEmission;
        }

        // sample random direction
        Vector3 wr = generateRandomVector(n);

        // next bounce
        Vector3 origin = hitPoint.coordinate.add(n.multiply(1e-4f));
        HitPoint next = findClosestHitPoint(s, origin, wr);
        Color bounce = computeColor(s, origin, wr, next, depth + 1);

        // brdf
        Color brdf = brdf(hitPoint, d, wr).multiply((2 * Math.PI) * Vector3.dot(wr, n) / (1f - p));

        return hpEmission.add(brdf.multiply(bounce));
    }

    private Vector3 generateRandomVector(Vector3 n) {
        Vector3 random;
        float length;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        do {
            float x = (float) (2.0 * rng.nextDouble() - 1.0);
            float y = (float) (2.0 * rng.nextDouble() - 1.0);
            float z = (float) (2.0 * rng.nextDouble() - 1.0);

            random = new Vector3(x, y, z);
            length = random.length();
        } while (length > 1f || length < 1e-8f); // if it's outside the unit circle or too small

        random = Vector3.normalize(random);

        if (Vector3.dot(n, random) < 0f) {
            random = random.multiply(-1f);  // to face random same direction as n
        }
        return random;
    }

    private Color brdf(HitPoint hitPoint, Vector3 d, Vector3 wr) {
        d = Vector3.normalize(d);
        wr = Vector3.normalize(wr);

        Vector3 n = Vector3.normalize(hitPoint.coordinate.subtract(hitPoint.sphere.center));

        double x = n.x();
        double y = n.y();
        double z = n.z();

        double u = Math.atan2(z, x) / (2.0 * Math.PI) + 0.5;
        double v = Math.acos(Math.max(-1.0, Math.min(1.0, y))) / Math.PI;

        if (Vector3.dot(n, d) > 0) {
            n = n.multiply(-1f); // to face n against d
        }

        Vector3 m = n.multiply(Vector3.dot(d, n));
        Vector3 dr = Vector3.normalize(d.add(m.multiply(-2f)));

        //add texture
        Color diffuse = (hitPoint.sphere.texture != null) ? hitPoint.sphere.texture.get(u, v) : hitPoint.sphere.diffuse;

        if (Vector3.dot(wr, dr) > 1 - 0.01f) {
            return diffuse.add(hitPoint.sphere.specular.multiply(10f));
        }

        return diffuse;
    }
}
