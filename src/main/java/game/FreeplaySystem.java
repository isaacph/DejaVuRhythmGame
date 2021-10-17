package game;

import java.util.*;
import java.util.function.Consumer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;

public class FreeplaySystem implements GameSystem {

    public static Map<String, ArrayList<Note>> rhythmNotes = new HashMap<>();
    static {
        {
            String rhythm = "A";
            ArrayList<Note> notes = new ArrayList<>();
            notes.add(new Note(0.0f, 0.0f, 1));
            notes.add(new Note(1.0f, 0.0f, 3));
            notes.add(new Note(2.0f, 0.0f, 7));
            notes.add(new Note(3.0f, 0.0f, 9));
            Collections.sort(notes);
            rhythmNotes.put(rhythm, notes);
        }
        {
            String rhythm = "B";
            ArrayList<Note> notes = new ArrayList<>();
            notes.add(new Note(0.0f, 0.0f, 2));
            notes.add(new Note(1.5f, 0.0f, 4));
            notes.add(new Note(2.0f, 0.0f, 6));
            notes.add(new Note(3.5f, 0.0f, 8));
            Collections.sort(notes);
            rhythmNotes.put(rhythm, notes);
        }
        {
            String rhythm = "C";
            ArrayList<Note> notes = new ArrayList<>();
            notes.add(new Note(0.0f, 0.0f, 1));
            notes.add(new Note(0.5f, 0.0f, 2));
            notes.add(new Note(1.0f, 0.0f, 3));
            notes.add(new Note(1.5f, 0.0f, 4));
            notes.add(new Note(2.0f, 0.0f, 6));
            notes.add(new Note(2.5f, 0.0f, 7));
            notes.add(new Note(3.0f, 0.0f, 8));
            notes.add(new Note(3.5f, 0.0f, 9));
            Collections.sort(notes);
            rhythmNotes.put(rhythm, notes);
        }
        {
            String rhythm = "D";
            ArrayList<Note> notes = new ArrayList<>();
            notes.add(new Note(0.0f, 0.0f, 1));
            notes.add(new Note(0.5f, 0.0f, 3));
            notes.add(new Note(1.5f, 0.0f, 5));
            notes.add(new Note(2.0f, 0.0f, 7));
            notes.add(new Note(3.0f, 0.0f, 9));
            Collections.sort(notes);
            rhythmNotes.put(rhythm, notes);
        }
    }

    public static ArrayList<Note> withConstantHitTime(ArrayList<Note> notes, float time) {
        ArrayList<Note> newNotes = new ArrayList<>();
        for(Note note : notes) {
            newNotes.add(new Note(note.timeInMeasure, time, note.position));
        }
        return newNotes;
    }

    public static ArrayList<Note> withCombinedSpawn(ArrayList<Note> notes, float spawnTimeAfterStart) {
        ArrayList<Note> newNotes = new ArrayList<>();
        for(Note note : notes) {
            newNotes.add(new Note(spawnTimeAfterStart * 4.0f, -spawnTimeAfterStart + note.timeInMeasure / 4.0f, note.position));
        }
        return newNotes;
    }

    private final Game game;
    private Consumer<State> onFinish;
    private Random random;
    private int phrases;
    private int score;

    private boolean ended = false;
    private double endedTime = 0;

    public FreeplaySystem(Game game) {
        this.game = game;
        this.random = new Random();
    }

    @Override
    public void setFinishedCallback(Consumer<State> onFinish) {
        this.onFinish = onFinish;
    }

    @Override
    public void init() {
        score = 0;
        ended = false;
        phrases = 0;
        game.playButton.show = false;
        game.tutorialButton.show = false;
        game.musicPlayer.waiting = false;
        game.musicPlayer.show = true;
        game.musicPlayer.topText = "";
        game.musicPlayer.currentMeasure = -0.01;
        game.musicPlayer.hearts.setHearts(6);
        Runnable onGood = () -> {
            score += 1;
        };
        Runnable onBad = () -> {
            game.musicPlayer.hearts.subtractHeart(game.musicPlayer.currentMeasure);
            if(game.musicPlayer.hearts.getHearts() <= 0) {
                game.musicPlayer.waiting = true;
                game.musicPlayer.topText = "       Game Over\n Press space for menu";
                game.soundPlayer.stopAllSounds();
                ended = true;
                endedTime = 0;
            }
            game.soundPlayer.play(game.musicPlayer.missSound, 0); // this has to go after stopAllSounds()
        };
        game.musicPlayer.onHitGood = onGood;
        game.musicPlayer.onHitBad = onBad;
        game.musicPlayer.onMiss = onBad;

        game.musicPlayer.measuresToPlay.clear();
        game.musicPlayer.activeNotes.clear();
        game.musicPlayer.goodness = 0;

        game.musicPlayer.measuresToPlay.add(new WaitMeasure(game, "Press the space key to start."));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "\n           4", 0.25f, game.musicPlayer.cueSound));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "\n           3", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "\n           2", 0.25f));
        game.musicPlayer.measuresToPlay.add(new CountingMeasure(game, "\n           1", 0.25f));

        addMemorizationPhrase();
        addPhrase();
        game.musicPlayer.onFinish = () -> this.onFinish.accept(State.MENU);
    }

    private void addMemorizationPhrase() {
        String[] rhythms = {"A", "B", "C", "D"};
        for(int i = 0; i < 4; ++i) {
            String rhythm = rhythms[i];
            int melody = i + 1;
            GameplayMeasure measure = new GameplayMeasure();
            measure.startingSounds.add(rhythm);
            measure.noteInfo.addAll(withConstantHitTime(rhythmNotes.get(rhythm), 1.0f));
            Collections.sort(measure.noteInfo);
            game.musicPlayer.measuresToPlay.add(measure);

            GameplayMeasure playMeasure = new GameplayMeasure();
            playMeasure.startingSounds.add(rhythm);
            playMeasure.startingSounds.add(melody + rhythm);
            game.musicPlayer.measuresToPlay.add(playMeasure);

            GameplayMeasure fastMeasure = new GameplayMeasure(0.25f);
            fastMeasure.startingSounds.add(rhythm);
            fastMeasure.startingSounds.add(melody + rhythm);
            fastMeasure.noteInfo.addAll(withCombinedSpawn(rhythmNotes.get(rhythm), -0.25f));
            game.musicPlayer.measuresToPlay.add(fastMeasure);
        }
        ++phrases;
    }


    private void addPhrase() {
        String[] rhythms = {"A", "B", "C", "D"};
        // do the first four measures twice
        for(int m = 0; m < 2; ++m) {
            for(int i = 0; i < 4; ++i) {
                String rhythm = rhythms[random.nextInt(4)];
                int melody = i + 1;
                int chord = i + 1;

                GameplayMeasure fastMeasure = new GameplayMeasure(0.25f);
                fastMeasure.startingSounds.add(rhythm);
                fastMeasure.startingSounds.add(melody + rhythm);
                fastMeasure.startingSounds.add("chord_" + chord);
                fastMeasure.noteInfo.addAll(withCombinedSpawn(rhythmNotes.get(rhythm), -0.25f));
                game.musicPlayer.measuresToPlay.add(fastMeasure);
            }
        }
//        // now we do the second phrase (measures 5-8)
        for(int m = 0; m < 2; ++m) {
            for(int i = 4; i < 8; ++i) {
                String rhythm = rhythms[random.nextInt(4)];
                int melody = i + 1;
                int chord = i + 1;

                GameplayMeasure fastMeasure = new GameplayMeasure(0.25f);
                fastMeasure.startingSounds.add(rhythm);
                fastMeasure.startingSounds.add(melody + rhythm);
                fastMeasure.startingSounds.add("chord_" + chord);
                fastMeasure.noteInfo.addAll(withCombinedSpawn(rhythmNotes.get(rhythm), -0.25f));
                game.musicPlayer.measuresToPlay.add(fastMeasure);
            }
        }
        ++phrases;
    }

    @Override
    public void update() {
        int currentPhrase = (int) ((game.musicPlayer.currentMeasure - 1) / 4.0);
        while(currentPhrase + 1 >= phrases) {
            addPhrase();
        }
        if(ended) {
            endedTime += game.delta;
            if(endedTime > 0.5 && glfwGetKey(game.window, GLFW_KEY_SPACE) == GLFW_PRESS) {
                this.onFinish.accept(State.MENU);
            }
        }
    }

    @Override
    public void render() {
        // draw the score
        Vector2f textPos = new Vector2f(104, 70);
        game.musicPlayer.translateToScreen(textPos);
        textPos.add(0, game.smallFont.getSize());
        game.smallFont.draw("Score: " + score, textPos.x, textPos.y, new Matrix4f(game.ortho), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
    }
}
