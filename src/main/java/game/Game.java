package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {

    // The window handle
    public long window;

    public SoundPlayer soundPlayer;
    public SoundHandle testSound;

    public Texture boardTexture;
    public Texture enemyTexture;
    public Texture enemyDieTexture;

    public DrawFramed drawFramed;
    public DrawSimple drawSimple;
    public DrawTexture drawTexture;
    public DrawFont mainFont;
    public DrawFont bigFont;
    public DrawFont smallFont;
    public final Matrix4f ortho = new Matrix4f();
    public final Vector2f screenSize = new Vector2f();
    public final Vector2f gameScreenSize = new Vector2f();
    public final Vector2f gameScreenCorner = new Vector2f();

    public double currentTime;
    public double delta;
    public final Vector2f mousePos = new Vector2f();

    public final Map<State, GameSystem> systems = new HashMap<>();
    private State currentSystem;

    public MusicPlayer musicPlayer;

    public MenuButton playButton;
    public MenuButton tutorialButton;

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
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });
        glfwSetWindowSizeCallback(window, (window1, width, height) -> {
            this.onWindowSize(width, height);
        });
        playButton = new MenuButton(this, new Vector2f(0), new Vector2f(200, 100), "Play");
        tutorialButton = new MenuButton(this, new Vector2f(0), new Vector2f(200, 100), "Tutorial");
        this.onWindowSize(800, 600);

        drawSimple = new DrawSimple();
        drawTexture = new DrawTexture();
        drawFramed = new DrawFramed();
        bigFont = new DrawFont("font.ttf", 80, 1024, 512);
        boardTexture = Texture.makeTexture("sprites/background_2.png");
        enemyTexture = Texture.makeTexture("sprites/enemy_1.png");
        enemyDieTexture = Texture.makeTexture("sprites/enemy_1_die.png");

        soundPlayer = new SoundPlayer();

        musicPlayer = new MusicPlayer(this);



        systems.put(State.MENU, new MenuSystem(this));
        systems.put(State.TUTORIAL, new TutorialSystem(this));
        systems.put(State.PLAY, new FreeplaySystem(this));

        systems.forEach((state, gameSystem) -> gameSystem.setFinishedCallback((nextState) -> {
            currentSystem = nextState;
            systems.get(currentSystem).init();
        }));
        currentSystem = State.MENU;
        systems.get(this.currentSystem).init();
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

            if(delta < 0.1) {

                double[] mx = new double[1], my = new double[1];
                glfwGetCursorPos(window, mx, my);
                mousePos.set((float) mx[0], (float) my[0]);

                systems.get(currentSystem).update();

                musicPlayer.update();
                playButton.update();
                tutorialButton.update();
            }

            glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer

            if(musicPlayer.show) {
                boardTexture.bind();
                drawTexture.draw(new Matrix4f(ortho).translate(screenSize.x / 2.0f, screenSize.y / 2.0f, 0).scale(gameScreenSize.x, gameScreenSize.y, 0), new Vector4f(1));
            }

            playButton.render();
            tutorialButton.render();
            musicPlayer.render();

            systems.get(currentSystem).render();

            //mainFont.draw("hello fon wotrld", 40, 40, new Matrix4f(ortho));

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }

        drawSimple.destroy();
        drawTexture.destroy();
        drawFramed.destroy();
        mainFont.cleanUp();
        soundPlayer.destroy();
    }

    public static void main(String[] args) {
        new Game().run();
    }

    private void onWindowSize(int x, int y) {
        glViewport(0, 0, x, y);
        this.ortho.identity().ortho(0, x, y, 0, -1, 1);
        this.screenSize.set(x, y);
        boolean smallerX = screenSize.x / 4 < screenSize.y / 3;
        float width, height;
        if(smallerX) {
            width = screenSize.x;
            height = screenSize.x / (4.0f / 3.0f);
        } else {
            width = screenSize.y / (3.0f / 4.0f);
            height = screenSize.y;
        }
        this.gameScreenSize.set(width, height);
        this.gameScreenCorner.set(screenSize.x / 2.0f - width / 2.0f, screenSize.y / 2.0f - height / 2.0f);
        this.playButton.center.set(gameScreenCorner.x + width / 2.0f, gameScreenCorner.y + height * 5.0f / 8.0f);
        this.playButton.scale.set(width * 100.0f / 320.0f, height * 30.0f / 240.0f);
        this.tutorialButton.center.set(gameScreenCorner.x + width / 2.0f, gameScreenCorner.y + height * 6.5f / 8.0f);
        this.tutorialButton.scale.set(width * 100.0f / 320.0f, height * 30.0f / 240.0f);
        if(smallFont != null) {
            smallFont.cleanUp();
        }
        smallFont = new DrawFont("font.ttf", (int) (gameScreenSize.y / 240.0f * 10.0f), 2048, 2048);
        if(mainFont != null) {
            mainFont.cleanUp();
        }
        mainFont = new DrawFont("font.ttf", (int) (gameScreenSize.y / 240.0f * 20.0f), 2048, 2048);
    }
}
