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
        game.playButton.show = false;
        game.musicPlayer.show = true;
        game.musicPlayer.musicTime = -3;
    }

    @Override
    public void setFinishedCallback(Consumer<State> runnable) {
        this.finishFunc = runnable;
    }

    @Override
    public void update() {
        musicPlayer.update();
    }

    @Override
    public void render() {
        musicPlayer.render();
    }
}
