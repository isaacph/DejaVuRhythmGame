package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class MusicPlayer {

    private final Game game;

    public final Map<Integer, Vector2f> notePosition;

    private Vector2f[] notePos = new Vector2f[] {
            new Vector2f(122, 128),
            new Vector2f(122 + 40, 128),
            new Vector2f(122 + 80, 128),
            new Vector2f(122, 128 + 40),
            new Vector2f(122 + 40, 128 + 40),
            new Vector2f(122 + 80, 128 + 40),
            new Vector2f(122, 128 + 80),
            new Vector2f(122 + 40, 128 + 80),
            new Vector2f(122 + 80, 128 + 80),
    };

    public ArrayList<MeasureInfo> measuresToPlay = new ArrayList<>();
    public double currentMeasure;

    public double bpm = 120.0;

    public int noteInfoPos = 0;
    public ArrayList<Note> activeNotes = new ArrayList<>();
    public Map<Note, Float> playMusicTime = new HashMap<>();
    public Map<Note, NoteStatus> activeNoteStatus = new HashMap<>();
    public Map<Note, Double> activeNoteStatusTime = new HashMap<>();

    public int goodness = 0;

    public boolean keyDown = false;

    public boolean show = true;

    public final Map<String, SoundHandle> sounds = new HashMap<>();

    public static final int[] MELODY_NUMBERS = {1, 2, 3, 4};
    public static final String[] RHYTHM_NAMES = {"A", "B", "C", "D"};

    public static final double HIT_LENIENCY = 0.05;
    public static final double HIT_GIVE_UP = 0.12;

    public String topText = "";

    public SoundHandle hitSound;
    public SoundHandle cueSound;

    public boolean waiting = false;

    public MusicPlayer(Game game) {
        this.game = game;

        // load not positions
        notePosition = new HashMap<>();
        for (int i = 0; i < notePos.length; ++i) {
            notePosition.put(i + 1, notePos[i]);
        }

        // load sounds
        hitSound = game.soundPlayer.loadSound("soundfx/clap.ogg");
        cueSound = game.soundPlayer.loadSound("music/cue.ogg");
        for(String rhythm : RHYTHM_NAMES) {
            sounds.put(rhythm, game.soundPlayer.loadSound("music/Rhythm_" + rhythm + ".ogg"));
            for(int melody : MELODY_NUMBERS) {
                sounds.put(melody + rhythm, game.soundPlayer.loadSound("music/Melody_" + melody + rhythm + ".ogg"));
            }
        }

        currentMeasure = -0.01f; // currentMeasure has to start less than 0
    }

    public void update() {
        if(!show) return;

        int prevMeasure = getMeasure(currentMeasure);
        if(!waiting) currentMeasure += game.delta * bpm / 60.0 / 4.0f;
        int measureIndex = getMeasure(currentMeasure);
//        System.out.println(prevMeasure + ", " + measureIndex + ": " + currentMeasure);
        for(int mI = Math.max(0, prevMeasure); mI <= Math.min(measuresToPlay.size() - 1, measureIndex); ++mI) {
            MeasureInfo measure = measuresToPlay.get(mI);

            if(mI > prevMeasure) {
                measure.measureStart(game, getMeasureStart(mI));
                if(waiting) {
                    break;
                }
            } else if(mI == measureIndex) {
                measure.measureUpdate(game);
            } else { //if(mI < measureIndex) {
                measure.measureEnd(game);
            }
        }

        // change notes to missed
        for(Note note : activeNotes) {
            if(activeNoteStatus.get(note) != NoteStatus.READY) continue;
            double diff = (playMusicTime.get(note) - currentMeasure) / bpm * 60.0;
            if(diff < -HIT_GIVE_UP) {
                activeNoteStatus.put(note, NoteStatus.MISSED);
            } else break;
        }

        // removed finished notes
        for(int i = activeNotes.size() - 1; i >= 0; --i) {
            Note note = activeNotes.get(i);
            if(activeNoteStatus.get(note) == NoteStatus.DEAD) {
                if((int) (1 + (currentMeasure - activeNoteStatusTime.get(note)) * 20.0f) >= 6) {
                    activeNotes.remove(i);
                    activeNoteStatusTime.remove(note);
                    activeNoteStatus.remove(note);
                    playMusicTime.remove(note);
                }
            } else if(activeNoteStatus.get(note) == NoteStatus.MISSED) {
                activeNotes.remove(i);
                activeNoteStatusTime.remove(note);
                activeNoteStatus.remove(note);
                playMusicTime.remove(note);
            }
        }

        if(glfwGetKey(game.window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            if(!keyDown) {
                for (Note note : activeNotes) {
                    if(activeNoteStatus.get(note) != NoteStatus.READY) continue;
                    double diff = (playMusicTime.get(note) - currentMeasure) / bpm * 60.0;
                    boolean die = false;
                    if(diff > HIT_GIVE_UP) {
                        goodness = 0;
                    }
                    else if (Math.abs(diff) <= HIT_GIVE_UP && Math.abs(diff) > HIT_LENIENCY) {
                        goodness = 1;
                        die = true;
                    } else  if(Math.abs(diff) <= HIT_LENIENCY){
                        goodness = 2;
                        die = true;
                    } else {
                        goodness = 0;
                        die = true;
                    }
                    System.out.println("Hit note with diff " + diff + " at time " + currentMeasure);
                    game.soundPlayer.play(hitSound, 0.1f);
                    if(die) {
                        activeNoteStatus.put(note, NoteStatus.DEAD);
                        activeNoteStatusTime.put(note, currentMeasure);
                    }
                    break;
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

            if(activeNoteStatus.get(note) == NoteStatus.DEAD) {
                double timeElapsed = currentMeasure - activeNoteStatusTime.get(note);
                int frame = 1 + (int) (timeElapsed * 20.0);

                game.enemyDieTexture.bind();
//            game.drawFramed.draw(new Matrix4f(game.ortho).translate(pos.x, pos.y, 0).scale(100),
//                    new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
//                    new Vector2f(0), new Vector2f(1));
                game.drawFramed.draw(new Matrix4f(game.ortho).translate(pos.x, pos.y, 0).scale(100),
                        new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                        6, 1, frame
                );
            } else {
                game.enemyTexture.bind();
                game.drawTexture.draw(new Matrix4f(game.ortho).translate(pos.x, pos.y, 0).scale(100),
                        new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)
                );
            }
        }

        Vector4f col = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        if(goodness == 1) {
            col.set(1.0f, 0.0f, 0.0f, 1.0f);
        } else if(goodness == 2) {
            col.set(0.0f, 1.0f, 0.0f, 1.0f);
        }

        // draw goodness
        game.mainFont.draw("Test: " + goodness, 100, 100, new Matrix4f(game.ortho));
        Vector2f topTextPos = new Vector2f(104, 39);
        translateToScreen(topTextPos);
        topTextPos.add(0, game.smallFont.getSize());
//        game.drawSimple.draw(new Matrix4f(game.ortho).translate(topTextPos.x, topTextPos.y, 0).scale(10, 10, 0), new Vector4f(1, 0, 0, 1));
        game.smallFont.draw(topText, topTextPos.x, topTextPos.y, new Matrix4f(game.ortho), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
    }

    public void translateToScreen(Vector2f pos) {
        pos.set(pos).div(320.0f, 240.0f).mul(game.gameScreenSize).add(game.gameScreenCorner);
    }

    // this is sloppy but I think it works
    public int getMeasure(double measureTime) {
        if(measureTime < 0) return -1;
        double accountedFor = 0;
        int measuresNum = 0;

        while (measuresNum < measuresToPlay.size()) {
            accountedFor += measuresToPlay.get(measuresNum).getLength();
            if(accountedFor < measureTime && measuresNum < measuresToPlay.size()) measuresNum++;
            else break;
        }
        if(measuresNum >= measuresToPlay.size()) return -1;
        return measuresNum;
    }

    public double getMeasureStart(int measure) {
        double time = 0;
        for(int i = 0; i < measure; ++i) {
            time += (measuresToPlay.get(i).getLength());
        }
        return time;
    }
}
