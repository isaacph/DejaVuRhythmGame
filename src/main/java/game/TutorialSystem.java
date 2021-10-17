package game;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Collections;
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
        game.musicPlayer.currentMeasure = -0.01;

        game.musicPlayer.measuresToPlay.clear();
        {
            WaitMeasure measure = new WaitMeasure(game, "Press the space key to start.");
            game.musicPlayer.measuresToPlay.add(measure);
        }
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Look at the pattern, and listen to the rhythm", 1.0f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "\n           3", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "\n           2", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "\n           1", 0.25f));
        {
            GameplayMeasure measure = new GameplayMeasure();
            measure.startingSounds.add("A");
            measure.startingSounds.add("1A");
            measure.noteInfo.add(new Note(0.0f, 2.0f, 1));
            measure.noteInfo.add(new Note(1.0f, 2.0f, 3));
            measure.noteInfo.add(new Note(2.0f, 2.0f, 5));
            measure.noteInfo.add(new Note(3.0f, 2.0f, 7));
            Collections.sort(measure.noteInfo);
            game.musicPlayer.measuresToPlay.add(measure);
        }
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              3", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              2", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              1", 0.25f));
        {
            GameplayMeasure measure = new GameplayMeasure();
            measure.startingSounds.add("A");
            measure.startingSounds.add("1A");
            game.musicPlayer.measuresToPlay.add(measure);
        }
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
