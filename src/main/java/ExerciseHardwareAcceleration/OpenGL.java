package ExerciseHardwareAcceleration;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;

import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import JavaVectors.Matrix4x4;

// add the .jar files as a library to the project not with maven

public class OpenGL {

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
        var hWindow = GLFW.glfwCreateWindow(720, 480, "ComGr", 0, 0);
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
        // glClearDepth(1);
        // glDisable(GL_DEPTH_TEST);
        // glDepthFunc(GL_LESS);
        // glDisable(GL_CULL_FACE);

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
                            
                in float inTimeOffset;
                                
                void main()
                {
                    float t = inTime + inTimeOffset;
                    gl_Position = vec4(inPos, 1.0) + vec4(sin(t) * 0.5, cos(t) * 0.5, 0.0, 0.0);
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

        // upload model vertices to a vbo
        //var triangleVertices = new float[]{0.0f, -0.5f, 0.0f, 0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0.0f};
        var triangleVertices = new float[]{
                // Rect A (v0..v3)
                -0.5f, -0.5f, 0.0f,   // 0
                0.5f, -0.5f, 0.0f,   // 1
                0.5f, 0.5f, 0.0f,   // 2
                -0.5f, 0.5f, 0.0f, // 3

                // Rect B (v4..v7) shifted inward
                -0.5f, -0.5f, 0.0f,   // 4
                0.5f, -0.5f, 0.0f,   // 5
                0.5f, 0.5f, 0.0f,   // 6
                -0.5f, 0.5f, 0.0f, // 7
        };


        var vboTriangleVertices = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTriangleVertices);
        glBufferData(GL_ARRAY_BUFFER, triangleVertices, GL_STATIC_DRAW);

        // upload model colors to a vbo
        var triangleColors = new float[]{
                // Rect A
                1.0f, 0.0f, 0.0f,  // 0 bottom-left  = red
                0.0f, 1.0f, 0.0f,  // 1 bottom-right = green
                1.0f, 0.0f, 0.0f,  // 2 top-right    = red
                0.0f, 0.0f, 1.0f,  // 3 top-left     = blue

                // Rect B (same scheme)
                1.0f, 0.0f, 0.0f,  // 4
                0.0f, 1.0f, 0.0f,  // 5
                1.0f, 0.0f, 0.0f,  // 6
                0.0f, 0.0f, 1.0f   // 7
        };


        var vboTriangleColors = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTriangleColors);
        glBufferData(GL_ARRAY_BUFFER, triangleColors, GL_STATIC_DRAW);

        // upload model indices to a vbo (vertex buffer object, actual data in gpu)
        //var triangleIndices = new int[]{0, 1, 2};
        var triangleIndices = new int[]{
                // Rect A (0..3)
                0, 1, 2, 0, 2, 3,

                // Rect B (4..7)
                4, 5, 6, 4, 6, 7
        };
        var vboTriangleIndices = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, triangleIndices, GL_STATIC_DRAW);

        var triangleTimeOffsets = new float[]{
                0.0f, 0.0f, 0.0f, 0.0f,  // rect A vertices
                1.0f, 1.0f, 1.0f, 1.0f   // rect B vertices (phase shift +1)
        };

        var vboTriangleTimeOffsets = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTriangleTimeOffsets);
        glBufferData(GL_ARRAY_BUFFER, triangleTimeOffsets, GL_STATIC_DRAW);


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

        var timeOffsetAttribIndex = glGetAttribLocation(hProgram, "inTimeOffset");
        if (timeOffsetAttribIndex != -1) {
            glEnableVertexAttribArray(timeOffsetAttribIndex);
            glBindBuffer(GL_ARRAY_BUFFER, vboTriangleTimeOffsets);
            glVertexAttribPointer(timeOffsetAttribIndex, 1, GL_FLOAT, false, 0, 0L);
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
            glUniform1f(glGetUniformLocation(hProgram, "inTime"), (float) (System.currentTimeMillis() - startTime) * 0.001f);

            // TODO: add MVP
            var someMatrix = Matrix4x4.IDENTITY;
            glUniformMatrix4fv(glGetUniformLocation(hProgram, "inMatrix"), false, someMatrix.toArray());

            // render our model
            glBindVertexArray(vaoTriangle);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboTriangleIndices);
            glDrawElements(GL_TRIANGLES, triangleIndices.length, GL_UNSIGNED_INT, 0);

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
}
