package game;

import org.joml.Vector2f;

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
            new Vector2f(50, 0),
            new Vector2f(100, 0),
            new Vector2f(0, 50),
            new Vector2f(50, 50),
            new Vector2f(100, 50),
            new Vector2f(0, 100),
            new Vector2f(50, 100),
            new Vector2f(100, 100),
    };
    private ArrayList<Note> noteInfo = new ArrayList<>();

    public double bpm = 100.0;

    public double musicTime = -3.0f;
    private int noteInfoPos = 0;
    public ArrayList<Note> activeNotes = new ArrayList<>();
    private Map<Note, Float> playMusicTime = new HashMap<>();

    public int goodness = 0;

    public boolean keyDown = false;

    public MusicPlayer(Game game) {
        this.game = game;
        notePosition = new HashMap<>();
        for(int i = 0; i < notePos.length; ++i) {
            notePosition.put(i + 1, notePos[i]);
        }

        noteInfo = new ArrayList<>();
        noteInfo.add(new Note(0.0f, 1));
        noteInfo.add(new Note(0.5f, 2));
        noteInfo.add(new Note(1.0f, 3));
        noteInfo.add(new Note(1.5f, 4));
        noteInfo.add(new Note(2.0f, 5));
        noteInfo.add(new Note(2.5f, 6));
        noteInfo.add(new Note(3.0f, 7));
        noteInfo.add(new Note(3.5f, 8));
        Collections.sort(noteInfo);
    }

    public void update() {
        musicTime += game.delta * bpm / 60.0;

        boolean added = true;
        while(added && noteInfoPos < noteInfo.size()) {
            added = false;
            Note note = noteInfo.get(noteInfoPos);
            if(musicTime >= note.timeInMeasure) {
                activeNotes.add(note);
                playMusicTime.put(note, note.timeInMeasure + 4);
                noteInfoPos++;
                added = true;
            }
        }

        for(Note note : activeNotes) {

        }

        if(glfwGetKey(game.window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            if(!keyDown) {
                if (!activeNotes.isEmpty()) {
                    Note note = activeNotes.remove(0);
                    double diff = (playMusicTime.get(note) - musicTime) / bpm * 60.0;
                    if (Math.abs(diff) < 0.2) {
                        goodness = 2;
                    } else {
                        goodness = 1;
                    }
                }
            }
        }
        keyDown = glfwGetKey(game.window, GLFW_KEY_SPACE) == GLFW_PRESS;
    }

    public void render() {

    }
}
