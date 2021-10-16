package game;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.function.Consumer;

public class MenuSystem implements GameSystem {

    private final Game game;
    private Consumer<State> finishFunc;

    public MenuSystem(Game game) {
        this.game = game;
    }

    @Override
    public void init() {
        game.playButton.show = true;
        game.musicPlayer.show = false;
        game.playButton.onPressed = () -> {
            this.finishFunc.accept(State.TUTORIAL);
        };
    }

    @Override
    public void setFinishedCallback(Consumer<State> runnable) {
        this.finishFunc = runnable;
    }

    @Override
    public void update() {

    }

    @Override
    public void render() {
        String txt = "Test Menu";
        float width = game.bigFont.textWidth(txt);
        game.bigFont.draw("Test Menu", game.screenSize.x / 2.0f - width / 2.0f, game.screenSize.y / 4.0f, new Matrix4f(game.ortho), new Vector4f(1, 1, 1, 1));
    }
}
