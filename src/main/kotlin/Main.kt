import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.glfwGetTime
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import kotlin.concurrent.thread

const val wHeight = 1022
const val wWidth = 1920
const val wPrecision = 140
//const val delay = 2000L
class GraphicsDisplay {
    private var window: Long = 0
    private var listsToDraw = mutableListOf<DrawableList>()
    private var lastTime = 0.0
    private var currentTime = 0.0
    private var frames = 0
    private var toClose = false

    fun close(){
        toClose = true
        println(".close() gui called")
    }

    private fun printRenderTime(){
        frames++
        currentTime = glfwGetTime()
        if(currentTime - lastTime > 1.0) {
            println("${1000.0/frames.toDouble()}\t$frames")
            lastTime = currentTime
            frames = 0
        }
    }

    fun run(lineLists: MutableList<DrawableList>) {
        println("gui started with LWJGL " + Version.getVersion() + " (OpenGl)")

        //listsToDraw.addAll(lineLists)
        listsToDraw = lineLists

        init()
        loop()

        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window)
        GLFW.glfwDestroyWindow(window)

        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null)!!.free()
    }

    private fun init() {
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE) // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE) // the window will be resizable

        // Create the window
        window = GLFW.glfwCreateWindow(wWidth, wHeight, "GraphicsDisplay", MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == MemoryUtil.NULL) throw RuntimeException("Failed to create the GLFW window")

        // Set up a key callback. It will be called every time a key is pressed, repeated or released.
        GLFW.glfwSetKeyCallback(
            window
        ) { window: Long, key: Int, _: Int, action: Int, _: Int ->
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) GLFW.glfwSetWindowShouldClose(
                window,
                true
            ) // We will detect this in the rendering loop
        }
        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            GLFW.glfwGetWindowSize(window, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())

            // Center the window
            GLFW.glfwSetWindowPos(
                window,
                (vidMode!!.width() - pWidth[0]) / 2,
                (vidMode.height() - pHeight[0]) / 2
            )
        }

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window)
        // Enable v-sync
        GLFW.glfwSwapInterval(1)

        // Make the window visible
        GLFW.glfwShowWindow(window)
    }

    private fun loop() {
        GL.createCapabilities()

        glClearColor(1.0f, 1.0f, 1.0f, 0.0f)
        glEnable(GL_POINT_SMOOTH)//изменяемый размер точки
        glEnable(GL_LINE_SMOOTH)//изменяемая ширина линии

        while (!GLFW.glfwWindowShouldClose(window) && !toClose) {
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            //printRenderTime()
            synchronized(listsToDraw) {
                for (list in listsToDraw) list.draw()
            }
            GLFW.glfwSwapBuffers(window)
            GLFW.glfwPollEvents()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val renderList = mutableListOf<DrawableList>()
            val coreModel = CoreModel(renderList)
            val graphicsDisplay = GraphicsDisplay()
            val controller = Controller(coreModel, graphicsDisplay)
            val coreThread = thread {
                coreModel.run()
            }
            val guiThread = thread{
                graphicsDisplay.run(renderList)
            }
            controller.run()//пока в этом потоке
            coreThread.join()
            guiThread.join()
            println("program finished")
        }
    }
}