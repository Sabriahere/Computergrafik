package ExerciseSoftwareRendering;

import Mesh.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.List;

public class Exercise6 {

    float angle = 0.0f;
    int width = 600;
    int height = 400;
    int[] pixels = new int[width * height];
    private final ArrayList<int[]> faces = new ArrayList<>();
    private final ArrayList<Vector3> faceColors = new ArrayList<>();

    Vector2 A = new Vector2(100, 100);
    Vector2 B = new Vector2(300, 200);
    Vector2 C = new Vector2(100, 300);

    public void render2DTriangles() {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g.clearRect(0, 0, this.getWidth(), this.getHeight());
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
                renderWireframeMesh();

                for (int i = 0; i < faces.size(); i++) {
                    int[] f = faces.get(i);
                    Vector3 c = faceColors.get(i);
                    g.setColor(new Color(c.x(), c.y(), c.z()));
                    g.fillPolygon(new int[]{f[0], f[2], f[4]}, new int[]{f[1], f[3], f[5]}, 3);
                }
                repaint();
            }
        };

        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setVisible(true);
    }

    public void renderWireframeMesh() {
        angle += 0.001f;
        if (angle > Math.PI * 2) {
            angle -= (float) (Math.PI * 2);
        }

        Mesh mesh = Mesh.createCube(
                new Vector3(1, 0, 0),//red
                new Vector3(0, 1, 0),//green
                new Vector3(0, 0, 1),//blue
                new Vector3(1, 1, 0),//yellow
                new Vector3(1, 0, 1),//magenta
                new Vector3(0, 1, 1) //cyan
        );

        List<Vertex> vertices = mesh.vertices;
        List<Tri> tris = mesh.triangles;
        faces.clear();
        faceColors.clear();

        for (Tri tri : tris) {
            Vertex a = vertexShader(vertices.get(tri.a()));
            Vertex b = vertexShader(vertices.get(tri.b()));
            Vertex c = vertexShader(vertices.get(tri.c()));

            // if vertex is behind the camera, no division by 0 -> ignore this triangle, perspective divide
            if (a.position().w() <= 0 || b.position().w() <= 0 || c.position().w() <= 0) {
                continue;
            }

            // vertex projection
            Vertex ap = vertexProjection(a);
            Vertex bp = vertexProjection(b);
            Vertex cp = vertexProjection(c);

            // ndc -> pixel
            Vector2 pixelA = ndcToPixels(ap);
            Vector2 pixelB = ndcToPixels(bp);
            Vector2 pixelC = ndcToPixels(cp);

            // draw vertices -> put into edges
            int ax = (int) pixelA.x(), ay = (int) pixelA.y();
            int bx = (int) pixelB.x(), by = (int) pixelB.y();
            int cx = (int) pixelC.x(), cy = (int) pixelC.y();

            // Backface Culling with 2D cross product of the triangle edges, (AB x AC) = n, n_z > 0 => CW we need this
            // if n_z < 0 => back facing -> we skip
            if ((bx - ax) * (cy - ay) - (by - ay) * (cx - ax) <= 0) {
                continue;
            }

            faces.add(new int[]{ax, ay, bx, by, cx, cy});
            faceColors.add(a.color());
        }
    }

    Vertex vertexShader(Vertex v) {
        Matrix4x4 MVP = createMVP();
        Vector4 p = v.position();
        Vector4 position = new Vector4(
                MVP.m11() * p.x() + MVP.m21() * p.y() + MVP.m31() * p.z() + MVP.m41() * p.w(),
                MVP.m12() * p.x() + MVP.m22() * p.y() + MVP.m32() * p.z() + MVP.m42() * p.w(),
                MVP.m13() * p.x() + MVP.m23() * p.y() + MVP.m33() * p.z() + MVP.m43() * p.w(),
                MVP.m14() * p.x() + MVP.m24() * p.y() + MVP.m34() * p.z() + MVP.m44() * p.w()
        );
        return new Vertex(position, v.worldCoordinates(), v.color(), v.texCoord(), v.normal());
    }

    Vertex vertexProjection(Vertex v) {
        float vw = 1.0f / v.position().w();
        return new Vertex(v.position().multiply(vw), v.worldCoordinates(), v.color(), v.texCoord(), v.normal());
    }

    Vector2 ndcToPixels(Vertex v) {
        float x = (v.position().x() * 0.5f + 0.5f) * width;
        float y = (-v.position().y() * 0.5f + 0.5f) * height;
        return new Vector2(x, y);
    }

    private Matrix4x4 createMVP() {
        Matrix4x4 M1 = Matrix4x4.createRotationY(angle);
        //Matrix4x4 M2 = Matrix4x4.createRotationX(angle);
        Matrix4x4 V = Matrix4x4.createLookAt(new Vector3(0, -3, -4), new Vector3(0, 0, 0), new Vector3(0, -1, 0));
        float zNear = 0.1f;
        float zFar = 100.0f;
        Matrix4x4 P = Matrix4x4.createPerspectiveFieldOfView(1.57f, (1.0f * width) / height, zNear, zFar);
        //return Matrix4x4.multiply(M1.multiply(M2), V, P);
        return Matrix4x4.multiply(M1, V, P);
    }

    public void exerciseInClass() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                List<Double> uvList = calculateUV(x + 0.5, y + 0.5); // + 0.5 for some smoother edges
                if (uvList == null) {
                    continue;
                }

                double u = uvList.get(0);
                double v = uvList.get(1);

                if (u >= 0 && v >= 0 && (u + v) < 1) {
                    pixels[y * width + x] = ExerciseRayTracing.Color.RED.toARGB();
                }
            }
        }

        Image img = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(width, height, pixels, 0, width));
        Frame f = new Frame() {
            public void paint(Graphics g) {
                g.drawImage(img, 0, 0, this);
            }
        };
        f.setSize(width, height);
        f.setVisible(true);
    }

    private List<Double> calculateUV(double x, double y) {
        Vector2 AB = B.subtract(A);
        Vector2 AC = C.subtract(A);

        double det = AB.x() * AC.y() - AC.x() * AB.y();
        if (Math.abs(det) < 1e-8) { // ignore these triangles
            return null;
        }

        double invDet = 1.0 / det;

        double u = (AC.y() * (x - A.x()) - AC.x() * (y - A.y())) * invDet;
        double v = (-AB.y() * (x - A.x()) + AB.x() * (y - A.y())) * invDet;

        return List.of(u, v);
    }
}
