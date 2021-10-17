package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
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
    public Texture heartTexture;
    public Texture heartBrokenTexture;
    public Texture gradientTexture;

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

    private State prevTransitionState, nextTransitionState;
    private boolean transitioning = false;
    private double transitionStart = 0;

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

        boolean fullScreen = true;

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, fullScreen ? GLFW_FALSE : GLFW_TRUE); // the window will be resizable

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);
        if(mode == null) throw new IllegalStateException("Could not get video mode of primary monitor");

        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());

        if(fullScreen) this.screenSize.set(mode.width(), mode.height());
        else this.screenSize.set(800, 600);

        // Create the window
        window = glfwCreateWindow((int) screenSize.x, (int) screenSize.y, "Hello World!", fullScreen ? monitor : NULL, NULL);
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
        this.onWindowSize((int) screenSize.x, (int) screenSize.y);

        drawSimple = new DrawSimple();
        drawTexture = new DrawTexture();
        drawFramed = new DrawFramed();
        bigFont = new DrawFont("font.ttf", 80, 1024, 512);
        boardTexture = Texture.makeTexture("sprites/background_2.png");
        enemyTexture = Texture.makeTexture("sprites/enemy_1.png");
        enemyDieTexture = Texture.makeTexture("sprites/enemy_1_die.png");
        heartTexture = Texture.makeTexture("sprites/heart.png");
        heartBrokenTexture = Texture.makeTexture("sprites/heart_broken.png");

        soundPlayer = new SoundPlayer();

        musicPlayer = new MusicPlayer(this);



        systems.put(State.MENU, new MenuSystem(this));
        systems.put(State.TUTORIAL, new TutorialSystem(this));
        systems.put(State.PLAY, new FreeplaySystem(this));

        systems.forEach((state, gameSystem) -> gameSystem.setFinishedCallback((nextState) -> {
            prevTransitionState = currentSystem;
            nextTransitionState = nextState;
            transitioning = true;
            transitionStart = currentTime;

//            currentSystem = nextState;
//            systems.get(currentSystem).init();
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

            if(delta < 0.1 || true) {

                double[] mx = new double[1], my = new double[1];
                glfwGetCursorPos(window, mx, my);
                mousePos.set((float) mx[0], (float) my[0]);

                systems.get(currentSystem).update();

                musicPlayer.update();
                playButton.update();
                tutorialButton.update();
            }

            if(transitioning) {
                if(this.currentSystem == this.prevTransitionState) {
                    if(this.currentTime - transitionStart > 0.2f) {
                        this.currentSystem = this.nextTransitionState;
                        systems.get(currentSystem).init();
                    }
                }
                if(this.currentTime - transitionStart > 0.4f) {
                    this.transitioning = false;
                }
            }

            glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer

            // draw gradient
            gradientTexture.bind();
            drawTexture.draw(new Matrix4f().scale(2), new Vector4f(1));

            if(musicPlayer.show) {
                boardTexture.bind();
                drawTexture.draw(new Matrix4f(ortho).translate(screenSize.x / 2.0f, screenSize.y / 2.0f, 0).scale(gameScreenSize.x, gameScreenSize.y, 0), new Vector4f(1));
            }

            playButton.render();
            tutorialButton.render();
            musicPlayer.render();

            systems.get(currentSystem).render();

            if(transitioning) {
                double timeDiff = currentTime - transitionStart;
                double alpha;
                if(timeDiff < 0.2f) {
                    alpha = timeDiff / 0.2f;
                } else {
                    alpha = 1 - (timeDiff - 0.2f) / 0.2f;
                }
                this.gradientTexture.bind();
                this.drawTexture.draw(new Matrix4f().scale(2), new Vector4f(1, 1, 1, (float) alpha));
            }

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
        smallFont = new DrawFont("font.ttf", (int) (gameScreenSize.y / 240.0f * 10.0f), 4096, 4096);
        if(mainFont != null) {
            mainFont.cleanUp();
        }
        mainFont = new DrawFont("font.ttf", (int) (gameScreenSize.y / 240.0f * 20.0f), 8192, 4096);

        int res = (int) screenSize.y;
        makeGradient(res);
    }

    private void makeGradient(int resolution) {
//        Vector4f startColor = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
//        Vector4f endColor = new Vector4f(62.0f / 255.0f, 62.0f / 255.0f, 115.0f / 255.0f, 1.0f);
        Vector4f startColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        float divisor = 1.7f;
        Vector4f endColor = new Vector4f(137 / 255.0f / divisor, 100 / 255.0f / divisor, 61 / 255.0f / divisor, 1.0f);

        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(resolution * 4);
            float[] beginColor = {startColor.x, startColor.y, startColor.z, startColor.w};
            float[] currentColor = {startColor.x, startColor.y, startColor.z, startColor.w};
            float[] destColor = {endColor.x, endColor.y, endColor.z, endColor.w};
            for(int i = 0; i < resolution; ++i) {

                // this function determines how to blend the color in terms of i (how far we are in the gradient)
                // and resolution (the length of the gradient)
                float fraction = (float) Math.sqrt((float) i / resolution);

                for(int j = 0; j < 4; ++j) {
                    currentColor[j] = beginColor[j] * fraction + destColor[j] * (1 - fraction);
                }
                buffer.put(currentColor);
            }
            buffer.flip();
            if(this.gradientTexture != null) {
                this.gradientTexture.destroy();
            }
            this.gradientTexture = new Texture(buffer, new Vector2i(1, resolution), new Texture.Settings());
        }
    }
}
