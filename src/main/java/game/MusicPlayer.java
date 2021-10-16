package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class MusicPlayer {

    private final Game game;

    public final Map<Integer, Vector2f> notePosition;

    private Vector2f[] notePos = new Vector2f[] {
            new Vector2f(0, 0),
            new Vector2f(640, 0),
            new Vector2f(0, 480),
            new Vector2f(640, 480),
            new Vector2f(50, 50),
            new Vector2f(100, 50),
            new Vector2f(0, 100),
            new Vector2f(50, 100),
            new Vector2f(100, 100),
    };

    public ArrayList<MeasureInfo> measuresToPlay = new ArrayList<>();
    public double currentMeasure;

    public double bpm = 120.0;

    public int noteInfoPos = 0;
    public ArrayList<Note> activeNotes = new ArrayList<>();
    public Map<Note, Float> playMusicTime = new HashMap<>();

    public int goodness = 0;

    public boolean keyDown = false;

    public boolean show = true;

    public final Map<String, SoundHandle> sounds = new HashMap<>();

    public static final int[] MELODY_NUMBERS = {1};
    public static final String[] RHYTHM_NAMES = {"A"};

    public int counter = 0;

    public SoundHandle hitSound;

    public MusicPlayer(Game game) {
        this.game = game;
        notePosition = new HashMap<>();
        for(int i = 0; i < notePos.length; ++i) {
            notePosition.put(i + 1, notePos[i]);
        }

        {
            WaitMeasure measure = new WaitMeasure();
            measuresToPlay.add(measure);
        }
        {
            GameplayMeasure measure = new GameplayMeasure();
            measure.startingSounds.add("A");
            measure.startingSounds.add("1A");
            measure.noteInfo.add(new Note(0.0f, 4.0f, 1));
            measure.noteInfo.add(new Note(0.5f, 4.0f, 2));
            measure.noteInfo.add(new Note(1.0f, 4.0f, 3));
            measure.noteInfo.add(new Note(1.5f, 4.0f, 4));
            measure.noteInfo.add(new Note(2.0f, 4.0f, 5));
            measure.noteInfo.add(new Note(2.5f, 4.0f, 6));
            measure.noteInfo.add(new Note(3.0f, 4.0f, 7));
            measure.noteInfo.add(new Note(3.5f, 4.0f, 8));
            Collections.sort(measure.noteInfo);
            measuresToPlay.add(measure);
        }
        {
            GameplayMeasure measure = new GameplayMeasure();
            measure.startingSounds.add("A");
            measure.startingSounds.add("1A");
            measuresToPlay.add(measure);
        }

        hitSound = game.soundPlayer.loadSound("");

        for(String rhythm : RHYTHM_NAMES) {
            sounds.put(rhythm, game.soundPlayer.loadSound("music/Rhythm_" + rhythm + ".ogg"));
            for(int melody : MELODY_NUMBERS) {
                sounds.put(melody + rhythm, game.soundPlayer.loadSound("music/Melody_" + melody + rhythm + ".ogg"));
            }
        }

        currentMeasure = 0;
    }

    public void update() {
        if(!show) return;

        int prevMeasure = MathUtil.roundDown(currentMeasure);
        currentMeasure += game.delta * bpm / 60.0 / 4.0f;
        int measureIndex = MathUtil.roundDown(currentMeasure);
        for(int mI = Math.max(0, prevMeasure); mI <= Math.min(measuresToPlay.size() - 1, measureIndex); ++mI) {
            MeasureInfo measure = measuresToPlay.get(mI);

            if(mI > prevMeasure) {
                measure.measureStart(game, mI);
            } else if(mI == measureIndex) {
                measure.measureUpdate(game);
            } else { //if(mI < measureIndex) {
                measure.measureEnd(game);
            }
        }

        if(glfwGetKey(game.window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            if(!keyDown) {
                if (!activeNotes.isEmpty()) {
                    Note note = activeNotes.remove(0);
                    double diff = (playMusicTime.get(note) - currentMeasure) / bpm * 60.0;
                    if (Math.abs(diff) < 0.2) {
                        goodness = 2;
                    } else {
                        goodness = 1;
                    }
                    System.out.println("Hit note with diff " + diff);
                }
            }
        }
        keyDown = glfwGetKey(game.window, GLFW_KEY_SPACE) == GLFW_PRESS;
    }

    public void render() {
        if(!show) return;
        /* draw important stuff */
        for(Note note : activeNotes) {
            Vector2f pos = new Vector2f(notePosition.get(note.position));
            translateToScreen(pos);
            game.enemyTexture.bind();
            game.drawTexture.draw(new Matrix4f(game.ortho).translate(pos.x, pos.y, 0).scale(100), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
        }

        Vector4f col = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        if(goodness == 1) {
            col.set(1.0f, 0.0f, 0.0f, 1.0f);
        } else if(goodness == 2) {
            col.set(0.0f, 1.0f, 0.0f, 1.0f);
        }

        // draw goodness
        game.mainFont.draw("Test: " + goodness, 100, 100, new Matrix4f(game.ortho));
    }

    public void translateToScreen(Vector2f pos) {
        pos.set(pos).div(640.0f, 480.0f).mul(game.gameScreenSize).add(game.gameScreenCorner);
    }
}
