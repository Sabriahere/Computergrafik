package ExerciseHardwareAcceleration;

import Mesh.*;
import Mesh.Vector3;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

// add the .jar files as a library to the project not with maven

public class OpenGL {
    static final int WIDTH = 720;
    static final int HEIGHT = 480;
    static final float zNear = 0.1f;
    static final float zFar = 100.0f;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        // open a window
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        var hWindow = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "ComGr", 0, 0);
        GLFW.glfwSetWindowSizeCallback(hWindow, (window, width, height) -> {
            var w = new int[1];
            var h = new int[1];
            GLFW.glfwGetFramebufferSize(window, w, h);
            glViewport(0, 0, w[0], h[0]);
        });
        GLFW.glfwMakeContextCurrent(hWindow);
        GLFW.glfwSwapInterval(1);
        createCapabilities();

        // set up opengl
        if (GLFW.glfwExtensionSupported("GL_KHR_debug")) {
            GL43.glDebugMessageCallback(
                    GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
                        var msg = GLDebugMessageCallback.getMessage(length, message);
                        if (type == GL43.GL_DEBUG_TYPE_ERROR) {
                            throw new RuntimeException(msg);
                        } else {
                            System.out.println(msg);
                        }
                    }), 0);
            glEnable(GL43.GL_DEBUG_OUTPUT);
            glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        }
        glEnable(GL_FRAMEBUFFER_SRGB);
        glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
        glClearDepth(1.0);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glDisable(GL_CULL_FACE);

        // load, compile and link shaders
        // see https://www.khronos.org/opengl/wiki/Vertex_Shader

        /*
        inTime: Uniform (parameter per draw call)
        fromVertexShaderToFragmentShader: Output to fragment shader
        inPos: Input value per vertex
        inColor: per vertex color attribute (from color VBO)
        gl_Position: Transformed Position for FragmentShader [-1, 1]
         */
        var VertexShaderSource = """
                #version 400 core

                uniform mat4 uModel;
                uniform mat4 uView;
                uniform mat4 uProj;

                in vec3 inPos;
                in vec3 inColor;
                in vec2 inUV;
                in vec3 inNormal;

                out vec3 vColor;
                out vec2 vUV;
                out vec3 vWorldPos;
                out vec3 vWorldNormal;

                void main()
                {
                    vec4 worldPos = uModel * vec4(inPos, 1.0);
                    vWorldPos = worldPos.xyz;

                    // correct normal transform for rotations (ok for your current rotation-only model)
                    vWorldNormal = normalize(mat3(uModel) * inNormal);

                    vColor = inColor;
                    vUV = inUV;

                    gl_Position = uProj * uView * worldPos;
                }
                """;

        var hVertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(hVertexShader, VertexShaderSource);
        glCompileShader(hVertexShader);
        if (glGetShaderi(hVertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new Exception(glGetShaderInfoLog(hVertexShader));
        }

        // see https://www.khronos.org/opengl/wiki/Fragment_Shader

        /*
        fromVertexShaderToFragmentShader: user-defined output of the vertex shader,
        interpolated per fragment and used as input in the fragment shader
        outColor: user-defined fragment shader output variable
        chessboardTexture: TODO
         */
        var FragmentShaderSource = """
                #version 400 core

                in vec3 vColor;
                in vec2 vUV;
                in vec3 vWorldPos;
                in vec3 vWorldNormal;

                uniform sampler2D chessboardTexture;

                uniform vec3 uLightPos;
                uniform vec3 uCameraPos;

                out vec4 outColor;

                void main()
                {
                    vec3 albedo = texture(chessboardTexture, vUV).rgb;

                    vec3 N = normalize(vWorldNormal);
                    vec3 L = normalize(uLightPos - vWorldPos);
                    vec3 V = normalize(uCameraPos - vWorldPos);

                    // Diffuse
                    float diff = max(dot(N, L), 0.0);

                    // Specular (Phong)
                    vec3 R = reflect(-L, N);
                    float spec = pow(max(dot(V, R), 0.0), 64.0); // shininess = 64

                    vec3 ambient = 0.1 * albedo;
                    vec3 diffuse = diff * albedo;
                    vec3 specular = spec * vec3(1.0);

                    vec3 lit = ambient + diffuse + specular;

                    // Optional: keep your per-face tint
                    lit *= vColor;

                    outColor = vec4(lit, 1.0);
                }
                """;

        var hFragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(hFragmentShader, FragmentShaderSource);
        glCompileShader(hFragmentShader);
        if (glGetShaderi(hFragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new Exception(glGetShaderInfoLog(hFragmentShader));
        }

        // link shaders to a program
        var hProgram = glCreateProgram();
        glAttachShader(hProgram, hFragmentShader);
        glAttachShader(hProgram, hVertexShader);
        glLinkProgram(hProgram);
        if (glGetProgrami(hProgram, GL_LINK_STATUS) != GL_TRUE) {
            throw new Exception(glGetProgramInfoLog(hProgram));
        }

        int uModel = glGetUniformLocation(hProgram, "uModel");
        int uView = glGetUniformLocation(hProgram, "uView");
        int uProj = glGetUniformLocation(hProgram, "uProj");

        int uLightPos = glGetUniformLocation(hProgram, "uLightPos");
        int uCameraPos = glGetUniformLocation(hProgram, "uCameraPos");

        Mesh cubeMesh = Mesh.createCube(
                new Vector3(1, 0, 0),
                new Vector3(0, 1, 0),
                new Vector3(0, 0, 1),
                new Vector3(1, 1, 0),
                new Vector3(1, 0, 1),
                new Vector3(0, 1, 1));

        Mesh sphereMesh = Mesh.createSphere(16, new Vector3(1, 1, 1));

        float[] triangleVertices = new float[(cubeMesh.vertices.size() + sphereMesh.vertices.size()) * 3];
        int v = 0;
        for (var vert : cubeMesh.vertices) {
            triangleVertices[v++] = vert.position().x();
            triangleVertices[v++] = vert.position().y();
            triangleVertices[v++] = vert.position().z();
        }
        for (var vert : sphereMesh.vertices) {
            triangleVertices[v++] = vert.position().x();
            triangleVertices[v++] = vert.position().y();
            triangleVertices[v++] = vert.position().z();
        }

        // upload model vertices to a vbo
        var vboTriangleVertices = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTriangleVertices);
        glBufferData(GL_ARRAY_BUFFER, triangleVertices, GL_STATIC_DRAW);

        float[] triangleUVs = new float[(cubeMesh.vertices.size() + sphereMesh.vertices.size()) * 2];
        int t = 0;

        // cube UVs from mesh
        for (var vert : cubeMesh.vertices) {
            triangleUVs[t++] = vert.texCoord().x();
            triangleUVs[t++] = vert.texCoord().y();
        }

        // sphere UVs from mesh
        for (var vert : sphereMesh.vertices) {
            triangleUVs[t++] = vert.texCoord().x();
            triangleUVs[t++] = vert.texCoord().y();
        }

        int vboTriangleUVs = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTriangleUVs);
        glBufferData(GL_ARRAY_BUFFER, triangleUVs, GL_STATIC_DRAW);

        // upload model colors to a vbo
        float[] triangleColors = new float[(cubeMesh.vertices.size() + sphereMesh.vertices.size()) * 3];
        int c = 0;

        for (var vert : cubeMesh.vertices) {
            triangleColors[c++] = vert.color().x();
            triangleColors[c++] = vert.color().y();
            triangleColors[c++] = vert.color().z();
        }
        for (var vert : sphereMesh.vertices) {
            triangleColors[c++] = vert.color().x();
            triangleColors[c++] = vert.color().y();
            triangleColors[c++] = vert.color().z();
        }

        var vboTriangleColors = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTriangleColors);
        glBufferData(GL_ARRAY_BUFFER, triangleColors, GL_STATIC_DRAW);

        // upload model indices to a vbo (vertex buffer object, actual data in gpu)
        //var triangleIndices = new int[]{0, 1, 2};
        int cubeVertCount = cubeMesh.vertices.size();
        int cubeIndexCount = cubeMesh.triangles.size() * 3;

        int sphereIndexCount = sphereMesh.triangles.size() * 3;

        // correct total index array size
        int[] triangleIndices = new int[cubeIndexCount + sphereIndexCount];

        int i = 0;

        // cube indices first
        for (var tri : cubeMesh.triangles) {
            triangleIndices[i++] = tri.a();
            triangleIndices[i++] = tri.b();
            triangleIndices[i++] = tri.c();
        }

        // sphere indices appended WITH OFFSET
        for (var tri : sphereMesh.triangles) {
            triangleIndices[i++] = tri.a() + cubeVertCount;
            triangleIndices[i++] = tri.b() + cubeVertCount;
            triangleIndices[i++] = tri.c() + cubeVertCount;
        }

        float[] triangleNormals = new float[(cubeMesh.vertices.size() + sphereMesh.vertices.size()) * 3];
        int n = 0;

        for (var vert : cubeMesh.vertices) {
            triangleNormals[n++] = vert.normal().x();
            triangleNormals[n++] = vert.normal().y();
            triangleNormals[n++] = vert.normal().z();
        }
        for (var vert : sphereMesh.vertices) {
            triangleNormals[n++] = vert.normal().x();
            triangleNormals[n++] = vert.normal().y();
            triangleNormals[n++] = vert.normal().z();
        }

        int vboTriangleNormals = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTriangleNormals);
        glBufferData(GL_ARRAY_BUFFER, triangleNormals, GL_STATIC_DRAW);

        // TODO: add textures
        int hTexture1 = addTextureObject("/ExerciseHardwareAcceleration/chessboard.png");
        int hTexture2 = addTextureObject("/ExerciseHardwareAcceleration/water.png");

        var vboTriangleIndices = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, triangleIndices, GL_STATIC_DRAW);

        // set up a vao (vertex array objects, where each in variable is read from)
        var vaoTriangle = glGenVertexArrays();
        glBindVertexArray(vaoTriangle);
        var posAttribIndex = glGetAttribLocation(hProgram, "inPos");

        int normalAttribIndex = glGetAttribLocation(hProgram, "inNormal");
        if (normalAttribIndex != -1) {
            glEnableVertexAttribArray(normalAttribIndex);
            glBindBuffer(GL_ARRAY_BUFFER, vboTriangleNormals);
            glVertexAttribPointer(normalAttribIndex, 3, GL_FLOAT, false, 0, 0L);
        }

        int uvAttribIndex = glGetAttribLocation(hProgram, "inUV");
        if (uvAttribIndex != -1) {
            glEnableVertexAttribArray(uvAttribIndex);
            glBindBuffer(GL_ARRAY_BUFFER, vboTriangleUVs);
            glVertexAttribPointer(uvAttribIndex, 2, GL_FLOAT, false, 0, 0L);
        }

        var colorAttribIndex = glGetAttribLocation(hProgram, "inColor");
        if (colorAttribIndex != -1) {
            glEnableVertexAttribArray(colorAttribIndex);
            glBindBuffer(GL_ARRAY_BUFFER, vboTriangleColors);
            glVertexAttribPointer(colorAttribIndex, 3, GL_FLOAT, false, 0, 0L);
        }

        if (posAttribIndex != -1) {
            glEnableVertexAttribArray(posAttribIndex);
            glBindBuffer(GL_ARRAY_BUFFER, vboTriangleVertices);
            glVertexAttribPointer(posAttribIndex, 3, GL_FLOAT, false, 0, 0);
        }
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);

        // check for errors during all previous calls
        var error = glGetError();
        if (error != GL_NO_ERROR) {
            throw new Exception(Integer.toString(error));
        }

        // render loop
        var startTime = System.currentTimeMillis();
        while (!GLFW.glfwWindowShouldClose(hWindow)) {
            // clear screen and z-buffer
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // switch to our shader
            glUseProgram(hProgram);

            glUseProgram(hProgram);

            // B) set per-frame uniforms
            Matrix4x4 V = createView();
            Matrix4x4 P = createProj();
            glUniformMatrix4fv(uView, false, V.toArray());
            glUniformMatrix4fv(uProj, false, P.toArray());

            // world-space light and camera position (match the V you used)
            glUniform3f(uLightPos, 0f, 0f, 20.0f);
            glUniform3f(uCameraPos, 0.0f, 0.0f, 10.0f);

            //TODO: setup texture
            int tiuIndex = 1;
            glActiveTexture(GL_TEXTURE0 + tiuIndex);
            glBindTexture(GL_TEXTURE_2D, hTexture1);
            int loc = glGetUniformLocation(hProgram, "chessboardTexture");
            if (loc == -1) {
                throw new RuntimeException("Uniform chessboardTexture not found");
            }
            glUniform1i(loc, tiuIndex);

            // set uniform values
            float time = (float) (System.currentTimeMillis() - startTime) * 0.001f;
            glUniform1f(glGetUniformLocation(hProgram, "inTime"), time);

            glBindVertexArray(vaoTriangle);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);

            Vector3[] cubePos = new Vector3[]{
                    new Vector3(-3.0f, 0.0f, 0.0f),
                    new Vector3(0.0f, -3.0f, 0.0f),
                    new Vector3(3.0f, 0.0f, 0.0f),
                    new Vector3(0.0f, 3.0f, 0.0f),
            };

            glBindTexture(GL_TEXTURE_2D, hTexture1);
            for (int k = 0; k < 4; k++) {
                Matrix4x4 M = createModel(time, cubePos[k], k * 3.0f);
                glUniformMatrix4fv(uModel, false, M.toArray());
                glDrawElements(GL_TRIANGLES, cubeIndexCount, GL_UNSIGNED_INT, 0L);
            }

            Vector3 spherePos = new Vector3(0.0f, 0.0f, 0.0f);
            long sphereIndexOffsetBytes = (long) cubeIndexCount * Integer.BYTES;

            glBindTexture(GL_TEXTURE_2D, hTexture2);
            Matrix4x4 sphereM = createModel(time, spherePos, 3.0f);
            glUniformMatrix4fv(uModel, false, sphereM.toArray());
            glDrawElements(GL_TRIANGLES, sphereIndexCount, GL_UNSIGNED_INT, sphereIndexOffsetBytes);

            // display
            GLFW.glfwSwapBuffers(hWindow);
            GLFW.glfwPollEvents();

            error = glGetError();
            if (error != GL_NO_ERROR) {
                throw new Exception(Integer.toString(error));
            }
        }

        GLFW.glfwDestroyWindow(hWindow);
        GLFW.glfwTerminate();
    }

    private static Matrix4x4 createModel(float time, Vector3 pos, float phase) {
        Matrix4x4 R = Matrix4x4.createRotationY(time + phase).multiply(Matrix4x4.createRotationX(time * 0.7f + phase));
        Matrix4x4 T = Matrix4x4.createTranslation(pos.x(), pos.y(), pos.z());
        return T.multiply(R);
    }

    private static Matrix4x4 createView() {
        return Matrix4x4.createTranslation(0.0f, 0.0f, -10.0f);
    }

    private static Matrix4x4 createProj() {
        float aspect = (float) WIDTH / (float) HEIGHT;
        float fov = (float) Math.toRadians(60.0f);
        float f = 1.0f / (float) Math.tan(fov * 0.5f);

        return new Matrix4x4(
                f / aspect, 0, 0, 0,
                0, f, 0, 0,
                0, 0, (zFar + zNear) / (zNear - zFar), -1,
                0, 0, (2 * zFar * zNear) / (zNear - zFar), 0
        );
    }

    private static int addTextureObject(String resourcePath) {
        int hTextures = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, hTextures);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST); //if one pixel covers multiple screen pixels how is the color picked

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR); //if many pixels map to one screen pixel how is the color picked
        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) { //anisotropic filtering activated if available
            float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);
        }

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1); //how is pixel data laid out in cpu memory when uploading to gpu, no padding bytes between rows of image
        try {
            uploadTextureImage(Objects.requireNonNull(OpenGL.class.getResourceAsStream(resourcePath)));
        } catch (IOException e) {
            System.out.println("Failed to upload texture image: " + e.getMessage());
        }
        glBindTexture(GL_TEXTURE_2D, 0);
        return hTextures;
    }

    private static void uploadTextureImage(InputStream is) throws IOException {
        // decode PNG -> raw RGB bytes
        try (is) {
            byte[] fileBytes = is.readAllBytes();

            ByteBuffer fileBuffer = BufferUtils.createByteBuffer(fileBytes.length);
            fileBuffer.put(fileBytes).flip();

            IntBuffer w = BufferUtils.createIntBuffer(1);
            IntBuffer h = BufferUtils.createIntBuffer(1);
            IntBuffer comp = BufferUtils.createIntBuffer(1);

            ByteBuffer pixelData = stbi_load_from_memory(fileBuffer, w, h, comp, 3);
            if (pixelData == null) {
                throw new RuntimeException(stbi_failure_reason());
            }

            int texWidth = w.get(0);
            int texHeight = h.get(0);

            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_SRGB8,
                    texWidth,
                    texHeight,
                    0,
                    GL_RGB,
                    GL_UNSIGNED_BYTE,
                    pixelData
            ); //0 -> mipmap level, 0 is base image
            glGenerateMipmap(GL_TEXTURE_2D);
            stbi_image_free(pixelData);
        }
    }
}
