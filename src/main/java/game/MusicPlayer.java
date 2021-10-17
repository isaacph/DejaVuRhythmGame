package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public ArrayList<ActiveNote> activeNotes = new ArrayList<>();

    public int goodness = 0;

    public boolean keyDown = false;

    public boolean show = true;

    public final Map<String, SoundHandle> sounds = new HashMap<>();

    public static final int[] MELODY_NUMBERS = {1, 2, 3, 4, 5, 6, 7, 8};
    public static final String[] RHYTHM_NAMES = {"A", "B", "C", "D"};

    public static final double HIT_LENIENCY = 0.05;
    public static final double HIT_GIVE_UP = 0.12;

    public String topText = "";

    public SoundHandle hitSound;
    public SoundHandle cueSound;
    public SoundHandle missSound;

    public boolean waiting = false;

    public Runnable onFinish = () -> {};

    public Runnable onHitGood = () -> {};
    public Runnable onHitBad = () -> {};
    public Runnable onMiss = () -> {};

    public static class HeartCounter {
        private int numHearts;
        public final Map<Integer, Double> heartLostTime = new HashMap<>();

        public HeartCounter() {

        }

        public void setHearts(int n) {
            this.numHearts = n;
            this.heartLostTime.clear();
        }

        public int getHearts() {
            return numHearts;
        }

        public void subtractHeart(double currentMeasure) {
            subtractHearts(1, currentMeasure);
        }

        public void subtractHearts(int n, double currentMeasure) {
            for(int i = numHearts - 1; i >= numHearts - n && i >= 0; --i) {
                heartLostTime.put(i, currentMeasure);
            }
            this.numHearts = Math.max(0, numHearts - n);
        }
    }

    public final HeartCounter hearts = new HeartCounter();

    public MusicPlayer(Game game) {
        this.game = game;

        // load not positions
        notePosition = new HashMap<>();
        for (int i = 0; i < notePos.length; ++i) {
            notePosition.put(i + 1, notePos[i]);
        }

        // load sounds
        hitSound = game.soundPlayer.loadSound("soundfx/clap.ogg");
        missSound = game.soundPlayer.loadSound("soundfx/glass.ogg");
        cueSound = game.soundPlayer.loadSound("music/cue.ogg");
        for(String rhythm : RHYTHM_NAMES) {
            sounds.put(rhythm, game.soundPlayer.loadSound("music/Rhythm_" + rhythm + ".ogg"));
            for(int melody : MELODY_NUMBERS) {
                sounds.put(melody + rhythm, game.soundPlayer.loadSound("music/Melody_" + melody + rhythm + ".ogg"));
            }
        }
        for(int chord : MELODY_NUMBERS) {
            sounds.put("chord_" + chord, game.soundPlayer.loadSound("music/Chords_" + chord + ".ogg"));
        }

        currentMeasure = -0.01f; // currentMeasure has to start less than 0
    }

    public void update() {
        if(!show) return;

        int prevMeasure = getMeasure(currentMeasure);
        if(!waiting) {
            currentMeasure += game.delta * bpm / 60.0 / 4.0f;
        }
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

        // do pre-updates
        for(int preUpdateIndex : getPreUpdateMeasures(currentMeasure)) {
            measuresToPlay.get(preUpdateIndex).measurePreUpdate(game, getMeasureStart(preUpdateIndex));
        }

        if(measureIndex == measuresToPlay.size()) {
            Runnable onF = this.onFinish;
            this.onFinish = () -> {};
            onF.run();
        }

        // change notes to missed
        for(ActiveNote note : activeNotes) {
            if(note.status != NoteStatus.READY) continue;
            double diff = (note.playTime - currentMeasure) / bpm * 60.0;
            if(diff < -HIT_GIVE_UP) {
                note.status = NoteStatus.MISSED;
                note.statusLastChanged = currentMeasure;
                this.onMiss.run();
            } else break;
        }

        // removed finished notes
        for(int i = activeNotes.size() - 1; i >= 0; --i) {
            ActiveNote note = activeNotes.get(i);
            if(note.status == NoteStatus.DEAD) {
                if((int) (1 + (currentMeasure - note.statusLastChanged) * 20.0f) >= 6) {
                    activeNotes.remove(i);
                }
            } else if(note.status == NoteStatus.MISSED) {
                activeNotes.remove(i);
            }
        }

        if(glfwGetKey(game.window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            if(!keyDown) {
                for (ActiveNote note : activeNotes) {
                    if(note.status != NoteStatus.READY) continue;
                    double diff = (note.playTime - currentMeasure) / bpm * 60.0;
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
                        note.status = (NoteStatus.DEAD);
                        note.statusLastChanged = (currentMeasure);
                        if(goodness == 1) {
                            this.onHitBad.run();
                        } else {
                            this.onHitGood.run();
                        }
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
        for(ActiveNote note : activeNotes) {
            Vector2f pos = new Vector2f(notePosition.get(note.note.position));
            translateToScreen(pos);

            if(note.status == NoteStatus.DEAD) {
                double timeElapsed = currentMeasure - note.statusLastChanged;
                int frame = 1 + (int) (timeElapsed * 20.0);

                game.enemyDieTexture.bind();
//            game.drawFramed.draw(new Matrix4f(game.ortho).translate(pos.x, pos.y, 0).scale(100),
//                    new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
//                    new Vector2f(0), new Vector2f(1));
                game.drawFramed.draw(new Matrix4f(game.ortho).translate(pos.x, pos.y, 0).scale(game.gameScreenSize.x / 320.0f * 36.0f * 1.2f),
                        new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                        6, 1, frame
                );
            } else {
                game.enemyTexture.bind();
                game.drawTexture.draw(new Matrix4f(game.ortho).translate(pos.x, pos.y, 0).scale(game.gameScreenSize.x / 320.0f * 32.0f * 1.2f),
                        new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)
                );
            }
        }

        if(topText.isEmpty()) {
            Vector2f heartPosStart = new Vector2f(120, 58);
            for (int i = 0; i < hearts.getHearts(); ++i) {
                Vector2f heartPos = new Vector2f(heartPosStart).add(16.0f * i, 0.0f);
                translateToScreen(heartPos);
                game.heartTexture.bind();
                game.drawTexture.draw(new Matrix4f(game.ortho)
                                .translate(heartPos.x, heartPos.y, 0)
                                .scale(game.gameScreenSize.x / 320.0f * 16.0f),
                        new Vector4f(1.0f));
            }
            List<Integer> toRemoveFromHearts = new ArrayList<>();
            for (Integer n : hearts.heartLostTime.keySet()) {
                double timeSinceLost = (currentMeasure - hearts.heartLostTime.get(n)) * 4 / bpm * 60;
                if (timeSinceLost > 0.2) {
                    toRemoveFromHearts.add(n);
                } else {
                    Vector2f heartPos = new Vector2f(heartPosStart).add(16.0f * n, 0.0f);
                    translateToScreen(heartPos);
                    game.heartBrokenTexture.bind();
                    game.drawTexture.draw(new Matrix4f(game.ortho)
                                    .translate(heartPos.x, heartPos.y, 0)
                                    .scale(game.gameScreenSize.x / 320.0f * 16.0f),
                            new Vector4f(1.0f, 1.0f, 1.0f, 1.0f - (float) timeSinceLost / 0.2f));
                }
            }
            for (Integer n : toRemoveFromHearts) {
                hearts.heartLostTime.remove(n);
            }
        }

        Vector4f col = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        if(goodness == 1) {
            col.set(1.0f, 0.0f, 0.0f, 1.0f);
        } else if(goodness == 2) {
            col.set(0.0f, 1.0f, 0.0f, 1.0f);
        }

        // draw goodness
        // game.mainFont.draw("Test: " + goodness, 100, 100, new Matrix4f(game.ortho));
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
        if(measuresNum >= measuresToPlay.size()) return measuresToPlay.size();
        return measuresNum;
    }

    public double getMeasureStart(int measure) {
        double time = 0;
        for(int i = 0; i < measure; ++i) {
            time += (measuresToPlay.get(i).getLength());
        }
        return time;
    }

    public ArrayList<Integer> getPreUpdateMeasures(double time) {
        int currentMeasure = getMeasure(time);
        if(currentMeasure + 1 >= measuresToPlay.size()) return new ArrayList<>();

        ArrayList<Integer> preUpdates = new ArrayList<>();
        double measureSum = getMeasureStart(currentMeasure + 1);
        for(int i = currentMeasure + 1; i < measuresToPlay.size(); ++i) {
            if(measureSum - measuresToPlay.get(i).getPreUpdateRequirement() <= time) {
                preUpdates.add(i);
            }
            measureSum += (measuresToPlay.get(i).getLength());
        }
        return preUpdates;
    }
}
