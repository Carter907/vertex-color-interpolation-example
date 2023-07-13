
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL33.*;
import org.lwjgl.system.MemoryUtil
import java.nio.file.FileSystems
import java.nio.file.Files

var window = -1L;
var programRef = -1;

fun main(args: Array<String>) {

    GLFWErrorCallback.createPrint(System.err).set()

    check(glfwInit()) {
        "failed to init glfw"
    }

    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)

    window = glfwCreateWindow(500,500, "window", MemoryUtil.NULL, MemoryUtil.NULL);

    check(window != MemoryUtil.NULL) {
        "failed to create window"
    }
    glfwSetKeyCallback(window) {
        window, key, scancode, action, mods ->

        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
    }
    glfwSetFramebufferSizeCallback(window) {
        window, width, height ->

        glViewport(0,0, width, height)
    }

    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    glfwShowWindow(window);



    GL.createCapabilities();


    programRef = glCreateProgram();

    val fragmentShaderCode = getShaderCode("src/main/resources/shaders/frag.glsl");
    val vertexShaderCode = getShaderCode("src/main/resources/shaders/vert.glsl")
    val fragmentShaderRef = initShader(fragmentShaderCode, GL_FRAGMENT_SHADER)
    val vertexShaderRef = initShader(vertexShaderCode, GL_VERTEX_SHADER)

    glAttachShader(programRef, fragmentShaderRef)
    glAttachShader(programRef, vertexShaderRef)

    glLinkProgram(programRef)
    val linkStatus = IntArray(1)
    glGetProgramiv(programRef, GL_LINK_STATUS, linkStatus)
    check(linkStatus[0] == GLFW_TRUE) {
        val message = glGetProgramInfoLog(programRef)

        glDeleteProgram(programRef)

        message;
    }
    // start on vertex array and buffers
    glPointSize(10f)
    val vaoRef = glGenVertexArrays();
    glBindVertexArray(vaoRef);

    val triangleVertices = floatArrayOf(
        0f, .5f, 0f,
        -.5f, -.5f, 0f,
        .5f, -.5f, 0f

    )

    val vertBufferRef = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, vertBufferRef)
    glBufferData(GL_ARRAY_BUFFER, triangleVertices, GL_STATIC_DRAW)

    // linking the vertex array to the pos shader variable;

    val posVaribRef = glGetAttribLocation(programRef, "pos")

    check (posVaribRef != -1) {

        "could not find variable name"
    }
//    glBindBuffer(GL_ARRAY_BUFFER, bufferRef);
    glVertexAttribPointer(posVaribRef, 3, GL_FLOAT, false, 0, 0)

    glEnableVertexAttribArray(posVaribRef)

    val vertexColors = floatArrayOf(
        1f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 1f,

    )
    val verColBufferRef = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, verColBufferRef)
    glBufferData(GL_ARRAY_BUFFER, vertexColors, GL_STATIC_DRAW)

    val vertColorVari = glGetAttribLocation(programRef, "vertColor")

    glVertexAttribPointer(vertColorVari, 3, GL_FLOAT, false, 0, 0)

    glEnableVertexAttribArray(vertColorVari)

    glClearColor(0f,0f,0f,1f)

    while(!glfwWindowShouldClose(window)) {
        glfwSwapBuffers(window)

        glClear(GL_COLOR_BUFFER_BIT .or (GL_DEPTH_BUFFER_BIT))

        glUseProgram(programRef)

        glDrawArrays(GL_TRIANGLES, 0, 3)

        glfwPollEvents();
    }

    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);

    glfwSetErrorCallback(null)!!.free();
    glfwTerminate();


}

fun initShader(sourceCode: String, type: Int): Int {
    val shaderRef = glCreateShader(type);
    glShaderSource(shaderRef, sourceCode)
    glCompileShader(shaderRef)
    val compileStatus = IntArray(1)
    glGetShaderiv(shaderRef, GL_COMPILE_STATUS, compileStatus)
    check(compileStatus[0] == GLFW_TRUE) {
        val message = glGetShaderInfoLog(shaderRef);

        glDeleteShader(shaderRef);

        message
    }
    return shaderRef;
}

fun getShaderCode(location: String): String {
    val path = FileSystems.getDefault().getPath(location);
    val contents: String;
    Files.newBufferedReader(path).use {
        contents = it.readLines().joinToString("\n");
    }
    return contents;
}