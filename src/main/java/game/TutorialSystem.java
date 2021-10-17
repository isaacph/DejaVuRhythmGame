package game;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Collections;
import java.util.function.Consumer;

public class TutorialSystem implements GameSystem {

    private final Game game;
    private Consumer<State> finishFunc;

    public TutorialSystem(Game game) {
        this.game = game;
    }

    @Override
    public void init() {
        System.out.println("Tutorial init");
        game.playButton.show = false;
        game.tutorialButton.show = false;
        game.musicPlayer.show = true;
        game.musicPlayer.currentMeasure = -0.01;

        game.musicPlayer.measuresToPlay.clear();
        game.musicPlayer.activeNotes.clear();
        game.musicPlayer.goodness = 0;

        {
            WaitMeasure measure = new WaitMeasure(game, "Press the space key to start.");
            game.musicPlayer.measuresToPlay.add(measure);
        }
        // Tutorial Rhythm A
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Look at the pattern, and listen to the rhythm", 1.0));
        // game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "\n           4", 0.25f, game.musicPlayer.cueSound));
        // game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "\n           3", 0.25f));
        // game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "\n           2", 0.25f));
        // game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "\n           1", 0.25f));
        {
            GameplayMeasure measure = new GameplayMeasure();
            // measure.startingSounds.add("A");
            measure.startingSounds.add("1A");
            measure.noteInfo.add(new Note(0.0f, 2.25f, 1));
            measure.noteInfo.add(new Note(1.0f, 2.25f, 3));
            measure.noteInfo.add(new Note(2.0f, 2.25f, 7));
            measure.noteInfo.add(new Note(3.0f, 2.25f, 9));
            Collections.sort(measure.noteInfo);
            game.musicPlayer.measuresToPlay.add(measure);
        }
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              4", 0.25f, game.musicPlayer.cueSound));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              3", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              2", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              1", 0.25f));
        {
            GameplayMeasure measure = new GameplayMeasure();
            measure.startingSounds.add("A");
            measure.startingSounds.add("1A");
            game.musicPlayer.measuresToPlay.add(measure);
        }
        // Tutorial Rhythm B
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Look at the pattern, and listen to the rhythm", 1.0));
        {
            GameplayMeasure measure = new GameplayMeasure();
            measure.startingSounds.add("1B");
            measure.noteInfo.add(new Note(0.0f, 2.25f, 2));
            measure.noteInfo.add(new Note(1.5f, 2.25f, 4));
            measure.noteInfo.add(new Note(2.0f, 2.25f, 6));
            measure.noteInfo.add(new Note(3.5f, 2.25f, 8));
            Collections.sort(measure.noteInfo);
            game.musicPlayer.measuresToPlay.add(measure);
        }
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              4", 0.25f, game.musicPlayer.cueSound));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              3", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              2", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              1", 0.25f));
        {
            GameplayMeasure measure = new GameplayMeasure();
            measure.startingSounds.add("B");
            measure.startingSounds.add("1B");
            game.musicPlayer.measuresToPlay.add(measure);
        }
        // Tutorial Rhythm C
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Look at the pattern, and listen to the rhythm", 1.0));
        {
            GameplayMeasure measure = new GameplayMeasure();
            measure.startingSounds.add("1C");
            measure.noteInfo.add(new Note(0.0f, 2.25f, 1));
            measure.noteInfo.add(new Note(0.5f, 2.25f, 2));
            measure.noteInfo.add(new Note(1.0f, 2.25f, 3));
            measure.noteInfo.add(new Note(1.5f, 2.25f, 4));
            measure.noteInfo.add(new Note(2.0f, 2.25f, 6));
            measure.noteInfo.add(new Note(2.5f, 2.25f, 7));
            measure.noteInfo.add(new Note(3.0f, 2.25f, 8));
            measure.noteInfo.add(new Note(3.5f, 2.25f, 9));
            Collections.sort(measure.noteInfo);
            game.musicPlayer.measuresToPlay.add(measure);
        }
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              4", 0.25f, game.musicPlayer.cueSound));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              3", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              2", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              1", 0.25f));
        {
            GameplayMeasure measure = new GameplayMeasure();
            measure.startingSounds.add("C");
            measure.startingSounds.add("1C");
            game.musicPlayer.measuresToPlay.add(measure);
        }
        // Tutorial Rhythm D
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Look at the pattern, and listen to the rhythm", 1.0));
        {
            GameplayMeasure measure = new GameplayMeasure();
            measure.startingSounds.add("1D");
            measure.noteInfo.add(new Note(0.0f, 2.25f, 1));
            measure.noteInfo.add(new Note(0.5f, 2.25f, 3));
            measure.noteInfo.add(new Note(1.5f, 2.25f, 5));
            measure.noteInfo.add(new Note(2.0f, 2.25f, 7));
            measure.noteInfo.add(new Note(3.0f, 2.25f, 9));
            Collections.sort(measure.noteInfo);
            game.musicPlayer.measuresToPlay.add(measure);
        }
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              4", 0.25f, game.musicPlayer.cueSound));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              3", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              2", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "Now tap the rhythm\n              1", 0.25f));
        {
            GameplayMeasure measure = new GameplayMeasure();
            measure.startingSounds.add("D");
            measure.startingSounds.add("1D");
            game.musicPlayer.measuresToPlay.add(measure);
        }
        game.musicPlayer.onFinish = () -> this.finishFunc.accept(State.MENU);
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
