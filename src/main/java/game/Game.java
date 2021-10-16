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
    private long window;

    private DrawSimple drawSimple;
    private final Matrix4f ortho = new Matrix4f();

    private final Vector2f screenSize = new Vector2f();

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
        // will print the error message in System.err.
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
    }

    private void loop() {

        Map<Integer, Vector2f> notePosition = new HashMap<>();

        Vector2f[] notePos = new Vector2f[] {
                new Vector2f(0, 0),
                new Vector2f(50, 0),
                new Vector2f(100, 0),
                new Vector2f(0, 50),
                new Vector2f(50, 50),
                new Vector2f(100, 50),
                new Vector2f(0, 100),
                new Vector2f(50, 100),
                new Vector2f(100, 100),
        };

        for(int i = 0; i < notePos.length; ++i) {
            notePosition.put(i + 1, notePos[i]);
        }

        ArrayList<Note> noteInfo = new ArrayList<>();
        noteInfo.add(new Note(0.0f, 1));
        noteInfo.add(new Note(0.5f, 2));
        noteInfo.add(new Note(1.0f, 3));
        noteInfo.add(new Note(1.5f, 4));
        noteInfo.add(new Note(2.0f, 5));
        noteInfo.add(new Note(2.5f, 6));
        noteInfo.add(new Note(3.0f, 7));
        noteInfo.add(new Note(3.5f, 8));



//        noteInfo.add(new Note(4.0f, 3));
//        noteInfo.add(new Note(5.5f, 2));
//        noteInfo.add(new Note(6.0f, 1));
//        noteInfo.add(new Note(7.0f, 8));
        Collections.sort(noteInfo);

        ArrayList<Note> activeNotes = new ArrayList<>();
        Map<Note, Float> playMusicTime = new HashMap<>();

        double musicTime = -3.0f;
        int noteInfoPos = 0;

        double bpm = 100.0;

        int goodness = 0;

        boolean keyDown = false;

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        double currentTime = glfwGetTime();
        double lastTime = currentTime;
        while ( !glfwWindowShouldClose(window) ) {
            currentTime = glfwGetTime();
            double delta = currentTime - lastTime;
            lastTime = currentTime;

            musicTime += delta * bpm / 60.0;

            boolean added = true;
            while(added && noteInfoPos < noteInfo.size()) {
                added = false;
                Note note = noteInfo.get(noteInfoPos);
                if(musicTime >= note.timeInMeasure) {
                    activeNotes.add(note);
                    playMusicTime.put(note, note.timeInMeasure + 4);
                    noteInfoPos++;
                    added = true;
                }
            }

            for(Note note : activeNotes) {

            }

            if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
                if(!keyDown) {
                    if (!activeNotes.isEmpty()) {
                        Note note = activeNotes.remove(0);
                        double diff = (playMusicTime.get(note) - musicTime) / bpm * 60.0;
                        if (Math.abs(diff) < 0.2) {
                            goodness = 2;
                        } else {
                            goodness = 1;
                        }
                    }
                }
            }
            keyDown = glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            for(Note note : activeNotes) {
                Vector2f pos = new Vector2f(notePosition.get(note.position));
                translateToScreen(pos);
                drawSimple.draw(new Matrix4f(ortho).translate(pos.x, pos.y, 0).scale(100), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
            }
            Vector4f col = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
            if(goodness == 1) {
                col = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);
            } else if(goodness == 2) {
                col = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);
            }
            drawSimple.draw(new Matrix4f(ortho).translate(screenSize.x / 2.0f, 80, 0).scale(100), col);

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }

        drawSimple.destroy();
    }

    public static void main(String[] args) {
        new Game().run();
    }

    private void onWindowSize(int x, int y) {
        glViewport(0, 0, x, y);
        this.ortho.ortho(0, x, y, 0, 0, 1);
        this.screenSize.set(x, y);
    }

    public void translateToScreen(Vector2f pos) {
        pos.set((pos.x / 100.0f * 0.5f + 0.25f) * screenSize.x, (pos.y / 100.0f * 0.5f + 0.25f) * screenSize.y);
    }
}
