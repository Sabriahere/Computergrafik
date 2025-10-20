import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Mesh.*;
import Mesh.Vector2;
import Mesh.Vector3;

/**
 * @author u244353 (Sabria Karim)
 * @since 10/13/2025
 */
public class Exercise5 {

    static int i = 0;
    int width = 600;
    int height = 400;
    int[] pixels = new int[width * height];
    private List<int[]> edges = new ArrayList<>();


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

                g.setColor(Color.WHITE);
                for (int[] e : edges) {
                    g.drawLine(e[0], e[1], e[2], e[3]);
                }

            }
        };

        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setVisible(true);
    }

    public void renderWireframeMesh() {

        Vector3 color = new Vector3(100, 100, 100);

        Mesh mesh = Mesh.createCube(color, color, color, color, color, color);
        List<Vertex> vertices = mesh.vertices;
        List<Tri> tris = mesh.triangles;
        edges.clear();

        for (Tri tri : tris) {
            // vertex shader
            Vertex a = vertexShader(vertices.get(tri.a()));
            Vertex b = vertexShader(vertices.get(tri.b()));
            Vertex c = vertexShader(vertices.get(tri.c()));

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

            edges.add(new int[]{ax, ay, bx, by});
            edges.add(new int[]{bx, by, cx, cy});
            edges.add(new int[]{cx, cy, ax, ay});
        }

        render2DTriangles();
    }

    Vertex vertexShader(Vertex v) {
        Vector4 position = new Vector4(v.position().x(), v.position().y(), 0, v.position().z() + 4);
        return new Vertex(position, v.worldCoordinates(), v.color(), v.texCoord(), v.normal());
    }

    Vertex vertexProjection(Vertex v) {
        float vw = 1.0f / v.position().w();
        return new Vertex(v.position().multiply(vw), v.worldCoordinates(), v.color(), v.texCoord(), v.normal());
    }

    Vector2 ndcToPixels(Vertex v) {
        float x = (float) (v.position().x() * width / 2.0 + width / 2.0);
        float y = (float) (v.position().y() * width / 2.0 + height / 2.0);
        return new Vector2(x, y);
    }

    public void exerciseInClass() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                List<Double> uvList = calculateUV(x, y);
                int u = uvList.get(0).intValue();
                int v = uvList.get(1).intValue();

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

    private List<Double> calculateUV(int x, int y) {
        Vector2 AB = B.subtract(A);
        Vector2 AC = C.subtract(A);

        double inverseParameter = 1.0 / (AB.x() * AC.y() - AC.x() * AB.y());

        double u = AC.y() * (x - A.x()) - AC.x() * (y - A.y());
        double v = -AB.y() * (x - A.x()) + AB.x() * (y - A.y());

        List<Double> uvListe = new ArrayList<>();
        uvListe.add(u * inverseParameter);
        uvListe.add(v * inverseParameter);

        return uvListe;
    }

}
