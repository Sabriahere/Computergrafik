package ExerciseHardwareAcceleration;

import Mesh.*;
import Mesh.Vector3;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;

import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

// add the .jar files as a library to the project not with maven

public class OpenGL {
    static final int WIDTH = 720;
    static final int HEIGHT = 480;
    static final float zNear = 0.1f;
    static final float zFar = 100.0f;
    static final Vector3 cameraPos = new Vector3(0, 0, -6);


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
            org.lwjgl.opengl.GL43.glDebugMessageCallback(
                    GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
                        var msg = GLDebugMessageCallback.getMessage(length, message);
                        if (type == org.lwjgl.opengl.GL43.GL_DEBUG_TYPE_ERROR) {
                            throw new RuntimeException(msg);
                        } else {
                            System.out.println(msg);
                        }
                    }), 0);
            glEnable(org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT);
            glEnable(org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
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
        inTime & inMatrix: Uniform (parameter per draw call)
        fromVertexShaderToFragmentShader: Output to fragment shader
        inPos: Input value per vertex
        inColor: per vertex color attribute (from color VBO)
        gl_Position: Transformed Position for FragmentShader [-1, 1]
         */
        var VertexShaderSource = """
                #version 400 core
                            
                uniform float inTime;
                uniform mat4 inMatrix;
                out vec3 fromVertexShaderToFragmentShader;
                in vec3 inPos;
                in vec3 inColor;
                                                            
                void main()
                {
                    gl_Position = inMatrix * vec4(inPos, 1.0);
                   
                    fromVertexShaderToFragmentShader = inColor;
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
         */
        var FragmentShaderSource = """
                #version 400 core

                in vec3 fromVertexShaderToFragmentShader;
                out vec4 outColor;

                void main()
                {
                    float s = 0.9 + 0.1 * sin(gl_FragCoord.x * 0.05);
                    outColor = vec4(fromVertexShaderToFragmentShader * s, 1.0);
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

        Mesh cubeMesh = Mesh.createCube(
                new Vector3(1, 0, 0),
                new Vector3(0, 1, 0),
                new Vector3(0, 0, 1),
                new Vector3(1, 1, 0),
                new Vector3(1, 0, 1),
                new Vector3(0, 1, 1));

        Mesh sphereMesh = Mesh.createSphere(16, new Vector3(1, 0, 0));

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


        var vboTriangleIndices = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, triangleIndices, GL_STATIC_DRAW);


        // set up a vao (vertex array objects, where each in variable is read from)
        var vaoTriangle = glGenVertexArrays();
        glBindVertexArray(vaoTriangle);
        var posAttribIndex = glGetAttribLocation(hProgram, "inPos");

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

            // set uniform values
            float time = (float) (System.currentTimeMillis() - startTime) * 0.001f;
            glUniform1f(glGetUniformLocation(hProgram, "inTime"), time);

            glBindVertexArray(vaoTriangle);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);

            int uMatrix = glGetUniformLocation(hProgram, "inMatrix");

            Vector3[] cubePos = new Vector3[]{
                    new Vector3(-3.0f, 0.0f, 0.0f),
                    new Vector3(0.0f, -3.0f, 0.0f),
                    new Vector3(3.0f, 0.0f, 0.0f),
                    new Vector3(0.0f, 3.0f, 0.0f),
            };


            for (int k = 0; k < 4; k++) {
                Matrix4x4 mvp = createMVP(time, cubePos[k], k * 3.0f);
                glUniformMatrix4fv(uMatrix, false, mvp.toArray());
                glDrawElements(GL_TRIANGLES, cubeIndexCount, GL_UNSIGNED_INT, 0L);
            }

            Vector3 spherePos = new Vector3(0.0f, 0.0f, 0.0f);
            Matrix4x4 mvp = createMVP(time, spherePos, 3.0f);
            glUniformMatrix4fv(uMatrix, false, mvp.toArray());
            long sphereIndexOffsetBytes = (long) cubeIndexCount * Integer.BYTES;
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

    private static Matrix4x4 createMVP(float time, Vector3 pos, float phase) {
        Matrix4x4 M =
                Matrix4x4.createRotationY(time + phase)
                        .multiply(Matrix4x4.createRotationX(time * 0.7f + phase));
        Matrix4x4 T = Matrix4x4.createTranslation(pos.x(), pos.y(), pos.z());
        M = T.multiply(M);

        Matrix4x4 V = Matrix4x4.createTranslation(0.0f, 0.0f, -10.0f);

        float aspect = (float) WIDTH / (float) HEIGHT;
        float fov = (float) Math.toRadians(60.0f);
        float f = 1.0f / (float) Math.tan(fov * 0.5f);
        Matrix4x4 P = new Matrix4x4(
                f / aspect, 0, 0, 0,
                0, f, 0, 0,
                0, 0, (zFar + zNear) / (zNear - zFar), -1,
                0, 0, (2 * zFar * zNear) / (zNear - zFar), 0
        );

        return M.multiply(V).multiply(P);
    }


}
