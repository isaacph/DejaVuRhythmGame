package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {

    // The window handle
    public long window;

    private DrawSimple drawSimple;
    private DrawFont mainFont;
    private final Matrix4f ortho = new Matrix4f();

    private final Vector2f screenSize = new Vector2f();

    public double currentTime;
    public double delta;

    public final ArrayList<GameSystem> systems = new ArrayList<>();
    private int currentSystem;

    public MusicPlayer musicPlayer;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in GameSystem.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(800, 600, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });
        glfwSetWindowSizeCallback(window, (window1, width, height) -> {
            this.onWindowSize(width, height);
        });
        this.onWindowSize(800, 600);

        drawSimple = new DrawSimple();

        musicPlayer = new MusicPlayer(this);

        systems.add(new MenuSystem(this));
        systems.add(new TutorialSystem(this));

        systems.forEach(gameSystem -> gameSystem.setFinishedCallback(() -> {
            ++this.currentSystem;
            systems.get(currentSystem).init();
        }));
        systems.get(this.currentSystem).init();

        mainFont = new DrawFont("font.ttf", 32, 512, 512);
    }

    private void loop() {

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        currentTime = glfwGetTime();
        double lastTime = currentTime;
        while ( !glfwWindowShouldClose(window) ) {
            currentTime = glfwGetTime();
            delta = currentTime - lastTime;
            lastTime = currentTime;

            for(GameSystem system : systems) {
                system.update();
            }

            musicPlayer.update();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            for(GameSystem system : systems) {
                system.render();
            }

            /* draw important stuff */
            for(Note note : musicPlayer.activeNotes) {
                Vector2f pos = new Vector2f(musicPlayer.notePosition.get(note.position));
                translateToScreen(pos);
                //drawSimple.draw(new Matrix4f(ortho).translate(pos.x, pos.y, 0).scale(100), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
            }

            Vector4f col = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
            if(musicPlayer.goodness == 1) {
                col = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);
            } else if(musicPlayer.goodness == 2) {
                col = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);
            }

            // draw goodness
            //drawSimple.draw(new Matrix4f(ortho).translate(screenSize.x / 2.0f, 80, 0).scale(100), col);
            drawSimple.draw(new Matrix4f(ortho).translate(100, 100, 0).scale(100, 100, 0), new Vector4f(1));

            //mainFont.draw("hello fon wotrld", 40, 40, new Matrix4f(ortho));

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }

        drawSimple.destroy();
        mainFont.cleanUp();
    }

    public static void main(String[] args) {
        new Game().run();
    }

    private void onWindowSize(int x, int y) {
        glViewport(0, 0, x, y);
        this.ortho.identity().ortho(0, x, y, 0, -1, 1);
        this.screenSize.set(x, y);
    }

    public void translateToScreen(Vector2f pos) {
        pos.set((pos.x / 100.0f * 0.5f + 0.25f) * screenSize.x, (pos.y / 100.0f * 0.5f + 0.25f) * screenSize.y);
    }
}
