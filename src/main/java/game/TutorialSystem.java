package game;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.function.Consumer;

public class TutorialSystem implements GameSystem {

    private final Game game;
    private Consumer<State> finishFunc;
    private MusicPlayer musicPlayer;

    public TutorialSystem(Game game) {
        this.game = game;
        this.musicPlayer = new MusicPlayer(game);
    }

    @Override
    public void init() {
        System.out.println("Tutorial init");
        game.playButton.show = false;
        game.musicPlayer.show = true;
        game.musicPlayer.currentMeasure = 0;
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

    }
}
