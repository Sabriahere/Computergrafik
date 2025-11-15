package ExerciseSoftwareRendering;

import Mesh.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.List;

public class Exercise7 {

    float angle = 0.0f;
    int width = 600;
    int height = 400;

    float zNear = 0.1f;
    float zFar = 100.0f;

    Vector3 lightPos = new Vector3(3, 3, -3);
    Vector3 lightColor = new Vector3(1, 1, 1);
    Vector3 cameraPos = new Vector3(0, -3, -4);

    float ambientFactor = 0.1f;
    float diffuseFactor = 0.9f;
    float specularFactor = 0.5f;
    float k = 32.0f;

    float[] zBuffer = new float[width * height];

    SceneGraphNode root;
    Matrix4x4 currentModel;
    Matrix4x4 currentMVP;

    int[] pixels = new int[width * height];

    public void render2DTriangles() {
        root = initScene();
        JFrame frame = new JFrame();
        JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g.clearRect(0, 0, this.getWidth(), this.getHeight());
                g.setColor(java.awt.Color.BLACK);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());

                renderWireframeMeshWithRasterization();

                Image img = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(width, height, pixels, 0, width));
                g.drawImage(img, 0, 0, this);
            }
        };

        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setVisible(true);

        Timer timer = new Timer(16, e -> panel.repaint()); // ~60 FPS
        timer.start();
    }

    public void renderWireframeMeshWithRasterization() {
        Arrays.fill(zBuffer, 1.0f);
        Arrays.fill(pixels, 0xFF000000);

        angle += 0.01f;
        if (angle > Math.PI * 2) {
            angle -= (float) (Math.PI * 2);
        }

        Matrix4x4 view = createV();
        Matrix4x4 proj = createP();
        Matrix4x4 viewProjection = Matrix4x4.multiply(view, proj);

        if (root != null) {
            root.render(Matrix4x4.IDENTITY, viewProjection, this);
        }
    }

    void renderMesh(Mesh mesh) {
        List<Vertex> vertices = mesh.vertices;
        List<Tri> tris = mesh.triangles;

        for (Tri tri : tris) {
            Vertex a = vertexShader(vertices.get(tri.a()));
            Vertex b = vertexShader(vertices.get(tri.b()));
            Vertex c = vertexShader(vertices.get(tri.c()));

            // if all vertices are behind the camera, ignore this triangle
            // (previous code skipped the triangle if ANY vertex was behind the camera,
            // which causes aggressive clipping for triangles that are partially visible)
            if (a.position().w() <= 0 && b.position().w() <= 0 && c.position().w() <= 0) {
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

            int ax = (int) pixelA.x(), ay = (int) pixelA.y();
            int bx = (int) pixelB.x(), by = (int) pixelB.y();
            int cx = (int) pixelC.x(), cy = (int) pixelC.y();

            // Backface culling
            if ((bx - ax) * (cy - ay) - (by - ay) * (cx - ax) <= 0) {
                continue;
            }

            // Rasterization
            rasterization(pixelA, pixelB, pixelC, ap, bp, cp);
        }
    }

    Vertex vertexShader(Vertex v) {
        Matrix4x4 M = currentModel;
        Matrix4x4 MVP = currentMVP;

        Vector4 clipPos = MVP.multiply(v.position());
        Vector4 worlds = M.multiply(v.position());
        Vector3 worldPos = new Vector3(worlds.x(), worlds.y(), worlds.z());
        Vector3 worldNormal = Vector3.normalize(Vector3.transformNormal(v.normal(), M));

        return new Vertex(clipPos, worldPos, v.color(), v.texCoord(), worldNormal);
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
        Matrix4x4 M = createM();
        Matrix4x4 V = createV();
        Matrix4x4 P = createP();
        return Matrix4x4.multiply(M, V, P);
    }

    private Matrix4x4 createM() {
        return Matrix4x4.createRotationY(angle);
    }

    private Matrix4x4 createV() {
        return Matrix4x4.createLookAt(cameraPos, new Vector3(0, 0, 0), new Vector3(0, -1, 0));
    }

    private Matrix4x4 createP() {
        return Matrix4x4.createPerspectiveFieldOfView(1.57f, (1.0f * width) / height, zNear, zFar);
    }

    public void rasterization(Vector2 A, Vector2 B, Vector2 C, Vertex ap, Vertex bp, Vertex cp) {
        float u, v, w;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                List<Float> uvList = calculateUV(x + 0.5, y + 0.5, A, B, C); // + 0.5 for some smoother edges
                if (uvList == null) {
                    continue;
                }

                u = uvList.get(0);
                v = uvList.get(1);
                w = uvList.get(2);

                if (u >= 0 && v >= 0 && (u + v) < 1) {
                    Vertex q = ap.multiply(w).add(bp.multiply(u)).add(cp.multiply(v));

                    // Z-Buffer
                    int index = y * width + x;
                    float z = q.position().z();          // NDC z in [-1,1]
                    float zMod = (z + 1.0f) * 0.5f;      // [0,1], near=0, far=1

                    if (zMod < zBuffer[index]) {
                        zBuffer[index] = zMod;

                        Vector3 shadedColor = fragmentShader(q);
                        pixels[index] = Color.trans(shadedColor).toARGB();
                    }
                }
            }
        }
    }

    private List<Float> calculateUV(double x, double y, Vector2 A, Vector2 B, Vector2 C) {
        Vector2 AB = B.subtract(A);
        Vector2 AC = C.subtract(A);

        double det = AB.x() * AC.y() - AC.x() * AB.y();
        if (Math.abs(det) < 1e-8) { // ignore these triangles
            return null;
        }

        double invDet = 1.0 / det;

        float u = (float) ((AC.y() * (x - A.x()) - AC.x() * (y - A.y())) * invDet);
        float v = (float) ((-AB.y() * (x - A.x()) + AB.x() * (y - A.y())) * invDet);
        float w = 1 - u - v;

        return List.of(u, v, w);
    }

    private Vector3 fragmentShader(Vertex q) {
        Vector3 baseColor = q.color();
        Vector3 p = q.worldCoordinates();
        Vector3 n = Vector3.normalize(q.normal());

        Vector3 l = Vector3.normalize(lightPos.subtract(p));// to light
        Vector3 r = Vector3.normalize(cameraPos.subtract(p));// to camera

        // Emission
        Vector3 ambient = baseColor.multiply(ambientFactor);

        // Diffuse Lambert
        float nDotL = Math.max(0.0f, Vector3.dot(n, l));
        Vector3 diffuse = baseColor.multiply(diffuseFactor * nDotL);

        // Specular Phong
        Vector3 R = n.multiply(2.0f * nDotL).subtract(l);
        float rV = Math.max(0.0f, Vector3.dot(R, r));
        float spec = (float) Math.pow(rV, k);
        Vector3 specular = lightColor.multiply(specularFactor * spec);

        // Combine
        Vector3 color = ambient.add(diffuse).add(specular);

        return new Vector3(
                Math.min(1.0f, Math.max(0.0f, color.x())),
                Math.min(1.0f, Math.max(0.0f, color.y())),
                Math.min(1.0f, Math.max(0.0f, color.z()))
        );
    }

    private SceneGraphNode initScene() {
        SceneGraphNode root = new SceneGraphNode(null, Matrix4x4.IDENTITY);

        // Cube 1
        Mesh cubeMesh1 = Mesh.createCube(
                new Vector3(1, 0, 0),
                new Vector3(0, 1, 0),
                new Vector3(0, 0, 1),
                new Vector3(1, 1, 0),
                new Vector3(1, 0, 1),
                new Vector3(0, 1, 1));
        SceneGraphNode cubeNode1 =
                new SceneGraphNode(cubeMesh1, Matrix4x4.createRotationY(angle));

        // Cube 2
        Mesh cubeMesh2 = Mesh.createCube(
                new Vector3(1, 0, 0),
                new Vector3(0, 1, 0),
                new Vector3(0, 0, 1),
                new Vector3(1, 1, 0),
                new Vector3(1, 0, 1),
                new Vector3(0, 1, 1));
        SceneGraphNode cubeNode2 =
                new SceneGraphNode(cubeMesh2, Matrix4x4.createTranslation(-3, 0, 0));

        // Cube 3
        Mesh cubeMesh3 = Mesh.createCube(
                new Vector3(0, 0, 1),
                new Vector3(1, 0, 0),
                new Vector3(1, 1, 0),
                new Vector3(1, 0, 1),
                new Vector3(0, 1, 0),
                new Vector3(0, 1, 1));
        SceneGraphNode cubeNode3 =
                new SceneGraphNode(cubeMesh3, Matrix4x4.createTranslation(-3, 1, 1));

        // Sphere
        Mesh sphereMesh = Mesh.createSphere(16, new Vector3(1, 0, 0));
        SceneGraphNode sphereNode = new SceneGraphNode(sphereMesh, Matrix4x4.createTranslation(3, 0, 0));

        root.children.add(cubeNode1);
        root.children.add(sphereNode);
        root.children.add(cubeNode2);
        root.children.add(cubeNode3);

        return root;
    }


}
