package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;

public class MenuButton {

    private final Game game;

    public final Vector2f center, scale;
    public String text;
    public Texture texture;

    public boolean pressedLast = false;

    public Runnable onPressed;

    public boolean show = true;

    public MenuButton(Game game, Vector2f center, Vector2f scale, String text) {
        this.game = game;
        this.center = center;
        this.scale = scale;
        this.text = text;
        this.onPressed = () -> {};
        texture = Texture.makeTexture("sprites/button.png");
    }

    public void update() {
        if(!show) return;

        boolean pressed = glfwGetMouseButton(game.window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
        if(pressed && !pressedLast && MathUtil.pointInside(game.mousePos, center, scale)) {
            onPressed.run();
        }
        pressedLast = pressed;
    }

    public void render() {
        if(!show) {
            return;
        } 

        texture.bind();
        game.drawTexture.draw(new Matrix4f(game.ortho).translate(center.x, center.y, 0).scale(scale.x, scale.y, 0), new Vector4f(1));
        //game.drawSimple.draw(new Matrix4f(game.ortho).translate(center.x, center.y, 0).scale(scale.x, scale.y, 0), new Vector4f(1));
        float textWidth = game.mainFont.textWidth(text);
        game.mainFont.draw(text, center.x - textWidth / 2.15f, center.y + game.mainFont.getSize() / 2.0f * 0.9f, new Matrix4f(game.ortho), new Vector4f(1, 1, 1, 1));
    }
}
